package mouse.peg;

import java.util.List;

public record Parsed (

  List<Expr.Rule> rules, // List of Rules
  List<Expr> subs,       // List of subexpressions
  List<Expr> terms,      // List of terminals

  Expr[] index, // Array of expressions
  int E,        // Number of expressions
  int R,        // Number of rules
  int N,        // Number of nonterminals

  Links links   // Expression relations

) {}