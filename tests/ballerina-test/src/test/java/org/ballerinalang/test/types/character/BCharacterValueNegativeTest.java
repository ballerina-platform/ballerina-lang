/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.test.types.character;

import org.ballerinalang.launcher.util.BAssertUtil;
import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test class will test negative the behaviour of character cast operations.
 *
 * @since 0.964
 */

public class BCharacterValueNegativeTest {

    @Test(description = "Test string to character conversion with error")
    public void testStringToCharConversionWithError() {
        CompileResult resultNegative = BCompileUtil.compile("test-src/types/character/character-negative.bal");
        Assert.assertEquals(resultNegative.getErrorCount(), 3);

        BAssertUtil.validateError(resultNegative, 0,
                "incompatible types: 'string' cannot be cast to 'char'", 3, 20);

        BAssertUtil.validateError(resultNegative, 1,
                "incompatible types: 'string' cannot be convert to 'char'", 9, 22);

        BAssertUtil.validateError(resultNegative, 2,
                "incompatible types: 'float' cannot be cast to 'char', use conversion expression",
                15, 14);
    }
}
