package cryptography.blocks;

public interface Cryptable {

    public String crypt(String base);

    public String decrypt(String base);

}