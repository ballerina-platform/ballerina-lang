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

import io.ballerina.tools.text.LinePosition;
import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.RangeFormatter;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Paths;

public class RangesTest extends RangeFormatter {
    @Test(dataProvider = "test-file-provider")
    public void test(String source, String sourcePath) throws IOException, FormatterException {
        LinePosition startPos = LinePosition.from(26, 13);
        LinePosition endPos = LinePosition.from(275, 14);
        super.test(source, sourcePath, startPos, endPos);
    }

    @DataProvider(name = "test-file-provider")
    @Override
    public Object[][] dataProvider() {
        return this.getConfigsList();
    }

    @Override
    public String getTestResourceDir() {
        return Paths.get("misc", "ranges").toString();
    }
}
