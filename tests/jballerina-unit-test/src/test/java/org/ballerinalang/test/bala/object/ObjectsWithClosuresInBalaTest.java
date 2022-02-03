/*
*  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.bala.object;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test cases for user defined object types in ballerina.
 */
@Test
public class ObjectsWithClosuresInBalaTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project");
        BCompileUtil.compileAndCacheBala("test-src/bala/test_projects/test_project_utils");
        result = BCompileUtil.compile("test-src/bala/test_bala/object/test_object_closures.bal");
    }

    @Test(description = "Test Basic object closure")
    public void testBasicStructAsObject() {
        BValue[] returns = BRunUtil.invoke(result, "testObjectWithClosuresFromFoo");
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }
}
