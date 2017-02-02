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


    @Test(dataProvider="getData")
    public void openBallerinaFile(String fileName) throws IOException, InterruptedException,
            ParserConfigurationException, SAXException, TransformerException, URISyntaxException {

        //creating relevant browser webdriver
        //TODO make this generic for multiple browsers
        WebDriver driver = new FirefoxDriver();
        //opening base page - welcome page this case
        driver.get(TestConstants.SERVER_URL);
        //once the open button available click it
        waitAndGetElementByXpath(driver, TestConstants.WELCOME_PAGE_OPEN_BUTTON_XPATH).click();
        //fill the location of the ballerina file to be opened
        URL BallerinaResourceLocation = BallerinaEditorUITest.class.getResource(
                TestConstants.BALLERINA_RESOURCE_FOLDER + File.separator + fileName + ".bal");
        waitAndGetElementByXpath(driver, TestConstants.FILE_OPEN_POPUP_LOCATION_INPUT_XPATH).
                sendKeys(BallerinaResourceLocation.getPath());
        //wait for the open button in the pop-up window
        waitAndGetElementByXpath(driver, TestConstants.FILE_OPEN_POPUP_LOCATION_OPEN_XPATH).click();
        //wait for the SVG element where the diagram is rendered
        WebElement domElement = waitAndGetElementByXpath(driver, TestConstants.SVG_XPATH);
        //Getting inner HTML of the SVG node
        String dom = TestUtils.preprocessDOMContent(domElement.getAttribute("innerHTML"));
        //TODO Add mechanism to generate DOM files
        //TestUtils.fileWriter(dom, fileName + "DOM.xml");
        URL DOMResourceLocation = BallerinaEditorUITest.class.getResource(TestConstants.DOM_RESOURCE_FOLDER +
                File.separator + fileName +"DOM.xml");
        //destroying browser instance
        driver.quit();
        //checking inner content of the DOM element
        assertEquals("Rendered diagram of " + fileName + "is not equal to the expected diagram",
                TestUtils.fileReader(DOMResourceLocation.getPath()), dom);
    }

    /*
    Data provider for running the test case for multiple ballerina files
     */
    @DataProvider()
    public Object[][] getData()
    {
        Object[][] data = new Object[9][1];
        data[0][0] ="helloWorld";
        data[1][0] ="echoService";
        data[2][0] ="passthroughService";
        data[3][0] ="ecommerceService";
        data[4][0] ="helloWorldService";
        data[5][0] ="ATMLocatorService";
        data[6][0] ="routingServices";
        data[7][0] ="tweetMediumFeed";
        data[8][0] ="tweetOpenPR";
        return data;
    }

    /*
    Wait for visibility of an element and provide that element
     */
    private WebElement waitAndGetElementByXpath(WebDriver driver, String xpath){
        WebDriverWait wait = new WebDriverWait(driver, 50);
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        return driver.findElement(By.xpath(xpath));
    }
}