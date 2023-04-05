package bplustree;

public class BPlusTree {
  private int degree;
  private No root;
  private int nElements;

  public BPlusTree(int degree) {
    this.degree = degree;
    this.root = new No(degree);
    this.nElements = 0;
  }

  public void showDegreeElements() {
    System.out.println(this.degree + " - " + this.nElements);
  }

  public void insert(int id, long pointer) {
    // passa a raiz e, por isso, parent e null | pointer -> ponteiro da posicao do id no arquivo
    this.root = insert(this.root, null, id, pointer);
  }

  private No insert(No no, No parent, int id, long pointer) {
    // Se o nó for folha, insere a chave
    if (no.folha) {
      // Se o nó estiver cheio, divide o nó
      if (no.nChaves + 1 == no.degree) {
        int i = 0;
        while (parent != null && i < parent.nChaves && id > parent.chaves[i].id) {
          i++; // Posição do filho
        }
        if (parent == null) {
          parent = dividir(no, parent, i, id, pointer);
          this.nElements++;
          return parent;
        } else if (parent.nChaves + 1 == parent.degree) {
          parent = dividir(no, parent, i, id, pointer);
          this.nElements++;
          return parent;
        } else {
          dividir(no, parent, i, id, pointer);
          this.nElements++;

        }
      } else { // se nao estiver cheio insere normal
        no = insertChave(no, id, pointer); // aqui e quando finalmete e inserido
        this.nElements++;
      }

    } else {
      // Se o nó não for folha, procura o filho onde a chave deve ser inserida
      int i = 0;
      while (i < no.nChaves && id > no.chaves[i].id) {
        i++;
      }
      if (no != null && no.nChaves + 1 == no.degree && no.filhos[i].folha
          && no.filhos[i].nChaves + 1 == no.filhos[i].degree) { // procura ate achar um >filho< que seja folha e tenha espaco
            // se for folha mas nao tem espaco para inserir
        no = insert(no.filhos[i], no, id, pointer);
      } else {
        no.filhos[i] = insert(no.filhos[i], no, id, pointer); // filho continua sendo filho e insere no filho
      }
    }
    return no; // retorna filho ou pai?
  }

  private No insertChave(No no, int id, long pointer) {
    int i = no.nChaves - 1;
    while (i >= 0 && no.chaves[i] != null && id < no.chaves[i].id) {
      no.chaves[i + 1] = no.chaves[i];
      i--;
    }
    no.chaves[i + 1] = new Key(id, pointer);
    no.nChaves++;

    return no;
  }

  private No dividir(No no, No parent, int i, int id, long pointer) {
    // Cria um novo nó
    No novo = new No(no.degree);
    // Copia a metade das chaves para o novo nó
    int meio = no.nChaves / 2;

    // cria novo no
    for (int j = meio; j < no.degree - 1; j++) {
      novo.chaves[j - meio] = no.chaves[j];
      novo.nChaves++; // aumenta qnt de elementos da pagina nova
      no.nChaves--; // diminui qnt de elementos da pagina principal
    }
    // Insere a nova chave no nó correto
    if (id < no.chaves[meio].id) {
      no = insertChave(no, id, pointer);
    } else {
      novo = insertChave(novo, id, pointer);
    }

    // Se o nó não for folha, copia a metade dos filhos para o novo nó
    if (!no.folha) {
      for (int j = meio; j < no.degree; j++) {
        novo.filhos[j - meio] = no.filhos[j];
      }
    }

    // Se o nó for folha, atualiza o ponteiro para o próximo nó
    if (no.folha) {
      no.irmao = novo;
    }
    // Se o nó não tiver parent, cria um novo nó parent
    if (parent == null) {
      parent = new No(no.degree);
      parent.filhos[0] = no;
      parent.folha = false;
    }
    // Insere a chave do meio do nó no parent | ia entender mais se fosse a primeira chave do no novo, mas ok ne
    if (parent.nChaves + 1 == parent.degree) {
      No tmp = dividir(parent.clone(), null, 0, no.chaves[meio].id, no.chaves[meio].pointer);

      for (int j = 0; j < tmp.degree; j++) {
        if (tmp.filhos[j] != null) {
          for (int k = 0; k < tmp.degree; k++) {
            if (tmp.filhos[j].chaves[tmp.filhos[j].nChaves - 1].id >= parent.filhos[k].chaves[0].id &&
                (j == 0 || tmp.filhos[j - 1].chaves[tmp.filhos[j - 1].nChaves - 1].id < parent.filhos[k].chaves[0].id)) {
              tmp.filhos[j].filhos[k - (3 * j)] = parent.filhos[k];
              if (k == tmp.degree - 1) {
                tmp.filhos[j].filhos[(k + 1) - (3 * j)] = novo;
              }
            }
          }
        }
      }

      parent = tmp;

      return parent;
    } else {
      parent = insertChave(parent, no.chaves[meio].id, no.chaves[meio].pointer); // ao inves de no.chaves[meio].id poderia ser novo.chaves[0].id?
      // Insere o novo nó no parent | nao entendi
      for (int j = parent.nChaves - 1; j > i; j--) {
        parent.filhos[j + 1] = parent.filhos[j];
      }
      parent.filhos[i + 1] = novo;

      return parent;
    }
  }

  public long buscar(int id) {
    return buscar(this.root, id);
  }

  private long buscar(No no, int id) {
    // Se o nó for folha, retorna o ponteiro da chave
    if (no.folha) {
      for (int i = 0; i < no.nChaves; i++) {
        if (no.chaves[i].id == id) {
          return no.chaves[i].pointer;
        }
      }
      return -1;
    } else {
      // Se o nó não for folha, procura o filho onde a chave deve estar
      int i = 0;
      while (i < no.nChaves && id > no.chaves[i].id) {
        i++;
      }
      return buscar(no.filhos[i], id);
    }
  }

  public long[] buscar(int id, int tamanho) {
    return buscar(this.root, id, tamanho);
  }

  private long[] buscar(No no, int id, int tamanho) {
    // Se o nó for folha, retorna os ponteiros das chaves
    if (no.folha) {
      long[] ponteiros = new long[tamanho];
      int j = 0;
      // Percorre as chaves do nó e dos irmãos
      for (No n = no; n != null; n = n.irmao) {
        for (int i = 0; i < n.nChaves; i++) {
          if (n.chaves[i].id <= id) {
            ponteiros[j] = n.chaves[i].pointer;
            j++;
            if (j == tamanho) {
              return ponteiros;
            }
          }
        }
      }
      return ponteiros;
    } else {
      // Se o nó não for folha, procura o filho onde a chave deve estar
      int i = 0;
      while (i < no.nChaves && id > no.chaves[i].id) {
        i++;
      }
      return buscar(no.filhos[i], id, tamanho);
    }
  }

  public void imprimir() {
    System.out.println("Imprimindo arvore:");
    imprimir(this.root, 0);
    System.out.println("\n----\n");
  }

  private void imprimir(No no, int nivel) {
    if (no != null) {
      System.out.print(nivel + ": ");
      no.print();
      System.out.println("");
      for (int i = 0; i < no.degree; i++) {
        imprimir(no.filhos[i], nivel + 1);
      }
    }
  }
}
