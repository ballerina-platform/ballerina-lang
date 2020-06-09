/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.balo.types;

import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


/**
 * Test never type with balo.
 */
public class NeverTypeBaloTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "foo");
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "records");
        result = BCompileUtil.compile("test-src/balo/test_balo/types/never_type_test.bal");
    }

    @Test
    public void testTypeOfNeverReturnTypedFunction() {
        BRunUtil.invoke(result, "testTypeOfNeverReturnTypedFunction");
    }

    @Test
    public void testNeverReturnTypedFunctionCall() {
        BRunUtil.invoke(result, "testNeverReturnTypedFunctionCall");
    }

    @Test
    public void testInclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(result, "testInclusiveRecord");
    }

    @Test
    public void testExclusiveRecordTypeWithNeverTypedField() {
        BRunUtil.invoke(result, "testExclusiveRecord");
    }

    @Test
    public void testNeverWithKeyLessTable() {
        BRunUtil.invoke(result, "testNeverWithKeyLessTable");
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "foo");
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg", "records");
    }
}
