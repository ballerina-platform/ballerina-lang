/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.service;

//import com.google.gson.JsonObject;
//import com.google.gson.JsonParser;
//import org.testng.Assert;
//import org.testng.annotations.AfterClass;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.DataProvider;
//import org.testng.annotations.Test;
//import org.ballerinalang.core.EnvironmentInitializer;
//import org.ballerinalang.core.exception.BallerinaException;
//import org.ballerinalang.model.Application;
//import org.ballerinalang.model.values.BJSON;
//import org.ballerinalang.core.utils.MessageUtils;
//import org.wso2.ballerina.model.util.Services;
//import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Test class for Uri Template based resource dispatchers.
 * Ex: /products/{productId}?regID={regID}
 */
public class UriTemplateDispatcherTest {

    /*private Application application;

    @BeforeClass()
    public void setup() {
        application = EnvironmentInitializer.setup("model/service/uri-template.bal");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products/{productId}/{regId}",
            dataProvider = "validUrl")
    public void testValidUrlTemplateDispatching(String path) {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        final String xOrderIdHeadeName = "X-ORDER-ID";
        final String xOrderIdHeadeValue = "ORD12345";
        cMsg.setHeader(xOrderIdHeadeName, xOrderIdHeadeValue);
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        JsonObject jsonResponse = new JsonParser().parse(bJson.stringValue()).getAsJsonObject();
        Assert.assertEquals(jsonResponse.get(xOrderIdHeadeName).getAsString(), xOrderIdHeadeValue
                , "Header value mismatched");
        Assert.assertEquals(jsonResponse.get("ProductID").getAsString(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(jsonResponse.get("RegID").getAsString(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test resource dispatchers with invalid URL. /products/{productId}/{regId}",
            dataProvider = "inValidUrl", expectedExceptions = BallerinaException.class
            , expectedExceptionsMessageRegExp = ".* no resource found to handle the request to Service .*")
    public void testInValidUrlTemplateDispatching(String path) {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        final String xOrderIdHeadeName = "X-ORDER-ID";
        final String xOrderIdHeadeValue = "ORD12345";
        cMsg.setHeader(xOrderIdHeadeName, xOrderIdHeadeValue);
        Services.invoke(cMsg);
    }

    @Test(description = "Test accessing the variables parsed with URL. /products/{productId}?regID={regID}",
            dataProvider = "validUrlWithQueryParam")
    public void testValidUrlTemplateWithQueryParamDispatching(String path) {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        JsonObject jsonResponse = new JsonParser().parse(bJson.stringValue()).getAsJsonObject();
        Assert.assertEquals(jsonResponse.get("Template").getAsString(), "T4"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(jsonResponse.get("ProductID").getAsString(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(jsonResponse.get("RegID").getAsString(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test resource dispatchers with invalid URL. /products/{productId}?regID={regID}",
            dataProvider = "inValidUrlWithQueryParam", expectedExceptions = BallerinaException.class
            , expectedExceptionsMessageRegExp = ".* no resource found to handle the request to Service .*")
    public void testInValidUrlTemplateWithQueryParamDispatching(String path) {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        Services.invoke(cMsg);
    }

    @Test(description = "Test accessing the variables parsed with URL. /products2/{productId}/{regId}/item")
    public void testValidUrlTemplate2Dispatching() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products2/PID125/RID125/item", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T2","ProductID":"PID125","RegID":"RID125"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        JsonObject jsonResponse = new JsonParser().parse(bJson.stringValue()).getAsJsonObject();
        Assert.assertEquals(jsonResponse.get("Template").getAsString(), "T2"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(jsonResponse.get("ProductID").getAsString(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(jsonResponse.get("RegID").getAsString(), "RID125"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products3/{productId}/{regId}*//*")
    public void testValidUrlTemplate3Dispatching() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products3/PID125/RID125/xyz?para1=value1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T3","ProductID":"PID125","RegID":"RID125"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        JsonObject jsonResponse = new JsonParser().parse(bJson.stringValue()).getAsJsonObject();
        Assert.assertEquals(jsonResponse.get("Template").getAsString(), "T3"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(jsonResponse.get("ProductID").getAsString(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(jsonResponse.get("RegID").getAsString(), "RID125"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products5/{productId}/reg?regID={regID}*")
    public void testValidUrlTemplate5Dispatching() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products5/PID125/reg?regID=RID125&para1=value1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T5","ProductID":"PID125","RegID":"RID125"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        JsonObject jsonResponse = new JsonParser().parse(bJson.stringValue()).getAsJsonObject();
        Assert.assertEquals(jsonResponse.get("Template").getAsString(), "T5"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(jsonResponse.get("ProductID").getAsString(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(jsonResponse.get("RegID").getAsString(), "RID125"
                , "RegID variable not set properly.");
    }


    @DataProvider(name = "validUrl")
    public static Object[][] validUrl() {
        return new Object[][]{
                {"/ecommerceservice/products/PID123/RID123"}
                , {"/ecommerceservice/products/PID123/RID123/"}
        };
    }

    @DataProvider(name = "inValidUrl")
    public static Object[][] inValidUrl() {
        return new Object[][]{
                {"/ecommerceservice/products"}
                , {"/ecommerceservice/prod/PID123/RID123"}
                , {"/ecommerceservice/products/PID123"}
                , {"/ecommerceservice/products/PID123/"}
                , {"/ecommerceservice/products/PID123/RID123?"}
                , {"/ecommerceservice/products/PID123/RID123?param=value"}
                , {"/ecommerceservice/products/PID123/RID123?param1=value1&param2=value2"}
                , {"/ecommerceservice/products/PID123/RID123/ID"}
                , {"/ecommerceservice/products/PID123/RID123/ID?param=value"}
                , {"/ecommerceservice/products/PID123/RID123/ID?param1=value1&param2=value2"}
        };
    }

    @DataProvider(name = "validUrlWithQueryParam")
    public static Object[][] validUrlWithQueryParam() {
        return new Object[][]{
                {"/ecommerceservice/products/PID123?regID=RID123"}
        };
    }

    @DataProvider(name = "inValidUrlWithQueryParam")
    public static Object[][] inValidUrlWithQueryParam() {
        return new Object[][]{
                {"/ecommerceservice/products"}
                , {"/ecommerceservice/products?regId=123"}
                , {"/ecommerceservice/products/PID123"}
                , {"/ecommerceservice/products/PID123/"}
                , {"/ecommerceservice/products/PID123/?regID=RID123"}
                , {"/ecommerceservice/products/PID123?regID=RID123&"}
                , {"/ecommerceservice/products/PID123?regID=RID123&param=value"}
                , {"/ecommerceservice/products/PID123/?regID=RID123&param=value"}
                , {"/ecommerceservice/products/PID123?regID=RID123&param1=value1&param2=value2"}
                , {"/ecommerceservice/products/PID123/RID123/ID"}
                , {"/ecommerceservice/products/PID123?param=value"}
                , {"/ecommerceservice/products/PID123/?param=value"}
                , {"/ecommerceservice/products/PID123?regId=value1&param2=value2"}
        };
    }

    @AfterClass
    public void tearDown() {
        EnvironmentInitializer.cleanup(application);
    }*/
}
