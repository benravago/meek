package mouse.peg;

import java.util.List;
import mouse.util.BitMatrix;

public record Links (

  List<LRec> lrecs,  // Left-recursion classes

  BitMatrix first,   // Expression relations
  BitMatrix clean,
  BitMatrix calls,

  BitMatrix First,   // Transitive closures
  BitMatrix Clean

) {}