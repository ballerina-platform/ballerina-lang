/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.hover;

import com.google.gson.JsonParser;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Test performance of hover feature in language server.
 *
 * @since 2.0.0
 */
public class HoverPerformanceTest extends HoverProviderTest {
    private JsonParser parser = new JsonParser();

    @Test(description = "Test Hover provider", dataProvider = "hover-data-provider")
    public void testHover(String config) throws IOException {
        super.testHover(config);
    }

    @Override
    public String getResponse(Path sourcePath, Position position) {
        long start = System.currentTimeMillis();
        String responseString = parser.parse(TestUtil
                .getHoverResponse(sourcePath.toString(), position, serviceEndpoint)).getAsJsonObject().toString();
        long end = System.currentTimeMillis();
        TestUtil.closeDocument(serviceEndpoint, sourcePath);
        long actualResponseTime = end - start;
        int expectedResponseTime = Integer.parseInt(System.getProperty("responseTimeThreshold")) / 2;
        Assert.assertTrue(actualResponseTime < expectedResponseTime,
                String.format("Expected response time = %d, received %d.", expectedResponseTime, actualResponseTime));
        return responseString;
    }

    @Override
    @DataProvider(name = "hover-data-provider")
    public Object[][] dataProvider() {
        return new Object[][]{
                {"hover_performance.json"},
        };
    }
}
