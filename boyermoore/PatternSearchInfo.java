package boyermoore;

/**
 * utilitaries functions to count all pattherns foind and number of comparassions
 */
public class PatternSearchInfo {
  private int foundCount;
  private int numComp;
  
  public PatternSearchInfo(){}

  public int getFoundCount() {
    return foundCount;
  }

  public void setFoundCount(int foundCount) {
    if (foundCount > -1)
      this.foundCount = foundCount;
  }

  public int getNumComp() {
    return numComp;
  }

  public void setNumComp(int numComp) {
    if (numComp > -1)
      this.numComp = numComp;
  }


  public PatternSearchInfo(int foundCount, int numComp) {
    setFoundCount(foundCount);
    setNumComp(numComp);
  }

  public void sum(PatternSearchInfo aux) {
    this.foundCount += aux.foundCount;
    this.numComp += aux.numComp;
  }

  public void addNumComp(int i) {
    this.numComp += i;
  }

  public void addFoundCount(int i) {
    this.foundCount += i;
  }

}