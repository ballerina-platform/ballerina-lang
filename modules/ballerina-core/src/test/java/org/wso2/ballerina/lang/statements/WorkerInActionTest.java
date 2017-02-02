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
package org.wso2.ballerina.lang.statements;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.connectors.http.client.HTTPConnector;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
import org.wso2.ballerina.core.runtime.registry.PackageRegistry;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

public class WorkerInActionTest {
    private BallerinaFile bFile;

    @BeforeClass()
    public void setup() {
        SymScope globalSymScope = GlobalScopeHolder.getInstance().getScope();
        BuiltInNativeConstructLoader.loadConstructs();
        PackageRegistry.getInstance().registerNativeConnector(new HTTPConnector());
        bFile = ParserUtils.parseBalFile("lang/statements/worker-in-action.bal", globalSymScope);
    }

    @Test(description = "Test TestConnector action1")
    public void testConnectorAction1() {
        BValue[] returns = Functions.invoke(bFile, "testAction1");
        Assert.assertEquals(returns.length, 1);
        Assert.assertTrue(returns[0] instanceof BMessage);
    }
}
