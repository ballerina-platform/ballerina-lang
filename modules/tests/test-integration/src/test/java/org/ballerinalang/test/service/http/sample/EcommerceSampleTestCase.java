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
package org.ballerinalang.test.service.http.sample;

import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.util.HttpClientRequest;
import org.ballerinalang.test.util.HttpResponse;
import org.ballerinalang.test.util.TestConstant;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Testing the E-Commerce sample located in
 * ballerina_home/samples/restfulService/ecommerceService.bal.
 */
public class EcommerceSampleTestCase extends IntegrationTestCase {

    @Test(description = "Test resource GET products in E-Commerce sample")
    public void testGetProducts() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(getServiceURLHttp("ecommerceservice/products/123001"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE), TestConstant.CONTENT_TYPE_JSON,
                "Content-Type mismatched");
        Assert.assertEquals(response.getData(),
                "{\"Product\":{\"ID\":\"123001\",\"Name\":\"ABC_2\",\"Description\":\"Sample product.\"}}",
                "Message content mismatched");
    }

    @Test(description = "Test resource GET orders in E-Commerce sample")
    public void testGetOrders() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(getServiceURLHttp("ecommerceservice/orders"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "{\"Order\":{\"ID\":\"111999\",\"Name\":\"ABC123\"," +
                                                "\"Description\":\"Sample order.\"}}"
                , "Message content mismatched");
    }

    @Test(description = "Test resource GET customers in E-Commerce sample")
    public void testGetCustomers() throws IOException {
        HttpResponse response = HttpClientRequest.doGet(getServiceURLHttp("ecommerceservice/customers"));
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "{\"Customer\":{\"ID\":\"987654\",\"Name\":\"ABC PQR\"," +
                                                "\"Description\":\"Sample Customer.\"}}"
                , "Message content mismatched");
    }

    @Test(description = "Test resource POST orders in E-Commerce sample")
    public void testPostOrder() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp("ecommerceservice/orders")
                , "{\"Order\":{\"ID\":\"111222\",\"Name\":\"XYZ123\"}}", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "{\"Status\":\"Order is successfully added.\"}"
                , "Message content mismatched");
    }

    @Test(description = "Test resource POST products in E-Commerce sample")
    public void testPostProduct() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp("ecommerceservice/products")
                , "{\"Product\":{\"ID\":\"123345\",\"Name\":\"PQR\"}}", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "{\"Status\":\"Product is successfully added.\"}"
                , "Message content mismatched");
    }

    @Test(description = "Test resource POST customers in E-Commerce sample")
    public void testPostCustomers() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put(TestConstant.HEADER_CONTENT_TYPE, TestConstant.CONTENT_TYPE_JSON);
        HttpResponse response = HttpClientRequest.doPost(getServiceURLHttp("ecommerceservice/customers")
                , "{\"Customer\":{\"ID\":\"97453\",\"Name\":\"ABC XYZ\"}}", headers);
        Assert.assertEquals(response.getResponseCode(), 200, "Response code mismatched");
        Assert.assertEquals(response.getHeaders().get(TestConstant.HEADER_CONTENT_TYPE)
                , TestConstant.CONTENT_TYPE_JSON, "Content-Type mismatched");
        Assert.assertEquals(response.getData(), "{\"Status\":\"Customer is successfully added.\"}"
                , "Message content mismatched");
    }
}
