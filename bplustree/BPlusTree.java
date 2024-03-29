package bplustree;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import musica.Musica;

public class BPlusTree {
  private int degree;
  private Node root;
  private int nElements;

  private String fileName = "db" + File.separator + "musicas.db";
  private RandomAccessFile file;

  public BPlusTree(int degree) throws FileNotFoundException {
    this.degree = degree;
    this.root = new Node(degree);
    this.nElements = 0;

    this.file = new RandomAccessFile(fileName, "rw");
  }

  public void insert() throws Exception {
    Key key = new Key();
    file.readInt();
    while (!isAvaliable()) {
      key = readKey(file);
      insert(key.getId(), key.getPointer());
    }
  }

  private Key readKey(RandomAccessFile file) throws Exception {
    Musica reg = null;
    char lapide = file.readChar();
    int sizeReg = file.readInt();
    byte[] bytearray = new byte[sizeReg];
    file.read(bytearray);
    Key key = new Key();
    if (lapide != '*') {
      long pointer = file.getFilePointer();
      reg = new Musica();
      reg.fromByteArray(bytearray);
      int id = reg.getId();
      key = new Key(id, pointer);
    }
    return key;
  }

  private boolean isAvaliable() throws Exception {
    return file.getFilePointer() == file.length();
  }

  public void insert(int id, long pointer) throws IOException {
    // passa a raiz e, por isso, parent e null | pointer -> ponteiro da posicao do id no arquivo
    this.root = insert(this.root, null, id, pointer);
  }

  private Node insert(Node no, Node parent, int id, long pointer) throws IOException {
    // Se o nó for folha, insere a chave
    if (no.leaf) {
      // Se o nó estiver cheio, divide o nó
      if (no.nKeys + 1 == no.degree) {
        int i = 0;
        while (parent != null && i < parent.nKeys && id > parent.keys[i].getId()) {
          i++; // Posição do filho
        }
        if (parent == null) {
          parent = split(no, parent, i, id, pointer);
          this.nElements++;
          return parent;
        } else if (parent.nKeys + 1 == parent.degree) { // esse bloco e identico ao anterior, pq so nao colocou um || ?
          parent = split(no, parent, i, id, pointer);
          this.nElements++;
          return parent;
        } else { // se o pai existe e nao ta cheio
          split(no, parent, i, id, pointer);
          this.nElements++;
        }
      } else { // se nao estiver cheio insere normal
        no = insertKey(no, id, pointer);
        this.nElements++;
      }

    } else {
      // Se o nó não for leaf, procura o filho onde a chave deve ser inserida
      int i = 0;
      while (i < no.nKeys && id > no.keys[i].getId()) {
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
    return no; 
  }

  private Node insertKey(Node no, int id, long pointer) throws IOException {
    int i = no.nKeys - 1;
    while (i >= 0 && no.keys[i] != null && id < no.keys[i].getId()) {
      no.keys[i + 1] = no.keys[i];
      i--;
    }
    no.keys[i + 1] = new Key(id, pointer);
    no.nKeys++;

    // escrever no arquivo
    File musicaSort = new File("db" + File.separator + "musicaBPlusTree.db");
    RandomAccessFile treeFile = new RandomAccessFile(musicaSort, "rw");
    // verifica se arquivo existe
    if (musicaSort.exists()) {
      // exclui se ja existir
      treeFile.setLength(0);
    }

    writeFile(treeFile, root);

    return no;
  }

  private void writeFile(RandomAccessFile file, Node no) throws IOException{
    // procura a primera folha
    if(no != null) {
      if(no.leaf) {
        // escreve as chaves
        for(int i = 0; i < no.nKeys; i++) {
          file.write(no.keys[i].toByteArray());
        }
        // passa pra proxima folha
        writeFile(file, no.sibling);
      }
      else {
        // procura a folha
        writeFile(file, no.children[0]);
      }
    }
  }

  /**
   * @param node
   * @param parent
   * @param i
   * @param id
   * @param pointer
   * @return
   * @throws IOException
   */
  private Node split(Node node, Node parent, int i, int id, long pointer) throws IOException {
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
    if (id < node.keys[half].getId()) {
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
      Node tmp = split(parent.clone(), null, 0, node.keys[half].getId(), node.keys[half].getPointer());

      for (int j = 0; j < tmp.degree; j++) {
        if (tmp.children[j] != null) {
          for (int k = 0; k < tmp.degree; k++) {
            if (tmp.children[j].keys[tmp.children[j].nKeys - 1].getId() >= parent.children[k].keys[0].getId() &&
                (j == 0 || tmp.children[j - 1].keys[tmp.children[j - 1].nKeys - 1].getId() < parent.children[k].keys[0].getId())) {
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
      parent = insertKey(parent, node.keys[half].getId(), node.keys[half].getPointer()); // ao inves de no.keys[half].id poderia ser newNode.keys[0].id?
      // Insere o novo nó no parent
      for (int j = parent.nKeys - 1; j > i; j--) {
        parent.children[j + 1] = parent.children[j];
      }
      parent.children[i + 1] = newNode;

      return parent;
    }
  }

  public long search(int id) throws IOException {
    File musicaSort = new File("db" + File.separator + "musicaBPlusTree.db");
    RandomAccessFile treeFile = new RandomAccessFile(musicaSort, "rw");
    int idFile;
    long pos;

    treeFile.seek(0);
    while (treeFile.getFilePointer() < treeFile.length()) {
      idFile = treeFile.readInt();
      if (idFile == id) {
        pos = treeFile.readLong();
        treeFile.close();
        return pos;
      }
      treeFile.readLong();
    }

    treeFile.close();
    return -1;
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

  public void showDegreeElements() {
    System.out.println(this.degree + " - " + this.nElements);
  }
}
