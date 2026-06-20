package mouse.util;

import java.util.BitSet;

/**
 *  A square matrix with boolean elements.
 *
 *  A matrix of size <code>n</code> has <code>n</code> rows and <code>n</code> columns.
 *  The rows and columns are numbered from <code>0</code> through <code>n-1</code>.
 *  The element in row <code>i</code> and column <code>j</code> is referred to as the <code>(i,j)</code>-th element.
 *
 *  For convenience, the values of elements are in this documentation denoted by <code>0</code> (meaning <code>false</code>) and <code>1</code> (meaning <code>true</code>).
 */
public class BitMatrix {

  //  BitMatrix is implemented as an array 'm' of BitSets, each BitSet representing one row.
  private int n; // Size
  private BitSet m[]; // The matrix

  //  Construct incomplete n by n matrix.
  private BitMatrix(int n) {
    this.n = n;
    m = new BitSet[n];
  }

  /**
   *  Constructs empty matrix.
   *
   *  @param  n Size of the matrix.
   *  @return An <code>n</code> by <code>n</code> matrix with all elements <code>0</code>.
   */
  public static BitMatrix empty(int n) {
    var R = new BitMatrix(n);
    for (var i = 0; i < n; i++) {
      R.m[i] = new BitSet();
    }
    return R;
  }

  /**
   *  Constructs unit matrix.
   *
   *  @param  n Size of the matrix.
   *  @return An <code>n</code> by <code>n</code> matrix with all diagonal elements <code>1</code> and remaining elements <code>0</code>.
   */
  public static BitMatrix unit(int n) {
    var R = empty(n);
    for (var i = 0; i < n; i++) {
      R.m[i].set(i);
    }
    return R;
  }

  /**
   *  Obtains size of this matrix.
   *
   *  @return Number of rows / columns.
   */
  public int size() {
    return n;
  }

  /**
   *  Obtains number of ones in this matrix.
   *
   *  @return Number of ones.
   */
  public int weight() {
    var w = 0;
    for (var i = 0; i < n; i++) {
      w += m[i].cardinality();
    }
    return w;
  }

  /**
   *  Obtains the value of <code>(i,j)</code>-th element.
   *
   *  @param  i Row number.
   *  @param  j Column number.
   *  @return Value of the <code>(i,j)</code>-th element.
   */
  public boolean at(int i, int j) {
    if (j < 0 || j >= n) {
      throw new IndexOutOfBoundsException(j);
    }
    return m[i].get(j);
  }

  /**
   *  Sets the <code>(i,j)</code>-th element to <code>b</code>.
   *
   *  @param  i Row number.
   *  @param  j Column number.
   *  @param  b The value to be set.
   */
  public void set(int i, int j, boolean b) {
    if (j < 0 || j >= n) {
      throw new IndexOutOfBoundsException(j);
    }
    m[i].set(j, b);
  }

  /**
   *  Sets the <code>(i,j)</code>-th element to <code>1</code>.
   *
   *  @param  i Row number.
   *  @param  j Column number.
   */
  public void set(int i, int j) {
    if (j < 0 || j >= n) {
      throw new IndexOutOfBoundsException(j);
    }
    m[i].set(j);
  }

  /**
   *  Obtains the contents of row <code>r</code> as a BitSet.
   *
   *  @param  r Row number.
   *  @return The contents of row <code>r</code> as a BitSet.
   */
  public BitSet row(int r) {
    return (BitSet) (m[r].clone());
  }

  /**
   *  Obtains the contents of column <code>c</code> as a BitSet.
   *
   *  @param  c Column number.
   *  @return The contents of column <code>c</code> as a BitSet.
   */
  public BitSet column(int c) {
    var col = new BitSet(n);
    for (var i = 0; i < n; i++) {
      col.set(i, m[i].get(c));
    }
    return col;
  }

  /**
   *  Constructs a copy of this matrix.
   *
   *  @return New matrix, identical to this matrix.
   */
  public BitMatrix copy() {
    var R = new BitMatrix(n);
    for (var i = 0; i < n; i++) {
      R.m[i] = (BitSet) (m[i].clone());
    }
    return R;
  }

  /**
   *  Constructs transpose of this matrix.
   *
   *  @return New matrix that is the transpose of this matrix.
   */
  public BitMatrix transpose() {
    var R = empty(n);
    for (var i = 0; i < n; i++) {
      for (var j = 0; j < n; j++) {
        if (at(i, j)) {
          R.set(j, i);
        }
      }
    }
    return R;
  }

  /**
   *  Computes transitive closure of this matrix.
   *  The matrix is considered to represent a relation <code>R</code> within a set of <code>n</code> objects, where <code>n</code> is the size of the matrix.
   *  The resulting matrix represents the transitive closure of <code>R</code>.
   *
   *  Computed using the Floyd-Warshall algorithm as presented in https://en.wikipedia.org/wiki/Floyd-Warshall_algorithm.
   *
   *  @return New matrix that is the transitive closure of this matrix.
   */
  public BitMatrix closure() {
    var M = copy();
    for (var k = 0; k < n; k++) {
      for (var i = 0; i < n; i++) {
        if (M.at(i, k)) {
          M.m[i].or(M.m[k]);
        }
      }
    }
    return M;
  }

  /**
   *  Computes transitive and reflexive closure of this matrix.
   *  (Such closure of M is often denoted by M*.)
   *
   *  @return New matrix that is the transitive and reflexive closure of this matrix.
   */
  public BitMatrix star() {
    return closure().or(unit(n));
  }

  /**
   *  Modifies a specified matrix by performing the element-by-element 'or' with this matrix.
   *
   *  @param  M A bit matrix of the same size as this.
   */
  public void orInto(BitMatrix M) {
    if (M.n != n) {
      throw new IllegalArgumentException("size mismatch " + M.n + "!=" + n);
    }
    for (var i = 0; i < n; i++) {
      M.m[i].or(m[i]);
    }
  }

  /**
   *  Modifies a specified matrix by performing the element-by-element 'and' with this matrix.
   *
   *  @param  M A bit matrix of the same size as this.
   */
  public void andInto(BitMatrix M) {
    if (M.n != n) {
      throw new IllegalArgumentException("size mismatch " + M.n + "!=" + n);
    }
    for (var i = 0; i < n; i++) {
      M.m[i].and(m[i]);
    }
  }

  /**
   *  Computes element-by-element 'or' of this matrix and the specified matrix.
   *
   *  @param  M A bit matrix of the same size as this.
   *  @return New matrix that is the element-by-element 'or' of this matrix and <code>M</code>.
   */
  public BitMatrix or(BitMatrix M) {
    if (M.n != n) {
      throw new IllegalArgumentException("size mismatch " + M.n + "!=" + n);
    }
    var R = copy();
    M.orInto(R);
    return R;
  }

  /**
   *  Computes element-by-element 'and' of this matrix and the specified matrix.
   *
   *  @param  M A bit matrix of the same size as this.
   *  @return New matrix that is the element-by-element 'and' of this matrix and <code>M</code>.
   */
  public BitMatrix and(BitMatrix M) {
    if (M.n != n) {
      throw new IllegalArgumentException("size mismatch " + M.n + "!=" + n);
    }
    var R = copy();
    M.andInto(R);
    return R;
  }

  /**
   *  Computes element-by-element 'not' of this matrix.
   *
   *  @return New matrix that is the element-by-element 'not' of this matrix.
   */
  public BitMatrix not() {
    var R = copy();
    for (var i = 0; i < n; i++) {
      R.m[i].flip(0, n);
    }
    return R;
  }

  /**
   *  Computes product of this matrix and the specified matrix.
   *  The product is defined as for numeric matrices, with logical 'or' instead of addition and logical 'and' instead of multiplication.
   *
   *  @param  M A bit matrix of the same size as this.
   *  @return New matrix that is the product of this matrix and <code>M</code>.
   */
  public BitMatrix times(BitMatrix M) {
    if (M.n != n) {
      throw new IllegalArgumentException("size mismatch " + M.n + "!=" + n);
    }
    var R = empty(n);
    var T = M.transpose();
    for (var i = 0; i < n; i++) {
      for (var j = 0; j < n; j++) {
        if (m[i].intersects(T.m[j])) {
          R.set(i, j);
        }
      }
    }
    return R;
  }

  /**
   *  Computes product of this matrix and the specified vector.
   *  The product is defined as for numeric matrices, with logical 'or'
   *  instead of addition and logical 'and' instead of multiplication.
   *
   *  @param  V A bit vector of the same size as this.
   *  @return New matrix that is the product of this matrix and <code>V</code>.
   */
  public BitSet times(BitSet V) {
    var R = new BitSet(n);
    for (var i = 0; i < n; i++) {
      if (m[i].intersects(V)) {
        R.set(i);
      }
    }
    return R;
  }

  /**
   *  Computes n by n matrix as the Cartesian product of two vectors.
   *
   *  @param  V1 A bit vector.
   *  @param  V2 A bit vector.
   *  @param  n  Dimension of the result.
   *  @return New matrix that is the product of <code>V1</code> and <code>V2</code>.
   */
  public static BitMatrix product(BitSet V1, BitSet V2, int n) {
    var M = new BitMatrix(n);
    for (var i = 0; i < n; i++) {
      M.m[i] = V1.get(i) ? (BitSet)(V2.clone()) : new BitSet(n);
    }
    return M;
  }

  /**
   *  Replaces a square area of this matrix by the contents of another matrix.
   *
   *  @param  M The matrix to be inserted.
   *  @param  i starting row of the area to be replaced.
   *  @param  j starting column of the area to be replaced.
   *  @return This matrix with modified contents.
   */
  public BitMatrix insert(BitMatrix M, int i, int j) {
    if (i + M.n > n || j + M.n > n) {
      throw new IllegalArgumentException("Insertion overflow");
    }
    for (var r = 0; r < M.n; r++) {
      var src = M.m[r];
      var trg = m[i + r];
      for (var c = 0; c < M.n; c++) {
        trg.set(c + j, src.get(c));
      }
    }
    return this;
  }

  /**
   *  Returns a square matrix cut out from this matrix.
   *
   *  @param  s Size of the resulting matrix.
   *  @param  i starting row of the area to be cut.
   *  @param  j starting column of the area to be cut.
   *  @return New <code>n</code> by <code>n</code> matrix.
   */
  public BitMatrix cut(int s, int i, int j) {
    if (s <= 0 || s > n) {
      throw new IllegalArgumentException("s="+s);
    }
    if (i + s > n || j + s > n) {
      throw new IllegalArgumentException("Cut overflow");
    }
    var M = empty(s);
    for (var r = 0; r < s; r++) {
      var src = m[i + r];
      var trg = M.m[r];
      for (var c = 0; c < s; c++) {
        trg.set(c, src.get(c + j));
      }
    }
    return M;
  }

}