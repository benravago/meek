package mouse.util;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public interface Template {

  static Map<String,String> from(String src) {
    var map = new LinkedHashMap<String,String>(); // NOTE: order-preserving map
    var key = new StringBuilder();
    var buf = new StringBuilder();
    new BufferedReader(new StringReader(src))
      .lines().forEach(s -> {
        if (s.charAt(0) == '<') { // key item
          map.put(key.toString(), buf.toString());
          buf.setLength(0);
          key.setLength(0); // clear buffers
          key.append(s.substring(1,s.indexOf('>')));
        } else { // data value
          buf.append(s).append('\n');
        }
      });
    map.put(key.toString(), buf.toString()); // put pending item
    map.remove(""); // remove empty items

    return map;
  }

  static Map<String,String> from(Object ref, String name) {
    return from(resource(ref,name));
  }

  static String resource(Object ref, String name) {
    try { return new String(ref.getClass().getResourceAsStream(name).readAllBytes()); }
    catch (Exception e) { throw new NoSuchElementException(name,e); }
  }

}