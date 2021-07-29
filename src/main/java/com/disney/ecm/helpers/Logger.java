package com.disney.ecm.helpers;

import org.testng.Reporter;

import java.util.*;

public class Logger extends Reporter
{
    public static final int TRACE = 0;
    public static final int DEBUG = 1;
    public static final int INFO = 2;
    public static final int WARN = 3;
    public static final int ERROR = 4;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private static String color;
    private static List<String> colorsList = new LinkedList<>(List.of(ANSI_BLACK,
            ANSI_RED, ANSI_GREEN, ANSI_YELLOW, ANSI_BLUE, ANSI_PURPLE, ANSI_CYAN, ANSI_WHITE));

    static Random rand = new Random();


    private static Map<Number, String> threadMap = new HashMap<Number, String>();

    private static String randomColor()
    {
        int randomIndex = rand.nextInt(colorsList.size());
        return colorsList.get(randomIndex);
    }

    private static String addOrGetThread(long threadId)
    {
        if (!threadMap.containsKey(threadId))
        {
            threadMap.put(threadId, randomColor());
        }
        return threadMap.get(threadId);
    }

    public static void log(String msg, int level)
    {
        color = addOrGetThread(Thread.currentThread().getId());
        log(color + msg + ANSI_RESET, level, true);
    }
}
