package mouse.peg;

import java.util.ArrayList;
import java.util.List;

import mouse.util.BitMatrix;
import static mouse.util.Text.*;

//  Holds relations between Expressions in the PEG class.
//  The relations are represented by BitMatrix objects.
//  Each expression is assigned an index in the matrix.
//  The index is held in 'index' field of the Expression.
//  The array 'index' translates indices to the corresponding expressions.
class Relations {

  // Relation 'first'.
  // first[i,j] = true means that expression i may call expression j at its starting position.
  BitMatrix first;

  // Relation 'First'.
  // The transitive closure of 'first'.
  // First[i,j] = true means that expression i may call, directly or indirectly, expression j at its starting position.
  // It is used to detect left recursion.
  BitMatrix First;

  // Relation 'calls'.
  // It is used to identify entry expressions of recursion class.
  BitMatrix calls;

  // Relation 'clean'.
  // clean[i,j] = true means that expression i may call expression j without consuming input.
  // Computed only for expressions i that support left recursion.
  BitMatrix clean;

  // Relation 'Clean'. The transitive closure of 'clean'.
  // Clean[i,j] = true means that expression i may call, directly or indirectly, expression j  without consuming input.
  // It is used to detect cycles in the grammar.
  BitMatrix Clean;

  // Compute relations.
  void findRelations(Expr[] exprs, int E) {
    // Initialize matrices.
    first = BitMatrix.empty(E);
    calls = BitMatrix.empty(E);
    clean = BitMatrix.empty(E);
    // Construct matrices
    buildMatrices(exprs);
    // Compute closures.
    First = first.closure();
    Clean = clean.closure();
  }

  void buildMatrices(Expr[] exprs) {
    for (var x:exprs) switch (x) {
      case Expr.Rule e -> {
        doChoice(e, e.args);
      }
      case Expr.Choice e -> {
        doChoice(e, e.args);
      }
      case Expr.Sequence e -> {
        for (var arg : e.args) {
          first.set(e.index, arg.index); // arg i is in first
          if (!arg.nul) {
            break; // rest is not first
          }
        }
        for (var arg : e.args) {
          calls.set(e.index, arg.index);
        }
        for (var i = 0; i < e.args.length; i++) {
          if (e.args[i].isTerm) {
            continue;
          }
          var alNul = true;
          for (var j = 0; j < e.args.length; j++) {
            if (j != i && !e.args[j].nul) {
              alNul = false;
            }
          }
          if (alNul) {
            clean.set(e.index, e.args[i].index);
          }
        }
      }
      case Expr.And e -> {
        first.set(e.index, e.arg.index);
        calls.set(e.index, e.arg.index);
      }
      case Expr.Not e -> {
        first.set(e.index, e.arg.index);
        calls.set(e.index, e.arg.index);
      }
      case Expr.Plus e -> {
        first.set(e.index, e.arg.index);
        calls.set(e.index, e.arg.index);
      }
      case Expr.Star e -> {
        first.set(e.index, e.arg.index);
        calls.set(e.index, e.arg.index);
      }
      case Expr.Query e -> {
        first.set(e.index, e.arg.index);
        calls.set(e.index, e.arg.index);
      }
      case Expr.StarPlus e -> {
        first.set(e.index, e.arg1.index);
        first.set(e.index, e.arg2.index);
        calls.set(e.index, e.arg1.index);
        calls.set(e.index, e.arg2.index);
      }
      case Expr.PlusPlus e -> {
        first.set(e.index, e.arg1.index);
        first.set(e.index, e.arg2.index);
        calls.set(e.index, e.arg1.index);
        calls.set(e.index, e.arg2.index);
      }
      case Expr.Is e -> {
        first.set(e.index, e.arg1.index);
        first.set(e.index, e.arg2.index);
        calls.set(e.index, e.arg1.index);
        calls.set(e.index, e.arg2.index);
      }
      case Expr.IsNot e -> {
        first.set(e.index, e.arg1.index);
        first.set(e.index, e.arg2.index);
        calls.set(e.index, e.arg1.index);
        calls.set(e.index, e.arg2.index);
      }
      default -> {}
    };
  }

  // Common for Rule and Choice.
  void doChoice(Expr expr, Expr[] args) {
    for (var arg : args) {
      first.set(expr.index, arg.index);
      calls.set(expr.index, arg.index);
      clean.set(expr.index, arg.index);
    }
  }

  List<LRec> lrecs;
  int errors;

  // Find left-recursion classes.
  void findRecClasses(Expr[] exprs, List<Expr.Rule> rules) {
    lrecs = new ArrayList<LRec>();
    errors = 0;
    //  Recursion class must contain at least one Rule so it is enough to create recursion classes for Rules.
    for (var rule : rules) {
      var r = rule.index;
      if (First.at(r, r) && rule.lrec == null) {
        // left-recursive and class does not already exist
        lrecs.add(lrec(exprs,rule)); // Create class
      }
    }
  }

  // creates LRec object containing Rule 'rule'.
  LRec lrec(Expr[] exprs, Expr.Rule rule) {
    // assert First.at(r,r) && rule.lrec == null : "Incorrect call";
    var r = rule.index;
    // The class is named after 'rule'
    var rc = new LRec(rule.name,
      First.row(r), // memberIndex
      new ArrayList<>(), // members
      new ArrayList<>(), // entries
      new ArrayList<>(), // exits
      new ArrayList<>()  // seeds
    );
    // Identify members of the class
    rc.memberIndex().and(First.column(r));
    // List members and mark their membership
    var i = 0;
    while ((i = rc.memberIndex().nextSetBit(i)) > -1) {
      rc.members().add(exprs[i]);
      exprs[i].lrec = rc; // this;
    }
    // Identify entries: members called from outside the class.
    // Member with index 0 is assumed to be called on start.
    for (var member : rc.members()) {
      i = member.index;
      var callers = calls.column(i);
      callers.andNot(rc.memberIndex());
      if (!callers.isEmpty() || i == 0) {
        rc.entries().add(member);
      }
    }
    // Identify exits and seeds.
    identifySeeds(rc);
    return rc;
  }

  // identifies seeds and exits; detects unsupported.
  void identifySeeds(LRec lrec) {
    for (var member : lrec.members()) {
      switch (member) {
        case Expr.Rule e ->     getSeeds(lrec, e, e.args);
        case Expr.Choice e ->   getSeeds(lrec, e, e.args);
        case Expr.And e ->      notSupported(e);
        case Expr.Not e ->      notSupported(e);
        case Expr.Plus e ->     notSupported(e);
        case Expr.Star e ->     notSupported(e);
        case Expr.Query e ->    notSupported(e);
        case Expr.PlusPlus e -> notSupported(e);
        case Expr.StarPlus e -> notSupported(e);
        case Expr.Is e ->       notSupported(e);
        case Expr.IsNot e ->    notSupported(e);
        default -> {}
      }
      // Sequence can not be an exit.
      // Terminals are not recursive.
    }
  }

  // Identify seeds and exits.
  void getSeeds(LRec lrec, Expr expr, Expr args[]) {
    // One-argument recursive Rule or Choice is not an exit.
    if (args.length == 1) {
      return;
    }
    // Check for args outside the class: they are seeds.
    var isExit = false;
    for (var arg : args) {
      if (arg.lrec != lrec) { // arg is a seed
        if (!lrec.seeds().contains(arg)) { // If not already in list..
          lrec.seeds().add(arg); // ..add
        }
        isExit = true; // expr is an exit
      }
    }
    if (isExit) {
      lrec.exits().add(expr); // Add exit to list
    }
  }

  void notSupported(Expr expr) {
    var name = expr.isNamed ? expr.name : str(expr.asString);
    error("Error: " + name + " is not supported in left-recursion.\n");
    errors++;
  }

  Links metadata() {
    return new Links(lrecs,first,clean,calls,First,Clean);
  }

}