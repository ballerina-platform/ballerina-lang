/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.streams.negative;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This contains methods to test the errors thrown when the alias is not defined for select expressions.
 *
 * @since 0.990.1
 */
public class AliasNotDefinedForInvocationsTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.
                compile("test-src/negative/alias-not-found.bal");
    }

    @Test
    public void testAliasNotFoundError() {
        Assert.assertEquals(result.getErrorCount(), 2);
        BAssertUtil.validateError(result, 0,
                "alias not defined for expression in select clause", 81, 84);
        BAssertUtil.validateError(result, 1,
                "fields defined in select clause, incompatible with output fields in type " +
                "'TeacherOutput', expected '[name(string), age(int), sumAge(int), count(int)]' but found " +
                "'[sumAge(null), name(string), age(int)]'", 83, 9);
    }
}
