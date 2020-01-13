/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.test.imports;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

import static org.ballerinalang.test.util.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;

/**
 * Negative test cases for imports.
 *
 *  @since 1.1.0
 */
public class ImportsNegativeTests {

    @Test(description = "Test invalid import on single file")
    public void testInvalidImportOnMultipleFiles() {
        CompileResult result = BCompileUtil.compile("test-src/imports/invalid-import", "foo");
        assertEquals(result.getErrorCount(), 2);
        validateError(result, 0, "cannot resolve module 'unknown/module'", 1, 1);
        validateError(result, 1, "cannot resolve module 'unknown/module'", 1, 1);
    }
}
