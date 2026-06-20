package mouse.peg;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static mouse.util.Text.*;

// resolves references and removes Expr.Ref objects.
class References {

  void compute(List<Expr.Rule> rules) {
    names = new HashMap<>();
    referenced = new HashSet<>();
    errors = 0;
    // Build table of Rule names, checking for duplicates.
    for (var r : rules) {
      var prev = names.put(r.name, r);
      if (prev != null) {
        error("Error: duplicate name '" + r.name + "'.");
        errors++;
      }
    }
    // Replace Expr.Ref objects by direct references to Rules.
    for (var e : rules) {
      accept(e);
    }
    // Detect unused rules.
    // Top rule is assumed referenced.
    referenced.add(rules.get(0).name);
    for (var r : rules) {
      if (referenced.contains(r.name)) continue;
      note("Info: Rule '" + r.name + "' is not used.");
    }
  }

  Map<String, Expr.Rule> names; // Mapping from names to Rules
  Set<String> referenced;       // Referenced names.
  int errors;                   // error count

  void accept(Expr x) {
    switch (x) {
      case Expr.Rule e -> { doCompound(e, e.args); }
      case Expr.Choice e -> { doCompound(e, e.args); }
      case Expr.Sequence e -> { doCompound(e, e.args); }
      case Expr.And e -> { e.arg = getRule(e.arg); }
      case Expr.Not e -> { e.arg = getRule(e.arg); }
      case Expr.Plus e -> { e.arg = getRule(e.arg); }
      case Expr.Star e -> { e.arg = getRule(e.arg); }
      case Expr.Query e -> { e.arg = getRule(e.arg); }
      case Expr.PlusPlus e -> { e.arg1 = getRule(e.arg1); e.arg2 = getRule(e.arg2); }
      case Expr.StarPlus e -> { e.arg1 = getRule(e.arg1); e.arg2 = getRule(e.arg2); }
      case Expr.Is e -> { e.arg1 = getRule(e.arg1); e.arg2 = getRule(e.arg2); }
      case Expr.IsNot e -> { e.arg1 = getRule(e.arg1); e.arg2 = getRule(e.arg2); }
      default -> {}
    }
  }

  // Common for expressions with argument array
  void doCompound(Expr expr, Expr[] args) {
    for (var i = 0; i < args.length; i++) {
      args[i] = getRule(args[i]);
    }
  }

  // If 'expr' is Expr.Ref, return Rule referenced by it.
  // Otherwise return 'expr'.
  Expr getRule(Expr expr) {
    if (expr.isRef) {
      var rule = names.get(expr.name);
      if (rule == null) {
        names.put(expr.name, dummy);
        error("Error: undefined name '" + expr.name + "'.");
        errors++;
      } else {
        referenced.add(expr.name);
        rule.isNamed = true;
      }
      return rule;
    } else {
      accept(expr);
      return expr;
    }
  }

  // Dummy rule - replaces undefined to stop multiple messages.
  static Expr.Rule dummy = new Expr.Rule();

}