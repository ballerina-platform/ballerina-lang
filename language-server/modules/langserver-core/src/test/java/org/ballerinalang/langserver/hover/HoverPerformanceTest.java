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
import org.ballerinalang.langserver.util.PerformanceTestUtils;
import org.ballerinalang.langserver.util.TestUtil;
import org.eclipse.lsp4j.Position;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Test performance of hover feature in language server.
 *
 * @since 2.0.0
 */
public class HoverPerformanceTest extends HoverProviderTest {

    private static final int TEST_ITERATIONS = 10;

    private final List<Long> executionTimes = new ArrayList<>();

    @Test(description = "Test Hover provider", dataProvider = "hover-data-provider", enabled = false)
    public void testHover(String config) throws IOException {
        // We run the same test multiple times and take the average of them as the execution time. This is to
        // reduce the effect of first compilation and outliers
        for (int i = 0; i < TEST_ITERATIONS; i++) {
            super.testHover(config);
        }

        // Compute the average execution time
        long avgResponseTime = executionTimes.stream().reduce(0L, Long::sum) / executionTimes.size();

        int expectedResponseTime = PerformanceTestUtils.getHoverResponseTimeThreshold();
        Assert.assertTrue(avgResponseTime < expectedResponseTime,
                String.format("Expected avg response time = %d, received %d.", expectedResponseTime, avgResponseTime));
    }

    @Override
    public String getResponse(Path sourcePath, Position position) {
        long start = System.currentTimeMillis();
        String responseString = JsonParser.parseString(TestUtil
                .getHoverResponse(sourcePath.toString(), position, serviceEndpoint)).getAsJsonObject().toString();
        long end = System.currentTimeMillis();
        long actualResponseTime = end - start;
        executionTimes.add(actualResponseTime);
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
