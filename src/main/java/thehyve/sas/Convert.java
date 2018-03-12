/**
 * Copyright (c) 2015 The Hyve
 * This file is distributed under the MIT License (see accompanying file LICENSE).
 */
package thehyve.sas;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Date;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opencsv.CSVWriter;

import com.epam.parso.Column;
import com.epam.parso.SasFileProperties;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.SasFileReaderImpl;

/**
 * Command-line utility to convert files in SAS7BDAT format to 
 * comma-separated format (CSV). 
 * Based on the Parso library ({@link http://lifescience.opensource.epam.com/parso.html})
 * and opencsv ({@link http://opencsv.sourceforge.net/})
 * 
 * @author gijs@thehyve.nl
 */
public class Convert {

    public static final String USAGE = "Usage: sas-convert [foo.sas7bdat]\n\nYou can supply multiple files, or use a wildcard. CSV files will be created, named the same as the sas7bdat files but with .csv instead of .sas7bdat.";
    private static final Logger log = LoggerFactory.getLogger(Convert.class);

    public void convert(InputStream in, OutputStream out) throws IOException {
        Date start = new Date();
        SasFileReader reader = new SasFileReaderImpl(in);
        CSVWriter writer = new CSVWriter(new OutputStreamWriter(out));
        Object[] data;
        SasFileProperties properties = reader.getSasFileProperties();
        log.info("Reading file " + properties.getName());
        log.info(properties.getRowCount() + " rows.");
        List<Column> columns = reader.getColumns();
        String[] outData = new String[columns.size()];
        // Writing column names
        for(int i=0; i < columns.size(); i++) {
            outData[i] = columns.get(i).getName();
        }
        writer.writeNext(outData);

        try {
            log.info("Writing data...");
            long rowCount = 0;
            while((data = reader.readNext()) != null) {
                assert(columns.size() == data.length);
                for(int i=0; i < data.length; i++) {
                    outData[i] = data[i] == null ? "" : data[i].toString();
                }
                writer.writeNext(outData);
                rowCount++;
            }
            log.info("Done writing data.");
            log.info(rowCount + " rows written.");
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
        Date end = new Date();
        log.info("Converting took {} seconds.", (end.getTime() - start.getTime())/1000);
    }
    
    public static void main(String[] args) {
        Options options = new Options();
        options.addOption("h", "help", false, "Help");
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cl = parser.parse(options, args);
            if (cl.hasOption("help")) {
                System.err.printf(USAGE + "\n");
                return;
            }
            List<String> argList = cl.getArgList();
            if (argList.size() < 1) {
                System.err.printf("Too few parameters.\n\n" + USAGE + "\n");
                return;
            }
            try {
                for (String in_filename: argList) {
                    FileInputStream fin = new FileInputStream(in_filename);
                    OutputStream fout;

                    String out_filename;
                    if (in_filename.contains(".")) {
                        out_filename = in_filename.replaceAll("\\.[^.]*$", ".csv");
                    } else {
                        out_filename = in_filename.concat(".csv");
                    }
                    log.info("Writing to file: {}", out_filename);
                    fout = new FileOutputStream(out_filename);

                    Convert converter = new Convert();
                    converter.convert(fin, fout);
                    fin.close();
                    fout.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (ParseException e) {
            System.err.printf(USAGE + "\n");
            e.printStackTrace();
            return;
        }
    }

}
