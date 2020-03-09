/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
package org.ballerinalang.test.types.tuples;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Compile time and run time tests for tuple fill member.
 *
 * @since 1.2.0
 */
public class TupleFillMemberTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/types/tuples/tuple_fill_member_test.bal");
    }

    @Test
    public void testTupleFillMember() {
        BRunUtil.invoke(compileResult, "testTupleFillMemberSimpleTypes");
        BRunUtil.invoke(compileResult, "testTupleFillMemberSimpleTypesWithRest");
        BRunUtil.invoke(compileResult, "testTupleFillMemberStructuredTypes");
        BRunUtil.invoke(compileResult, "testTupleFillMemberStructuredTypesWithRest");
    }
}
