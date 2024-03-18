/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.cli;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * Test operand CLI arguments.
 */
public class OperandTest {
    @DataProvider(name = "fileAndParameters")
    public Object[] getFileAndParameters() {
        return new Object[][]{
                {"operand_default_only", new String[0]}, {"operand_int_and_default", new String[]{"1"}},
                {"operand_int_and_default", new String[]{"2", "John"}},
                {"operand_rest_param_only", new String[]{"Riyafa", "John"}},
                {"operand_rest_param_without_value", new String[0]},
                {"operand_rest_param_without_value", new String[0]}, {"option_defaultable_optional_with_arg",
                new String[]{"--name=Riyafa", "--score=100", "--height=5.6"}},
                {"operands_with_defaultable_values", new String[]{"10", "12", "0", "", "0.0", "0", "0"}}
        };
    }

    @Test(dataProvider = "fileAndParameters")
    public void testOperands(String fileName, String[] args) {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/" + fileName + ".bal");
        BRunUtil.runMain(compileResult, args);
    }
}
