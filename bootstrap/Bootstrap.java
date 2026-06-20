void main() throws Exception {
  // read input PEG spec
  var src = new String(System.in.readAllBytes());
  // parse the PEG spec
  var ev = new mouse.peg.Evaluate();
  ev.parse(src);
  var p = ev.metadata();
  // generate the PEG parser
  var ge = new mouse.peg.Generate();
  ge.apply(
    System.out, 
    "mouse.peg", "PEG",
    p.rules(), p.terms(), p.links().lrecs(),
    p.index(), p.links().first()
  );
}
