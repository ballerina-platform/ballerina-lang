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

package org.wso2.ballerina.core.service.test;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.apache.commons.io.IOUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.service.BLangFileRestService;
import org.wso2.msf4j.MicroservicesRunner;
import org.wso2.msf4j.formparam.util.StreamUtil;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.ws.rs.HttpMethod;

public class BLangJSONModelTest {

    private MicroservicesRunner microservicesRunner;
    private String expectedString = "{\"package_definition\":{\"package_name\":null},\"import_declarations\":[]," +
            "\"service_definitions\":[],\"functions\":[{\"function_name\":\"test_int\",\"is_public_function\":false," +
            "\"annotations\":[],\"variable_declarations\":[],\"argument_declarations\":[{\"parameter_name\":\"a\"," +
            "\"parameter_type\":\"int\",\"annotations\":[]}]," +
            "\"connector_declarations\":[],\"function_return_types\":[\"int\"],\"block_statement\":" +
            "{\"statement_declarations\":[{\"type\":\"assignment_statement\"," +
            "\"expressions\":[{\"type\":\"variable_reference_expression\",\"variable_reference_name\":\"a\"}," +
            "{\"type\":\"add_expression\"," +
            "\"expressions\":[{\"type\":\"variable_reference_expression\",\"variable_reference_name\":\"a\"}," +
            "{\"type\":\"basic_literal_expression\",\"basic_literal_type\":\"int\"," +
            "\"basic_literal_value\":\"2\"}]}]}," +
            "{\"type\":\"return_statement\",\"expressions\":[{\"type\":\"variable_reference_expression\"," +
            "\"variable_reference_name\":\"a\"}]}]}}]," +
            "\"connectors\":[]}";

    /* public static void main(String[] args) {
        try {
            BLangJSONModelTest test = new BLangJSONModelTest();
            test.setup();
            test.testBLangJSONModelService();
        } catch (Exception ex) {
            //Ignore
        }
    } */

    @Ignore
    @BeforeClass
    public void setup() throws Exception {
        microservicesRunner = new MicroservicesRunner(9091);
        microservicesRunner.deploy(new BLangFileRestService()).start();
    }

    @Ignore
    @Test
    public void testBLangJSONModelService() throws IOException, URISyntaxException {
        File file = new File(getClass().getClassLoader().getResource("samples/service/function.bal")
                .getFile());
        HttpURLConnection urlConn = request("/ballerina/model?location=" + file.getPath(), HttpMethod.GET);
        InputStream inputStream = urlConn.getInputStream();
        String response = StreamUtil.asString(inputStream);
        Assert.assertEquals(response, expectedString);
        IOUtils.closeQuietly(inputStream);
        urlConn.disconnect();
    }

    @Ignore
    @AfterClass
    public void teardown() throws Exception {
        microservicesRunner.stop();
    }

    private HttpURLConnection request(String path, String method) throws IOException, URISyntaxException {
        URI baseURI = new URI("http://localhost:9091");
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
            urlConn.setDoOutput(true);
        }
        urlConn.setRequestMethod(method);
        return urlConn;
    }

}
