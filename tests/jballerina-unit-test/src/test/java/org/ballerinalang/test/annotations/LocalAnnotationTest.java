/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.annotations;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test annotation evaluation within functions.
 *
 * @since 1.0
 */
public class LocalAnnotationTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/annotations/local_annot.bal");
        Assert.assertEquals(result.getErrorCount(), 0);
    }

    @Test (enabled = false)
    public void testLocalServiceAnnotEvaluation() {
        BValue[] returns = BRunUtil.invoke(result, "testAnnotEvaluation");
        Assert.assertEquals(returns.length, 1);
        BValueArray array = ((BValueArray) returns[0]);
        Assert.assertEquals(array.size(), 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(array.getBoolean(i), 1);
        }
    }
}
