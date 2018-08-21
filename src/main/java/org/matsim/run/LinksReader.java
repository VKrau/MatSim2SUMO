package org.matsim.run;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class LinksReader {

    public static HashSet<String> read(String filename) {
        HashSet<String> set = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String s;
            while ((s = br.readLine()) != null) {
                set.add(s);
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            System.exit(-1);
        }
        return set;
    }
}
