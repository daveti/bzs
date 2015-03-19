/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package bzs;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Main
 * @author daveti
 */
public class Bzs {

    private static final boolean debug = false;
    private static boolean isZip = true;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        // Process the arguments
        OUTER:
        for (String s : args) {
            if (debug) {
                System.out.println("Debug: arg [" + s + "]");
            }

            switch (s) {
                case "1":
                    if (debug) {
                        System.out.println("Debug: compression");
                    }
                    isZip = true;
                    break OUTER;
                case "2":
                    if (debug) {
                        System.out.println("Debug: decompression");
                    }
                    isZip = false;
                    break OUTER;
                default:
                    System.out.println("Error: unsupport argument [" + s + "]");
                    return;
            }
        }

        // Zip or Unzip
        if (isZip) {
            try {
                // Load the input file
                Utils.readInputFile();

                // Set the output file
                if (!debug) {
                    Utils.setOutput4Zip();
                }

                // Zip
                Zip.zip(Utils.input);

            } catch (IOException ex) {
                Logger.getLogger(Bzs.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                // Load the input file
                Utils.readInputZipFile();

                // Set the output file
                if (!debug) {
                    Utils.setOutput4Unzip();
                }

                // Unzip
                Zip.unzip(Utils.inputZip, Utils.dict);

            } catch (IOException ex) {
                Logger.getLogger(Bzs.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

}
