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
package org.ballerinalang.core.lang.worker;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


public class WorkerInResourceTest {
    //private BallerinaFile bFile;

    @BeforeClass
    public void setup() {
//        BuiltInNativeConstructLoader.loadConstructs();
//        SymScope globalSymScope = GlobalScopeHolder.getInstance().getScope();
//        bFile = ParserUtils.parseBalFile("model/statements/worker-in-resource.bal", globalSymScope);
//        EnvironmentInitializer.initialize("samples/worker-in-resource.bal");
    }

    @Test(description = "Test worker declaration")
    public void testWorkerInResource() {
//        CarbonMessage cMsg = MessageUtils.generateHTTPMessage("/passthrough/test", "POST");
//        cMsg.addMessageBody(ByteBuffer.wrap("hello".getBytes()));
//        cMsg.setEndOfMsgAdded(true);
//        CarbonMessage response = Services.invoke(cMsg);
//
//        Assert.assertNotNull(response);
    }
}
