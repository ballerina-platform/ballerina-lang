/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.semantic.api.test.allreferences;

import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Test cases for the find all references API when the symbol is a module prefix.
 *
 * @since 2.0.0
 */
@Test
public class FindModulePrefixRefsTest extends FindAllReferencesTest {

    @Override
    @BeforeClass
    public void setup() {
        CompileResult compileResult = BCompileUtil.compileAndCacheBala("test-src/testproject");
        if (compileResult.getErrorCount() != 0) {
            Arrays.stream(compileResult.getDiagnostics()).forEach(System.out::println);
            Assert.fail("Compilation contains error");
        }
        super.setup();
    }

    @Override
    @DataProvider(name = "PositionProvider")
    public Object[][] getLookupPositions() {
        return new Object[][]{
                {16, 15, location(16, 15, 26),
                        List.of(location(16, 15, 26),
                                location(19, 0, 11),
                                location(44, 1, 12),
                                location(22, 4, 15),
                                location(36, 27, 38),
                                location(39, 28, 39),
                                location(40, 18, 29))
                },
                {25, 16, null,
                        List.of(location(25, 16, 20),
                                location(31, 24, 28)),
                },
                {35, 27, location(17, 33, 36),
                        List.of(location(17, 33, 36),
                                location(35, 27, 30)),
                }
        };
    }

    @Override
    public String getTestSourcePath() {
        return "test-src/find-all-ref/find_var_ref_in_module_prefix.bal";
    }
}
