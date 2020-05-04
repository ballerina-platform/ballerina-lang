/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.test.javainterop;

import org.ballerinalang.core.model.values.BHandleValue;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test cases for java interop instance function invocations.
 *
 * @since 1.0.0
 */
public class MiscTests {

    @Test(description = "Test interoperability with Java ArryList")
    public void testInteropWithJavaArrayList() {
        CompileResult result = BCompileUtil.compile("test-src/javainterop/java_array_list_interop_tests.bal");
        BValue[] returns = BRunUtil.invoke(result, "interopWithJavaArrayList");
        Assert.assertEquals(returns.length, 3);
        Assert.assertEquals(((BHandleValue) returns[0]).getValue(), "[Ballerina, Language, Specification]");
        Assert.assertEquals(((BInteger) returns[1]).intValue(), 3);
        Assert.assertEquals(returns[2].stringValue(), "Specification");
    }
}
