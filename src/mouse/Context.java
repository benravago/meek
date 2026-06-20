package mouse;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Context {

  Path grammarPath;      // src/Peg.peg
  Path targetDirectory;  // src/

  String grammarName;    // Peg
  String packageName;    // peg
  String parserName;     // PEG

  public void grammarPath(String path) {
    grammarPath = fileExists(path);
    targetDirectory = grammarPath.getParent();
    grammarName(basename(grammarPath));
  }

  public Path grammarPath() { return grammarPath; }

  public void targetDirectory(String path) { targetDirectory = directoryExists(path); }
  public Path targetDirectory() { return targetDirectory != null ? targetDirectory : Paths.get("."); }

  public void grammarName(String name) { if (nonBlank(name)) grammarName = capitalize(name); }
  public String grammarName() { return grammarName != null ? grammarName : "null" ; }

  public void parserName(String name) {
    if (nonBlank(name)) {
      var p = name.lastIndexOf('.');
      if (p < 0) {
        packageName = null;
        parserName = name;
      } else {
        packageName = name.substring(0,p);
        parserName = name.substring(p+1);
      }
    }
  }

  public String packageName() { return packageName != null ? packageName : grammarName.toLowerCase(); }
  public String parserName() { return parserName != null ? parserName : grammarName.toUpperCase(); }

  public String sourceText() { return source(grammarPath); }
  public PrintStream targetFile(String file) { return target(targetDirectory,file); }

  static String capitalize(String name) {
    return Character.toUpperCase(name.charAt(0))+name.substring(1);
  }
  static boolean nonBlank(String s) {
    return s != null && !s.isBlank();
  }

  static Path directory(Path path) {
    return path.getParent();
  }
  static String basename(Path path) {
    var file = path.getFileName().toString();
    var p = file.lastIndexOf('.');
    return p < 0 ? file : file.substring(0,p);
  }

  static String source(Path path) {
    try { return Files.readString(path); }
    catch (IOException e) { throw new UncheckedIOException(e); }
  }

  static PrintStream target(Path dir, String file) {
    try { return new PrintStream(Files.newOutputStream(dir.resolve(file))); }
    catch (IOException e) { throw new UncheckedIOException(e); }
  }

  static Path fileExists(String first, String...more) {
    var path = Paths.get(first,more);
    try { if (Files.size(path) < 1) throw new IllegalArgumentException(path+" is empty"); }
    catch (IOException e) { throw new UncheckedIOException(e); }
    return path;
  }

  static Path directoryExists(String first, String...more) {
    var dir = Paths.get(first,more);
    try { Files.createDirectories(dir); }
    catch (IOException e) { throw new UncheckedIOException(e); }
    return dir;
  }

}