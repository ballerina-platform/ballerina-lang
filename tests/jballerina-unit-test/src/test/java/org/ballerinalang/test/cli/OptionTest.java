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
import org.testng.annotations.Test;

/**
 * Test option CLI arguments.
 */
public class OptionTest {
    @Test
    public void testCliArg() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_all.bal");
        String[] args =
                {"--name", "Riyafa=Riyafa", "--good", "--score", "100", "--height", "5.5", "--energy", "10e99",
                        "--ratings", "5", "--ratings", "3", "--friends", "--friends"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test
    public void testCliNamedArg() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_all.bal");
        String[] args =
                {"--name=Riyafa=Riyafa", "--good", "--score=100", "--height=5.5", "--energy=10e99",
                        "--ratings=5", "--ratings=3", "--friends", "--friends"};
        BRunUtil.runMain(compileResult, args);
    }

    @Test
    public void testCliDefaultableOptional() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_defaultable_optional.bal");
        BRunUtil.runMain(compileResult, new String[0]);
    }

    @Test
    public void testCliDefaultableOptionalWithArg() {
        CompileResult compileResult = BCompileUtil.compile("test-src/cli/option_defaultable_optional_with_arg.bal");
        String[] args = {"--name=Riyafa", "--score=100", "--height=5.6"};
        BRunUtil.runMain(compileResult, args);
    }
}
