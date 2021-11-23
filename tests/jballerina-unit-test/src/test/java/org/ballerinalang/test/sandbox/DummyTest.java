/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.sandbox;

import io.ballerina.runtime.api.creators.ErrorCreator;
import io.ballerina.runtime.api.utils.StringUtils;
import org.ballerinalang.core.model.values.BString;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.Arrays;

/**
 * Dummy test
 */
public class DummyTest {

    private CompileResult result;
    
    @BeforeClass
    public void setup() {
        this.result = BCompileUtil.compile("test-src/sandbox/dummy.bal");
        Assert.assertEquals(result.getErrorCount(), 0, Arrays.asList(result.getDiagnostics()).toString());
    }

    @Test
    public void sandboxtest() {
        BValue[] returns = BRunUtil.invoke(result, "dummyTest", new BValue[0]);
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }

    public static void print(Object value) {
        System.out.println("############################");
        System.out.println(StringUtils.getStringValue(value, null));
    }
}
