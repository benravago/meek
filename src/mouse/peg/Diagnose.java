package mouse.peg;

import java.util.BitSet;
import java.util.List;

import mouse.util.BitMatrix;
import static mouse.util.Text.*;

/**
 *  Contains methods to detect and write messages about:
 *  - not well-formed expressions;
 *  - cycles in the grammar;
 *  - recursion classes without entry;
 *  - recursive sequence expression with nullable first element;
 *  - semantic actions not allowed for recursive rule;
 *  - nullable argument of star and plus;
 *  - expressions that never fail;
 *  - superfluous '?' operators.
 */
class Diagnose {

  int errors;

  void apply(Expr[] index, int R, int N, List<LRec> lrecs, BitMatrix Clean) {
    errors = 0;
    // Check that all expressions are well-formed.
    for (var i = 0; i < N; i++) {
      var e = index[i];
      if (!e.def) {
        error("Error: " + e.asString + " is void.");
        errors++;
      }
    }
    // Check for cycles.
    var inCycles = new BitSet(R);
    // List Rules involved in cycles (cycle must contain a Rule)
    for (var i = 0; i < R; i++) {
      if (Clean.at(i,i)) inCycles.set(i);
    }
    for (var i = 0; i < R; i++) {
     // For a Rule involved in cycle
     if (inCycles.get(i)) {
        // Complain
        error("Error: the grammar has cycle inolving "+ index[i].asString + ".");
        errors++;
        // Find other expressions in this cycle
        var cycle = Clean.row(i);
        cycle.and(Clean.column(i));
        // Remove them from list
        inCycles.andNot(cycle);
      }
    }
    // Check recursion classes.
    // Class not used.
    for (var rc: lrecs) {
      if (rc.entries().size() == 0 ) {
        note("Info: recursion class of "  + rc.name() + " is not used.");
      }
    }
    // Scan expressions using DiagVisitor.
    for (var e: index) {
      accept(e);
    }
  }

  // collects diagnostic information.
  void accept(Expr x) {
    switch(x) {
      case Expr.Rule e -> {
        doChoice(e, e.args);
        if (e.lrec == null) break; // return;
        for (var a:e.onSucc) {
          if ( a != null && a.and) {
            error("Error: boolean action \"" + a.name + "\" is not supported in recursive " + e.asString + ".");
            errors++;
          }
        }
        for (var a:e.onFail) {
          if (a != null) {
            error("Error: action on failure \"" + a.name + "\" is not supported in recursive " + e.asString + ".");
            errors++;
          }
        }
      }
      case Expr.Choice e -> {
        doChoice(e,e.args);
      }
      case Expr.Sequence e -> {
        if (e.lrec == null) break; // return;
        if (e.args[0].nul) {
          error("Error: left-recursive " + e.asString + " starts with nullable expression.");
          errors++;
        }
      }
      case Expr.And _ -> {}
      case Expr.Not _ -> {}
      case Expr.Plus e -> {
        if (e.arg.nul) nullArg(e);
      }
      case Expr.Star e -> {
        if (e.arg.nul) nullArg(e);
      }
      case Expr.Query e -> {
        if (!e.arg.fal) {
          note("Info: as " + e.arg.asString + " never fails, '?' after it can be droppped.");
        }
      }
      case Expr.StarPlus e -> {
        if (e.arg1.nul) nullArg(e);
      }
      case Expr.PlusPlus e -> {
        if (e.arg1.nul) nullArg(e);
      }
      default -> {}
    }
  }

  //  Common for Rule and Choice.
  void doChoice(Expr expr, Expr[] args) {
    for (var i = 0; i < args.length-1; i++) {
      if (!args[i].fal) {
        note("Info: " +args[i].asString + " in " + expr.asString + " never fails and hides other alternative(s).");
      }
    }
  }
  // Nullable argument of star or plus.
  void nullArg(Expr expr) {
    error("Error: argument of " + expr.asString + " is nullable.");
    errors++;
  }

}
