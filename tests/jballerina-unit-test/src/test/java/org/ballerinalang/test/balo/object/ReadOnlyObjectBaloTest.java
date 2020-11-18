/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.balo.object;

import org.ballerinalang.test.balo.BaloCreator;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Tests for `readonly object`s.
 *
 * @since 2.0.0
 */
@Test(groups = { "brokenOnOldParser" })
public class ReadOnlyObjectBaloTest {

    private CompileResult result;

    @Test
    public void testReadonlyRecordFields() {
        BaloCreator.cleanCacheDirectories();
        BaloCreator.createAndSetupBalo("test-src/balo/test_projects/test_project", "testorg", "readonly_objects");
        result = BCompileUtil.compile("test-src/balo/test_balo/object/test_readonly_objects.bal");
    }

    @Test (enabled = false)
    public void testReadonlyType() {
        BRunUtil.invoke(result, "testReadonlyObjects");
    }

    @Test
    public void testReadonlyRecordFieldsNegative() {
        CompileResult result =
                BCompileUtil.compile("test-src/balo/test_balo/object/test_readonly_objects_negative.bal");
        int index = 0;

        validateError(result, index++, "invalid intersection type: cannot have a 'readonly' intersection with a " +
                "'readonly object'", 20, 5);
        assertEquals(result.getErrorCount(), index);
    }

    @AfterClass
    public void tearDown() {
        BaloCreator.clearPackageFromRepository("test-src/balo/test_projects/test_project", "testorg",
                                               "readonly_objects");
    }
}
