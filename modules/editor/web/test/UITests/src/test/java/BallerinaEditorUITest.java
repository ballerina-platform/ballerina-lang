/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import org.openqa.selenium.*;
import org.openqa.selenium.firefox.FirefoxDriver;

import java.io.IOException;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

public class BallerinaEditorUITest {


    @BeforeTest
    public void openBrowser() {}

    @AfterTest
    public void saveScreenshotAndCloseBrowser() throws IOException {}

    @Test(dataProvider="getData")
    public void openBallerinaFile(String fileName) throws IOException, InterruptedException,
            ParserConfigurationException, SAXException, TransformerException {

        //opening base URL
        WebDriver driver = new FirefoxDriver();
        driver.manage().deleteAllCookies();
        driver.get(TestConstants.SERVER_URL);

        Thread.sleep(50000);
        WebDriverWait wait = new WebDriverWait(driver, 50);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.WELCOME_PAGE_OPEN_BUTTON_XPATH)));
        driver.findElement(By.xpath(TestConstants.WELCOME_PAGE_OPEN_BUTTON_XPATH)).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_INPUT_XPATH)));
        driver.findElement(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_INPUT_XPATH)).sendKeys("/home/malintha/ballerina/samples/getting_started/helloWorld/helloWorld.bal");

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_OPEN_XPATH)));
        driver.findElement(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_OPEN_XPATH)).click();


        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.SVG_XPATH)));
        WebElement domElement = driver.findElement(By.xpath(TestConstants.SVG_XPATH));

        String dom = TestUtils.preprocessDOMContent(domElement.getAttribute("innerHTML"));

        //TestUtils.fileWriter(dom, "helloWorldDOM.xml");
        assertEquals("Rendered diagram is not equal to the expected diagram",
                TestUtils.fileReader("/home/malintha/ScreenCap/src/test/java/resources/DOMFiles/helloWorldDOM.xml"), dom);
    }

    @DataProvider(parallel = true)
    public Object[][] getData()
    {

        Object[][] data = new Object[2][1];

        data[0][0] ="helloWorld";
        data[1][0] ="helloWorld1";

        return data;
    }
}