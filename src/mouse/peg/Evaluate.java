package mouse.peg;

import java.util.List;

import static mouse.util.Text.*;

/**
 *  The class represents parsed grammar.
 *
 *  The parsed grammar is a structure of Expr objects in the form of trees
 *    with terminals and Expressions marked 'isNamed' as leaves.
 *
 *  The trees are rooted in Expr.Rule objects listed in the List 'rules'.
 *
 *  Additional Lists contain lists of other Expr's in the structure:
 *    'subs' for subexpressions,
 *    'terms' for terminals.
 *
 *  All Expr's are listed in array 'index':
 *    first all Rules,
 *    then all subexpressions,
 *    then all terminals.
 *
 *  Each Expr object carries its position in the array in field 'index'.
 *    Different relations between Expressions are collected in class Relations.
 *    Left-recursion classes of the grammar are represented by LRec objects listed  in the List 'lrecs'.
 *
 *  Method 'parse' builds this structure from a file containing PEG.
 *    It uses for this purpose the Parser and Semantics from this package.
 *    The Parser is constructed using Mouse from grammar.peg.
 */
public class Evaluate { // was Mouse-2.3 PEG.java

  //  Rules, subexpressions, terminals, references.
  List<Expr.Rule> rules;
  List<Expr> subs;
  List<Expr> terms;

  Expr index[]; // Array of expressions
  int E;        // Number of expressions
  int R;        // Number of rules
  int N;        // Number of nonterminals

  //  Relations between Expressions
  Links links;

  //  Counters.
  // public int errors;   // Errors // TODO: don't bother to accumulate this; just quit if non-zero
  int attrs;    // Iterations for attributes

  //  Parse PEG grammar supplied as 'src'.
  public boolean parse(CharSequence src) {

    var sem = new Semantics();
    var parser = new PEG.Parser();
    if (!parser.parse(src,sem)) {
      var current = parser.lhs();
      if (current.errMark() >= 0) {
        for (var e:current.errMsgs()) error(e);
      }
      return false; // Quit if parser failed.
    }
    // Quit if semantics failed.
    if (sem.errors > 0) return false;

    // Get parsed Rules
    rules = sem.rules;

    // Resolve references and remove Expr.Ref objects.
    // After removal of Expr.Ref objects, the parse trees have terminals and references to Rules as leafs.
    // These latter must be checked for in visitors that descend the trees.
    var rv = new References();
    rv.compute(rules);
    // Quit if unresolved reference(s) found.
    if (rv.errors > 0) return false;

    // Make linear lists of expressions contained in the parse tree: inner expressions ('subs') and terminals ('terms').
    // Assign names to subexpressions and terminals.
    var lv = new Expressions();
    lv.compute(rules);
    subs = lv.subs;
    terms = lv.terms;

    // Build index of all expressions.
    buildIndex();

    // Reconstruct source string for all Expression.
    new Sources().compute(index, R);

    // Compute attributes for all Expressions.
    var av = new Attributes();
    av.compute(index, N);
    attrs += av.steps;

    // Compute relations.
    var mv = new Relations();
    mv.findRelations(index, E);
    // Find left-recursion classes.
    mv.findRecClasses(index, rules);
    // Quit if any errors.
    if (mv.errors > 0) return false;
    links = mv.metadata();

    // Diagnose.
    var dv = new Diagnose();
    dv.apply(index, R, N, links.lrecs(), links.Clean());
    // Quit if any errors.
    if (dv.errors > 0) return false;

    // PEG.compact()
    new Compact().compute(rules,subs,terms);

    // PEG.parse() done
    return true;
  }

  //  Build the array 'index' containing all expressions, first Rules, then inner expressions, then terminals.
  //  Set 'index' field of each Expression to the Expression's index in the array.
  void buildIndex() {
    R = rules.size();
    N = R + subs.size();
    E = N + terms.size();
    index = new Expr[E];
    var i = 0;
    for (var e:rules) {
      e.index = i;
      index[i++] = e;
    }
    for (var e:subs) {
      e.index = i;
      index[i++] = e;
    }
    for (var e:terms) {
      e.index = i;
      index[i++] = e;
    }
  }

  public Parsed metadata() {
    return new Parsed(rules,subs,terms,index,E,R,N,links); // ,attrs);
  }

}