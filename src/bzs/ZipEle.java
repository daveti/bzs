/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzs;

/**
 *
 * Zip Element class
 * Used to save the duplicated index searching during compression
 * @author daveti
 */
public class ZipEle {
    
    public int diffNum;
    public String diffString;
    public int diffIndex1;
    public int diffIndex2;
    
    public ZipEle(int num, String s, int index1, int index2) {
        this.diffNum = num;
        this.diffString = s;
        this.diffIndex1 = index1;
        this.diffIndex2 = index2;
    }
    
}
