package mouse.peg;

import java.util.Set;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.LinkedHashMap;
import java.io.PrintStream;
import java.time.Instant;

import mouse.util.BitMatrix;
import mouse.util.Template;

public class Generate {

  PrintStream ps;
  String pkgName, gramName;
  List<Expr.Rule> rules;
  List<Expr> terms;
  List<LRec> lrecs;
  BitMatrix first;
  Expr[] index;

  public record Summary(
    String root,
    int rules, int sub_expressions, int left_recursions,
    int semantic_actions
  ) {}

  String firstRule;
  int ruleProcs, innerProcs, recProcs, semActions;
  Set<String> include, optional;

  Map<String,String> template;

  public boolean apply(
    PrintStream out,       // output file
    String packageName,    // Parser package
    String grammarName,    // Parser class name
    List<Expr.Rule> rules, // Grammar Rules
    List<Expr> terms,      // Grammar Terminals
    List<LRec> lrecs,      // Left-Recursion classes
    Expr[] index,          // Array of expressions
    BitMatrix first        // Expr relations 'first'
  ) {
    ps = out;
    pkgName = packageName;
    gramName = grammarName;
    this.rules = rules;
    this.terms = terms;
    this.lrecs = lrecs;
    this.first = first;
    this.index = index;

    firstRule = rules.isEmpty() ? null : rules.getFirst().name;

    template = Template.from(this,"grammar.template");

    template.forEach((k,v) -> { switch (k) {
      case "Package" -> ps.format(v, pkgName);
      case "Imports" -> ps.print(v); // TODO: generateImports()
      case "Grammar" -> ps.format(v, gramName, myName(), Instant.now(), DONUT);
      case "Semantics" -> ps.print(v);
      case "Semantic_Action" -> ps.print(semanticActions(v));
      case "Semantics_body" -> ps.print(v);
      case "Semantics_end" -> ps.print(v);
      case "Parser" -> ps.print(v);
      case "Parse_Rule" -> generateParseRules(v);
      default -> { if (include.contains(k)) ps.print(v); }
        // accept, acceptInner, acceptPred,
        // reject, rejectInner, rejectPred,
        // next, ahead, aheadNot,
        // next_c, ahead_c, aheadNot_c, 
        // next_s, ahead_s, aheadNot_s, 
        // nextIn_c, aheadIn_c, aheadNotIn_c,
        // nextIn_s, aheadIn_s, aheadNotIn_s,
        // nextNot_c, aheadNotNot_c,
        // nextNotIn_s, aheadNotNotIn_s, 
        // is_b, setAction_a,
        // beginAsc, endAsc, endGrow,
        // ascentSemantics, descend,
        // push, pop,
        // consume, fail,
        // begin,
      case "parse" -> ps.format(v, firstRule);
      case "Parser_body" -> ps.format(v, optionalFields());
      case "Parser_api" -> ps.print(v);
      case "Parser_end" -> ps.print(v);
      case "Rule" -> ps.print(v);
      case "Phrase" -> ps.print(v);
      case "Grammar_end" -> ps.print(v);
    }});

    return true; // TODO:
  }

  String myName() { return getClass().getName(); }

  CharSequence semanticActions(String v) {
    var buf = new StringBuilder();
    ruleActions().forEach(act -> buf.append(
      String.format(v, (act.and ? "boolean" : "void"), act.name ) //  returnType actionName() {}
    ));
    return buf;
  }

  Collection<Action> ruleActions() {
    var sa = new LinkedHashMap<String, Action>(); // order-preserving
    for (var r:rules) {
      for (var i = 0; i < r.args.length; i++) {
        if (r.onSucc[i] != null) sa.put(r.onSucc[i].name, r.onSucc[i]);
        if (r.onFail[i] != null) sa.put(r.onFail[i].name, r.onFail[i]);
      }
    }
    semActions = sa.size();
    return sa.values();
  }

  @SuppressWarnings("unchecked")
  void generateParseRules(String body) { // template
    if (rules.isEmpty()) {
      include = optional = Collections.EMPTY_SET;
      return;
    }
    var rh = new Parsers();
    rh.apply( ps, body, rules, lrecs, index, first );
    // marker
    ps.println("//");
    // counters
    ruleProcs = rh.ruleProcs;
    innerProcs = rh.innerProcs;
    recProcs = rh.recProcs;
    // extra names
    include = rh.include;
    optional = rh.optional;
  }

  Object[] optionalFields() {
    var dcl = new StringBuilder(); // Type field;
    var dfn = new StringBuilder(); // field = Object;
    for (var f:optional) {
      var d = f.indexOf('=') < 0 ? dcl : dfn;
      d.append(f).append(' ');
    }
    return new Object[]{dcl,dfn};
  }

  public Summary summary() {
    return new Summary(firstRule,ruleProcs,innerProcs,recProcs,semActions);
  }

  final static String DONUT = "DO NOT EDIT";
}
