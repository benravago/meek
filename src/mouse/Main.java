package mouse;

import java.util.Map;

import mouse.peg.Evaluate;
import mouse.peg.Generate;

public class Main {
  public static void main(String...args) {
    var ctx = new Context();
    options(ctx, args);
    options(ctx, System.getProperties());
    // ctx.ready();
    assert ctx.grammarPath != null : "no grammar file declared";
    exec(ctx);
  }

  static void exec(Context ctx) {
    var ev = new Evaluate();
    var ok = ev.parse(ctx.sourceText());
    if (!ok) return;
    var p = ev.metadata();
    var pn = ctx.packageName();
    var cn = ctx.parserName();
    var ge = new Generate();
    ok = ge.apply(
      ctx.targetFile(javaFile(pn,cn)),
      pn, cn,
      p.rules(), p.terms(), p.links().lrecs(),
      p.index(), p.links().first()
    );
    if (!ok) return;
    var s = ge.summary();
    //  Summary
    System.out.println("Parsing procedures:");
    System.out.println(s.rules() + " rules; starting with "+s.root());
    System.out.println(s.sub_expressions() + " sub-expressions");
    System.out.println(p.terms().size() + " terminals");
    if (!p.links().lrecs().isEmpty()) {
      System.out.println(s.left_recursions() + " procedures for "
                 + p.links().lrecs().size() + " left-recursion class(es)");
    }
  }

  static String javaFile(String pn, String cn) {
    return pn.replace('.','/')+'/'+cn+".java";
  }

  static void options(Context ctx, String...args) {
    if (args.length > 0) ctx.grammarPath(args[0]);
  }

  static void options(Context ctx, Map<Object,Object> map) {
    map.forEach((k,v) -> {
      var n = k.toString();
      if (n.startsWith("peg.")) { // -Dpeg.{key}={value}
        var s = v.toString();
        switch (n) {
          case "peg.grammar" -> ctx.grammarPath(s);        // path/to/grammar.peg input file
          case "peg.parser"  -> ctx.parserName(s);         // fqcn.Grammar to generate
          case "peg.target"  -> ctx.targetDirectory(s);    // path/to/target directory
        }
      }
    });
    //  peg.grammar -> required
    //     .parser  -> defaults to <grammar.GRAMMAR>
    //     .target  -> defaults to dirname(<peg.grammar>)
  }
}