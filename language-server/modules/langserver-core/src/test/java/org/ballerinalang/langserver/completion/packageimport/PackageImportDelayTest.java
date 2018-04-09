/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.completion.packageimport;

import com.google.gson.JsonObject;
import org.ballerinalang.langserver.completion.CompletionTest;
import org.ballerinalang.langserver.completion.util.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Tests the endurance of the heavy package imports(eg. http) for the completion.
 *
 * Checks the time consumed is equal or below MAX_DELAY_THRESHOLD.
 */
public class PackageImportDelayTest extends CompletionTest {
    private static final long MAX_DELAY_THRESHOLD = 1L;

    @DataProvider(name = "completion-data-provider")
    @Override
    public Object[][] dataProvider() {
        return new Object[][]{
                {"packageImport.json", "packageimport"}
        };
    }

    @Test(dataProvider = "completion-data-provider", enabled = false)
    @Override
    public void test(String config, String configPath) {
        String configJsonPath = SAMPLES_COPY_DIR + File.separator + configPath + File.separator + config;
        JsonObject configJsonObject = FileUtils.fileContentAsObject(configJsonPath);

        // Allow warmup and caching
        getResponseItemList(configJsonObject);

        //Measure stats
        long startTime = System.nanoTime();
        getResponseItemList(configJsonObject);
        long endTime = System.nanoTime();
        long totalTime = TimeUnit.NANOSECONDS.toSeconds(endTime - startTime);

        Assert.assertTrue(totalTime <= MAX_DELAY_THRESHOLD);
    }
}
