package com.disney.ecm.tests;

import com.disney.ecm.dataprovider.CsvProvider;
import com.disney.ecm.helpers.Logger;
import com.disney.ecm.helpers.Utils;
import com.disney.ecm.pages.Page;
import com.disney.ecm.pages.ThreadedBasePage;
import org.testng.Reporter;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ABEnvironmentsTest extends ThreadedBasePage
{

    // Regexs to get info from URL
    Pattern localeR = Pattern.compile("((?<=\\/)\\w{2}-\\w{2}(?=\\/))");
    Pattern entityR = Pattern.compile("((?<=\\/)\\w+(?=\\/))");
    Pattern urlFriendlyR = Pattern.compile("(?<=\\/)(\\w+|-)+(?=\\/$)");
    Vector<String> screenshotsList = new Vector<>();
    //List<Page> listsOfFirstEnvPages = Collections.synchronizedList(new ArrayList<Page>());
    //List<Page> listOfSecondEnvPages = Collections.synchronizedList(new ArrayList<Page>());
    List<Page> listOfFirstEnvPages = new ArrayList<>();
    List<Page> listOfSecondEnvPages= new ArrayList<>();



    @BeforeTest(alwaysRun = true, groups = {"ABTest"})
    public void beforeTest()
    {
        screenshotsList.clear();
    }

    @Test(dataProviderClass = CsvProvider.class, dataProvider = "csv", groups = {"ABTest"}, testName = "ATest")
    @Parameters({"headless"})
    public void ATest(String category, String url)
    {
        Page page;
        try
        {
            screenshotsList.clear();
            setUpDriver(headless, url, browser, bypass_onelink);
            Matcher match = localeR.matcher(url);
            String locale = "en-us";
            String entity = "";
            String urlFriendly = "";
            if (match.find())
            {
                locale = url.substring(match.start(), match.end());
            }
            else
            {
                Reporter.log("No locale found in url");
            }

            match = entityR.matcher(url);
            if (match.find())
            {
                entity = url.substring(match.start(), match.end());
            }

            match = urlFriendlyR.matcher(url);
            if (match.find())
            {
                urlFriendly = url.substring(match.start(), match.end());
            }

            Reporter.log("Test Execution Start");
            Reporter.log(url);
            openPage(url);
            //page=new Page(url,locale,entity,urlFriendly,environment,searchForTextsInPage(url),getListUrlImages());
            String urlEnv = Utils.getEnvironment(url);
            List<String> getListUrlImages = getListUrlImages();
            //List<String> searchForTextsInPage= searchForTextsInPage(url);//checar esto
            page = new Page(url, locale, entity, urlFriendly, urlEnv, searchForTextsInPage(url), getListUrlImages,searchForAltTexts(url));
            listOfFirstEnvPages.add(page);
            syncMap.addKey(locale + entity + urlFriendly, takeScreenshot(url, locale));
        } catch(Exception e)
        {
            Logger.log("Error When Executing Test " + e.getMessage(), Logger.ERROR);
        }
    }

    @Test(dataProviderClass = CsvProvider.class, dataProvider = "csv", groups = {"ABTest"}, testName = "BTest")
    public void BTest(String category, String url)
    {
        Page page;
        try
        {
            screenshotsList.clear();
            Reporter.log("Test Execution Start");
            String urlEnv = Utils.getEnvironment(url);
            System.out.println("Source environment is " + urlEnv);
            if (environment.contains("prod"))
            {
                url = url.replace(urlEnv + ".", "");
            }
            else if(urlEnv.contains("prod"))
            {
                url = url.replace("https://", "https://"+environment+".");
            }
            else
            {
                url = url.replace(urlEnv, environment);
            }
            setUpDriver(headless, url, browser, bypass_onelink);
            Matcher match = localeR.matcher(url);
            String locale = "en-us";
            String entity = "";
            String urlFriendly = "";
            if (match.find())
            {
                locale = url.substring(match.start(), match.end());
            }
            else
            {
                Reporter.log("No locale found in url");
            }

            match = entityR.matcher(url);
            if (match.find())
            {
                entity = url.substring(match.start(), match.end());
            }

            match = urlFriendlyR.matcher(url);
            if (match.find())
            {
                urlFriendly = url.substring(match.start(), match.end());
            }
            Reporter.log(url);
            openPage(url);
            System.out.println("creating Page object");
            List<String> listUrlImages =  getListUrlImages();
            page = new Page(url, locale, entity, urlFriendly, environment, searchForTextsInPage(url), listUrlImages,searchForAltTexts(url));
            System.out.println("adding to page list");
            listOfSecondEnvPages.add(page);
            syncMap.addKey(locale + entity + urlFriendly, takeScreenshot(url, locale));
        } catch(Exception e)
        {
            System.out.println("Error When Executing Test " + e.getMessage());
        }
    }

    @Test(dependsOnMethods = {"ATest", "BTest"}, groups = {"comparator"}, enabled = false)
    public void compareScreenshots()
    {
        String url1;
        String url2;
        String locale;
        String[] keys = syncMap.getAllKeys();
        Queue<String> ll;
        for (String key : keys)
        {
            ll = syncMap.getFromQueue(key);
            url1 = ll.remove();
            url2 = ll.remove();
            locale = Paths.get(url1).subpath(2,3).toString();
            System.out.println(url1);
            System.out.println(url2);
            Utils.compareImages(url1, url2, Paths.get("screenshots", datetime, locale, key + "diff.png").toString());
        }
    }
    @Test(dependsOnMethods = {"ATest", "BTest"}, groups = {"comparator"}, enabled = true)
    public void createHTMLReport()throws IOException{
        createHTMLFinalReport(listOfFirstEnvPages,listOfSecondEnvPages);
        //viewResults(listOfFirstEnvPages,listOfSecondEnvPages);
    }

}
