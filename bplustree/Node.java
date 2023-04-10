package bplustree;

public class Node {
  public int degree;
  public Key[] keys;
  public Node[] children;
  public int nKeys;
  public boolean leaf;
  public Node sibling;

  public Node(int degree) {
    this.degree = degree;
    this.keys = new Key[degree - 1];
    this.children = new Node[degree];
    this.nKeys = 0;
    this.leaf = true;
    this.sibling = null; // ponteiro apenas pra leaf seguinte
  }

  public void print() {
    System.out.print("[");
    for (int i = 0; i < nKeys; i++) {
      System.out.print(keys[i] == null ? "null" : keys[i].getId());
      if (i < nKeys - 1) {
        System.out.print(", ");
      }
    }
    System.out.print("]");
  }

  public Node clone() {
    Node newNode = new Node(degree);

    newNode.nKeys = nKeys;
    newNode.leaf = leaf;
    newNode.sibling = sibling;
    for (int i = 0; i < nKeys; i++) {
      newNode.keys[i] = keys[i];
    }
    for (int i = 0; i <= nKeys; i++) {
      newNode.children[i] = children[i];
    }
    return newNode;
  }
}
