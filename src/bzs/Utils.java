/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Utils collection - input/output handling
 * @author daveti
 */
public class Utils {

    private static final String inputFile = "original.txt";
    private static final String inputZipFile = "compressed.txt";
    private static final String outputZipFile = "cout.txt";
    private static final String outputFile = "dout.txt";
    private static final String zipSeparator = "xxxx";
    private static final boolean debug = false;
    
    public static ArrayList<String> input;
    public static ArrayList<String> dict;
    public static String inputZip;
    

    public static void readInputFile() throws IOException {
        // Init the input
        input = new ArrayList();
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line = br.readLine();
            while (line != null) {
                if (debug) {
                    System.out.println("Debug: line [" + line + "]");
                }
                input.add(line);
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void readInputZipFile() throws IOException {
        boolean isDict = false;
        // Init the input and the dict
        inputZip = "";
        dict = new ArrayList();
        
        try (BufferedReader br = new BufferedReader(new FileReader(inputZipFile))) {
            String line = br.readLine();
            while (line != null) {
                if (debug) {
                    System.out.println("Debug: line [" + line + "]");
                }
                
                if (zipSeparator.equals(line)) {
                    isDict = true;
                    line = br.readLine();
                    continue;
                }
                
                if (isDict) {
                    dict.add(line);
                } else {
                    inputZip += line;
                }
                
                line = br.readLine();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if (debug) {
            System.out.println("Debug: zip [" + inputZip + "]");
        }
    }

    public static void setOutput4Zip() {
        try {
            System.setOut(new PrintStream(new File(outputZipFile)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void setOutput4Unzip() {
        try {
            System.setOut(new PrintStream(new File(outputFile)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
