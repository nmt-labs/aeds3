package bplustree;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.ParseException;

public class Key {
  private int id;
  private long pointer;

  public Key() {
    this.id = -1;
    this.pointer = -1;
  }

  public Key(int id, long pointer) {
    this.id = id;
    this.pointer = pointer;
  }

  public int getId() {return id;}
  public long getPointer() {return pointer;}
  public void setId(int id) {this.id = id;}
  public void setPointer(long pointer) {this.pointer = pointer;}

  public byte[] toByteArray() throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    DataOutputStream dos = new DataOutputStream(baos);

    dos.writeInt(this.id);
    dos.writeLong(this.pointer);

    return baos.toByteArray();
  }

  public void fromByteArray(byte[] ba) throws IOException, ParseException {
    ByteArrayInputStream bais = new ByteArrayInputStream(ba);
    DataInputStream dis = new DataInputStream(bais);

    this.id = dis.readInt();
    this.pointer = dis.readLong();
  }

  public String toString(){
    
    return 
    "\nId: " + this.id +
    "\nPointer: " + this.pointer;
  }
}
