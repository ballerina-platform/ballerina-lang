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

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

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
            ParserConfigurationException, SAXException, TransformerException, URISyntaxException {

        //creating relevant browser webdriver
        //TODO make this generic for multiple browsers
        WebDriver driver = new FirefoxDriver();

        //opening base page - welcome page this case
        driver.get(TestConstants.SERVER_URL);

        //wait for the open button in welcome page
        WebDriverWait wait = new WebDriverWait(driver, 50);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.WELCOME_PAGE_OPEN_BUTTON_XPATH)));
        //once the open button available click it
        driver.findElement(By.xpath(TestConstants.WELCOME_PAGE_OPEN_BUTTON_XPATH)).click();

        //wait for the input box in the pop-up windows
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_INPUT_XPATH)));
        //fill the location of the ballerina file to be opened

        URL resource = BallerinaEditorUITest.class.getResource("BallerinaSourceFiles" + File.separator + fileName + ".bal");
        driver.findElement(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_INPUT_XPATH)).sendKeys(resource.getPath());

        //wait for the open button in the pop-up window
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_OPEN_XPATH)));
        //click opn button once it is available
        driver.findElement(By.xpath(TestConstants.FILE_OPEN_POPUP_LOCATION_OPEN_XPATH)).click();

        //wait for the SVG element where the diagram is rendered
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(TestConstants.SVG_XPATH)));
        //get DOM of the SVG element
        WebElement domElement = driver.findElement(By.xpath(TestConstants.SVG_XPATH));

        //Getting inner HTML of the SVG node
        String dom = TestUtils.preprocessDOMContent(domElement.getAttribute("innerHTML"));

        //TestUtils.fileWriter(dom, "helloWorldDOM.xml");

        URL resource1 = BallerinaEditorUITest.class.getResource("DOMFiles" + File.separator + fileName +"DOM.xml");
        //checking inner content of the DOM element
        assertEquals("Rendered diagram is not equal to the expected diagram",
                TestUtils.fileReader(resource1.getPath()), dom);
    }

    /*
    Data provider for running the test case for multiple ballerina files in parallel
     */
    @DataProvider(parallel = true)
    public Object[][] getData()
    {
        Object[][] data = new Object[2][1];
        data[0][0] ="helloWorld";
        data[1][0] ="echoService";
        return data;
    }
}