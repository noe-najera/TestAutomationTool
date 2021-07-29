package com.disney.ecm.helpers;

import org.im4java.core.CompareCmd;
import org.im4java.core.IMOperation;
import org.im4java.process.StandardStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

public class Utils
{
    public static String prettyString(String name)
    {
        name = name.replace("://", ".").replace("/", ".");
        if (name.charAt(name.length() - 1) == '.')
        {
            name = name.substring(0, name.length() - 1);
        }
        return name;
    }

    public static boolean compareImages(String image1, String image2, String result)
    {
        System.out.println("Comparing [" + image1 + "] and [" + image2 + "]");
        CompareCmd compare = new CompareCmd() {
            // the 'compare' call fails in Windows since it's an alias to "magick compare"
            // so this override patches this call in Windows
            @Override
            public void setCommand(String... strings)
            {
                super.setCommand("magick", "compare");
            }
        };
        compare.setErrorConsumer(StandardStream.STDERR);
        IMOperation cmpOp = new IMOperation();

        cmpOp.metric("mae");
        cmpOp.fuzz(35d);
        cmpOp.addImage(image1);
        cmpOp.addImage(image2);
        cmpOp.addImage(result);

        try
        {
            compare.run(cmpOp);
            return true;
        }
        catch (Exception e)
        {
            System.out.println("Exception from compareImages -> " + e.getMessage());
            return false;
        }
    }

    public static String getTime()
    {
        LocalDateTime ldt = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
        return ldt.format(formatter);
    }

    public static String getEnvironment(String url)
    {
        ArrayList<String> envs = new ArrayList<>(Arrays.asList("latest", "stage", "lt01"));
        for (String env : envs)
        {
            if ((url.indexOf(env)) == 8)
            {
                return env;
            }
        }
        return "prod";
    }
}