package com.disney.ecm.pages;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;


public class BasePage {
    private WebDriver driver;
    private JavascriptExecutor js;
    By anchors = By.xpath("//a[*]");

    public WebDriver setUpDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(true);
        options.addArguments("--headless");
        driver=new ChromeDriver(options);
        js=(JavascriptExecutor) driver;
        return driver;
    }

    public WebDriver setUpDriver(@Optional("false") Boolean headless) {
        //create driver
        System.out.println("isHeadless -> " + headless.toString());
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.setHeadless(headless);
        return driver = new ChromeDriver(options);

    }

    public void openPage(String url) {
        driver.get(url);
        driver.manage().window().maximize();
    }

    public WebElement findElement(By locator) {
        return driver.findElement(locator);
    }

    public List<WebElement> findElements(By locator) {
        return driver.findElements(locator);
    }

    public int sizeElements(By locator) {
        return driver.findElements(locator).size();
    }

    public Boolean isPresent(By locator) {
        return driver.findElements(locator).size() > 0;
    }

    public void write(String text, By locator) {
        driver.findElement(locator).sendKeys(text);
    }

    public String getText(By locator) {
        return driver.findElement(locator).getText();
    }

    public void click(By locator) {
        driver.findElement(locator).click();
    }

    public void clear(By locator) {
        driver.findElement(locator).clear();
    }

    public void submit(By locator) {
        driver.findElement(locator).submit();
    }

    public Boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public Boolean isEnabled(By locator) {
        try {
            return driver.findElement(locator).isEnabled();
        } catch (org.openqa.selenium.NoSuchElementException e) {
            return false;
        }
    }

    public void waitFor(ExpectedCondition<WebElement> condition, Integer timeOutInSeconds) {
        timeOutInSeconds = timeOutInSeconds != null ? timeOutInSeconds : 30;
        WebDriverWait wait = new WebDriverWait(driver, timeOutInSeconds);
        wait.until(condition);
    }

    public void waitForVisibilityOf(By locator, Integer... timeOutInSeconds) {
        int attemps = 0;
        while (attemps < 2) {
            try {
                waitFor(ExpectedConditions.visibilityOfElementLocated(locator), (timeOutInSeconds.length > 0 ? timeOutInSeconds[0] : null));
                break;
            } catch (StaleElementReferenceException e) {
            }
            attemps++;
        }
    }

    public void wait(By locator) {
        try {
            WebDriverWait load = new WebDriverWait(driver, 10);
            load.until(ExpectedConditions.presenceOfElementLocated(locator));
        } catch (Exception e) {
            System.out.println("Couln´t find the element(s) in the expected time");
        }
    }

    public void sleep(long time){
        try{
            Thread.sleep(time);
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void tearDownDriver(){
        driver.quit();
    }

    public List<String> searchForTextsInPage(Page page) {
        List<String> ListOfTextsOfPage= new ArrayList<>();
        By selectorTemp = By.xpath("//div[@id='pageContent']");
        if (isPresent(selectorTemp)) {
            ListOfTextsOfPage=getListOfContent(selectorTemp);
        }
        selectorTemp=By.xpath("//div[@class='finder-content']");
        if (isPresent(selectorTemp)) {
            ListOfTextsOfPage=getListOfContent(selectorTemp);
        }
        return ListOfTextsOfPage;
    }

    public List<String> getListOfContent(By selectorTemp) {
        wait(selectorTemp);

        js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
        sleep(3000);
        WebElement textsFromPage=findElement(selectorTemp);
        List<WebElement> listOfElements = findElements(selectorTemp);
        String stringTemp=textsFromPage.getText();
        String[] stringArrayTemp= stringTemp.split("\n");
        List<String> listWithTextOfElements = new ArrayList<>();
        for(int i=0;i<stringArrayTemp.length;i++)
            listWithTextOfElements.add(stringArrayTemp[i]);
        return listWithTextOfElements;
    }

    public void printListWithContent(List<String> listOfContent){
        System.out.println("Quantity Of Elements in List: "+listOfContent.size());
        for(String element:listOfContent)
            System.out.println(element.toString());
    }

    public String compareContentOfLists(List<String> listFirstPage, List<String> listSecondPage,String url1, String url2) {
        String resultReport="";
        int indexPage1=0, indexPage2, x = 0, ii = 0;
        int[] matches = new int[200];
        int[] noMatchesFirstPage = new int[300];
        List<String> listSecondPageDuplicate=new ArrayList<>(listSecondPage);
        boolean coincidenceFounded = false;
        for(String element: listFirstPage){
            indexPage2=0;
            coincidenceFounded=false;
            while(coincidenceFounded==false && indexPage2 <listSecondPage.size()){
                if(element.equals(listSecondPage.get(indexPage2))) {
                    coincidenceFounded = true;
                    listSecondPageDuplicate.set(indexPage2,"matched");
                }else
                    indexPage2++;
            }
            if(coincidenceFounded){
                matches[x] = indexPage1;
                matches[x + 1] = indexPage2;
                x = x + 2;
            }else if (indexPage2>=listSecondPage.size()){
                noMatchesFirstPage[ii] = indexPage1;
                ii++;
            }
            indexPage1++;
        }
        resultReport=resultReport+"Total of same texts in both pages: " +x/2;
        int y = 0;
        String stringTemp="";
        while (y < x) {
            System.out.println(listFirstPage.get(matches[y]) + " matches with: " + listSecondPage.get(matches[y + 1]));
            //resultReport=resultReport+"\n"+listFirstPage.get(matches[y]) + " ==matches with==> " + listSecondPage.get(matches[y + 1]);
            y = y + 2;
        }
        resultReport=resultReport+ "\n+----------------------------------------------------------------------------------------+\n";
        resultReport=resultReport+   "|                    Text founded in Production NOT PRESENT in Latest                    |";
        resultReport=resultReport+ "\n+----------------------------------------------------------------------------------------+\n";
        int spaces;
        for(y=0;y<ii;y++) {
            resultReport=resultReport+ listFirstPage.get(noMatchesFirstPage[y])+"\n";
        }
        resultReport=resultReport+   "+----------------------------------------------------------------------------------------+\n";
        resultReport=resultReport+   "|                    Text founded in Latest NOT PRESENT in Production                    |";
        resultReport=resultReport+ "\n+----------------------------------------------------------------------------------------+\n";
        for(String element:listSecondPageDuplicate){
            if(element!="matched") {
                    resultReport=resultReport + element+"\n";
            }
        }
        return resultReport;
    }

    public String addSpaces(String string, int spaces){
        for(int i=0;i<spaces;i++)
            string=string+" ";
        return string;
    }

    public void fillPageObjectWithElementsInformation(Page page) throws InterruptedException {
        /*page.setInputsTotal(searchForQuantityOfInputs());
        page.setInputsText(searchForTextInInputs());
        page.setButtonsTotal(searchForQuantityOfButtons());
        page.setButtonsText(searchForTextInButtons());
        page.setImagesTotal(searchForQuantityOfImages());
        page.setImagesSrc(searchForImagesSrc());
        page.setAnchors(searchForQuantityOfAnchors());
        page.setHrefAnchors(searchForHrefInAnchors());
        page.setH1Total(searchForQuantityOfH1());
        page.setH1Text(searchForTextH1());
        page.setH2Total(searchForQuantityOfH2());
        page.setH2Text(searchForTextH2());
        page.setH3Total(searchForQuantityOfH3());
        page.setH3Text(searchForTextH3());
        page.setH4Total(searchForQuantityOfH4());
        page.setH4Text(searchForTextH4());*/
        //page.setH5Total(searchForQuantityOfH5());
        //page.setH5Text(searchForTextH5());
        //page.setH6Total(searchForQuantityOfH6());
        //page.setH6Text(searchForTextH6());
        //page.setDivDescription(searchForTextInDivDescription());
    }

    public void takeScreenshot(){
        File src = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        try {
            FileUtils.copyFile(src, new File("screenshots//screenshot1.png"));
        }catch(Exception e) {System.out.print("Error copying screenshot file " +e);}
    }

    public List<String> getListUrlImages(){
        List<String> urlOfImages= new ArrayList<>();
        By selectorTemp = By.xpath("//div[@id='pageContent']//img");
        if (isPresent(selectorTemp)) {
            urlOfImages = getImagesResults(selectorTemp);
        }
        selectorTemp = By.xpath("//div[@class='finder-content']//picture//img");
        if (isPresent(selectorTemp)) {
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
        //List<String> sortedList = results.stream().sorted().collect(Collectors.toList());
        //return sortedList;
        return results;
    }

    public String compareImages(List<String> list1,List<String> list2){
        String results="";
        if(list1.size()==list2.size()) {
            results=results+"Same number of images"+compareImagesProcessor(list1,list2);

        }
        else{
            results="Different number of Images"+compareImagesProcessor(list1,list2);
        }
    return results;
    }

    public String compareImagesProcessor(List<String> list1, List<String> list2){
        int[] matchesList1=new int[50];
        int elementCounter=0, matchesList1Counter=0;
        String results="";
        for(String element:list1){
            for(int i=0;i<list2.size();i++){
                if(element.equals(list2.get(i))){
                    list2.set(i,"matched");
                    matchesList1[matchesList1Counter]=elementCounter;
                    matchesList1Counter++;
                    break;
                }
            }
            elementCounter++;
        }
        results=" with this ocurrencies";
        results=results+ "\n+----------------------------------------------------------------------------------------+\n";
        results=results+   "|                    Images founded in Production NOT PRESENT in Latest                    |";
        results=results+ "\n+----------------------------------------------------------------------------------------+\n";
        boolean flagFounded=false;
        for(int i=0;i<list1.size();i++){
            for(int ii=0;ii<matchesList1Counter;ii++) {
                if (i == matchesList1[ii])
                    flagFounded=true;
            }
            if(flagFounded==false)
                results=results+list1.get(i)+"\n";
            else
                flagFounded=false;
        }
        results=results+ "\n+----------------------------------------------------------------------------------------+\n";
        results=results+   "|                    Images founded in Latest NOT PRESENT in Production                    |";
        results=results+ "\n+----------------------------------------------------------------------------------------+\n";
        for(String element:list2){
            if(!element.equals("matched")){
                results=results+element+"\n";
            }
        }
        return results;
    }
}
