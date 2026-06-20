package mouse.peg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Compact {

  // Eliminate duplicate expressions from parse tree.
  // Expressions involved in left-recursion, including terminals, are not eliminated.
  // The result is no longer a tree.
  void compute(List<Expr.Rule> rules, List<Expr> subs, List<Expr> terms) {
    this.subs = subs;
    this.terms = terms;
    sources = new HashMap<>();
    for (var e:rules) {
      if (e.lrec == null) {
        accept(e);
      }
    }
  }

  List<Expr> subs, terms;

  // Map to detect identical expressions.
  // The table maps sources to expressions.
  Map<String, Expr> sources;

  // Each visit examines subexpressions of a visited expression.
  // If it finds the subexpression identical to a previously encountered, replaces the subexpression by the latter.
  // Otherwise, it proceeds to visit the subexpression.
  // Expressions are considered identical if they have the same reconstructed source.
  void accept(Expr x) {
    switch (x) {
      case Expr.Rule e -> { doCompound(e, e.args); }
      case Expr.Choice e -> { doCompound(e, e.args); }
      case Expr.Sequence e -> { doCompound(e, e.args); }
      case Expr.And e -> { e.arg = alias(e.arg); }
      case Expr.Not e -> { e.arg = alias(e.arg); }
      case Expr.Plus e -> { e.arg = alias(e.arg); }
      case Expr.Star e -> { e.arg = alias(e.arg); }
      case Expr.Query e -> { e.arg = alias(e.arg); }
      case Expr.PlusPlus e -> { e.arg1 = alias(e.arg1); e.arg2 = alias(e.arg2); }
      case Expr.StarPlus e -> { e.arg1 = alias(e.arg1); e.arg2 = alias(e.arg2); }
      case Expr.Is e -> { e.arg1 = alias(e.arg1); e.arg2 = alias(e.arg2); }
      case Expr.IsNot e -> { e.arg1 = alias(e.arg1); e.arg2 = alias(e.arg2); }
      default -> {}
    }
  }

  void doCompound(Expr expr, Expr[] args) {
    for (int i = 0; i < args.length; i++) {
      args[i] = alias(args[i]);
    }
  }

  // If the 'sources' table already contains an expression with the same source as 'expr', return that expression and remove 'expr' from its list.
  // Otherwise add 'expr' to 'sources', visit 'expr', and return 'expr'.
  Expr alias(Expr expr) {
    // Do not compact left-recursive expressions
    if (expr.lrec != null) {
      return expr;
    }
    var source = expr.asString;
    var found = sources.get(source);
    if (found != null) {
      var ok = true;
      while (ok) {
        ok = subs.remove(expr);
      }
      ok = true;
      while (ok) {
        ok = terms.remove(expr);
      }
      return found;
    } else {
      sources.put(source, expr);
      if (!expr.isNamed) {
        accept(expr);
      }
      return expr;
    }
  }

}
