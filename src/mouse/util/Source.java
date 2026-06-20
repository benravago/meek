package mouse.util;

import java.util.Arrays;

public class Source {

  public final String text;
  public final int[] index;

  public Source(String s) {
    text = s;
    index = lines(s);
    index[0] = -1;
  }

  public int[] at(int p) {
    var i = Arrays.binarySearch(index,p);
    if (i < 0) i = (-i) - 1;
    return new int[]{ i, (p - index[i-1]) };
  }

  public static int[] lines(String s) {
    var a = new int[256];
    var i = 1; // a[0] = 0
    var p = 0;
    var q = s.length();
    while (p < q) {
      if (i >= a.length) a = Arrays.copyOf(a,i+256);
      p = s.indexOf('\n',p);
      if (p < 0) p = q;
      a[i++] = p++;
    }
    if (i < a.length) a = Arrays.copyOf(a,i);
    return a;
  }

}