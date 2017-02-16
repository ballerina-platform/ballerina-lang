/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.lang.worker;

import org.ballerinalang.util.program.BLangFunctions;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BLangProgram;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.nativeimpl.util.BTestUtils;

public class WorkerCallingFunction {
    private BLangProgram bLangProgram;

    @BeforeClass
    public void setup() {
        bLangProgram = BTestUtils.parseBalFile("samples/worker-calling-function.bal");
    }

    @Test(description = "Test worker declaration")
    public void testWorkerDeclaration() {
        BValue[] returns = BLangFunctions.invoke(bLangProgram, "testWorker");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMessage);
        final String expected = "{\"name\":\"chanaka\"}";
        Assert.assertEquals(returns[0].stringValue(), expected);
    }
}
