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
package org.ballerinalang.test.connectors.h2;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.utils.SQLDBUtils;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Test class for H2 Connector.
 *
 * @since 0.970.0
 */
public class H2ClientTest {

    private CompileResult result;
    private static final String DB_NAME = "TestDBH2";
    public static final String DB_DIRECTORY_H2 = "./target/H2Cleint/";

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/connectors/h2/h2-client-test.bal");
        SQLDBUtils.deleteFiles(new File(DB_DIRECTORY_H2), DB_NAME);
        SQLDBUtils.initH2Database(DB_DIRECTORY_H2, DB_NAME, "datafiles/sql/SQLH2CustomerTableCreate.sql");
    }

    @Test
    public void testConnecotrInit() {
        BValue[] returns = BRunUtil.invoke(result, "testConnecotrInit");
        BInteger retValue = (BInteger) returns[0];
        Assert.assertEquals(retValue.intValue(), 1);
    }

    @AfterSuite
    public void cleanup() {
        SQLDBUtils.deleteDirectory(new File(DB_DIRECTORY_H2));
    }
}
