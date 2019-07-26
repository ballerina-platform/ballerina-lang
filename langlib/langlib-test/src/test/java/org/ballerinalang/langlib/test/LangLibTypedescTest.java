/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.langlib.test;


import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for the lang.typedesc library.
 *
 * @since 1.0
 */
public class LangLibTypedescTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/typedesclib_test.bal");
    }

    @Test
    public void jsonConstructFromRecTest() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testRecToJson");
        Assert.assertEquals(returns[0].getType().toString(), "map<json>");
        Assert.assertEquals(returns[0].stringValue(), "{\"name\":\"N\", \"age\":3}");
    }

    @Test
    public void jsonConstructFromRecTest2() {
        BValue[] returns = BRunUtil.invokeFunction(compileResult, "testOnTypeName");
        BValueArray array = (BValueArray) returns[0];
        Assert.assertEquals(array.getRefValue(0).getType().toString(), "Person");
        Assert.assertEquals(array.getRefValue(0).stringValue(), "{name:\"tom\", age:2}");
        Assert.assertEquals(array.getRefValue(1).getType().toString(), "map<json>");
        Assert.assertEquals(array.getRefValue(1).stringValue(), "{\"name\":\"bob\", \"age\":4}");
    }
}
