import com.github.javafaker.Faker;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import java.util.List;

public class SwoopMovieTest {

    private static final String CINEMA_NAME = "კავეა ისთ ფოინთი";

    WebDriver driver;

    @BeforeTest
    @Parameters("browser")
    public void setup(String browser) throws Exception {
        if(browser.equalsIgnoreCase("chrome")){
            WebDriverManager.chromedriver().setup();
            driver = new ChromeDriver();
            driver.manage().window().maximize();
        }else if(browser.equalsIgnoreCase("edge")){
            WebDriverManager.edgedriver().setup();
            driver = new EdgeDriver();
            driver.manage().window().maximize();
        }else {
            throw new Exception("Browser is not correct");
        }
    }

    @AfterTest
    public void tearDown(){
        driver.close();
    }

    @Test
    public void movieTest() throws InterruptedException {
        driver.get("https://www.swoop.ge/");
        WebElement movieButton = driver.findElement(By.xpath("//label[contains(text(),\"კინო\")]"));
        movieButton.click();
        List<WebElement> movieContainer = driver.findElements(By.xpath("//div[@class=\"container cinema_container\"]/div[@class= 'movies-deal']"));
        WebElement firstMovie = movieContainer.get(0);

        Actions actions = new Actions(driver);
        actions.moveToElement(firstMovie).perform();

        String movieTitle = firstMovie.findElement(By.xpath(".//div[@class=\"movie-name\"]")).getText();
        firstMovie.findElement(By.xpath("//div[@class=\"info-cinema-ticket\"]/p[text()=\"ყიდვა\"]")).click();

        JavascriptExecutor js = (JavascriptExecutor) driver;
        By caveaEastPointTabLoc = By.xpath("//ul[contains(@class, \"cinema-tabs\")]//a[text()='"+CINEMA_NAME+"']");
        WebElement caveaEastPointTab = driver.findElement(caveaEastPointTabLoc);
        js.executeScript("arguments[0].scrollIntoView(); window.scrollBy(0, -50);", caveaEastPointTab);
        caveaEastPointTab.click();

        String selectedPanel = "//div[contains(@class,\"all-cinemas\")]/div[@role=\"tabpanel\"][@aria-expanded='true']";
        List<WebElement> seanseDetailsList = driver.findElements(By.xpath(selectedPanel + "//div[contains(@class,\"seanse-details\")][@aria-expanded='true']"));
        int count = 0;
        for (WebElement seanseDetails :
                seanseDetailsList) {
            WebElement locationColumn = seanseDetails.findElement(By.xpath(".//p[2]"));
            if(locationColumn.getText().equalsIgnoreCase(CINEMA_NAME)){
                count++;
            }
        }
        Assert.assertEquals(count,seanseDetailsList.size());

        List<WebElement> dateButtons = driver.findElements(By.xpath(selectedPanel + "//div[contains(@class, \"calendar-tabs\")]/ul/li/a"));
        WebElement lastDateButton = dateButtons.get(dateButtons.size()-1);
        String date = lastDateButton.getText().substring(0,2);
        lastDateButton.click();

        List<WebElement> seanseDetailByDate = driver.findElements(By.xpath(selectedPanel + "//div[contains(@class,\"seanse-details\")][@aria-expanded='true']"));
        WebElement lastSeanseByDate = seanseDetailByDate.get(seanseDetailByDate.size()-1);
        String time = lastSeanseByDate.findElement(By.xpath(".//p[1]")).getText();
        lastSeanseByDate.click();

        By contentHeadersLocation = By.xpath("//div[@class=\"content-header\"]/p");
        WebDriverWait wait= new WebDriverWait(driver, 10);
        wait.until(ExpectedConditions.visibilityOfElementLocated(contentHeadersLocation));
        List<WebElement> contentHeaders = driver.findElements(contentHeadersLocation);
        Assert.assertEquals(contentHeaders.get(0).getText(), movieTitle);
        Assert.assertEquals(contentHeaders.get(1).getText(), CINEMA_NAME);
        String fullDateTime = contentHeaders.get(2).getText();
        Assert.assertEquals(fullDateTime.substring(0,2), date);
        Assert.assertEquals(fullDateTime.substring(fullDateTime.length()-5), time);

        List<WebElement> freeSeats = driver.findElements(By.xpath("//div[@class=\"seat free\"]"));
        freeSeats.get(0).click();

        By registrationButtonLocation = By.xpath("//ul[contains(@class,\"login-register\")]//p[@class=\"register\"]");
        wait.until(ExpectedConditions.elementToBeClickable(registrationButtonLocation));
        WebElement registrationButton = driver.findElement(registrationButtonLocation);
        registrationButton.click();
        driver.findElement(By.xpath("//ul[contains(@class,\"profile-tabs\")]//p[text()=\"იურიდიული პირი\"]")).click();

        Select formSelect = new Select(driver.findElement(By.xpath("//select[@id='lLegalForm']")));
        formSelect.selectByIndex(1);

        Faker faker = new Faker();
        driver.findElement(By.id("lName")).sendKeys(faker.company().name());
        driver.findElement(By.id("lTaxCode")).sendKeys(faker.idNumber().valid());
        driver.findElement(By.id("lCity")).sendKeys(faker.address().city());
        driver.findElement(By.id("lPostalCode")).sendKeys(faker.address().zipCode());
        driver.findElement(By.id("lContactPersonEmail")).sendKeys(faker.internet().emailAddress());
        // "პაროლის და ჩექბოქსის ლოგიკა დატოვებული მაქვს კომენტარად.
        // String fakerPassword = faker.internet().password();
        // driver.findElement(By.id("lContactPersonPassword")).sendKeys(fakerPassword);
        // driver.findElement(By.id("lContactPersonConfirmPassword")).sendKeys(fakerPassword);
        driver.findElement(By.id("lContactPersonName")).sendKeys(faker.name().fullName());
        driver.findElement(By.id("lContactPersonPhone")).sendKeys(faker.phoneNumber().phoneNumber());
        // driver.findElement(By.id("IsLegalAgreedTerms")).click();

        driver.findElement(By.xpath("//a[@onclick=\"SubmitLegalForm()\"]")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("legalInfoMassage")));
        String illegalMessage = driver.findElement(By.id("legalInfoMassage")).getText();
        Assert.assertEquals(illegalMessage,"რეგისტრაციის დროს დაფიქსირდა შეცდომა!");


    }
}
