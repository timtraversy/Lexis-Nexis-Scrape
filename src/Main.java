import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main (String[] args) {
        String baseUrl = "http://proxygw.wrlc.org/login?url=http://www.lexisnexis.com/hottopics/lnacademic/";
        String username = "";
        String password = "";

        WebDriver driver;
        System.setProperty("webdriver.chrome.driver", "chromedriver");
        driver = new ChromeDriver();
        driver.get(baseUrl);

        // Use this is you're not using GWireless/are off campus
        driver.findElement(By.name("j_username")).clear();
        driver.findElement(By.name("j_username")).sendKeys(String.valueOf(username));
        driver.findElement(By.name("j_password")).clear();
        driver.findElement(By.name("j_password")).sendKeys(String.valueOf(password));
        driver.findElement(By.name("_eventId_proceed")).click();
        driver.findElement(By.name("_shib_idp_consentIds")).click();
        driver.findElement(By.name("_eventId_proceed")).click();
        driver.findElement(By.id("_shib_idp_globalConsent")).click();
        driver.findElement(By.name("_eventId_proceed")).click();

        driver.switchTo().frame("mainFrame");
        driver.findElement(By.linkText("Search by Subject or Topic")).click();
        driver.findElement(By.linkText("Federal and State Cases")).click();
        driver.findElement(By.id("lblAdvancDwn")).click();

        List<WebElement> columnCountList = driver.findElements(By.xpath("//*[@id=\"advanceDiv_1\"]/table/tbody/tr[3]/td/div/div[8]/div/table/tbody/tr/td[1]/ol"));
        int columnCount = columnCountList.size();
        WebDriverWait wait = new WebDriverWait(driver, 10);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.id("txtFrmDate")));

        element.clear();
        // 01/01/1948
        char[] startDateChars = "01/01/1983".toCharArray();
        for (char character : startDateChars) {
            element.sendKeys("" + character);
        }

        // 12/31/1999
        // 12/31/1975
        //1989
        driver.findElement(By.id("txtToDate")).clear();
        char[] endDateChars = "12/31/1989".toCharArray();
        for (char character : endDateChars) {
            driver.findElement(By.id("txtToDate")).sendKeys("" + character);
        }


        driver.findElement(By.id("txtSegTerms")).clear();
        driver.findElement(By.id("txtSegTerms")).sendKeys("(statute AND unconstitution!) OR (legislation AND unconstitution!)");
        driver.findElement(By.id("OkButt")).click();

        int[] columnCounts = {13, 13, 13, 12};
        int[][] undone = {{3,10}};
        boolean firstRun = true;

        for (int[] und : undone) {
            int column = und[0];
            int row = und[1];


//        for (int column = 4; column < 5; ++column) {
//            for (int row = 1; row <= columnCounts[column - 1]; ++row) {
//                // little hack to jump to the state you want
//                if (firstRun && column == 4) {
//                    row += 9;
//                    firstRun = false;
//                }
                driver.findElement(By.id("terms")).clear();
                driver.findElement(By.id("lblAdvancDwn")).click();

                for (int columnDelete = 1; columnDelete < 5; ++columnDelete) {
                    for (int rowDelete = 1; rowDelete <= columnCounts[columnDelete - 1]; ++rowDelete) {
                        WebElement box = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"advanceDiv_1\"]/table/tbody/tr[3]/td/div/div[8]/div/table/tbody/tr/td[" + columnDelete + "]/ol/li[" + rowDelete + "]/input")));
                        if (box.isSelected()) {
                            box.click();
                        }
                    }
                }

                WebElement element1 = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@id=\"advanceDiv_1\"]/table/tbody/tr[3]/td/div/div[8]/div/table/tbody/tr/td[" + column + "]/ol/li[" + row + "]/input")));
                element1.click();

                driver.findElement(By.id("txtSegTerms")).clear();
                driver.findElement(By.id("txtSegTerms")).sendKeys("(statute AND unconstitution!) OR (legislation AND unconstitution!)");

                driver.findElement(By.id("OkButt")).click();
                driver.findElement(By.id("srchButt")).click();

                driver.switchTo().defaultContent();
                driver.switchTo().frame("mainFrame");
                driver.switchTo().frame(1);
                String countString = driver.findElement(By.xpath("//*[@id=\"results_docview_DocumentForm\"]/table/tbody/tr[1]/td/table/tbody/tr/td[2]/table/tbody/tr[2]/td[1]/table/tbody/tr[2]/td[4]/table/tbody/tr[2]/td/table/tbody/tr/td/span/strong[2]")).getText();
                int count = Integer.parseInt(countString);

                Scanner scanner = new Scanner(System.in);
                System.out.println("Continue? Enter any text.");
                scanner.next();

                int startRange = 1;
                int endRange = 200;

                while (endRange < count) {
                    driver.switchTo().defaultContent();
                    driver.switchTo().frame("mainFrame");
                    driver.switchTo().frame(1);
                    driver.findElement(By.cssSelector("img[alt=\"Download Documents\"]")).click();
                    Set handles = driver.getWindowHandles();
                    String subWindowHandler = "";
                    Iterator<String> iterator = handles.iterator();
                    while (iterator.hasNext()) {
                        subWindowHandler = iterator.next();
                    }
                    driver.switchTo().window(subWindowHandler);
                    driver.findElement(By.id("sel")).click();
                    driver.findElement(By.id("rangetextbox")).clear();
                    driver.findElement(By.id("rangetextbox")).sendKeys(startRange + "-" + endRange);
                    new Select(driver.findElement(By.id("delView"))).selectByVisibleText("Custom...");
                    if (startRange == 1) {
                        if (driver.findElement(By.id("termBold")).isSelected()) {
                            driver.findElement(By.id("termBold")).click();
                        }
                        new Select(driver.findElement(By.id("delFmt"))).selectByVisibleText("HTML");
                        driver.findElement(By.linkText("Modify...")).click();
                        driver.findElement(By.id("chk11")).click();
                        // needs to be chk15 for NY, SD, ND
                        driver.findElement(By.id("chk14")).click();
                        System.out.println("Continue? Enter any text.");
                        scanner.next();
                        driver.findElement(By.xpath("(//input[@name=''])[2]")).click();
                    }
                    driver.findElement(By.cssSelector("img[alt=\"Download\"]")).click();
                    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
                    driver.findElement(By.xpath("//*[@id=\"center\"]/center/p/a")).click();
                    driver.findElement(By.cssSelector("img[alt=\"Close Window\"]")).click();
                    handles = driver.getWindowHandles();
                    subWindowHandler = "";
                    iterator = handles.iterator();
                    while (iterator.hasNext()) {
                        subWindowHandler = iterator.next();
                    }
                    driver.switchTo().window(subWindowHandler);
                    startRange += 200;
                    endRange += 200;
                }

                if (endRange != count) {
                    endRange = count;
                    driver.switchTo().defaultContent();
                    driver.switchTo().frame("mainFrame");
                    driver.switchTo().frame(1);
                    driver.findElement(By.cssSelector("img[alt=\"Download Documents\"]")).click();
                    Set handles = driver.getWindowHandles();
                    String subWindowHandler = "";
                    Iterator<String> iterator = handles.iterator();
                    while (iterator.hasNext()) {
                        subWindowHandler = iterator.next();
                    }
                    driver.switchTo().window(subWindowHandler);
                    new Select(driver.findElement(By.id("delFmt"))).selectByVisibleText("HTML");
                    driver.findElement(By.id("sel")).click();
                    driver.findElement(By.id("rangetextbox")).clear();
                    driver.findElement(By.id("rangetextbox")).sendKeys(startRange + "-" + endRange);
                    new Select(driver.findElement(By.id("delView"))).selectByVisibleText("Custom...");
                    driver.findElement(By.cssSelector("img[alt=\"Download\"]")).click();
                    driver.manage().timeouts().implicitlyWait(20, TimeUnit.SECONDS);
                    driver.findElement(By.xpath("//*[@id=\"center\"]/center/p/a")).click();
                    driver.findElement(By.cssSelector("img[alt=\"Close Window\"]")).click();
                    handles = driver.getWindowHandles();
                    subWindowHandler = "";
                    iterator = handles.iterator();
                    while (iterator.hasNext()) {
                        subWindowHandler = iterator.next();
                    }
                    driver.switchTo().window(subWindowHandler);
                }
                driver.findElement(By.linkText("Edit Search")).click();
                driver.switchTo().frame("mainFrame");

//            }
        }
    }
}