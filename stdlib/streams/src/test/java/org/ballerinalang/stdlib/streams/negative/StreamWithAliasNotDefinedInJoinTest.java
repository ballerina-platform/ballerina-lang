/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.stdlib.streams.negative;

import org.ballerinalang.test.util.BAssertUtil;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * This class tests if the proper errors are logged if the stream is not defined.
 *
 * @since 0.990.1
 */
public class StreamWithAliasNotDefinedInJoinTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/negative/stream-with-alias-not-defined.bal");
    }

    @Test(description = "Tests if the streams with alias is not defined.")
    public void testStreamJoinQuery() {
        Assert.assertEquals(result.getErrorCount(), 8);
        BAssertUtil.validateError(result, 0, "undefined symbol 'stockSream'", 48, 14);
        BAssertUtil.validateError(result, 1, "undefined symbol 'twitterSream'", 49, 14);
        BAssertUtil.validateError(result, 2, "undefined symbol 'x'", 50, 12);
        BAssertUtil.validateError(result, 3, "undefined symbol 'y'", 50, 24);
        BAssertUtil.validateError(result, 4, "undefined symbol 'x'", 51, 16);
        BAssertUtil.validateError(result, 5, "undefined symbol 'y'", 51, 36);
        BAssertUtil.validateError(result, 6, "undefined symbol 'x'", 51, 54);
        BAssertUtil.validateError(result, 7, "fields defined in select clause, incompatible with output " +
                "fields in type 'StockWithPrice', expected '[symbol, tweet, price]' but found '[symbol, price, tweet]'",
                52, 9);
    }
}
