package com.disney.ecm.dataprovider;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.testng.annotations.DataProvider;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class CsvProvider
{
    @DataProvider(name = "csv", parallel = true)
    public Object[][] url() {
        Object[][] obj = null;
        ClassLoader cl = getClass().getClassLoader();

        try
        {
            String inputParam = System.getProperty("f");
            String inputFile = "";
            FileReader fr = null;
            try
            {
                if (inputParam.length() > 0)
                {
                    System.out.println("Using file [" + inputParam + "] for input");
                    fr = new FileReader(new File(inputParam), Charset.defaultCharset());
                }
                else
                {
                    fr = new FileReader(new File(cl.getResource("urls.csv").getFile()));
                }
            } catch (NullPointerException e)
            {
                System.out.println("No -Df parameter passed. Using 'urls'csv' from resources folder as default");
                //fr = new FileReader(new File(cl.getResource("urls.csv").getFile()));
                fr=new FileReader("src/test/resources/urls.csv" );
            }
            CSVReader cr = new CSVReaderBuilder(fr).build();
            List<String[]> allData = cr.readAll();
            int rowCount = allData.size();
            int colCount = 2;
            obj = new Object[rowCount][colCount];

            for (int i = 0, j = 0; i < rowCount; i++)
            {
                while (j < colCount)
                {
                    String[] rowData = allData.get(i);
                    for (String cell : rowData)
                    {
                        obj[i][j] = cell;
                        j++;
                    }
                }
                j = 0;
            }
        }
        catch (IOException | CsvException e)
        {
            e.printStackTrace();
        }
        return obj;
    }

    @DataProvider(name = "csv2", parallel = true)
    public Iterator<Object[]> createData()
    {
        String CSV_FILE = "urls.csv";
        String DELIMETER = ",";
        ClassLoader cl = getClass().getClassLoader();
        try {
            Scanner scanner = new Scanner(new FileReader(new File(cl.getResource("urls.csv").getFile()))).useDelimiter(DELIMETER);
            return new Iterator<Object[]>()
            {
                @Override
                public boolean hasNext()
                {
                    return scanner.hasNext();
                }
                @Override
                public Object[] next()
                {
                    return new Object[] { scanner.next() };
                }
            };
        } catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
}