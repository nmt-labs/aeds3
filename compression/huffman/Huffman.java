package compression.huffman;
import java.util.*;
import java.io.IOException;
import java.io.RandomAccessFile;

class Leaf extends Tree {
    public final char value; 
 
    public Leaf(int freq, char val) {
        super(freq);
        value = val;
    }
}
abstract class Tree implements Comparable<Tree> {
    public final int frequency;
    //
    public Tree(int freq) { 
    	frequency = freq; 
    }
    
    public int compareTo(Tree tree) {
        return frequency - tree.frequency;
    }
}


public class Huffman {

    /**
     * 
     * @param freq
     * @return
     */
    // Constrói a árvore de Huffman com base nas frequências dos caracteres
    public static Tree buildHuffmanTree(int[] freq) {
        PriorityQueue<Tree> trees = new PriorityQueue<Tree>();

        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0)
                trees.offer(new Leaf(freq[i], (char) i));
        }

        while (trees.size() > 1) {
            Tree a = trees.poll();
            Tree b = trees.poll();

            trees.offer(new Node(a, b));
        }

        return trees.poll();
    }

    // Codifica o arquivo usando a árvore de Huffman
    public static RandomAccessFile compress(Tree tree, RandomAccessFile compress) throws IOException {
        assert tree != null;

        compress.seek(0);
        RandomAccessFile compressedFile = new RandomAccessFile("compressedFile", "rw");

        while (compress.getFilePointer() < compress.length()) {
            char character = (char) compress.readByte();
            compressedFile.writeUTF((codes(tree, new StringBuffer(), character)));
        }

        return compressedFile;
    }

    /**
     * 
     * @param tree
     * @param compressedFile
     * @return
     * @throws IOException
     */
    // Decodifica o arquivo comprimido usando a árvore de Huffman
    public static RandomAccessFile decoding(Tree tree, RandomAccessFile compressedFile) throws IOException {
        assert tree != null;

        RandomAccessFile uncompress = new RandomAccessFile("uncompress", "rw");
        Node node = (Node) tree;
        compressedFile.seek(0);

        while (compressedFile.getFilePointer() < compressedFile.length()) {
            String code = compressedFile.readUTF();

            for (char c : code.toCharArray()) {
                if (c == '0') {
                    if (node.left instanceof Leaf) {
                        uncompress.writeChar(((Leaf) node.left).value);
                        node = (Node) tree;
                    } else {
                        node = (Node) node.left;
                    }
                } else if (c == '1') {
                    if (node.right instanceof Leaf) {
                        uncompress.writeChar(((Leaf) node.right).value);
                        node = (Node) tree;
                    } else {
                        node = (Node) node.right;
                    }
                }
            }
        }

        return uncompress;
    }

    /**
     * 
     * @param tree
     * @param prefix
     * @param w
     * @return
     */
    public static String codes(Tree tree, StringBuffer prefix, char w) {
        assert tree != null;
        
        if (tree instanceof Leaf) {
            Leaf leaf = (Leaf)tree;
            
            // Se o caractere corresponder à folha atual, retorna o código prefixo
            if (leaf.value == w ){
                return prefix.toString();
            }
            
        } else if (tree instanceof Node) {
            Node node = (Node)tree;
 
            // Percorre à esquerda e adiciona '0' ao prefixo
            prefix.append('0');
            String left = codes(node.left, prefix, w);
            prefix.deleteCharAt(prefix.length()-1);
 
            // Percorre à direita e adiciona '1' ao prefixo
            prefix.append('1');
            String right = codes(node.right, prefix,w);
            prefix.deleteCharAt(prefix.length()-1);
            
            // Verifica se o código foi encontrado na subárvore esquerda ou direita
            if (left == null) return right;
            else return left;
        }
        return null;
    }

}
