/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.formatter.core.misc;

import io.ballerina.tools.text.LineRange;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.RangeFormatterTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Test range formatting functionality with manually added fixed ranges.
 *
 * @since 2.0.0
 */
public class RangesTest extends RangeFormatterTest {

    @Test(dataProvider = "test-file-provider")
    @Override
    public void test(Path sourceFilePath, Path assertFilePath, ArrayList<LineRange> lineRanges) throws IOException,
            FormatterException {
        super.test(sourceFilePath, assertFilePath, lineRanges);
    }

    /**
     * Defines the data provider object for test execution.
     *
     * @return Data provider for tests
     */
    @DataProvider(name = "test-file-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return Paths.get("misc", "ranges").toString();
    }

    @Override
    public String getConfigJsonFileName() {
        return "range-test-config.json";
    }
}
