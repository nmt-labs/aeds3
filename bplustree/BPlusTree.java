package bplustree;

public class BPlusTree {
  private int degree;
  private Node root;
  private int nElements;

  public BPlusTree(int degree) {
    this.degree = degree;
    this.root = new Node(degree);
    this.nElements = 0;
  }

  public void showDegreeElements() {
    System.out.println(this.degree + " - " + this.nElements);
  }

  public void insert(int id, long pointer) {
    // passa a raiz e, por isso, parent e null | pointer -> ponteiro da posicao do id no arquivo
    this.root = insert(this.root, null, id, pointer);
  }

  private Node insert(Node no, Node parent, int id, long pointer) {
    // Se o nó for folha, insere a chave
    if (no.leaf) {
      // Se o nó estiver cheio, divide o nó
      if (no.nKeys + 1 == no.degree) {
        int i = 0;
        while (parent != null && i < parent.nKeys && id > parent.keys[i].id) {
          i++; // Posição do filho
        }
        if (parent == null) {
          parent = divide(no, parent, i, id, pointer);
          this.nElements++;
          return parent;
        } else if (parent.nKeys + 1 == parent.degree) { // esse bloco e identico ao anterior, pq so nao colocou um || ?
          parent = divide(no, parent, i, id, pointer);
          this.nElements++;
          return parent;
        } else { // se o pai existe e nao ta cheio
          divide(no, parent, i, id, pointer);
          this.nElements++;
        }
      } else { // se nao estiver cheio insere normal
        no = insertKey(no, id, pointer);
        this.nElements++;
      }

    } else {
      // Se o nó não for leaf, procura o filho onde a chave deve ser inserida
      int i = 0;
      while (i < no.nKeys && id > no.keys[i].id) {
        i++;
      }
      if (no != null && no.nKeys + 1 == no.degree && no.children[i].leaf
          && no.children[i].nKeys + 1 == no.children[i].degree) { // procura ate achar um >filho< que seja leaf e tenha espaco
            // se for leaf mas nao tem espaco para inserir
        no = insert(no.children[i], no, id, pointer);
      } else {
        no.children[i] = insert(no.children[i], no, id, pointer); // filho continua sendo filho e insere no filho
      }
    }
    return no; // retorna filho ou pai?
  }

  private Node insertKey(Node no, int id, long pointer) {
    int i = no.nKeys - 1;
    while (i >= 0 && no.keys[i] != null && id < no.keys[i].id) {
      no.keys[i + 1] = no.keys[i];
      i--;
    }
    no.keys[i + 1] = new Key(id, pointer);
    no.nKeys++;

    return no;
  }

  /**
   * ele escolheu subir com o item do meio do no a esquerda (menor) ao inves do primeiro a direita (maior). na divisao, a maior parte da
   * divisao fica no no novo
   * -> eu subiria com o priemiro da direita e deixaria a meior parte da divisao no no antigo
   * @param node
   * @param parent
   * @param i
   * @param id
   * @param pointer
   * @return
   */
  private Node divide(Node node, Node parent, int i, int id, long pointer) {
    // Cria um novo nó
    Node newNode = new Node(node.degree);
    // Copia a metade das chaves para o novo nó
    int half = node.nKeys / 2;

    // cria novo no
    for (int j = half; j < node.degree - 1; j++) {
      newNode.keys[j - half] = node.keys[j];
      newNode.nKeys++; // aumenta qnt de elementos da pagina nova
      node.nKeys--; // diminui qnt de elementos da pagina principal
    }
    // Insere a nova chave no nó correto
    if (id < node.keys[half].id) {
      node = insertKey(node, id, pointer);
    } else {
      newNode = insertKey(newNode, id, pointer);
    }

    // Se o nó não for folha, copia a metade dos filhos para o novo nó
    if (!node.leaf) {
      for (int j = half; j < node.degree; j++) {
        newNode.children[j - half] = node.children[j];
      }
    }

    // Se o nó for folha, atualiza o ponteiro para o próximo nó
    if (node.leaf) {
      node.sibling = newNode;
    }
    // Se o nó não tiver pai, cria um novo nó pai
    if (parent == null) {
      parent = new Node(node.degree);
      parent.children[0] = node;
      parent.leaf = false;
    }
    // Insere a chave do meio do nó no pai | ia entender mais se fosse a primeira chave do no novo, mas ok ne
    if (parent.nKeys + 1 == parent.degree) {
      Node tmp = divide(parent.clone(), null, 0, node.keys[half].id, node.keys[half].pointer);

      for (int j = 0; j < tmp.degree; j++) {
        if (tmp.children[j] != null) {
          for (int k = 0; k < tmp.degree; k++) {
            if (tmp.children[j].keys[tmp.children[j].nKeys - 1].id >= parent.children[k].keys[0].id &&
                (j == 0 || tmp.children[j - 1].keys[tmp.children[j - 1].nKeys - 1].id < parent.children[k].keys[0].id)) {
              tmp.children[j].children[k - (3 * j)] = parent.children[k];
              if (k == tmp.degree - 1) {
                tmp.children[j].children[(k + 1) - (3 * j)] = newNode;
              }
            }
          }
        }
      }

      parent = tmp;

      return parent;
    } else {
      parent = insertKey(parent, node.keys[half].id, node.keys[half].pointer); // ao inves de no.keys[half].id poderia ser newNode.keys[0].id?
      // Insere o novo nó no parent
      for (int j = parent.nKeys - 1; j > i; j--) {
        parent.children[j + 1] = parent.children[j];
      }
      parent.children[i + 1] = newNode;

      return parent;
    }
  }

  public long search(int id) {
    return search(this.root, id);
  }

  private long search(Node no, int id) {
    // Se o nó for leaf, retorna o ponteiro da chave
    if (no.leaf) {
      for (int i = 0; i < no.nKeys; i++) {
        if (no.keys[i].id == id) {
          return no.keys[i].pointer;
        }
      }
      return -1;
    } else {
      // Se o nó não for leaf, procura o filho onde a chave deve estar
      int i = 0;
      while (i < no.nKeys && id > no.keys[i].id) {
        i++;
      }
      return search(no.children[i], id);
    }
  }

  public long[] search(int id, int tamanho) {
    return search(this.root, id, tamanho);
  }

  private long[] search(Node no, int id, int tamanho) {
    // Se o nó for leaf, retorna os ponteiros das keys
    if (no.leaf) {
      long[] pointers = new long[tamanho];
      int j = 0;
      // Percorre as keys do nó e dos irmãos
      for (Node n = no; n != null; n = n.sibling) {
        for (int i = 0; i < n.nKeys; i++) {
          if (n.keys[i].id <= id) {
            pointers[j] = n.keys[i].pointer;
            j++;
            if (j == tamanho) {
              return pointers;
            }
          }
        }
      }
      return pointers;
    } else {
      // Se o nó não for leaf, procura o filho onde a chave deve estar
      int i = 0;
      while (i < no.nKeys && id > no.keys[i].id) {
        i++;
      }
      return search(no.children[i], id, tamanho);
    }
  }

  public void print() {
    System.out.println("Imprimindo arvore:");
    print(this.root, 0);
    System.out.println("\n----\n");
  }

  private void print(Node no, int level) {
    if (no != null) {
      System.out.print(level + ": ");
      no.print();
      System.out.println("");
      for (int i = 0; i < no.degree; i++) {
        print(no.children[i], level + 1);
      }
    }
  }
}
