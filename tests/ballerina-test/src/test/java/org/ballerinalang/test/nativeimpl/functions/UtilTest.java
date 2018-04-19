/*
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.nativeimpl.functions;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BJSON;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test Cases for ballerina.utils native functions.
 */
public class UtilTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/nativeimpl/functions/util-test.bal");
    }

    @Test
    public void testParseJson() {
        String jsonString = "{\"name\":\"apple\",\"color\":\"red\",\"price\":25}";
        BValue[] args = new BValue[]{new BString(jsonString)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParseJson", args);
        Assert.assertTrue(returns[0] instanceof BJSON);
    }

    @Test
    public void testParseInvalidJson() {
        String jsonString = "{\"name\":\"apple\",\"color\":\"red\",\"price\":25} sample invalid json";
        BValue[] args = new BValue[]{new BString(jsonString)};
        BValue[] returns = BRunUtil.invoke(compileResult, "testParseJson", args);
        Assert.assertTrue(returns[0] instanceof BStruct);
        Assert.assertTrue(((BStruct) returns[0]).getStringField(0).contains("Failed to parse json string:"));
    }
}
