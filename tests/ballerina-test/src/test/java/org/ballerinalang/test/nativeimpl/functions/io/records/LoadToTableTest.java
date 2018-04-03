/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.nativeimpl.functions.io.records;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.BServiceUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Test classes to verify functionality of io:loadToTable native function.
 *
 * @since 0.966.0
 */
public class LoadToTableTest {

    private CompileResult recordsInputOutputProgramFile;

    @BeforeClass
    public void setup() {
        recordsInputOutputProgramFile = BCompileUtil.compile("test-src/io/record_io.bal");
    }

    @Test(description = "Test successful data load")
    public void loadRecordFromFile() throws URISyntaxException {
        String resourceToRead = "datafiles/io/records/sample5.csv";
        BValue[] args = { new BString(getAbsoluteFilePath(resourceToRead))};
        final BValue[] result = BRunUtil.invoke(recordsInputOutputProgramFile, "loadToTable", args);
        final BFloat totalSalary = (BFloat) result[0];
        Assert.assertEquals(totalSalary.floatValue(), 60001.00d);
    }

    private String getAbsoluteFilePath(String relativePath) throws URISyntaxException {
        URL fileResource = BServiceUtil.class.getClassLoader().getResource(relativePath);
        String pathValue = "";
        if (null != fileResource) {
            Path path = Paths.get(fileResource.toURI());
            pathValue = path.toAbsolutePath().toString();
        }
        return pathValue;
    }
}
