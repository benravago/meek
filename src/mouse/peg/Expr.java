package mouse.peg;

//  Objects of class Expr represent parsing expressions.
//  Expr is an abstract class, with concrete subclasses representing different kinds of expressions:

//  - Expr.Rule - name = expression
//  - Expr.Choice - two or more expressions separated by '/'.
//  - Expr.Sequence - sequence of two or more expressions.
//  - Expr.And - expression preceded by '&'.
//  - Expr.Not - expression preceded by '!'.
//  - Expr.Plus - expression followed by '+'.
//  - Expr.Star - expression followed by '*'.
//  - Expr.Query - expression followed by '?'.
//  - Expr.PlusPlus - two expressions separated by by '++'.
//  - Expr.StarPlus - two expressions separated by by '*+'.
//  - Expr.Is - two expressions separated by by ':'.
//  - Expr.IsNot - two expressions separated by by ':!'.
//  - Expr.Ref - reference to another expression.
//  - Expr.StringLit - string literal.
//  - Expr.CharClass - character class.
//  - Expr.Range - character from range.
//  - Expr.Any - any character.
//  - Expr.End - expression '!_', meaning end of input.

public abstract class Expr {

  //  Common data.

  String name;
  int index;

  //  Recursion class if the expression is left-recursive.
  //  Otherwise null.
  LRec lrec = null;

  //  Reconstructed source text in 'true' form: with all literals converted to characters they represent.
  //  (Literals in the actual source may contain escapes.)
  String asString;

  //  Attributes: defaults.
  boolean def = false; // Defines a terminal string
  boolean nul = false; // Can generate null string
  boolean adv = false; // Can generate non-null string
  boolean end = false; // end-of-input in every generated string
  boolean fal = false; // Parsing procedure may fail

  //  Convenience flags: defaults.
  boolean isRule = false;
  boolean isSub = false;
  boolean isTerm = false;
  boolean isRef = false;
  boolean isPred = false;
  boolean isNamed = false;

  //  Binding strength.
  int bind = 4; // Default for terminals and Rule name

  //  A rule of the form: name = right-hand-side.
  static class Rule extends Expr {

    //  An absent action is represented by null (NOT empty String).
    Expr[] args; // Expressions on the right-hand side.
    Action[] onSucc; // Actions for components of Expr.
    Action[] onFail;
    String diagName; // Diagnostic name (null if none).

    //  Create the object with specified components.
    Rule(String name, String diagName, Expr[] args, Action[] onSucc, Action[] onFail) {
      this.name = name;
      this.diagName = diagName;
      this.args = args;
      this.onSucc = onSucc;
      this.onFail = onFail;
      isRule = true;
      isNamed = true;
    }

    //  Create dummy object - used in RefVisitor.
    Rule() {}
  }

  //  Expression 'arg-1 / arg-2 / ... / arg-n' where n>1.
  static class Choice extends Expr {

    Expr[] args;

    Choice(Expr[] args) {
      this.args = args;
      isSub = true;
      if (args.length > 1) {
        bind = 0;
      }
    }
  }

  //  Expression "arg-1 arg-2  ... arg-n" where n>1.
  static class Sequence extends Expr {

    Expr[] args;

    Sequence(Expr[] args) {
      this.args = args;
      isSub = true;
      bind = 1;
    }
  }

  //  Expression '&arg'.
  static class And extends Expr {

    Expr arg;

    And(Expr arg) {
      this.arg = arg;
      isSub = true;
      isPred = true;
      bind = 2;
    }
  }

  //  Expression '!arg'.
  static class Not extends Expr {

    Expr arg;

    Not(Expr arg) {
      this.arg = arg;
      isSub = true;
      isPred = true;
      bind = 2;
    }
  }

  //  Expression 'arg+'.
  static class Plus extends Expr {

    Expr arg;

    Plus(Expr arg) {
      this.arg = arg;
      isSub = true;
      bind = 3;
    }
  }

  //  Expression 'arg*'.
  static class Star extends Expr {

    Expr arg;

    Star(Expr arg) {
      this.arg = arg;
      isSub = true;
      bind = 3;
    }
  }

  //  Expression 'arg?'.
  static class Query extends Expr {

    Expr arg;

    Query(Expr arg) {
      this.arg = arg;
      isSub = true;
      bind = 3;
    }
  }

  //  Expression 'arg1++arg2'.
  static class PlusPlus extends Expr {

    Expr arg1, arg2;

    PlusPlus(Expr arg1, Expr arg2) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      isSub = true;
      bind = 3;
    }
  }

  //  Expression 'arg1*+arg2'.
  static class StarPlus extends Expr {

    Expr arg1, arg2;

    StarPlus(Expr arg1, Expr arg2) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      isSub = true;
      bind = 3;
    }
  }

  //  Expression 'arg1:arg2'.
  static class Is extends Expr {

    Expr arg1, arg2;

    Is(Expr arg1, Expr arg2) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      isSub = true;
      bind = 3;
    }
  }

  //  Expression 'arg1:!arg2'.
  static class IsNot extends Expr {

    Expr arg1, arg2;

    IsNot(Expr arg1, Expr arg2) {
      this.arg1 = arg1;
      this.arg2 = arg2;
      isSub = true;
      bind = 3;
    }
  }

  //  A reference to the Rule identified by 'name'.
  static class Ref extends Expr {

    Ref(String name) {
      this.name = name;
      asString = name;
      isRef = true;
      isNamed = true;
    }
  }

  //  A string literal.
  static class StringLit extends Expr {

    String s; // The string in true form.

    StringLit(String s) {
      this.s = s;
      def = true;
      fal = true;
      adv = true;
      isTerm = true;
      asString = "\"" + s + "\"";
    }
  }

  //  A range [a-z].
  //
  static class Range extends Expr {

    char a, z; // Range limits in true form.

    Range(char a, char z) {
      this.a = a;
      this.z = z;
      def = true;
      fal = true;
      adv = true;
      isTerm = true;
      asString = "[" + a + "-" + z + "]";
    }
  }

  //  A character class [s] or ^[s].
  static class CharClass extends Expr {

    String s; // The string in true form.
    boolean hat; // '^' present?

    CharClass(String s, boolean hat) {
      this.s = s;
      this.hat = hat;
      def = true;
      fal = true;
      adv = true;
      isTerm = true;
      asString = (hat ? "^[" : "[") + s + "]";
    }
  }

  //  'any character'.
  static class Any extends Expr {

    Any() {
      def = true;
      fal = true;
      adv = true;
      isTerm = true;
      asString = "_";
    }
  }

  //  end of input.
  static class End extends Expr {

    End() {
      def = true;
      fal = true;
      adv = true;
      end = true;
      isTerm = true;
      asString = "!_";
    }
  }

}