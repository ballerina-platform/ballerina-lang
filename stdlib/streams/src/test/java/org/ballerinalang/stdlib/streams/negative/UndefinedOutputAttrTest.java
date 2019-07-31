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
 * This contains methods to test if the proper error is thrown if a undefined output attributes is used in select
 * clause.
 *
 * @since 0.990.1
 */
public class UndefinedOutputAttrTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/negative/undefined-output-attr.bal");
    }

    @Test
    public void testOutputputFieldInSelect() {
        Assert.assertEquals(result.getErrorCount(), 3);
        BAssertUtil.validateError(result, 0, "invalid operation: type 'Teacher' does not " +
                "support field access for non-required field 'unknownField'", 64, 56);

        BAssertUtil.validateError(result, 1, "undefined stream attribute 'unknownField' found in select clause", 64,
                                  56);
        BAssertUtil.validateError(result, 2, "fields defined in select clause, incompatible with output fields " +
                "in type 'Teacher', expected '[name, age, status, batch, school]' but found '[school, batch, " +
                                             "unknownOutputField, age, status]'", 66, 9);

    }
}
