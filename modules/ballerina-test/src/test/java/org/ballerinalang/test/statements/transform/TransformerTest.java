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
package org.ballerinalang.test.statements.transform;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.PrintStream;
import java.util.Arrays;

public class TransformerTest {

    CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/statements/transform/transformer-def.bal");
        PrintStream log = System.err;
        Arrays.stream(result.getDiagnostics()).forEach(diag -> log.println(diag.getPosition() + diag.getMessage()));
    }

    @Test(description = "Test empty transformation")
    public void testEmptyTransform() {
//        BRunUtil.invoke(result, "castAndConversionInTransform");
    }
}
