package mouse.peg;

import java.util.ArrayList;
import java.util.List;

// makes lists of expressions and gives names to unnamed.
class Expressions {

  List<Expr> subs, terms;

  void compute(List<Expr.Rule> rules) {
    subs = new ArrayList<>();
    terms = new ArrayList<>();
    ruleName = null;
    number = 0;
    for (var r : rules) {
      accept(r);
    }
  }

  String ruleName;
  int number;

  // Each visit names the visited expression (other than Rule), adds it to its proper list, and then proceeeds to visit all subexpressions, if any.

  void accept(Expr x) {
    switch (x) {
      case Expr.Rule e -> {
        ruleName = e.name;
        number = 0;
        for (var arg : e.args) {
          descendFrom(arg);
        }
      }
      case Expr.Choice e -> doCompound(e, e.args);
      case Expr.Sequence e -> doCompound(e, e.args);
      case Expr.And e -> doUnary(e, e.arg);
      case Expr.Not e -> doUnary(e, e.arg);
      case Expr.Plus e -> doUnary(e, e.arg);
      case Expr.Star e -> doUnary(e, e.arg);
      case Expr.Query e -> doUnary(e, e.arg);
      case Expr.PlusPlus e -> doBinary(e, e.arg1, e.arg2);
      case Expr.StarPlus e -> doBinary(e, e.arg1, e.arg2);
      case Expr.Is e -> doBinary(e, e.arg1, e.arg2);
      case Expr.IsNot e -> doBinary(e, e.arg1, e.arg2);
      case Expr.StringLit e -> doTerm(e);
      case Expr.Range e -> doTerm(e);
      case Expr.CharClass e -> doTerm(e);
      case Expr.Any e -> doTerm(e);
      case Expr.End e -> doTerm(e);
      default -> {}
    }
  }

  void doCompound(Expr expr, Expr[] args) {
    doSub(expr);
    for (var arg : args) {
      descendFrom(arg);
    }
  }
  void doBinary(Expr expr, Expr arg1, Expr arg2) {
    doSub(expr);
    descendFrom(arg1);
    descendFrom(arg2);
  }
  void doUnary(Expr expr, Expr arg) {
    doSub(expr);
    descendFrom(arg);
  }
  void doTerm(Expr expr) {
    terms.add(expr);
    expr.name = ruleName + "_" + number;
    number++;
  }
  void doSub(Expr expr) {
    subs.add(expr);
    expr.name = ruleName + "_" + number;
    number++;
  }

  void descendFrom(Expr arg) {
    if (!arg.isNamed) {
      accept(arg);
    }
  }

}