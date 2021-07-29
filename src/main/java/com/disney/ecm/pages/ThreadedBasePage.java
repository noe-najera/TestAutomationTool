package com.disney.ecm.pages;

import com.browserup.bup.BrowserUpProxy;
import com.browserup.bup.BrowserUpProxyServer;
import com.browserup.bup.client.ClientUtil;
import com.browserup.bup.filters.RequestFilter;
import com.browserup.bup.util.HttpMessageContents;
import com.browserup.bup.util.HttpMessageInfo;
import com.disney.ecm.helpers.LocalDriverManager;
import com.disney.ecm.helpers.SynchronizedMap;
import com.disney.ecm.helpers.Utils;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import com.disney.ecm.helpers.Logger;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.ITestContext;
import org.testng.annotations.*;
import org.testng.annotations.Optional;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import static com.disney.ecm.helpers.Utils.getTime;

public class ThreadedBasePage {
    private String screenshotDir = "screenshots";
    protected ConcurrentMap<String, Queue<String>> screenshotMap;
    protected SynchronizedMap syncMap;
    protected Boolean headless;
    protected String browser;
    protected String environment;
    protected String bypass_onelink;
    protected BrowserUpProxy proxy;
    protected Proxy seleniumProxy;
    protected Dimension preScreenshotDimension = new Dimension(2024, 3100);
    protected Dimension postScreenshotDimension = new Dimension(2024, 4000);
    public String datetime;


    // synchronized
    public void setUpDriver(@Optional("false") Boolean headless, @Optional("stage") String url, @Optional("firefox") String browser, @Optional("latest") String bypass_onelink) {
        //create driver
        try
        {
            ChromeOptions options = new ChromeOptions();
            FirefoxOptions ffOptions = new FirefoxOptions();

            options.setHeadless(headless);
            ffOptions.setHeadless(headless);
            if (url.contains(bypass_onelink)) // use proxy
            {
                System.out.println(url + " matches target environment '" + bypass_onelink + "', special header will be injected");
                if (browser.compareTo("firefox") == 0)
                {
                    try
                    {
                        ffOptions.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                        ffOptions.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
                        ffOptions.setCapability(CapabilityType.PROXY, seleniumProxy);
                        System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
                        LocalDriverManager.setWebDriver(new FirefoxDriver(ffOptions));
                    } catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
                else
                {
                    try
                    {
                        options.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
                        options.addArguments("start-maximized"); // https://stackoverflow.com/a/26283818/1689770
                        options.addArguments("enable-automation"); // https://stackoverflow.com/a/43840128/1689770
                        options.addArguments("--no-sandbox"); //https://stackoverflow.com/a/50725918/1689770
                        options.addArguments("--disable-infobars"); //https://stackoverflow.com/a/43840128/1689770
                        options.addArguments("--disable-dev-shm-usage"); //https://stackoverflow.com/a/50725918/1689770
                        options.addArguments("--disable-browser-side-navigation"); //https://stackoverflow.com/a/49123152/1689770
                        options.addArguments("--disable-gpu"); //https://stackoverflow.com/questions/51959986/how-to-solve-selenium-chromedriver-timed-out-receiving-message-from-rendere
                        options.setCapability(CapabilityType.PROXY, seleniumProxy);
                        String proxyOption = "--proxy-server=" + seleniumProxy.getHttpProxy();
                        options.addArguments(proxyOption);
                        LocalDriverManager.setWebDriver(new ChromeDriver(options));
                    } catch (Exception e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
            }
            else
            {
                if (browser.compareTo("firefox") == 0)
                {
                    System.setProperty(FirefoxDriver.SystemProperty.BROWSER_LOGFILE,"/dev/null");
                    LocalDriverManager.setWebDriver(new FirefoxDriver(ffOptions));
                }
                else
                {
                    LocalDriverManager.setWebDriver(new ChromeDriver(options));
                }
            }

//            options.setCapability(CapabilityType.ACCEPT_INSECURE_CERTS, true);
//
//            LocalDriverManager.getDriver().manage().timeouts().implicitlyWait(90, TimeUnit.SECONDS);
            LocalDriverManager.getDriver().manage().timeouts().pageLoadTimeout(120, TimeUnit.SECONDS);
            LocalDriverManager.getDriver().manage().window().setSize(preScreenshotDimension);
            Logger.log("driver created -> " + LocalDriverManager.getDriver() + ", threadId -> " + String.valueOf(Thread.currentThread().getId()), Logger.INFO);
        }
        catch (Exception e)
        {
            System.out.println("Exception -> " + e.getMessage());
        }
    }


    public void openPage(String url) {
        try
        {
            Logger.log("Opening page -> " + url, Logger.INFO);
            Logger.log("driver -> " + LocalDriverManager.getDriver().toString() + ", url -> " + url, Logger.DEBUG);
            LocalDriverManager.getDriver().manage().deleteAllCookies();
            LocalDriverManager.getDriver().get(url);
            Logger.log("Page opened -> " + url, Logger.INFO);

            // Scroll down to bottom to trigger animated elements to be displayed
            sleep(4000);
            ((JavascriptExecutor)LocalDriverManager.getDriver()).executeScript("window.scrollTo(0, 800);");
            sleep(2000);
            ((JavascriptExecutor)LocalDriverManager.getDriver()).executeScript("window.scrollTo(800, 1600);");
            sleep(2000);
            //((JavascriptExecutor)LocalDriverManager.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight);");
            //((JavascriptExecutor)LocalDriverManager.getDriver()).executeScript("window.scrollTo(document.body.scrollHeight,0 );");
            //((JavascriptExecutor)LocalDriverManager.getDriver()).executeScript("window.scrollTo(0, document.body.scrollHeight);");




            //Thread.sleep(3000);
            if (url.contains("shops"))
            {
                wait(By.className("related-activities"));
                //wait(By.className("gav-container ng-star-inserted"));
            }
        }
        catch (Exception e)
        {
            System.out.println("Timeout -> url " + url + "ERROR message -> " + e.getMessage());
        }
    }

    public WebElement findElement(By locator) {
        return LocalDriverManager.getDriver().findElement(locator);
    }

    public List<WebElement> findElements(By locator) {
        return LocalDriverManager.getDriver().findElements(locator);
    }

    public int sizeElements(By locator) {
        return LocalDriverManager.getDriver().findElements(locator).size();
    }

    public Boolean isPresent(By locator) {
        return LocalDriverManager.getDriver().findElements(locator).size() > 0;
    }

    public void write(String text, By locator) {
        LocalDriverManager.getDriver().findElement(locator).sendKeys(text);
    }

    public String getText(By locator) {
        return LocalDriverManager.getDriver().findElement(locator).getText();
    }

    public void click(By locator) {
        LocalDriverManager.getDriver().findElement(locator).click();
    }

    public void clear(By locator) {
        LocalDriverManager.getDriver().findElement(locator).clear();
    }

    public void submit(By locator) {
        LocalDriverManager.getDriver().findElement(locator).submit();
    }

    public Boolean isDisplayed(By locator) {
        try {
            return LocalDriverManager.getDriver().findElement(locator).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public Boolean isEnabled(By locator) {
        try {
            return LocalDriverManager.getDriver().findElement(locator).isEnabled();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public void sleep(long time)
    {
        try{
            Thread.sleep(time);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void wait(By locator) {
        try {
            WebDriverWait load = new WebDriverWait(LocalDriverManager.getDriver(), 30);
            load.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            System.out.println("Couldn't find the element(s) " + locator.toString() + " in the expected time");
        }
    }

    public List<String> searchForTextsInPage(String url) {
        List<String> ListOfTextsOfPage= new ArrayList<>();
        By selectorTemp = By.xpath("//div[@id='pageContent']");
        if (!findElements(selectorTemp).isEmpty()) {
            ListOfTextsOfPage=getListOfContent(selectorTemp,url);
        }
        selectorTemp=By.xpath("//div[@class='finder-content']");
        if (!findElements(selectorTemp).isEmpty()) {
            ListOfTextsOfPage=getListOfContent(selectorTemp,url);
        }
        return ListOfTextsOfPage;
    }

    public List<String> getListOfContent(By selectorTemp,String url) {
        //WebDriver driver = LocalDriverManager.getDriver();
        JavascriptExecutor js=(JavascriptExecutor) LocalDriverManager.getDriver();

//        try {
//            WebDriverWait load = new WebDriverWait(driver, 30);
//            load.until(ExpectedConditions.presenceOfElementLocated(selectorTemp));
//       } catch (Exception e) { System.out.println("Couldn´t find the element(s) in the expected time"); }
        wait(selectorTemp);
        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
       sleep(3000);

        WebElement textsFromPage=findElement(selectorTemp);
        String stringTemp=textsFromPage.getText();
        String[] stringArrayTemp= stringTemp.split("\n");
        List<String> listWithTextOfElements = new ArrayList<>();
        for(int i=0;i<stringArrayTemp.length;i++)
            listWithTextOfElements.add(stringArrayTemp[i]);
        //driver.quit();
        return listWithTextOfElements;
    }

    public List<String> getListUrlImages(){
        List<String> urlOfImages= new ArrayList<>();
        By selectorTemp = By.xpath("//div[@id='pageContent']//img");
        if (!findElements(selectorTemp).isEmpty()) {
            urlOfImages = getImagesResults(selectorTemp);
        }
        selectorTemp = By.xpath("//div[@class='finder-content']//picture//img");
        if (!findElements(selectorTemp).isEmpty()) {
            urlOfImages = getImagesResults(selectorTemp);
        }
        return urlOfImages;
    }

    public List<String> getImagesResults(By selector){
        wait(selector);
        List<WebElement> listOfImages = findElements(selector);
        List<String> results = new ArrayList<>();
        String[] strTemp;
        for(WebElement element:listOfImages){
            strTemp=element.getAttribute("src").split("\\?");
            if(strTemp[0].substring(strTemp[0].length()-3).equals("jpg"))
                results.add(strTemp[0]);
        }
        return results;
    }

    public HashMap<String,String> searchForAltTexts(String url) {
        HashMap<String,String> mapAltTexts= new HashMap<>();
        By selectorTemp = By.xpath("//div[@id='pageContent']//img");
        if (!findElements(selectorTemp).isEmpty()) {
            mapAltTexts=getAltTextResults(selectorTemp);
        }
        selectorTemp=By.xpath("//div[@class='finder-content']//picture//img");
        if (!findElements(selectorTemp).isEmpty()) {
            mapAltTexts=getAltTextResults(selectorTemp);
        }
        return mapAltTexts;
    }
    public HashMap<String,String> getAltTextResults(By selector){
        wait(selector);
        List<WebElement> listOfImages = findElements(selector);
        HashMap<String,String> results = new HashMap<>();
        String[] strTemp;
        for(WebElement element:listOfImages){
            strTemp=element.getAttribute("src").split("\\?");
            if(strTemp[0].substring(strTemp[0].length()-3).equals("jpg")){
                results.put(strTemp[0],element.getAttribute("alt"));
            }
        }
        return results;
    }


    public int findCoincidenceInOtherPage(String urlTemp1, List<Page> listOfOtherPages) {
        for(Page page:listOfOtherPages){
            String urlTemp2 =page.getLocale()+"."+ page.getEntity()+"."+page.getUrlFriendly();
            if(urlTemp1.equals(urlTemp2)){
                return listOfOtherPages.indexOf(page);//position
            }
        }
        return -1; //not found
    }

    public String[] compareContentOfListsEnhanced(List<String> listFirstPage, List<String> listSecondPage) {
        String[] results= new String[3]; //total matches, noMatches1stPage, noMatches2ndPage
        results[0]=results[1]=results[2]="";
        int indexPage1=0, indexPage2, x = 0;
        Vector<Integer> matches = new Vector<>();
        Vector<Integer> noMatchesFirstPage = new Vector<>();
        List<String> listSecondPageDuplicate=new ArrayList<>(listSecondPage);
        boolean coincidenceFound = false;
        for(String element: listFirstPage){
            indexPage2=0;
            coincidenceFound=false;
            while(coincidenceFound==false && indexPage2 <listSecondPageDuplicate.size()){
                if(element.equals(listSecondPageDuplicate.get(indexPage2))) {
                    coincidenceFound = true;
                    listSecondPageDuplicate.set(indexPage2,"matched");
                }else
                    indexPage2++;
            }
            if(coincidenceFound){
                matches.add(indexPage1);
                matches.add(indexPage2);
            }else if (indexPage2>=listSecondPageDuplicate.size()){
                noMatchesFirstPage.add(indexPage1);
            }
            indexPage1++;
        }
        results[0]=String.valueOf(x/2);
        for(int iterator=0;iterator<noMatchesFirstPage.size();iterator++) {
            results[1] = results[1] + listFirstPage.get(noMatchesFirstPage.get(iterator)) + "\n";
        }
        for(String element:listSecondPageDuplicate){
            if(element!="matched") {
                results[2]=results[2] + element+"\n";
            }
        }
        return results;
    }

    public String[] compareUrlImages(List<String> list1, List<String> list2){
        String[] results= new String[3]; //total matches, noMatches1stPage, noMatches2ndPage
        results[0]=results[1]=results[2]="";
        Vector<Integer> matchesList1 = new Vector<>();
        int elementCounter=0;
        for(String element:list1){
            for(int i=0;i<list2.size();i++){
                if(element.equals(list2.get(i))){
                    list2.set(i,"matched");
                    matchesList1.add(elementCounter);
                    break;
                }
            }
            elementCounter++;
        }
        boolean flagFounded=false;
        for(int i=0;i<list1.size();i++){
            for(int ii=0;ii<matchesList1.size();ii++) {
                if (i == matchesList1.get(ii))
                    flagFounded=true;
            }
            if(flagFounded==false)
                results[1]=results[1]+list1.get(i)+"\n";
            else
                flagFounded=false;
        }
        for(String element:list2){
            if(!element.equals("matched")){
                results[2]=results[2]+element+"\n";
            }
        }
        return results;
    }

    public HashMap<String,String> compareAltTextImages(HashMap<String,String> map1, HashMap<String,String> map2){
        HashMap<String,String> results = new HashMap<>();
        for (Map.Entry<String, String> e : map1.entrySet()){
            if(map2.containsKey(e.getKey())){
                if(e.getValue().equals(map2.get(e.getKey()))){
                   map2.remove(e.getKey());
                }
                else{
                    results.put("FP"+e.getKey(),e.getValue()); //FP=FirstPage
                }
            }
            else{
                results.put("FP"+e.getKey(),e.getValue()); //FP=FirstPage
            }
        }
        for (Map.Entry<String, String> e : map2.entrySet())
            results.put("SP"+e.getKey(),e.getValue()); //SP=SecondPage
        return results;
    }

    // synchronized
    public String takeScreenshot(String name, String locale){
        try
        {
            // Set browser window size to a larger size so top of the page is not cropped out
            Thread.sleep(2000);
            LocalDriverManager.getDriver().manage().window().setSize(postScreenshotDimension);
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
        Logger.log("Taking screenshot of " + name, Logger.INFO);
        String destination = "";
        try
        {
            // These remove the Advisory from page header
            ((JavascriptExecutor)LocalDriverManager.getDriver()).executeScript("Array.from(document.getElementsByClassName(\"global-messaging-old\")).forEach(function(i) { i.remove() })");
            ((JavascriptExecutor)LocalDriverManager.getDriver()).executeScript("Array.from(document.getElementsByClassName(\"message-center\")).forEach(function(i) { i.remove() })");
            File src = ((TakesScreenshot)LocalDriverManager.getDriver()).getScreenshotAs(OutputType.FILE);
            name = Utils.prettyString(name);
            destination = Paths.get(screenshotDir, datetime, locale, name + ".png").toString();
            Logger.log("Saving screenshot in -> " + destination, Logger.INFO);
//            System.out.println("Saving screenshot in -> " + destination);
            FileUtils.copyFile(src, new File(destination));
            System.out.println("Saved");
        } catch(Exception e)
        {
            System.out.print("Error copying screenshot file " + e);
        }
        return destination;
    }

    @AfterMethod(groups = {"ABTest"})
    public void afterMethod()
    {
        System.out.println("@afterMethod");
        try
        {
            Logger.log("Closing driver -> " + LocalDriverManager.getDriver().toString(), Logger.INFO);
            //LocalDriverManager.getDriver().quit();
            Logger.log("Driver closed", Logger.INFO);
            LocalDriverManager.removeDriver();
            Logger.log("Driver removed", Logger.DEBUG);
        } catch (NullPointerException e)
        {
            System.out.println("Exception in @afterMethod -> " + e.getMessage());
        }
    }

    @BeforeSuite(groups = {"ABTest"})
    @Parameters({ "headless", "environment", "browser", "bypass_onelink"})
    public void beforeSuite(@Optional("false") Boolean headless, @Optional("stage") String environment, @Optional("firefox") String browser, @Optional("latest") String bypass_onelink)
    {
        this.headless = headless;
        this.environment = environment;
        this.browser = browser;
        this.bypass_onelink = bypass_onelink;
        syncMap = new SynchronizedMap();
        datetime = getTime();
        proxy = new BrowserUpProxyServer();
        proxy.start();
        seleniumProxy = ClientUtil.createSeleniumProxy(proxy);

        System.out.println(bypass_onelink + " is the environment OneLink will be bypassed");
        proxy.addRequestFilter(new RequestFilter()
        {
            @Override
            public HttpResponse filterRequest(HttpRequest request, HttpMessageContents contents, HttpMessageInfo messageInfo)
            {
                request.headers().add("x-disney-translations-off", "true");
                // in the request filter, you can return an HttpResponse object to "short-circuit" the request
                return null;
            }
        });

        if (browser.compareTo("firefox") == 0)
        {
            WebDriverManager.firefoxdriver().setup();
        }
        else
        {
            WebDriverManager.chromedriver().setup();
        }
    }

    @AfterSuite(alwaysRun = true, enabled = true)
    public void afterSuite(ITestContext context)
    {
        System.out.println("@compareScreenshots");
        String url1;
        String url2;
        String locale;
        String[] keys = syncMap.getAllKeys();
        Queue<String> ll;
        System.out.println(keys.toString());
        for (String key : keys)
        {
            ll = syncMap.getFromQueue(key);
            try
            {
                url1 = ll.remove();
            } catch (NoSuchElementException e)
            {
                System.out.println("Exception in @afterSuite -> " + e.getMessage());
                url1 = "";
            }
            try
            {
                url2 = ll.remove();
            } catch (NoSuchElementException e)
            {
                System.out.println("Exception in @afterSuite -> " + e.getMessage());
                url2 = "";
            }
            System.out.println(url1);
            System.out.println(url2);
            if ((url1.length() > 0) && (url2.length() > 0))
            {
                locale = Paths.get(url1).subpath(2, 3).toString();
                Utils.compareImages(url1, url2, Paths.get("screenshots", datetime, locale, key + ".diff.png").toString());
            }
        }
        LocalDriverManager.removeDriver();
    }

    public void createHTMLFinalReport(List<Page> listOfFirstEnvPages, List<Page> listOfSecondEnvPages) throws IOException {
        String destination = Paths.get("report.html").toString();
        File file = new File(destination);
        if(file.exists()){
            file.delete();
        }
        FileWriter fw = new FileWriter(file.getAbsoluteFile(),true);
        BufferedWriter bw = new BufferedWriter(fw);
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        bw.write("<html>"+"\n");
        bw.write("<head>"+"\n"+"<meta http-equiv='Content-Type' content='text/html; charset=utf-8'/>"+"\n");
        bw.write("<style type='text/css'>"+"\n");
        bw.write(".report {" +"\n" + "border: 2px solid black;" +"\n"+"}"+"\n"+"</style>"+"\n");
        bw.write("<title>"+"FINAL REPORT OF TEST EXECUTION"+"</title>"+"\n");
        bw.write("</head>"+"\n");
        bw.write("<body>"+"\n");
        bw.write("<u><h1 align='center'>COMPARISON TEST EXECUTION REPORT</h1></u>"+"\n");
        bw.write("<u><h2 align='center'>"+formatter.format(date)+"</h2></u>"+"\n");
        bw.write("<table align='center' border='0' width='75%' height='10'>"+"\n");
        bw.write("<tr><td width='70%' ></td></tr>"+"\n");
        String[] arrStr= new String[3];
        for(Page page:listOfFirstEnvPages) {
            String environmentA = page.getEnvironment();
            String urlTemp1 = page.getLocale() + "." + page.getEntity() + "." + page.getUrlFriendly();
            int index = findCoincidenceInOtherPage(urlTemp1, listOfSecondEnvPages);
            if(index!=-1) {
                arrStr = compareContentOfListsEnhanced(page.getTextsInPage(), listOfSecondEnvPages.get(index).getTextsInPage());
                bw.write("<table class='report' align='center' width='75%'>" + "\n");
                bw.write("<tr><td class='report' colspan='2' align='left'><b><font color='#000000' face='Tahoma' size='2'>URL 1: " + page.getUrl() +"<br>URL 2: "+ listOfSecondEnvPages.get(index).getUrl() +"</font></b></td></tr>" + "\n");
                bw.write("<tr><td class='report' bgcolor='#CCCCFF' colspan='2' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "RESULTS FROM TEXT COMPARISON" + "</font></b></td></tr>" + "\n");
                bw.write("<tr>" + "\n");
                bw.write("<td class='report' bgcolor='#FFFFDC' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "TEXT FOUND IN '" +environmentA.toUpperCase()+ "' NOT PRESENT IN '" + environment.toUpperCase() + "'</font></b></td>" + "\n");
                bw.write("<td class='report' bgcolor='#FFFFDC' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "TEXT FOUND IN '" + environment.toUpperCase() + "' NOT PRESENT IN '"+environmentA.toUpperCase()+"'</font></b></td>" + "\n");
                bw.write("</tr>" + "\n");
                bw.write("<tr>" + "\n");
                bw.write("<td align='left'>" + "\n");
                bw.write("<table style='width: 100%'>" + "\n");
                boolean colorFlag=true;
                String[] temp = arrStr[1].split("\n");
                for (int i = 0; i < temp.length; i++) {
                    if(colorFlag){
                        bw.write("<tr><td style='background-color:#ccffff'><font color='#000000' face='Tahoma' size='2'>" + temp[i] + "</font></td></tr>" + "\n");
                        colorFlag=false;
                    }
                    else {
                        bw.write("<tr><td><font color='#000000' face='Tahoma' size='2'>" + temp[i] + "</font></td></tr>" + "\n");
                        colorFlag=true;
                    }
                }
                bw.write("</table>");
                bw.write("</td>" + "\n");
                bw.write("<td align='left'>" + "\n");
                bw.write("<table style='width: 100%'>" + "\n");
                temp = arrStr[2].split("\n");
                colorFlag=true;
                for (int i = 0; i < temp.length; i++) {
                    if (colorFlag) {
                        bw.write("<tr><td style='background-color:#ccffff'><font color='#000000' face='Tahoma' size='2'>" + temp[i] + "</font></td></tr>" + "\n");
                        colorFlag=false;
                    }else {
                        bw.write("<tr><td><font color='#000000' face='Tahoma' size='2'>" + temp[i] + "</font></td></tr>" + "\n");
                        colorFlag=true;
                    }
                }
                bw.write("</table>");
                bw.write("</td>" + "\n");
                bw.write("</tr>" + "\n");
                bw.write("<tr><td class='report' bgcolor='#CCCCFF' colspan='2' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "RESULTS FROM URL IMAGES COMPARISON" + "</font></b></td></tr>" + "\n");
                bw.write("<tr>" + "\n");
                bw.write("<td class= 'report' bgcolor='#FFFFDC' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "URL IMAGES FOUND IN '"+environmentA.toUpperCase()+"' NOT PRESENT IN '" + environment.toUpperCase() + "'</font></b></td>" + "\n");
                bw.write("<td class='report' bgcolor='#FFFFDC' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "URL IMAGES FOUND IN '" + environment.toUpperCase()  + "' NOT PRESENT IN '"+environmentA.toUpperCase() + "'</font></b></td>" + "\n");
                bw.write("</tr>" + "\n");
                bw.write("<tr>"+"\n");
                bw.write("<td align='left'>" + "\n");
                bw.write("<table style='width: 100%'>" + "\n");
                arrStr=compareUrlImages(page.getUrlImages(),listOfSecondEnvPages.get(index).getUrlImages());
                temp=arrStr[1].split("\n");
                colorFlag=true;
                for(int i=0;i<temp.length;i++){
                    if(colorFlag) {
                        bw.write("<tr><td style='background-color:#ccffff'><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+temp[i] + "'>"+ temp[i] + "</a></font></td></tr>" + "\n");
                        colorFlag=false;
                    }
                    else{
                        bw.write("<tr><td><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+temp[i] + "'>"+ temp[i] + "</a></font></td></tr>" + "\n");
                        colorFlag=true;
                    }
                }
                bw.write("</table>");
                bw.write("</td>" + "\n");
                bw.write("<td align='left'>" + "\n");
                bw.write("<table style='width: 100%'>" + "\n");
                temp=arrStr[2].split("\n");
                colorFlag=true;
                for(int i=0;i<temp.length;i++){
                    if(colorFlag){
                        bw.write("<tr><td style='background-color:#ccffff'><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+temp[i] + "'>"+ temp[i] + "</a></font></td></tr>" + "\n");
                        colorFlag=false;
                    }
                    else{
                        bw.write("<tr><td><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+temp[i] + "'>"+ temp[i] + "</a></font></td></tr>" + "\n");
                        colorFlag=true;
                    }
                }
                bw.write("</table>");
                bw.write("</td>" + "\n");
                bw.write("</tr>" + "\n");
                bw.write("<tr><td class='report' bgcolor='#CCCCFF' colspan='2' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "RESULTS FROM IMAGE ALT TEXT COMPARISON" + "</font></b></td></tr>" + "\n");
                bw.write("<tr>" + "\n");
                bw.write("<td class= 'report' bgcolor='#FFFFDC' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "ALT TEXT FOUND IN '"+environmentA.toUpperCase()+"' NOT PRESENT IN '" + environment.toUpperCase() + "'</font></b></td>" + "\n");
                bw.write("<td class='report' bgcolor='#FFFFDC' align='center'><b><font color='#000000' face='Tahoma' size='2'>" + "ALT TEXT FOUND IN '" + environment.toUpperCase()  + "' NOT PRESENT IN '"+environmentA.toUpperCase() + "'</font></b></td>" + "\n");
                bw.write("</tr>" + "\n");
                bw.write("<tr>"+"\n");
                bw.write("<td align='left'>" + "\n");
                bw.write("<table style='width: 100%'>" + "\n");
                HashMap <String,String> tempMap = compareAltTextImages(page.getAltTextMap(),listOfSecondEnvPages.get(index).getAltTextMap());
                colorFlag=true;
                String strTemp="";
                for (Map.Entry<String, String> e : tempMap.entrySet()){
                    if(e.getKey().toString().substring(0,2).equals("FP")){ //FirstPage
                        strTemp= e.getKey().toString().substring(2,e.getKey().toString().length());
                        if(colorFlag) {
                            bw.write("<tr><td style='background-color:#ccffff'><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+strTemp + "'>"+ strTemp + "</a><br>"+e.getValue()+"</font></td></tr>" + "\n");
                            colorFlag=false;
                        }
                        else{
                            bw.write("<tr><td><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+strTemp + "'>"+ strTemp + "</a><br>"+e.getValue()+"</font></td></tr>" + "\n");
                            colorFlag=true;
                        }
                    }
                }
                bw.write("</table>");
                bw.write("</td>" + "\n");
                bw.write("<td align='left'>" + "\n");
                bw.write("<table style='width: 100%'>" + "\n");
                colorFlag=true;
                for (Map.Entry<String, String> e : tempMap.entrySet()){
                    if(e.getKey().toString().substring(0,2).equals("SP")){ //SecondPage
                        strTemp= e.getKey().toString().substring(2,e.getKey().toString().length());
                        if(colorFlag) {
                            bw.write("<tr><td style='background-color:#ccffff'><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+strTemp + "'>"+ strTemp + "</a><br>"+e.getValue()+"</font></td></tr>" + "\n");
                            colorFlag=false;
                        }
                        else{
                            bw.write("<tr><td><font color='#000000' face='Tahoma' size='2'>" +"<a href='"+strTemp + "'>"+ strTemp + "</a><br>"+e.getValue()+"</font></td></tr>" + "\n");
                            colorFlag=true;
                        }
                    }
                }
                bw.write("</table>");
                bw.write("</td>" + "\n");
                bw.write("</tr>" + "\n");
                bw.write("</table>"+"\n");
                bw.write("<br><br>");
            }
            else{
                bw.write("<table class='report' align='center' width='75%'>" + "\n");
                bw.write("<tr><td class='report' colspan='2' align='left'><b><font color='#000000' face='Tahoma' size='2'>" + page.getUrl() +"</font></b></td></tr>" + "\n");
                bw.write("<tr><td class='report' bgcolor='#ff0000' colspan='2' align='center'><b><font color='#000000' face='Tahoma' size='2'>TEST FAILED. COULD NOT COMPARE. RUN THE TOOL AGAIN FOR THIS WEBPAGE" + "</font></b></td></tr>" + "\n");
                bw.write("</table>");
                bw.write("<br><br>");
            }
        }
        bw.write("</table>"+"\n");
        bw.write("</body>"+"\n");
        bw.write("</html>");
        bw.flush();
        bw.close();
    }

    public void viewResults (List<Page> listOfFirstEnvPages, List<Page> listOfSecondEnvPages) {
        System.out.println("************************CHECKING PAGE CONTENT**********************");
        for(Page page: listOfFirstEnvPages){
            System.out.println(page.getUrl());
            System.out.println("Texts in page:");
            for(String element:page.getTextsInPage()){
                System.out.println(element);
            }
            System.out.println("-----------------------------------------------------------------");
            System.out.println("Url Images in page:");
            for(String element:page.getUrlImages()){
                System.out.println(element);
            }
            System.out.println("-----------------------------------------------------------------");
        }
        for(Page page: listOfSecondEnvPages){
            System.out.println(page.getUrl());
            System.out.println("Texts in page:");
            for(String element:page.getTextsInPage()){
                System.out.println(element);
            }
            System.out.println("-----------------------------------------------------------------");
            System.out.println("Url Images in page:");
            for(String element:page.getUrlImages()){
                System.out.println(element);
            }
            System.out.println("-----------------------------------------------------------------");
        }
    }
}