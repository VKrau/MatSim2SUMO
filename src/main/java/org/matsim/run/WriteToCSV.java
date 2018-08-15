package org.matsim.run;

import com.google.common.collect.Table;

import java.io.FileWriter;
import java.io.IOException;

public class WriteToCSV {

    public static void run(Table table, String file, boolean appendToFile) {

        String path = file;

        try {
            FileWriter writer;
            writer = new FileWriter(path, appendToFile);

            // Write CSV
            for (Object row:table.rowKeySet()) {
                for (Object col:table.columnKeySet()) {
                    if (table.get(row,col)!=null) {
                        writer.write(String.valueOf(row));
                        writer.write(",");
                        writer.write(String.valueOf(col));
                        writer.write(",");
                        writer.write(String.valueOf(table.get(row,col)));
                        writer.write("\r\n");
                    }
                }
            }
            System.out.println("Write success!");
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}