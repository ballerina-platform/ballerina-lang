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

import io.ballerina.runtime.api.values.BArray;
import io.ballerina.runtime.internal.values.HandleValue;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
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
        Object val = BRunUtil.invoke(result, "interopWithJavaArrayList");
        BArray returns = (BArray) val;
        Assert.assertEquals(returns.size(), 3);
        Assert.assertEquals(((HandleValue) returns.get(0)).getValue(), "[Ballerina, Language, Specification]");
        Assert.assertEquals(returns.get(1), 3L);
        Assert.assertEquals(returns.get(2).toString(), "Specification");
    }
}
