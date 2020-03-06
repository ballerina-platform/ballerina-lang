/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.mysql.query;

import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BFloat;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.mysql.BaseTest;
import org.ballerinalang.mysql.utils.SQLDBUtils;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;

/**
 * This test class handles the complex sql types to ballerina type conversion for query operation.
 *
 * @since 1.2.0
 */
public class ComplexTypesQueryTest {

    private CompileResult result;
    private static final String DB_NAME = "TEST_SQL_COMPLEX_QUERY";
    private static final String SQL_SCRIPT = SQLDBUtils.SQL_RESOURCE_DIR + File.separator + SQLDBUtils.QUERY_DIR +
            File.separator + "complex-test-data.sql";

    static {
        BaseTest.addDBSchema(DB_NAME, SQL_SCRIPT);
    }

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compileOffline(SQLDBUtils.getBalFilesDir("query", "complex-query-test.bal"));
    }


    @Test
    public void testQuery() {
        BValue[] returnVal = BRunUtil.invokeFunction(result, "testQuery");
        Assert.assertTrue(returnVal[0] instanceof BMap);
        LinkedHashMap result = ((BMap) returnVal[0]).getMap();
        Assert.assertEquals(result.size(), 6);
        Assert.assertEquals(((BInteger) result.get("INT_TYPE")).intValue(), 1);
        Assert.assertEquals(((BInteger) result.get("LONG_TYPE")).intValue(), 9223372036854774807L);
        DecimalFormat df = new DecimalFormat("###.##");

        Assert.assertEquals(df.format(((BFloat) result.get("FLOAT_TYPE")).floatValue()), "123.34");
        Assert.assertEquals(((BFloat) result.get("DOUBLE_TYPE")).floatValue(), 2139095039D);
        Assert.assertTrue(((BBoolean) result.get("BOOLEAN_TYPE")).booleanValue());
        Assert.assertEquals(((BString) result.get("STRING_TYPE")).stringValue(), "Hello");
    }

}
