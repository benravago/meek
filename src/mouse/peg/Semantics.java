package mouse.peg;

import java.util.ArrayList;
import java.util.List;

import mouse.util.Text;

import static mouse.util.Text.*;

//  Semantics - for parsing the PEG source
class Semantics extends PEG.Semantics {

  //  Results: list of Rules and number of errors.
  List<Expr.Rule> rules;
  int errors;

  @Override
  protected void init() { // no close() actions
    super.init();
    rules = new ArrayList<>();
    errors = 0;
  }

  //  Some shorthands
  Expr exprValue(int i) { return (Expr) rhs(i).get(); }
  Expr.Rule ruleValue(int i) { return (Expr.Rule) rhs(i).get(); }
  String stringValue(int i) { return (String) rhs(i).get(); }
  Action actionValue(int i) { return (Action) rhs(i).get(); }
  char charValue(int i) { return (Character) rhs(i).get(); }

  //  Semantic procedures

  //  Grammar = Space? (&_ (Rule / Skip))* EOT
  //              0         1,2,..,-2    -1
  @Override
  protected void Grammar() {
    var n = rhsSize() - 2; // Number of Rules, correct or not.
    if (n <= 0) {
      error("input file empty\n");
      errors++;
      return;
    }
    if (errors > 0) {
      return;
    }
    // All Rules were correctly parsed. Construct list of Rules.
    for (var i = 0; i < n; i++) {
      rules.add(ruleValue(i + 1));
    }
    // Print trace if requested.
    // if (trace.indexOf('G') < 0) {
    //   return;
    // }
    // for (var r : rules) {
    //   note(Diagnose.name(r)+"\n");
    // }
  }

  //  Rule = Name EQUAL RuleRhs DiagName? SEMI
  //           0    1      2        3     4(3)
  @Override
  protected void Rule() {
    // RuleRhs returns Expr.Rule object without name and diag name
    var rhs = ruleValue(2);
    // Fill name and diag name
    rhs.name = stringValue(0);
    rhs.diagName = rhsSize() == 5 ? stringValue(3) : null;
    // Fill default action names
    if (rhs.args.length == 1) {
      if (rhs.onSucc[0] != null && rhs.onSucc[0].name.isEmpty()) {
        rhs.onSucc[0].name = rhs.name;
      }
      if (rhs.onFail[0] != null && rhs.onFail[0].name.isEmpty()) {
        rhs.onFail[0].name = rhs.name + "_fail";
      }
    } else {
      for (var i = 0; i < rhs.args.length; i++) {
        if (rhs.onSucc[i] != null && rhs.onSucc[i].name.isEmpty()) {
          rhs.onSucc[i].name = rhs.name + "_" + i;
        }
        if (rhs.onFail[i] != null && rhs.onFail[i].name.isEmpty()) {
          rhs.onFail[i].name = rhs.name + "_" + i + "_fail";
        }
      }
    }
    // Return completed Expr.Rule
    lhs().put(rhs);
  }

  //  Rule not recognized
  @Override
  protected void Error() {
    // System.out.println(lhs().errMsg()); // TODO: maybe use 'StringBuilder errMsg' ??
    lhs().errMsgs().forEach(Text::error);
    lhs().errClear();
    errors++;
  }

  //  RuleRhs = Sequence Actions (SLASH Sequence Actions)*
  //                0       1     2,5,.. 3,6,..   4,7,..
  @Override
  protected void RuleRhs() {
    // Returns a temporary Rule object with 'name' and 'diagName' null.
    var n = (rhsSize() + 1) / 3; // Number of 'Sequence's
    var seq = new Expr[n];
    var succ = new Action[n];
    var fail = new Action[n];
    for (var i = 0; i < n; i++) {
      seq[i] = exprValue(3 * i);
      var actions = (Action[]) (rhs(3 * i + 1).get());
      succ[i] = actions[0];
      fail[i] = actions[1];
    }
    lhs().put(new Expr.Rule(null, null, seq, succ, fail));
  }

  //  Choice = Sequence (SLASH Sequence)*
  //               0     1,3,..  2,4,..
  @Override
  protected void Choice() {
    var n = rhsSize();
    if (n == 1) {
      lhs().put(rhs(0).get());
      return;
    }
    var seq = new Expr[(n + 1) / 2];
    for (var i = 0; i < seq.length; i++) {
      seq[i] = exprValue(2 * i);
    }
    lhs().put(new Expr.Choice(seq));
  }

  //  Sequence = Prefixed+
  //               0,1,..
  @Override
  protected void Sequence() {
    var n = rhsSize();
    if (n == 1) {
      lhs().put(rhs(0).get());
      return;
    }
    var pref = new Expr[n];
    for (var i = 0; i < n; i++) {
      pref[i] = exprValue(i);
    }
    lhs().put(new Expr.Sequence(pref));
  }

  //  Prefixed = (AND/NOT) Suffixed
  //                 0        1
  @Override
  protected void Prefix() {
    var arg = exprValue(1);
    var isAnd = rhs(0).rule().equals("AND");
    // If nested predicate: reduce to single one
    switch (arg) {
      case Expr.And and -> {
        if (isAnd) {
          lhs().put(arg);
        } else {
          lhs().put(new Expr.Not(and.arg));
        }
      }
      case Expr.Not not -> {
        if (isAnd) {
          lhs().put(arg);
        } else {
          lhs().put(new Expr.And(not.arg));
        }
      } // Argument is not a predicate
      default -> {
        if (isAnd) {
          lhs().put(new Expr.And(arg));
        } else if (arg instanceof Expr.Any) {
          lhs().put(new Expr.End());
        } else {
          lhs().put(new Expr.Not(arg));
        }
      }
    }
  }

  //  Suffixed  = Primary (STARPLUS/PLUSPLUS/IS/ISNOT) Primary
  //                 0                  1                  2
  @Override
  protected void Infix() {
    switch (rhs(1).rule()) {
      case "STARPLUS" -> lhs().put(new Expr.StarPlus(exprValue(0), exprValue(2)));
      case "PLUSPLUS" -> lhs().put(new Expr.PlusPlus(exprValue(0), exprValue(2)));
      case "IS"       -> lhs().put(new Expr.Is(exprValue(0), exprValue(2)));
      case "ISNOT"    -> lhs().put(new Expr.IsNot(exprValue(0), exprValue(2)));
    }
  }

  //  Suffixed  = Primary (QUERY/STAR/PLUS)
  //                 0           1
  @Override
  protected void Suffix() {
    switch (rhs(1).rule()) {
      case "QUERY" -> lhs().put(new Expr.Query(exprValue(0)));
      case "STAR"  -> lhs().put(new Expr.Star(exprValue(0)));
      case "PLUS"  -> lhs().put(new Expr.Plus(exprValue(0)));
    }
  }

  //  Primary = Name
  //             0
  @Override
  protected void Resolve() {
    var ref = new Expr.Ref(stringValue(0));
    lhs().put(ref);
  }

  //  Primary = LPAREN Choice RPAREN
  //               0      1      2
  @Override
  protected void Pass2() {
    lhs().put(rhs(1).get());
  }

  //  Primary = ANY
  @Override
  protected void Any() {
    lhs().put(new Expr.Any());
  }

  //  Primary = StringLit
  //  Primary = Range
  //  Primary = CharClass
  //  Char = Escape
  @Override
  protected void Pass() {
    lhs().put(rhs(0).get());
  }

  //  Actions = OnSucc OnFail
  //               0     1
  @Override
  protected void Actions() {
    lhs().put(new Action[]{actionValue(0), actionValue(1)});
  }

  //  OnSucc = (LWING AND? Name? RWING)?
  //              0    1    -2    -1
  @Override
  protected void OnSucc() {
    var n = rhsSize();
    if (n == 0) {
      lhs().put(null);
    } else {
      var name = rhs(n - 2).isA("Name") ? stringValue(n - 2) : "";
      if (rhs(1).isA("AND")) {
        lhs().put(new Action(name, true));
      } else {
        lhs().put(new Action(name, false));
      }
    }
  }

  //  OnFail = (TILDA LWING Name? RWING)?
  //              0     1    -2    -1
  @Override
  protected void OnFail() {
    var n = rhsSize();
    if (n == 0) {
      lhs().put(null);
    } else {
      var name = rhs(n - 2).isA("Name") ? stringValue(n - 2) : "";
      lhs().put(new Action(name, false));
    }
  }

  //  Name = Letter (Letter / Digit)* Space
  //            0        1 ... -2       -1
  @Override
  protected void Name() {
    lhs().put(rhsText(0, rhsSize() - 1));
  }

  //  DiagName = "(" (!")" Char)+ ")" Space
  //              0    1,2..,-3    -2   -1
  @Override
  protected void DiagName() {
    var sb = new StringBuilder();
    for (var i = 1; i < rhsSize() - 2; i++) {
      sb.append(charValue(i));
    }
    lhs().put(sb.toString());
  }

  //  StringLit = ["] (!["] Char)+ ["] Space
  //               0    1,2..,-3    -2   -1
  @Override
  protected void StringLit() {
    var sb = new StringBuilder();
    for (var i = 1; i < rhsSize() - 2; i++) {
      sb.append(charValue(i));
    }
    lhs().put(new Expr.StringLit(sb.toString()));
  }

  //  CharClass = ("[" / "^[") (!"]" Char)+ "]" Space
  //                0      0    1,2..,-3    -2   -1
  @Override
  protected void CharClass() {
    var sb = new StringBuilder();
    for (var i = 1; i < rhsSize() - 2; i++) {
      sb.append(charValue(i));
    }
    lhs().put(new Expr.CharClass(sb.toString(), rhs(0).charAt(0) == '^'));
  }

  //  Range = "[" Char "-" Char "]" Space
  //           0    1   2    3   4    5
  @Override
  protected void Range() {
    var a = charValue(1);
    var z = charValue(3);
    lhs().put(new Expr.Range(a, z));
  }

  //  Char = ![\r\n]_
  @Override
  protected void Char() {
    lhs().put(rhs(0).charAt(0));
  }

  //  Escape = "\\u" HexDigit HexDigit HexDigit HexDigit
  //              0       1       2        3        4
  @Override
  protected void Unicode() {
    var s = rhsText(1, 5).toString();
    lhs().put((char) Integer.parseInt(s, 16));
  }

  //  Escape = "\n"
  //             0
  @Override
  protected void Newline() {
    lhs().put('\n');
  }

  //  Escape = "\r"
  //             0
  @Override
  protected void CarRet() {
    lhs().put('\r');
  }

  //  Escape = "\t"
  //             0
  @Override
  protected void Tab() {
    lhs().put('\t');
  }

  //  Escape = "\" _
  //            0  1
  @Override
  protected void Escape() {
    lhs().put(rhs(1).charAt(0));
  }

  //  Space = ([ \r\n\t] / Comment)*
  @Override
  protected void Space() {
    lhs().errClear();
  }

}