/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.langlib.test;


import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * This class tests error lang module functionality.
 *
 * @since 1.0
 */
public class LangLibErrorTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/errorlib_test.bal");
    }

    @Test
    public void testTypeTestingErrorUnion() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testTypeTestingErrorUnion");
        assertEquals(returns[0].stringValue(), "{ballerina/io}GenericError");
        assertEquals(returns[1].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[1].stringValue(), "{message:\"Test union of errors with type test\"}");
    }

    @Test
    public void testPassingErrorUnionToFunction() {
        BValue[] returns = BRunUtil.invoke(compileResult, "testPassingErrorUnionToFunction");
        assertEquals(returns[0].getType().getTag(), TypeTags.RECORD_TYPE_TAG);
        assertEquals(returns[0].stringValue(), "{message:\"Test passing error union to a function\"}");
    }
}
