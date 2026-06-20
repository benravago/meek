package mouse.peg;

class Sources {

  // Reconstruct, in a standard form, the source string of each expression
  //  and assign it to 'asString' field of Expr object.
  void compute(Expr[] index, int R) {
    for (var i = 0; i < R; i++) {
      accept(index[i]);
    }
  }

  // Each visit starts with visiting the subexpressions to construct their source strings.
  // These strings are then used as building blocks to produce the final result.
  // Procedure 'enclose' encloses the subexpression in parentheses if needed, depending on the binding strength of subexpression and containing expression.

  void accept(Expr x) {
    switch (x) {
      case Expr.Rule r -> {
        var sb = new StringBuilder(r.name + " = ");
        var sep = "";
        for (var i = 0; i < r.args.length; i++) {
          sb.append(sep);
          sb.append(enclose(r.args[i], 0));
          if (r.onSucc[i] != null) {
            sb.append(" " + str(r.onSucc[i]));
          }
          if (r.onFail[i] != null) {
            sb.append(" ~" + str(r.onFail[i]));
          }
          sep = " / ";
        }
        if (r.diagName != null) {
          sb.append(" <" + r.diagName + ">");
        }
        sb.append(" ;");
        r.asString = sb.toString();
      }
      case Expr.Choice e -> {
        var sb = new StringBuilder();
        var sep = "";
        for (var arg : e.args) {
          sb.append(sep);
          sb.append(enclose(arg, 0));
          sep = " / ";
        }
        e.asString = sb.toString();
      }
      case Expr.Sequence e -> {
        var sb = new StringBuilder();
        var sep = "";
        for (var arg : e.args) {
          sb.append(sep);
          sb.append(enclose(arg, 1));
          sep = " ";
        }
        e.asString = sb.toString();;
      }
      case Expr.And e -> { e.asString = "&" + enclose(e.arg, 2); }
      case Expr.Not e -> { e.asString = "!" + enclose(e.arg, 2); }
      case Expr.Plus e -> { e.asString = enclose(e.arg, 3) + "+"; }
      case Expr.Star e -> { e.asString = enclose(e.arg, 3) + "*"; }
      case Expr.Query e -> { e.asString = enclose(e.arg, 3) + "?"; }
      case Expr.PlusPlus e -> { e.asString = enclose(e.arg1, 3) + "++ " + enclose(e.arg2, 3); }
      case Expr.StarPlus e -> { e.asString = enclose(e.arg1, 3) + "*+ " + enclose(e.arg2, 3); }
      case Expr.Is e -> { e.asString = enclose(e.arg1, 3) + ":" + enclose(e.arg2, 3); }
      case Expr.IsNot e -> { e.asString = enclose(e.arg1, 3) + ":!" + enclose(e.arg2, 3); }
      default -> {}
    }
  }

  // Reconstruct source of 'e', enclosing it in parentheses if binding strength of 'e' does not exceed 'mybind'.
  String enclose(Expr expr, int mybind) {
    if (expr.isNamed) {
      return expr.name;
    }
    accept(expr);
    var nest = expr.bind <= mybind;
    return (nest ? "(" : "") + expr.asString + (nest ? ")" : "");
  }

  static String str(Object o) {
    return o instanceof Expr e ? e.asString : ""+o;
  }
}