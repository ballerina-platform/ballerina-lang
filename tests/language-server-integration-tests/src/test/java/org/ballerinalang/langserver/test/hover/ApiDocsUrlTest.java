/*
 * Copyright (c) 2023, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.test.hover;

import org.ballerinalang.langserver.hover.APIDocReference;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Integration tests for validating api-docs urls.
 */
public class ApiDocsUrlTest {

    @Test(dataProvider = "data-provider")
    public void test(String orgName, String moduleName, String version, String constructName)
            throws IOException {
        String urlString = APIDocReference.from(orgName, moduleName, version, constructName);
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setInstanceFollowRedirects(true);
        connection.setRequestMethod("GET");
        connection.connect();
        int code = connection.getResponseCode();
        connection.disconnect();
        Assert.assertEquals(code, 200, "API Docs URL is not valid: " + urlString + "received code: " + code);
    }

    @DataProvider(name = "data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"ballerina", "http", "2.6.0", "Client"},
                {"ballerina", "http", "2.6.0", "authenticateResource"},
                {"ballerina", "http", "latest", "Listener"}
        };
    }
}
