/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.error;

import io.ballerina.runtime.api.values.BArray;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for Error destructure binding pattern.
 *
 * @since 2.0 Swan Lake
 */
public class ErrorDestructureTest {
    @Test(description = "Test error destruturing binding pattern")
    public void testErrorDestructuring() {
        CompileResult destructuringResult = BCompileUtil.
                compile("test-src/error/error_destructure_binding_pattern.bal");
        Object arr = BRunUtil.invoke(destructuringResult, "testErrorDestructure");
        BArray returns = (BArray) arr;
        Assert.assertEquals(returns.get(0).toString(), "Sample Error");
        Assert.assertEquals(returns.get(1).toString(), "Detail Info");
        Assert.assertEquals(returns.get(2).toString(), "true");
        Assert.assertEquals(returns.get(3).toString(), "Sample Error");
        Assert.assertEquals(returns.get(4).toString(), "{\"info\":\"Detail Info\",\"fatal\":true}");
        Assert.assertEquals(returns.get(5).toString(), "Failed Message");
    }
}
