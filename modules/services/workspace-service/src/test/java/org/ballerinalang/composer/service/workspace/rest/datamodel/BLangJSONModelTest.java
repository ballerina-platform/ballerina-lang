/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/

package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.formparam.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Scanner;
import javax.ws.rs.HttpMethod;

public class BLangJSONModelTest {

    private MicroservicesRunner microservicesRunner;
    private JsonParser jsonParse = new JsonParser();

    @BeforeClass
    public void setup() throws Exception {
        microservicesRunner = new MicroservicesRunner(9090);
        microservicesRunner.deploy(new BLangFileRestService()).start();
    }


    @Test
    public void testBLangJSONModelServiceUsingPost() throws IOException, URISyntaxException {
        File file = new File(getClass().getClassLoader().getResource("samples/service/ServiceSample.bal")
                .getFile());
        HttpURLConnection urlConn = request("/ballerina/model/content", HttpMethod.POST);
        urlConn.setRequestProperty("Content-Type", "application/json");
        OutputStream outputStream = urlConn.getOutputStream();
        String content = new Scanner(file).useDelimiter("\\Z").next();;
        JsonObject json = new JsonObject();
        json.addProperty("content", content);
        outputStream.write(json.toString().getBytes());
        outputStream.flush();
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);

        File fileExpected = new File(getClass().getClassLoader().getResource("samples/service/expected.json")
                                       .getFile());
        String expectedJsonStr = new Scanner(fileExpected).useDelimiter("\\Z").next();
        Assert.assertEquals(jsonParse.parse(response), jsonParse.parse(expectedJsonStr));
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
    }

    // TODO: Temporarily removing the test case
//    @Test
//    public void testBLangJSONModelTransformer() throws IOException, URISyntaxException {
//        File file = new File(getClass().getClassLoader().getResource("samples/transformStmt/transform-stmt.bal")
//                                       .getFile());
//        HttpURLConnection urlConn = request("/ballerina/model/content", HttpMethod.POST);
//        urlConn.setRequestProperty("Content-Type", "application/json");
//        OutputStream outputStream = urlConn.getOutputStream();
//        String content = new Scanner(file).useDelimiter("\\Z").next();;
//        JsonObject json = new JsonObject();
//        json.addProperty("content", content);
//        outputStream.write(json.toString().getBytes());
//        outputStream.flush();
//        InputStream inputStream = urlConn.getInputStream();
//        String response = StreamUtil.asString(inputStream);
//
//        File fileExpected = new File(
//                getClass().getClassLoader().getResource("samples/transformStmt/transform-stmt-expected.json")
//                          .getFile());
//        String expectedJsonStr = new Scanner(fileExpected).useDelimiter("\\Z").next();
//
//
//        Assert.assertEquals(jsonParse.parse(response), jsonParse.parse(expectedJsonStr));
//        IOUtils.closeQuietly(inputStream);
//        urlConn.disconnect();
//    }

    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }

    private HttpURLConnection request(String path, String method) throws IOException, URISyntaxException {
        URI baseURI = new URI("http://localhost:9090");
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        return urlConn;
    }

}
