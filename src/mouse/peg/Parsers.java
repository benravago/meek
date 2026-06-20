package mouse.peg;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mouse.util.BitMatrix;

import static mouse.util.Text.*;

public class Parsers {

  PrintStream ps;

  List<Expr.Rule> rules;
  List<LRec> lrecs;
  BitMatrix first;
  Expr[] index;

  // Counters
  int ruleProcs, innerProcs, recProcs;
  // Set of Parser optional fields
  Set<String> optional;
  // Set of Parser standard subroutines to include
  Set<String> include;

  // <Parse_Rule>
  //   %1$s %2$s(%3$s) {
  //     %4$s
  //   }
  // <...>
  String define, indent, endefi;

  void declare(String v) {
    var p = v.split("\n");
    define = p[0]+'\n';          // ps.format(define, returnType, ruleName);
    indent = p[1].split("%")[0]; // ps.println(indent + ... );
    endefi = p[2]+'\n';          // ps.print(endefi);
  }

  void define(Object...args) { ps.format(define,args); }
  void gen(CharSequence s, String...n) { ps.println(indent+s); for (var i:n) include.add(i); }
  void endefi() { ps.print(endefi); }

  // see lab/n/peg/tmp/z/Mouse-2.3/source/mouse/Generate.java  for comments

  void apply(
    PrintStream out,       // output file
    String body,           // Parser_Rule body template
    List<Expr.Rule> rules, // Grammar Rules
    List<LRec> lrecs,      // Left-Recursion classes
    Expr[] index,          // Array of expressions
    BitMatrix first        // Expression relations
  ) {
    this.ps = out;
    this.rules = rules;
    this.lrecs = lrecs;
    this.first = first;
    this.index = index;

    ruleProcs = innerProcs = recProcs = 0;
    subs = new ArrayList<>();
    optional = new HashSet<>();
    include = new HashSet<>();
    add(include, "begin","accept","reject","push","pop","consume","fail");

    declare(body);

    // MAIN

    for (var expr:rules) {
      if (expr.lrec == null) {
        generateRule(expr);
      }
    }
    for (var rc:lrecs) {
      generateCommonEntry(rc);
      for (var entry:rc.entries()) {
        generateEntry(entry);
      }
      for (var seed:rc.seeds()) {
        generateSeed(seed, rc);
      }
      for (var x:rc.members()) {
        switch(x) {
          case Expr.Rule e -> { generateRuleAscent(e); }
          case Expr.Choice e -> { generateChoiceAscent(e); }
          case Expr.Sequence e -> { generateSequenceAscent(e); }
          default -> throw new IllegalArgumentException(snoc("ascent",x));
        }
      }
    }
  }

  void generateRule(Expr.Rule rule) {
    define("boolean", rule.name, "");
    var n = rule.name;
    if (rule.diagName != null) n +=  "\", \"" + str(rule.diagName);
    gen("begin(\"" + n + "\");");
    if (rule.args.length == 1 && rule.onFail[0] == null) {
      var e = rule.args[0];
      var act = rule.onSucc[0];
      inline(e, "reject()");
      if (act == null) {
        gen("return accept();");
      } else if (act.and) {
        gen("if (sem." + act.name + "()) { return accept(); }");
        gen("return reject();");
      } else {
        gen("sem." + act.name + "();");
        gen("return accept();");
      }
    } else {
      for (var i = 0; i < rule.args.length; i++) {
        var succ = rule.onSucc[i];
       if (succ == null) {
          gen("if (" + ref(rule.args[i]) + ") { return accept(); }");
        } else if (succ.and) {
          gen("if (" + ref(rule.args[i]) + ") { if (sem." + succ.name + "()) { return accept(); } }");
        } else {
          gen("if (" + ref(rule.args[i]) + ") { sem." + succ.name + "(); return accept(); }");
        }
        var fail = rule.onFail[i];
        if (fail != null) {
          gen("sem." + fail.name + "();");
        }
      }
      gen("return reject();");
    }

    endefi();

    generateSubs();
    ruleProcs++;
  }

  void generateSub(Expr expr) {
    var name = expr.name;

    define("boolean", name, "");
    gen("begin(\"" + expr.name + "\");");
    procedure(expr);
    endefi();

    innerProcs++;
  }

  void generateEntry(Expr entry) {
    var rc = entry.lrec;

    define("boolean ", entry.name, "");
    gen("begin(\"" + entry.name + "\");");
    gen("if ($$" + rc.name() + "()) { return accept(); }");
    gen("return reject();");
    endefi();

    generateSubs();
    recProcs++;
  }

  void generateCommonEntry(LRec rc) {
    add(optional,
      "Deque<Phrase> ascents;",
      "ascents = new ArrayDeque<>();"
    );

    define("boolean", "$$"+rc.name(), "");
    gen("beginAsc();");
    gen("var ok =");
    for (var i = 0; i < rc.seeds().size(); i++) {
      gen("  " + rc.name() + "$" + rc.seeds().get(i).name + "()" + (i == rc.seeds().size() - 1 ? ";" : " ||"));
    }
    gen("endAsc();");
    gen("return ok;");
    endefi();
  }

  void generateSeed(Expr seed, LRec rc) {
    var name = seed.name;
    var procName = rc.name() + "$" + name;

    define("boolean", procName, "");
    gen("begin(\"" + procName + "\");");
    gen("if (!" + ref(seed) + ") { return reject(); }");
    generateClimb(seed, rc);
    gen("return reject();");
    endefi();

    generateSubs();
    recProcs++;
  }

  void generateRuleAscent(Expr.Rule expr) {
    var procName = "$" + expr.name;
    var rc = expr.lrec;

    define("boolean" + procName + "Runnable act");
    gen("begin(\"" + procName + "\",\"" + expr.name + "\");"); // diag
    gen("setAction(act);", "setAction_a");
    generateClimb(expr, rc);
    gen("return reject();");
    endefi();

    generateSubs();
    recProcs++;
  }

  void generateChoiceAscent(Expr.Choice expr) {
    var procName = "$" + expr.name;
    var rc = expr.lrec;

    define("boolean", procName, "");
    gen("begin(\"" + procName + "\",\"" + expr.name + "\");"); // diag
    generateClimb(expr, rc);
    gen("return rejectInner();", "rejectInner");
    endefi();

    generateSubs();
    recProcs++;
  }

  void generateSequenceAscent(Expr.Sequence expr) {
    var procName = "$" + expr.name;
    var rc = expr.lrec;

    define("boolean", procName, "");
    gen("begin(\"" + procName + "\",\"" + expr.name + "\");"); // diag
    for (var i = 1; i < expr.args.length; i++) {
      gen("if (!" + ref(expr.args[i]) + ") { return rejectInner(); }", "rejectInner");
    }
    generateClimb(expr, rc);
    gen("return rejectInner();", "rejectInner");
    endefi();

    generateSubs();
    recProcs++;
  }

  List<Expr> subs;
  int done;

  void generateSubs() {
    var toDo = subs.size();
    while (done < toDo) {
      for (var i = done; i < toDo; i++) {
        var expr = subs.get(i);
        generateSub(expr);
      }
      done = toDo;
      toDo = subs.size();
    }
  }

  String ref(Expr expr) {
    if (expr instanceof Expr.End) {
      return incl("aheadNot()");
    }
    if (expr.isTerm) {
      return incl("next" + terminal(expr));
    }
    if (expr.isSub && expr.lrec == null && !subs.contains(expr)) {
      subs.add(expr);
    }
    return expr.name + "()";
  }

  void generateClimb(Expr expr, LRec rc) {
    for (var prev:haveAsFirst(rc,expr)) {
      if (prev.isRule) {
        var rule = (Expr.Rule) prev;
        var i = alt(rule, expr);
        var act = rule.onSucc[i] == null
          ? "()->{}" // empty$$
          : "()->sem." + rule.onSucc[i].name + "()";
        gen("if ($" + prev.name + "(" + act + ")) { return accept(); }");
      } else {
        gen("if ($" + prev.name + "()) { return accept(); }");
      }
    }
    if (rc.entries().contains(expr)) {
      gen("if (endGrow()) { return accept(); }");
    }
  }

  int alt(Expr.Rule rule, Expr expr) {
    for (var i = 0; i < rule.args.length; i++) {
      if (rule.args[i] == expr) {
        return i;
      }
    }
    throw new IllegalStateException("invalid alternative");
  }

  void procedure(Expr x) {
    switch (x) {
      case Expr.Rule e -> {
        throw new IllegalArgumentException(snoc("procedure",e));
      }
      case Expr.Choice e -> {
        for (var a:e.args) {
          gen("if (" + ref(a) + ") { return acceptInner(); }", "acceptInner");
        }
        gen("return rejectInner();", "rejectInner");
      }
      case Expr.Sequence e -> {
        for (var a:e.args) {
          inline(a, "rejectInner()");
        }
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.And e -> {
        gen("if (!" + ref(e.arg) + ") { return rejectPred(); }");
        gen("return acceptPred();");
      }
      case Expr.Not e -> {
        gen("if (" + ref(e.arg) + ") { return rejectPred(); }");
        gen("return acceptPred();");
      }
      case Expr.Plus e -> {
        gen("if (!" + ref(e.arg) + ") { return rejectInner(); }", "rejectInner");
        gen("while (" + ref(e.arg) + ") {}");
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.Star e -> {
        gen("while (" + ref(e.arg) + ") {}");
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.Query e -> {
        gen(ref(e.arg) + ";");
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.PlusPlus e -> {
        gen("if (" + ref(e.arg2) + ") { return rejectInner(); }", "rejectInner");
        gen("do { if (!" + ref(e.arg1) + ") { return rejectInner(); } } while (!" + ref(e.arg2) + ");", "rejectInner");
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.StarPlus e -> {
        gen("while (!" + ref(e.arg2) + ") if (!" + ref(e.arg1) + ") { return rejectInner(); }", "rejectInner");
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.Is e -> {
        gen("if (!is(false,()->" + ref(e.arg1) + ",()->" + ref(e.arg2) + ")) { return rejectInner(); }", "rejectInner");
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.IsNot e -> {
        gen("if (!is(true,()->" + ref(e.arg1) + ",()->" + ref(e.arg2) + ")) { return rejectInner(); }", "rejectInner");
        gen("return acceptInner();", "acceptInner");
      }
      case Expr.Ref e -> {
        throw new IllegalStateException(snoc("procedure",e));
      }
      case Expr.StringLit e -> { procedureTerminal(e); }
      case Expr.CharClass e -> { procedureTerminal(e); }
      case Expr.Range e -> { procedureTerminal(e); }
      case Expr.Any e -> { procedureTerminal(e); }
      case Expr.End e -> { procedureTerminal(e); }

      default -> {} // throw new IllegalArgumentException(""+x);
    }
  }

  void procedureTerminal(Expr expr) {
    gen("if (!" + ref(expr) + ") { return rejectInner(); }", "rejectInner");
    gen("return acceptInner();", "acceptInner");
  }

  void inline(Expr x, String reject) {
    switch (x) {
      case Expr.Rule e -> {
        gen( e.fal ? "if (!" + e.name + "()) { return " + reject + "; }" : e.name + "();" );
      }
      case Expr.Choice e -> {
        gen("if (" + inlineChoice(e.args) + " ) { return " + reject + "; }");
      }
      case Expr.Sequence e -> {
        for (var a:e.args) inline(a,reject);
      }
      case Expr.And e -> {
        var cond = e.arg.isTerm ? "ahead" + terminal(e.arg) : ref(e);
        gen("if (!" + incl(cond) + ") { return " + reject + "; }");
      }
      case Expr.Not e -> {
        var cond = e.arg.isTerm ? "aheadNot" + terminal(e.arg) : ref(e);
        gen("if (!" + incl(cond) + ") { return " + reject + "; }");
      }
      case Expr.Plus e -> {
        gen("if (!" + ref(e.arg) + ") { return " + reject + "; }");
        gen("while (" + ref(e.arg) + ") {}");
      }
      case Expr.Star e -> {
        gen("while (" + ref(e.arg) + ") {}");
      }
      case Expr.Query e -> {
        gen(ref(e.arg) + ";");
      }
      case Expr.PlusPlus e -> {
        gen("if (" + ref(e.arg2) + ") { return " + reject + "; }");
        gen("do { if (!" + ref(e.arg1) + ") { return " + reject + "; } } while (!" + ref(e.arg2) + ");");
      }
      case Expr.StarPlus e -> {
        gen("while (!" + ref(e.arg2) + ") { if (!" + ref(e.arg1) + ") { return " + reject + "; } }");
      }
      case Expr.Is e -> {
        gen("if (!is(true,()->" + ref(e.arg1) + ",()->" + ref(e.arg2) + ")) { return " + reject + "; }");
      }
      case Expr.IsNot e -> {
        gen("if (!is(false,()->" + ref(e.arg1) + ",()->" + ref(e.arg2) + ")) { return " + reject + "; }");
      }
      case Expr.Ref e -> {
        throw new IllegalArgumentException(snoc("inline",e));
      }
      case Expr.StringLit e -> { inlineTerminal(e,reject); }
      case Expr.CharClass e -> { inlineTerminal(e,reject); }
      case Expr.Range e -> { inlineTerminal(e,reject); }
      case Expr.Any e -> { inlineTerminal(e,reject); }
      case Expr.End e -> { inlineTerminal(e,reject); }

      default -> {} // throw new IllegalArgumentException(""+x);
    }
    var p = reject.indexOf('(');
    include.add(p < 0 ? reject : reject.substring(0,p));
  }
  /*
      case Expr.Choice e -> {
        var arg = e.args[0];
        gen("if (!" + ref(arg));
        for (var i = 1; i < e.args.length; i++) {
          arg = e.args[i];
          gen(" && !" + ref(arg));
        }
        gen("   ) { return " + reject + "; }");
      }
   */
  String inlineChoice(Expr[] args) {
    var b = new StringBuilder();
    for (var a:args) {
      b.append(" && !").append(ref(a));
    }
    return b.substring(3); // skip leading ' &&'
  }

  void inlineTerminal(Expr expr, String reject) {
    gen("if (!" + ref(expr) + ") { return " + reject + "; }");
  }

  String terminal(Expr x) {
    return switch (x) {
      case Expr.StringLit e -> {
        yield e.s.length() == 1
          ? "('"  + str(e.s.charAt(0)) + "')"
          : "(\"" + str(e.s) + "\")";
      }
      case Expr.CharClass e -> {
        yield e.s.length() == 1
          ? (e.hat ? "Not"   : ""  ) + "(\'" + str(e.s.charAt(0)) + "\')"
          : (e.hat ? "NotIn" : "In") + "(\"" + str(e.s) + "\")";
      }
      case Expr.Range e -> "In('" + str(e.a) + "','" + str(e.z) + "')";
      case Expr.Any _ -> "()";

      case Expr.End e -> {
        throw new IllegalArgumentException(snoc("terminal",e));
      }

      default -> throw new IllegalStateException(snoc("terminal",x));
    };
  }

  String incl(String t) {
    var p = t.indexOf('(');
    var s = switch(t.charAt(p+1)) {
      case '"' -> "_s";
      case '\'' -> "_c";
      default -> "";
    };
    include.add(t.substring(0,p)+s);
    return t;
  }

  //  Get expressions that have 'expr' as first.
  List<Expr> haveAsFirst(LRec lrec, Expr expr) {
    var prec = first.column(expr.index);
    prec.and(lrec.memberIndex());
    var result = new ArrayList<Expr>();
    var i = 0;
    while ((i = prec.nextSetBit(i)) > -1) {
      result.add(index[i]);
    }
    return result;
  }

  static void add(Collection <String> c, String...v) { for (var e:v) c.add(e); }
  static String snoc(String m, Expr e) { return m+": "+e.name+' '+e; }
}
