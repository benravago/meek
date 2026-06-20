package mouse.peg;

class Action {

  String name;
  boolean and;

  Action(String name, boolean and) {
    this.name = name;
    this.and = and;
  }

  String text() {
    return "{" + (and ? "&" : "") + name + "}";
  }

}