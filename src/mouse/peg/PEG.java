package mouse.peg;
import java.util.*;
@javax.annotation.processing.Generated(value="mouse.peg.Generate",date="2026-06-21T23:53:59.835853Z",comments="DO NOT EDIT")
public class PEG {
public static class Semantics {
  protected void Grammar() {}
  protected void Rule() {}
  protected void Error() {}
  protected void RuleRhs() {}
  protected void Choice() {}
  protected void Sequence() {}
  protected void Prefix() {}
  protected void Pass() {}
  protected void Infix() {}
  protected void Suffix() {}
  protected void Resolve() {}
  protected void Pass2() {}
  protected void Any() {}
  protected void Actions() {}
  protected void OnSucc() {}
  protected void OnFail() {}
  protected void Name() {}
  protected void DiagName() {}
  protected void StringLit() {}
  protected void CharClass() {}
  protected void Range() {}
  protected void Char() {}
  protected void Unicode() {}
  protected void Tab() {}
  protected void Newline() {}
  protected void CarRet() {}
  protected void Escape() {}
  protected void Space() {}
  protected Rule rule;
  protected void init() {}
  protected void close() {}
  protected Phrase lhs() { return rule.lhs(); }
  protected int rhsSize() { return rule.rhsSize(); }
  protected Phrase rhs(int i) { return rule.rhs(i); }
  protected CharSequence rhsText(int i, int j) { return rule.rhsText(i, j); }
}
public static class Parser implements Rule {
  boolean Grammar() {
    begin("Grammar");
    Space();
    while (!EOT()) { if (!Grammar_2()) { return reject(); } }
    sem.Grammar();
    return accept();
  }
  boolean Grammar_2() {
    begin("Grammar_2");
    if (Rule()) { return acceptInner(); }
    if (Skip()) { return acceptInner(); }
    return rejectInner();
  }
  boolean Rule() {
    begin("Rule");
    if (Rule_0()) { sem.Rule(); return accept(); }
    sem.Error();
    return reject();
  }
  boolean Rule_0() {
    begin("Rule_0");
    if (!Name()) { return rejectInner(); }
    if (!EQUAL()) { return rejectInner(); }
    if (!RuleRhs()) { return rejectInner(); }
    DiagName();
    if (!SEMI()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Skip() {
    begin("Skip");
    if (SEMI()) { return accept(); }
    if (Skip_0()) { return accept(); }
    return reject();
  }
  boolean Skip_0() {
    begin("Skip_0");
    if (Skip_2()) { return rejectInner(); }
    do { if (!next()) { return rejectInner(); } } while (!Skip_2());
    return acceptInner();
  }
  boolean Skip_2() {
    begin("Skip_2");
    if (SEMI()) { return acceptInner(); }
    if (EOT()) { return acceptInner(); }
    return rejectInner();
  }
  boolean RuleRhs() {
    begin("RuleRhs", "right-hand side");
    if (!Sequence()) { return reject(); }
    Actions();
    while (RuleRhs_2()) {}
    sem.RuleRhs();
    return accept();
  }
  boolean RuleRhs_2() {
    begin("RuleRhs_2");
    if (!SLASH()) { return rejectInner(); }
    if (!Sequence()) { return rejectInner(); }
    Actions();
    return acceptInner();
  }
  boolean Choice() {
    begin("Choice");
    if (!Sequence()) { return reject(); }
    while (Choice_2()) {}
    sem.Choice();
    return accept();
  }
  boolean Choice_2() {
    begin("Choice_2");
    if (!SLASH()) { return rejectInner(); }
    if (!Sequence()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Sequence() {
    begin("Sequence");
    if (!Prefixed()) { return reject(); }
    while (Prefixed()) {}
    sem.Sequence();
    return accept();
  }
  boolean Prefixed() {
    begin("Prefixed");
    if (Prefixed_0()) { sem.Prefix(); return accept(); }
    if (Suffixed()) { sem.Pass(); return accept(); }
    return reject();
  }
  boolean Prefixed_0() {
    begin("Prefixed_0");
    if ( !AND() && !NOT() ) { return rejectInner(); }
    if (!Suffixed()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Suffixed() {
    begin("Suffixed");
    if (Suffixed_0()) { sem.Infix(); return accept(); }
    if (Suffixed_2()) { sem.Suffix(); return accept(); }
    if (Primary()) { sem.Pass(); return accept(); }
    return reject();
  }
  boolean Suffixed_0() {
    begin("Suffixed_0");
    if (!Primary()) { return rejectInner(); }
    if ( !STARPLUS() && !PLUSPLUS() && !IS() && !ISNOT() ) { return rejectInner(); }
    if (!Primary()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Suffixed_2() {
    begin("Suffixed_2");
    if (!Primary()) { return rejectInner(); }
    if ( !QUERY() && !STAR() && !PLUS() ) { return rejectInner(); }
    return acceptInner();
  }
  boolean Primary() {
    begin("Primary");
    if (Name()) { sem.Resolve(); return accept(); }
    if (Primary_0()) { sem.Pass2(); return accept(); }
    if (ANY()) { sem.Any(); return accept(); }
    if (StringLit()) { sem.Pass(); return accept(); }
    if (Range()) { sem.Pass(); return accept(); }
    if (CharClass()) { sem.Pass(); return accept(); }
    return reject();
  }
  boolean Primary_0() {
    begin("Primary_0");
    if (!LPAREN()) { return rejectInner(); }
    if (!Choice()) { return rejectInner(); }
    if (!RPAREN()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Actions() {
    begin("Actions");
    OnSucc();
    OnFail();
    sem.Actions();
    return accept();
  }
  boolean OnSucc() {
    begin("OnSucc");
    OnSucc_1();
    sem.OnSucc();
    return accept();
  }
  boolean OnSucc_1() {
    begin("OnSucc_1");
    if (!LWING()) { return rejectInner(); }
    AND();
    Name();
    if (!RWING()) { return rejectInner(); }
    return acceptInner();
  }
  boolean OnFail() {
    begin("OnFail");
    OnFail_1();
    sem.OnFail();
    return accept();
  }
  boolean OnFail_1() {
    begin("OnFail_1");
    if (!TILDA()) { return rejectInner(); }
    if (!LWING()) { return rejectInner(); }
    Name();
    if (!RWING()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Name() {
    begin("Name");
    if (!Letter()) { return reject(); }
    while (Name_2()) {}
    Space();
    sem.Name();
    return accept();
  }
  boolean Name_2() {
    begin("Name_2");
    if (Letter()) { return acceptInner(); }
    if (Digit()) { return acceptInner(); }
    return rejectInner();
  }
  boolean DiagName() {
    begin("DiagName");
    if (!next('<')) { return reject(); }
    if (next('>')) { return reject(); }
    do { if (!Char()) { return reject(); } } while (!next('>'));
    Space();
    sem.DiagName();
    return accept();
  }
  boolean StringLit() {
    begin("StringLit");
    if (!next('"')) { return reject(); }
    if (next('"')) { return reject(); }
    do { if (!Char()) { return reject(); } } while (!next('"'));
    Space();
    sem.StringLit();
    return accept();
  }
  boolean CharClass() {
    begin("CharClass");
    if ( !next('[') && !next("^[") ) { return reject(); }
    if (next(']')) { return reject(); }
    do { if (!Char()) { return reject(); } } while (!next(']'));
    Space();
    sem.CharClass();
    return accept();
  }
  boolean Range() {
    begin("Range");
    if (!next('[')) { return reject(); }
    if (!Char()) { return reject(); }
    if (!next('-')) { return reject(); }
    if (!Char()) { return reject(); }
    if (!next(']')) { return reject(); }
    Space();
    sem.Range();
    return accept();
  }
  boolean Char() {
    begin("Char");
    if (Escape()) { sem.Pass(); return accept(); }
    if (nextNotIn("\r\n\\")) { sem.Char(); return accept(); }
    return reject();
  }
  boolean Escape() {
    begin("Escape");
    if (Escape_0()) { sem.Unicode(); return accept(); }
    if (next("\\t")) { sem.Tab(); return accept(); }
    if (next("\\n")) { sem.Newline(); return accept(); }
    if (next("\\r")) { sem.CarRet(); return accept(); }
    if (Escape_5()) { sem.Escape(); return accept(); }
    return reject();
  }
  boolean Escape_0() {
    begin("Escape_0");
    if (!next("\\u")) { return rejectInner(); }
    if (!HexDigit()) { return rejectInner(); }
    if (!HexDigit()) { return rejectInner(); }
    if (!HexDigit()) { return rejectInner(); }
    if (!HexDigit()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Escape_5() {
    begin("Escape_5");
    if (!aheadNot("\\u")) { return rejectInner(); }
    if (!next('\\')) { return rejectInner(); }
    if (!next()) { return rejectInner(); }
    return acceptInner();
  }
  boolean Letter() {
    begin("Letter");
    if (nextIn('a','z')) { return accept(); }
    if (nextIn('A','Z')) { return accept(); }
    return reject();
  }
  boolean Digit() {
    begin("Digit");
    if (!nextIn('0','9')) { return reject(); }
    return accept();
  }
  boolean HexDigit() {
    begin("HexDigit");
    if (nextIn('0','9')) { return accept(); }
    if (nextIn('a','f')) { return accept(); }
    if (nextIn('A','F')) { return accept(); }
    return reject();
  }
  boolean AND() {
    begin("AND", "&");
    if (!next('&')) { return reject(); }
    Space();
    return accept();
  }
  boolean NOT() {
    begin("NOT", "!");
    if (!next('!')) { return reject(); }
    Space();
    return accept();
  }
  boolean QUERY() {
    begin("QUERY", "?");
    if (!next('?')) { return reject(); }
    Space();
    return accept();
  }
  boolean STAR() {
    begin("STAR", "*");
    if (!next('*')) { return reject(); }
    if (!aheadNot('+')) { return reject(); }
    Space();
    return accept();
  }
  boolean PLUS() {
    begin("PLUS", "+");
    if (!next('+')) { return reject(); }
    if (!aheadNot('+')) { return reject(); }
    Space();
    return accept();
  }
  boolean STARPLUS() {
    begin("STARPLUS", "*+");
    if (!next("*+")) { return reject(); }
    Space();
    return accept();
  }
  boolean PLUSPLUS() {
    begin("PLUSPLUS", "++");
    if (!next("++")) { return reject(); }
    Space();
    return accept();
  }
  boolean IS() {
    begin("IS", ":");
    if (!next(':')) { return reject(); }
    if (!aheadNot('!')) { return reject(); }
    Space();
    return accept();
  }
  boolean ISNOT() {
    begin("ISNOT", ":!");
    if (!next(":!")) { return reject(); }
    Space();
    return accept();
  }
  boolean EQUAL() {
    begin("EQUAL", "=");
    if (!next('=')) { return reject(); }
    Space();
    return accept();
  }
  boolean LPAREN() {
    begin("LPAREN", "(");
    if (!next('(')) { return reject(); }
    Space();
    return accept();
  }
  boolean RPAREN() {
    begin("RPAREN", ")");
    if (!next(')')) { return reject(); }
    Space();
    return accept();
  }
  boolean LWING() {
    begin("LWING", "{");
    if (!next('{')) { return reject(); }
    Space();
    return accept();
  }
  boolean RWING() {
    begin("RWING", "}");
    if (!next('}')) { return reject(); }
    Space();
    return accept();
  }
  boolean SEMI() {
    begin("SEMI", ";");
    if (!next(';')) { return reject(); }
    Space();
    return accept();
  }
  boolean SLASH() {
    begin("SLASH", "/");
    if (!next('/')) { return reject(); }
    Space();
    return accept();
  }
  boolean TILDA() {
    begin("TILDA", "~");
    if (!next('~')) { return reject(); }
    Space();
    return accept();
  }
  boolean ANY() {
    begin("ANY", "_");
    if (!next('_')) { return reject(); }
    Space();
    return accept();
  }
  boolean Space() {
    begin("Space");
    while (Space_1()) {}
    sem.Space();
    return accept();
  }
  boolean Space_1() {
    begin("Space_1");
    if (nextIn(" \r\n\t")) { return acceptInner(); }
    if (Comment()) { return acceptInner(); }
    return rejectInner();
  }
  boolean Comment() {
    begin("Comment");
    if (!next("//")) { return reject(); }
    while (!EOL()) { if (!next()) { return reject(); } }
    return accept();
  }
  boolean EOL() {
    begin("EOL", "end of line");
    if (EOL_0()) { return accept(); }
    if (aheadNot()) { return accept(); }
    return reject();
  }
  boolean EOL_0() {
    begin("EOL_0");
    next('\r');
    if (!next('\n')) { return rejectInner(); }
    return acceptInner();
  }
  boolean EOT() {
    begin("EOT", "end of text");
    if (!aheadNot()) { return reject(); }
    return accept();
  }
//
  boolean accept() {
    var p = pop();
    p.rhs.clear();
    current.end = pos;
    current.rhs.add(p);
    current.hwmUpdFrom(p);
    current.defAct.addAll(p.defAct);
    return true;
  }
  boolean acceptInner() {
    var p = pop();
    current.end = pos;
    current.rhs.addAll(p.rhs);
    current.hwmUpdFrom(p);
    current.defAct.addAll(p.defAct);
    return true;
  }
  boolean reject() {
    var p = pop();
    pos = p.start;
    p.end = pos;
    p.rhs = null;
    if (pos < p.hwm) {
      // no-op
    } else if (p.hwm == -2) {
      p.hwm = -1;
    } else if (p.hwm == -1 || p.name.charAt(0) != '$') {
      p.hwmSet(p.diag, p.start);
    }
    current.end = pos;
    current.hwmUpdFrom(p);
    return false;
  }
  boolean rejectInner() {
    var p = pop();
    pos = p.start;
    p.end = pos;
    p.rhs = null;
    current.end = pos;
    current.hwmUpdFrom(p);
    return false;
  }
  boolean next() {
    return (pos < endpos) ? consume(1) : fail("any character");
  }
  boolean aheadNot() {
    return (pos < endpos) ? fail("end of text") : true;
  }
  boolean next(char ch) {
    return (pos < endpos && source.charAt(pos) == ch) ? consume(1) : fail("'" + ch + "'");
  }
  boolean aheadNot(char ch) {
    return (pos < endpos && source.charAt(pos) == ch) ? fail("not '" + ch + "'") : true;
  }
  boolean next(String s) {
    var lg = s.length();
    return (pos + lg <= endpos && source.subSequence(pos, pos + lg).equals(s)) ? consume(lg) : fail("'" + s + "'");
  }
  boolean aheadNot(String s) {
    int lg = s.length();
    return (pos + lg <= endpos && source.subSequence(pos, pos + lg).equals(s)) ? fail("not '" + s + "'") : true;
  }
  boolean nextIn(char a, char z) {
    return (pos < endpos && source.charAt(pos) >= a && source.charAt(pos) <= z) ? consume(1) : fail("[" + a + "-" + z + "]");
  }
  boolean nextIn(String s) {
    return (pos < endpos && s.indexOf(source.charAt(pos)) >= 0) ? consume(1) : fail("[" + s + "]");
  }
  boolean nextNotIn(String s) {
    return (pos < endpos && s.indexOf(source.charAt(pos)) < 0) ? consume(1) : fail("not [" + s + "]");
  }
  Phrase push(Phrase p) {
    var top = current;
    p.parent = top;
    current = p;
    return top;
  }
  Phrase pop() {
    var p = current;
    current = p.parent;
    p.parent = null;
    return p;
  }
  boolean consume(int n) {
    var p = new Phrase("", "", pos);
    pos += n;
    p.end = pos;
    current.rhs.add(p);
    current.end = pos;
    return true;
  }
  boolean fail(String msg) {
    current.hwmUpd(msg, pos);
    return false;
  }
  void begin(String name, String diag) {
    push(new Phrase(name, diag, pos));
  }
  void begin(String name) {
    begin(name, name);
  }
  public boolean parse(CharSequence src, Semantics dest) {
    source = src;
    sem = dest;
    init();
    var rc = Grammar();
    close();
    return rc;
  }
  Semantics sem;
  CharSequence source;
  int pos, endpos;
  Phrase current;
  
  void init() {
    pos = 0;
    endpos = source.length();
    current = new Phrase("", "", 0);
    
    sem.rule = this;
    sem.init();
  }
  void close() {
    current.actExec();
    sem.close();
  }
  @Override
  public Phrase lhs() {
    return current;
  }
  @Override
  public Phrase rhs(int i) {
    return current.rhs.get(i);
  }
  @Override
  public int rhsSize() {
    return current.rhs.size();
  }
  @Override
  public CharSequence rhsText(int i, int j) {
    return j <= i ? "" : source.subSequence(rhs(i).start, rhs(j - 1).end);
  }
}
public interface Rule {
  Phrase lhs();
  int rhsSize();
  Phrase rhs(int i);
  CharSequence rhsText(int i, int j);
}
public static class Phrase {
  int start, end;
  String name, diag;
  List<Phrase> rhs = new ArrayList<>(10);
  Object value;
  Phrase parent;
  int hwm = -1;
  List<String> hwmExp = new ArrayList<>();
  List<Runnable> defAct = new ArrayList<>();
  public Phrase(String n, String d, int s) {
    name=n; diag=d; start=end=s;
  }
  public void put(Object o) { value = o; }
  public Object get() { return value; }
  public String rule() { return name; }
  public boolean isEmpty() { return start == end; }
  public boolean isA(String rule) { return name.equals(rule); }
  public boolean isTerm() { return name.isEmpty(); }
  public List<Phrase> rhs() { return rhs; }
  public int start() { return start; }
  public int end() { return end; }
  public List<String> tags() { return hwmExp; }
  public int mark() { return hwm; }
  public void errClear() {
    hwmExp.clear();
    hwm = -2;
  }
  public void errAdd(String expr, int i) {
    hwmSet(expr, start + i);
  }
  public void actClear() {
    defAct.clear();
  }
  public void actAdd(Runnable a) {
    defAct.add(a);
  }
  public void actExec() {
    for (var a : defAct) a.run();
    defAct.clear();
  }
  void hwmSet(String what, int where) {
    hwmExp.clear();
    hwmExp.add(what);
    hwm = where;
  }
  void hwmUpd(String what, int where) {
    if (hwm > where) {
      return;
    }
    if (hwm < where) {
      hwmExp.clear();
      hwm = where;
    }
    hwmExp.add(what);
  }
  void hwmUpdFrom(Phrase p) {
    if (hwm > p.hwm) {
      return;
    }
    if (hwm < p.hwm) {
      hwmExp.clear();
      hwm = p.hwm;
    }
    hwmExp.addAll(p.hwmExp);
  }
}
}
