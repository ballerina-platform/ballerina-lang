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

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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

    @Test
    public void testLocalServiceAnnotEvaluation() {
        Object returns = BRunUtil.invoke(result, "testAnnotEvaluation");
        BArray array = ((BArray) returns);
        Assert.assertEquals(array.size(), 6);
        for (int i = 0; i < 6; i++) {
            Assert.assertEquals(array.getBoolean(i), true);
        }
    }
}
