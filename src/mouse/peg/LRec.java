package mouse.peg;

import java.util.BitSet;
import java.util.List;

// Represents left-recursion class.
record LRec (

  // Name of the class - the name of Rule used in constructor
  String name,

  // The class as set of indexes of members
  BitSet memberIndex,

  // The class listed as List of members
  List<Expr> members,

  // Lists of entries, exits, and seeds
  List<Expr> entries,
  List<Expr> exits,
  List<Expr> seeds

) {}