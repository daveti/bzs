/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * Zip/Unzip Implementations
 *
 * @author daveti
 */
public class Zip {

    private static final boolean debug = false;
    private static final int dictSize = 8;
    private static final int lineSize = 32;
    private static final String dictSeparator = "xxxx";

    private static Map<String, String> dictionary; //Shared between zip and unzip
    private static String zipRef;  //Used by zip debug
    private static ArrayList<String> zipDict;   //Used by zip to output the dict
    private static String zipOutput;

    private static void dumpMapString(Map<String, String> map) {
        Map.Entry pair;
        Iterator it = map.entrySet().iterator();

        System.out.println("==============");
        while (it.hasNext()) {
            pair = (Map.Entry) it.next();
            System.out.println(pair.getKey() + " -> " + pair.getValue());
        }
        System.out.println("==============");
    }

    private static void dumpArrayListString(ArrayList<String> input) {
        System.out.println("--------------");
        for (String s : input) {
            System.out.println(s);
        }
        System.out.println("--------------");
    }

    private static void formatZipOutput(String s) {
        // This is a little bit tricky!
        int padLen = lineSize - s.length() % lineSize;
        int i;
        String padString = "";
        String newString;

        if (debug) {
            System.out.println("Debug: sLen=[" + s.length() + "], padLen=[" + padLen + "]");
        }
        // Construct the padding suffix
        for (i = 0; i < padLen; i++) {
            padString += "1";
        }

        newString = s + padString;
        if (debug) {
            System.out.println("Debug: totalLen=[" + newString.length() + "]");
        }
        for (i = 0; i < newString.length(); i += lineSize) {
            if (debug) {
                System.out.println("Debug: start=[" + i + "], end=[" + (i + lineSize) + "]");
            }
            System.out.println(newString.substring(i, i + lineSize));
        }
    }

    private static String buildLocIndex(int i) {
        String s;
        if (debug) {
            System.out.println("Debug: index [" + i + "]");
        }

        s = Integer.toBinaryString(i);
        switch (s.length()) {
            case 1:
                s = "0000" + s;
                break;
            case 2:
                s = "000" + s;
                break;
            case 3:
                s = "00" + s;
                break;
            case 4:
                s = "0" + s;
                break;
            case 5:
                break;
            default:
                System.out.println("Error: unsupported index value [" + s + "]");
                s = "";
                break;
        }

        if (debug) {
            System.out.println("Debug: index [" + s + "]");
        }

        return s;
    }

    private static String buildDictIndex(int i) {
        String s;
        if (debug) {
            System.out.println("Debug: index [" + i + "]");
        }

        s = Integer.toBinaryString(i);
        switch (s.length()) {
            case 1:
                s = "00" + s;
                break;
            case 2:
                s = "0" + s;
                break;
            case 3:
                break;
            default:
                System.out.println("Error: unsupported index value [" + s + "]");
                s = "";
                break;
        }

        if (debug) {
            System.out.println("Debug: index [" + s + "]");
        }

        return s;
    }

    private static String findTheMostFreqOne(LinkedHashMap<String, Integer> map) {
        // NOTE: the map passed in should be a linkedHashMap
        // which guarantees the insertion order during the map construction.
        int vMax = 0;
        int v;
        String s = "";
        Set set = map.entrySet();
        Iterator it = set.iterator();
        Map.Entry ele;

        // Go thru the linkedHashMap
        while (it.hasNext()) {
            ele = (Map.Entry) it.next();
            v = (Integer) ele.getValue();
            // Ignore the "==" case to keep the insertion order
            if (v > vMax) {
                vMax = v;
                s = (String) ele.getKey();
            }
        }

        return s;
    }

    private static ZipEle findMinDiffNum(String s) {
        ZipEle ze;
        int diffNum = 0;
        int[] diffIndex = new int[3];
        String diffString = "";
        char[] target = s.toCharArray();
        char[] cmp;
        int i;
        int j;
        int k;
        boolean notWork = false;

        // Try the 2-bit consecutive mismatches at first
        for (i = 0; i < dictSize; i++) {
            k = 0;
            diffNum = 0;
            diffString = zipDict.get(i);
            notWork = false;
            cmp = diffString.toCharArray();

            for (j = 0; j < lineSize; j++) {
                // Find the mismatch
                if (target[j] != cmp[j]) {
                    // Defensive checking
                    if (j == lineSize - 1) {
                        // There is no way we can use this encoding schema
                        notWork = true;
                        break;
                    }
                    // Check the next char
                    if (target[j + 1] == cmp[j + 1]) {
                        // There is no way we can use this encoding schema
                        notWork = true;
                        break;
                    }
                    // Found a 2-bit consecutive mismatches
                    diffNum++;
                    diffIndex[k] = j;
                    k++;
                    j++; // Bypass the next char
                    // Check for early return
                    if (diffNum == 2) {
                        notWork = true;
                        break;
                    }
                }
            }

            if (!notWork) {
                // Defensive checking
                if (diffNum != 1) {
                    System.out.println("Error: 2-bit consecutive mismatches goes wrong");
                }
                // Found one working
                break;
            }
        }

        if (notWork) {
            // Try the 2-bit mismatches anyware
            for (i = 0; i < dictSize; i++) {
                k = 0;
                diffNum = 0;
                diffString = zipDict.get(i);
                notWork = false;
                cmp = diffString.toCharArray();

                for (j = 0; j < lineSize; j++) {
                    // Find the mismatch
                    if (target[j] != cmp[j]) {
                        // Found a mismatch
                        diffNum++;
                        diffIndex[k] = j;
                        k++;
                        // Check for early return
                        if (diffNum == 3) {
                            notWork = true;
                            break;
                        }
                    }
                }

                if (!notWork) {
                    // Defensive checking
                    if (diffNum != 2) {
                        System.out.println("Error: 2-bit mismatches goes wrong");
                    }
                    // Found one working
                    break;
                }
            }
        }

        switch (diffNum) {
            case 1:
                ze = new ZipEle(1, diffString, diffIndex[0], -1);
                break;
            case 2:
                ze = new ZipEle(2, diffString, diffIndex[0], diffIndex[1]);
                break;
            default:
                ze = new ZipEle(-1, "", -1, -1);
                break;
        }

        return ze;
    }

    private static String reverseBinString(String bin) {
        switch (bin) {
            case "0":
                return "1";
            case "1":
                return "0";
            default:
                System.out.println("Error: unsupported bin [" + bin + "]");
                return "";
        }
    }

    private static String unzipImpl(String loc1, String loc2, String index) {
        String rtn = "";
        int i;
        int[] l = new int[2];

        // Get the text from the dictionary
        String txt = dictionary.get(index);
        String txt2 = txt;

        // Get the locations
        l[0] = Integer.parseInt(loc1, 2);
        if ("".equals(loc2)) {
            l[1] = l[0] + 1;
        } else {
            l[1] = Integer.parseInt(loc2, 2);
        }
        
        // Defensive checking
        if (l[0] >= l[1]) {
            System.out.println("Error: the mismatch is not ordered");
        }
        if (debug) {
            System.out.println("Debug: loc1=[" + l[0] + "], loc2=[" + l[1] + "]");
        }
        
        // Recover the mismatches
        for (i = 0; i < 2; i++) {
            rtn = txt2.substring(0, l[i]) + reverseBinString(txt2.substring(l[i], l[i]+1)) +
                    txt2.substring(l[i]+1);
            txt2 = rtn;
        }
        
        if (debug) {
            System.out.println("Debug: loc1=[" + loc1 + "], loc2=[" + loc2 + "], index=[" +
                    index + "]" + "\nref=[" + txt + "]\n" + "unz=[" + rtn + "]");
        }

        return rtn;
    }

    private static void buildDict4Zip(ArrayList<String> input) {
        int i;
        int v;
        String key;

        // Init the map
        dictionary = new HashMap<>();
        zipDict = new ArrayList<>();
        LinkedHashMap<String, Integer> freq = new LinkedHashMap<>(); // LinkedHashMap keeps the order of the insertion!

        // Check the frequency for each input
        for (String s : input) {
            if (freq.containsKey(s)) {
                v = freq.get(s);
                freq.put(s, v + 1);
                if (debug) {
                    System.out.println("Debug: s=[" + s + "], v=[" + v + "]+1");
                }
            } else {
                freq.put(s, 1);
                if (debug) {
                    System.out.println("Debug: s=[" + s + "], v=1");
                }
            }
        }

        // Find the most frequents ones
        for (i = 0; i < dictSize; i++) {
            key = findTheMostFreqOne(freq);
            if ("".equals(key)) {
                System.out.println("Error: findTheMostFreqOne failed - abort");
                break;
            }
            // Build the dictionary for zip processing
            dictionary.put(key, buildDictIndex(i));
            // Build the dictionary for zip output
            zipDict.add(key);
            freq.remove(key);
        }

        // Debug
        if (debug) {
            dumpMapString(dictionary);
            dumpArrayListString(zipDict);
        }
    }

    private static void buildDict4Unzip(ArrayList<String> dict) {
        int i;

        // Init the map
        dictionary = new HashMap<>();

        // Build the dictionary
        for (i = 0; i < dict.size(); i++) {
            dictionary.put(buildDictIndex(i), dict.get(i));
        }

        // Debug
        if (debug) {
            dumpMapString(dictionary);
        }
    }

    public static void zip(ArrayList<String> input) {
        int diff;
        ZipEle ze;
        String tmp;
        String ref;
        String tmp2;

        // Build the dictionary
        buildDict4Zip(input);
        if (debug) {
            System.out.println("Debug: dump the dictionary and zipDict again");
            dumpMapString(dictionary);
            dumpArrayListString(zipDict);
        }

        // Init the output
        zipOutput = "";
        zipRef = "";

        // Compress
        for (String s : input) {
            if (debug) {
                System.out.println("Debug: input=[" + s + "]");
                dumpMapString(dictionary);
            }
            // Go thru different schema
            if (dictionary.containsKey(s)) {
                if (debug) {
                    System.out.println("Debug: found a direct matching");
                }
                // Use direct matching
                tmp2 = dictionary.get(s);
                tmp = "00" + tmp2;
                ref = "00 " + tmp2;
            } else {
                if (debug) {
                    System.out.println("Debug: go on indirect matching");
                }
                // Find the number of difference
                ze = findMinDiffNum(s);
                switch (ze.diffNum) {
                    case 1:
                        // Use 2-bit consecutive mismatches encoding
                        tmp2 = buildLocIndex(ze.diffIndex1)
                                + dictionary.get(ze.diffString);
                        tmp = "01" + tmp2;
                        ref = "01 " + tmp2;
                        break;
                    case 2:
                        // Use 2-bit mismatches anyway encoding
                        tmp2 = buildLocIndex(ze.diffIndex1)
                                + buildLocIndex(ze.diffIndex2) + dictionary.get(ze.diffString);
                        tmp = "10" + tmp2;
                        ref = "10 " + tmp2;
                        break;
                    default:
                        // Use the original without encoding
                        tmp2 = s;
                        tmp = "11" + tmp2;
                        ref = "11 " + tmp2;
                        break;
                }
            }

            if (debug) {
                System.out.println(s + " --> " + tmp);
                zipRef += s + " --> " + ref + "\n";
            }

            zipOutput += tmp;
        }

        if (debug) {
            System.out.println("Debug: dump the zipRef" + "\n" + zipRef);
        }

        // Format the output
        formatZipOutput(zipOutput);

        // Append the separator and dictionary
        System.out.println(dictSeparator);
        for (String d : zipDict) {
            System.out.println(d);
        }
    }

    public static void unzip(String inputZip, ArrayList<String> dict) {
        // Build the dictionary
        buildDict4Unzip(dict);
        if (debug) {
            System.out.println("Debug: dump the dictionary again");
            dumpMapString(dictionary);
        }

        // Start unzipping
        int i = 0; // Start index
        int j = 2; // End index
        int index;
        String opcode;
        String dictIndex;
        String locIndex1;
        String locIndex2;
        // Assume we have at least more than 2 characters from the inputZip
        while (i < inputZip.length() && j <= inputZip.length()) {
            opcode = inputZip.substring(i, j);
            switch (opcode) {
                case "00":
                    // Direct matching
                    // Fetch the dictionary index
                    i += 2;
                    j = i + 3;
                    dictIndex = inputZip.substring(i, j);
                    // Output directly
                    System.out.println(dictionary.get(dictIndex));
                    // Update i,j
                    i += 3;
                    j = i + 2;
                    break;
                case "01":
                    // 2-bit consecutive mismatches
                    // Fetch the location index
                    i += 2;
                    j = i + 5;
                    locIndex1 = inputZip.substring(i, j);
                    // Fetch the dictionary index
                    i += 5;
                    j = i + 3;
                    dictIndex = inputZip.substring(i, j);
                    // Output
                    System.out.println(unzipImpl(locIndex1, "", dictIndex));
                    // Update i,j
                    i += 3;
                    j = i + 2;
                    break;
                case "10":
                    // 2-bit mismatches anyware
                    // Fetch the location indices
                    i += 2;
                    j = i + 5;
                    locIndex1 = inputZip.substring(i, j);
                    i += 5;
                    j = i + 5;
                    locIndex2 = inputZip.substring(i, j);
                    // Fetch the dictionary index
                    i += 5;
                    j = i + 3;
                    dictIndex = inputZip.substring(i, j);
                    // Output
                    System.out.println(unzipImpl(locIndex1, locIndex2, dictIndex));
                    // Update i,j
                    i += 3;
                    j = i + 2;
                    break;
                case "11":
                    // Check for padding at first
                    // Make sure we still have at least 32 characters following!
                    if (j + lineSize > inputZip.length()) {
                        // Stop here
                        return;
                    }

                    // Original matching
                    // Fetch the original
                    i += 2;
                    j = i + lineSize;
                    // Output
                    System.out.println(inputZip.substring(i, j));
                    // Update i,j
                    i += lineSize;
                    j = i + 2;
                    break;
                default:
                    System.out.println("Error: unknown opcode [" + opcode + "]");
                    return;
            }
        }
    }

}
