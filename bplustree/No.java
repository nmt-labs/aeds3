package bplustree;

public class No {
  public int degree;
  public Key[] keys;
  public No[] children;
  public int nKeys;
  public boolean leaf;
  public No sibling;

  public No(int degree) {
    this.degree = degree;
    this.keys = new Key[degree - 1];
    this.children = new No[degree];
    this.nKeys = 0;
    this.leaf = true;
    this.sibling = null; // ponteiro apenas pra leaf seguinte
  }

  public void print() {
    System.out.print("[");
    for (int i = 0; i < nKeys; i++) {
      System.out.print(keys[i] == null ? "null" : keys[i].id);
      if (i < nKeys - 1) {
        System.out.print(", ");
      }
    }
    System.out.print("]");
  }

  public No clone() {
    No newKnot = new No(degree);

    newKnot.nKeys = nKeys;
    newKnot.leaf = leaf;
    newKnot.sibling = sibling;
    for (int i = 0; i < nKeys; i++) {
      newKnot.keys[i] = keys[i];
    }
    for (int i = 0; i <= nKeys; i++) {
      newKnot.children[i] = children[i];
    }
    return newKnot;
  }
}
