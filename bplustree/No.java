package bplustree;

public class No {
  public int degree;
  public Key[] chaves;
  public No[] filhos;
  public int nChaves;
  public boolean folha;
  public No irmao;

  public No(int degree) {
    this.degree = degree;
    this.chaves = new Key[degree - 1];
    this.filhos = new No[degree];
    this.nChaves = 0;
    this.folha = true;
    this.irmao = null; // ponteiro apenas pra folha seguinte
  }

  public void print() {
    System.out.print("[");
    for (int i = 0; i < nChaves; i++) {
      System.out.print(chaves[i] == null ? "null" : chaves[i].id);
      if (i < nChaves - 1) {
        System.out.print(", ");
      }
    }
    System.out.print("]");
  }

  public No clone() {
    No novo = new No(degree);

    novo.nChaves = nChaves;
    novo.folha = folha;
    novo.irmao = irmao;
    for (int i = 0; i < nChaves; i++) {
      novo.chaves[i] = chaves[i];
    }
    for (int i = 0; i <= nChaves; i++) {
      novo.filhos[i] = filhos[i];
    }
    return novo;
  }
}
