package mouse.peg;

class Attributes {

  int steps;

  /**
   *  Compute attributes for all expressions.
   *
   *  The meaning of attributes is:
   *    def - defines a terminal string.
   *    nul - can generate null string.
   *    adv - can generate non-null string.
   *    end - end-of-input in each generated string.
   *    fal - may fail.
   *
   *  The attributes are computed by iteration to a fixpoint.
   *  The attributes for terminals are preset by their constructors.
   *  For other expressions they are preset to 'false'.
   *  The iteration step is performed by AttrVisitor.
   *  The computation is monotone, meaning that a value is never changed from 'true' to 'false'.
   *
   *  The procedure has 'index' as parameter because it is also used for dual grammar.
   */
  void compute(Expr[] index, int N) {
    var trueAttrs = 0; // Number of true attributes after last step
    var a = 0;         // Number of true attributes before last step
    steps = 0;         // Number of steps
    // The iteration step increases 'trueAttrs'.
    // The iteration stops when it does not.
    for (;;) {
      // Iteration step
      for (var i=0; i < N; i++) {
        accept(index[i]);
      }
      // Count true attributes (non-terminals only)
      trueAttrs = 0;
      for (int i = 0; i < N; i++) {
        var e = index[i];
        trueAttrs += (e.def ? 1 : 0) + (e.nul ? 1 : 0) + (e.adv ? 1 : 0) + (e.end ? 1 : 0) + (e.fal ? 1 : 0);
      }
      // Break if fixpoint reached
      if (trueAttrs == a) {
        break;
      }
      // To next step
      a = trueAttrs;
      steps++;
    }
  }

  // Each visit computes attributes from those of subexpressions.
  // Attributes for terminals are preset by their constructors.
  // The computation is monotone starting with 'false', which means 'true' is never changed to 'false'.

  static void accept(Expr x) {
    switch (x) {
      case Expr.Rule e -> {
        var exGen = false;
        var exNul = false;
        var exAdv = false;
        var alEnd = true;
        var alFal = true;
        for (var i = 0; i < e.args.length; i++) {
          var a = e.args[i];
          exGen |= a.def;
          exNul |= a.nul;
          exAdv |= a.adv;
          alEnd &= a.end;
          alFal &= (a.fal | (e.onSucc[i] != null && e.onSucc[i].and));
        }
        e.def |= exGen;
        e.nul |= exNul;
        e.adv |= exAdv;
        e.end |= alEnd;
        e.fal |= alFal;
      }
      case Expr.Choice e -> {
        var exGen = false;
        var exNul = false;
        var exAdv = false;
        var alEnd = true;
        var alFal = true;
        for (var a : e.args) {
          exGen |= a.def;
          exNul |= a.nul;
          exAdv |= a.adv;
          alEnd &= a.end;
          alFal &= a.fal;
        }
        e.def |= exGen;
        e.nul |= exNul;
        e.adv |= exAdv;
        e.end |= alEnd;
        e.fal |= alFal;
      }
      case Expr.Sequence e -> {
        var alGen = true;
        var alNul = true;
        var exAdv = false;
        var exEnd = false;
        var exFal = false;
        for (var a : e.args) {
          alGen &= a.def;
          alNul &= a.nul;
          exAdv |= a.adv;
          exEnd |= a.end;
          exFal |= a.fal;
        }
        e.def |= alGen;
        e.nul |= alNul;
        e.adv |= exAdv;
        e.end |= exEnd;
        e.fal |= exFal;
      }
      case Expr.And e -> {
        var a = e.arg;
        e.def |= a.def;
        e.nul = true;
        e.fal = true;
      }
      case Expr.Not e -> {
        var a = e.arg;
        e.def |= a.def;
        e.nul = true;
        e.fal = true;
      }
      case Expr.Plus e -> {
        var a = e.arg;
        e.def |= a.def;
        e.nul |= a.nul;
        e.adv |= a.adv;
        e.end |= a.end;
        e.fal |= a.fal;
      }
      case Expr.Star e -> {
        var a = e.arg;
        e.def = true;
        e.nul = true;
        e.adv |= a.adv;
      }
      case Expr.Query e -> {
        var a = e.arg;
        e.def = true;
        e.nul = true;
        e.adv |= a.adv;
      }
      case Expr.PlusPlus e -> {
        var a = e.arg1;
        var b = e.arg2;
        e.def |= (a.def & b.def);
        e.nul |= (a.nul & b.nul);
        e.adv |= (a.adv | b.adv);
        e.end |= (a.end | b.end);
        e.fal |= (a.fal | b.fal);
      }
      case Expr.StarPlus e -> {
        var a = e.arg1;
        var b = e.arg2;
        e.def |= b.def;
        e.nul |= b.nul;
        e.adv |= (a.adv | b.adv);
        e.end |= b.end;
        e.fal |= b.fal;
      }
      case Expr.Is e -> {
        var a = e.arg1;
        var b = e.arg2;
        e.def |= (a.def & b.def);
        e.nul |= (a.nul & b.nul);
        e.adv |= (a.adv & b.adv);
        e.end |= (a.end & b.end);
        e.fal |= (a.fal | b.fal);
      }
      case Expr.IsNot e -> {
        var a = e.arg1;
        var b = e.arg2;
        e.def |= (a.def & b.def);
        e.nul |= a.nul;
        e.adv |= a.adv;
        e.end |= a.end;
        e.fal |= a.fal;
      }
      default -> {}
    }
  }

}
