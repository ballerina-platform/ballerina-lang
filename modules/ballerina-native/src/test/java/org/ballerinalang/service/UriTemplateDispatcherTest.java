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

import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.services.dispatchers.http.Constants;
import org.ballerinalang.testutils.EnvironmentInitializer;
import org.ballerinalang.testutils.MessageUtils;
import org.ballerinalang.testutils.Services;
import org.ballerinalang.util.codegen.ProgramFile;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * Test class for Uri Template based resource dispatchers.
 * Ex: /products/{productId}?regID={regID}
 */
public class UriTemplateDispatcherTest {

    private ProgramFile application;

    @BeforeClass()
    public void setup() {
        application = EnvironmentInitializer.setupProgramFile("lang/service/uritemplate/uri-template.bal");
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
        Assert.assertEquals(bJson.value().get(xOrderIdHeadeName).asText(), xOrderIdHeadeValue
                , "Header value mismatched");
        Assert.assertEquals(bJson.value().get("ProductID").asText(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(bJson.value().get("RegID").asText(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test resource dispatchers with invalid URL. /products/{productId}/{regId}",
            dataProvider = "inValidUrl")
    public void testInValidUrlTemplateDispatching(String path) {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        final String xOrderIdHeadeName = "X-ORDER-ID";
        final String xOrderIdHeadeValue = "ORD12345";
        cMsg.setHeader(xOrderIdHeadeName, xOrderIdHeadeValue);
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertEquals(
                response.getProperty(Constants.HTTP_STATUS_CODE), 404, "Response code mismatch");
        Assert.assertNotNull(response.getMessageDataSource(), "Message body null");
        //checking the exception message
        String errorMessage = response.getMessageDataSource().getMessageAsString();
        Assert.assertTrue(errorMessage.contains("no matching resource found for path"),
                "Expected error not found.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products/{productId}",
            dataProvider = "validUrlWithQueryParam")
    public void testValidUrlTemplateWithQueryParamDispatching(String path) {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("Template").asText(), "T4"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(bJson.value().get("ProductID").asText(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(bJson.value().get("RegID").asText(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products2/{productId}/{regId}/item")
    public void testValidUrlTemplate2Dispatching() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products2/PID125/RID125/item", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T2","ProductID":"PID125","RegID":"RID125"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("Template").asText(), "T2"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(bJson.value().get("ProductID").asText(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(bJson.value().get("RegID").asText(), "RID125"
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
        Assert.assertEquals(bJson.value().get("Template").asText(), "T3"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(bJson.value().get("ProductID").asText(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(bJson.value().get("RegID").asText(), "RID125"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test accessing the variables parsed with URL. /products5/{productId}/reg")
    public void testValidUrlTemplate5Dispatching() {
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(
                "/ecommerceservice/products5/PID125/reg?regID=RID125&para1=value1", "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"Template":"T5","ProductID":"PID125","RegID":"RID125"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("Template").asText(), "T5"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(bJson.value().get("ProductID").asText(), "PID125"
                , "ProductID variable not set properly.");
        Assert.assertEquals(bJson.value().get("RegID").asText(), "RID125"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test dispatching with URL. /products")
    public void testUrlTemplateWithMultipleQueryParamDispatching() {
        String path = "/ecommerceservice/products?prodId=PID123&regID=RID123";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("Template").asText(), "T6"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(bJson.value().get("ProductID").asText(), "PID123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(bJson.value().get("RegID").asText(), "RID123"
                , "RegID variable not set properly.");
    }

    @Test(description = "Test dispatching with URL. /products?productId={productId}&regID={regID} "
            + "Ex: products?productId=PID%20123&regID=RID%201123")
    public void testUrlTemplateWithMultipleQueryParamWithURIEncodeCharacterDispatching() {
        String path = "/ecommerceservice/products?prodId=PID%20123&regID=RID%20123";
        CarbonMessage cMsg = MessageUtils.generateHTTPMessage(path, "GET");
        CarbonMessage response = Services.invoke(cMsg);
        Assert.assertNotNull(response, "Response message not found");
        //Expected Json message : {"X-ORDER-ID":"ORD12345","ProductID":"PID123","RegID":"RID123"}
        BJSON bJson = ((BJSON) response.getMessageDataSource());
        Assert.assertEquals(bJson.value().get("Template").asText(), "T6"
                , "Resource dispatched to wrong template");
        Assert.assertEquals(bJson.value().get("ProductID").asText(), "PID 123"
                , "ProductID variable not set properly.");
        Assert.assertEquals(bJson.value().get("RegID").asText(), "RID 123"
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
                 {"/ecommerceservice/prod/PID123/RID123"}
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

    @AfterClass
    public void tearDown() {
//        EnvironmentInitializer.cleanup(application);
    }
}
