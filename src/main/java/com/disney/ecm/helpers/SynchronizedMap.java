package com.disney.ecm.helpers;

import java.util.*;

public class SynchronizedMap
{
    private Map<String, Queue<String>> screenshotMap = new HashMap<>();
    private Queue<String> items = new LinkedList<>();

    public synchronized void pushToQueue(String item)
    {
        items.add(item);
    }

    public synchronized void addKey(String key, String val)
    {
        try
        {
            if (screenshotMap.containsKey(key))
            {
                if (!screenshotMap.get(key).contains(val))
                {
                    screenshotMap.get(key).add(val);
                }
            }
            else
            {
                this.addNewKey(key, val);
            }

        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    private void addNewKey(String key, String val)
    {
        Queue<String> newVal = new LinkedList<>();
        newVal.add(val);
        screenshotMap.put(key, newVal);
    }

    public synchronized Queue<String> getFromQueue(String key)
    {
        if (screenshotMap.containsKey(key))
        {
            return screenshotMap.get(key);
        }
        else
        {
            return null;
        }
    }

    public synchronized String[] getAllKeys()
    {
        return screenshotMap.keySet().toArray(new String[0]);
    }
}
