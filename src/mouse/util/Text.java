package mouse.util;

public interface Text {

  static CharSequence str(char c) {
    return switch(c) {
      case '\n' -> "\\n";   // 0x0A
      case '\t' -> "\\t";   // 0x09
      case '\r' -> "\\r";   // 0x0D
      case '\b' -> "\\b";   // 0x08
      case '\f' -> "\\f";   // 0x0C
      case '\'' -> "\\'";   // 0x27
      case '\\' -> "\\\\";  // 0x5C
      default -> {
        yield c < 0x7F
          ? Character.toString(c)
          : "\\u%04x".formatted((int)c);
      }
    };
  }

  static CharSequence str(String s) {
    var sb = new StringBuilder();
    for (var i = 0; i < s.length(); i++) {
      var c = s.charAt(i);
      if (' ' <= c && c <= '~') {
        if (c == '\\' || c == '"') sb.append('\\');
        sb.append(c);
      } else {
        var x = switch (c) {
          case '\n' -> "\\n";
          case '\t' -> "\\t";
          case '\r' -> "\\r";
          case '\b' -> "\\b";
          case '\f' -> "\\f";
          default -> "\\u%04x".formatted((int)c);
        };
        sb.append(x);
      }
    }
    return sb;
  }

  static void note(String format, Object...args) {
    System.out.format(format,args);
  }

  static void error(String format, Object...args) {
    System.err.format(format,args);
  }

}