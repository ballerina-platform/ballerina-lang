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
package org.ballerinalang.test.javainterop.basic;

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.Test;

/**
 * Negative test cases for java interop.
 *
 * @since 1.0.0
 */
public class NegativeValidationTest {

    @Test(expectedExceptions = BLangCompilerException.class)
    public void testAcceptNothing() {
        String path = "test-src/javainterop/ballerina_types_as_interop_types_negative.bal";
        CompileResult result = BCompileUtil.compileInProc(path);
    }

}
