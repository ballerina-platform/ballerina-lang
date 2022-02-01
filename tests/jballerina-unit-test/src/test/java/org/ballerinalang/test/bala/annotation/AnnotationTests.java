/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.ballerinalang.test.bala.annotation;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BAssertUtil;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for reading annotations.
 */
public class AnnotationTests {

    CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        result = BCompileUtil.compile("test-src/bala/test_bala/annotations/annotation.bal");
    }

    @Test(description = "Test the deprecated construct from external module")
    public void testDeprecation() {
        CompileResult result = BCompileUtil.compile("test-src/bala/test_bala/annotations/deprecation_annotation.bal");
        Assert.assertEquals(result.getWarnCount(), 5);

        int i = 0;
        BAssertUtil.validateWarning(result, i++, "usage of construct 'foo:DummyObject1' is deprecated", 3, 23);
        BAssertUtil.validateWarning(result, i++, "usage of construct 'foo:Bar' is deprecated", 3, 45);
        BAssertUtil.validateWarning(result, i++, "usage of construct 'foo:C1' is deprecated", 3, 69);
        BAssertUtil.validateWarning(result, i++, "usage of construct 'obj.doThatOnObject()' is deprecated", 8
                , 5);
        BAssertUtil.validateWarning(result, i, "usage of construct 'foo:deprecated_func()' is deprecated", 9, 16);
    }

    @Test
    public void testNonBallerinaAnnotations() {
        BValue[] returns = BRunUtil.invoke(result, "testNonBallerinaAnnotations");
        Assert.assertNotNull(returns);
        Assert.assertEquals(returns.length, 1);
        Assert.assertNotNull(returns[0]);
        Assert.assertEquals(returns[0].stringValue(), "{numVal:10, textVal:\"text\", conditionVal:false, " +
                "recordVal:{nestNumVal:20, nextTextVal:\"nestText\"}}");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
