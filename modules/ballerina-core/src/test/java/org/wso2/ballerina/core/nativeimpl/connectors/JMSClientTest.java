/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.ballerina.core.nativeimpl.connectors;

//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//import org.wso2.ballerina.core.interpreter.SymScope;
//import org.wso2.ballerina.core.model.BallerinaFile;
//import org.wso2.ballerina.core.model.values.BBoolean;
//import org.wso2.ballerina.core.model.values.BValue;
//import org.wso2.ballerina.core.nativeimpl.connectors.http.client.HTTPConnector;
//import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
//import org.wso2.ballerina.core.runtime.internal.GlobalScopeHolder;
//import org.wso2.ballerina.core.runtime.registry.PackageRegistry;
//import org.wso2.ballerina.core.utils.ParserUtils;
//import org.wso2.ballerina.lang.util.Functions;
//
//public class JMSClientTest {
//    private BallerinaFile bFile;
//    private SymScope symScope;
//
//    @BeforeClass public void setup() {
//        symScope = GlobalScopeHolder.getInstance().getScope();
//        BuiltInNativeConstructLoader.loadConstructs();
//        PackageRegistry.getInstance().registerNativeConnector(new HTTPConnector());
//        bFile = ParserUtils.parseBalFile("lang/connectors/jmsClient.bal", symScope);
//    }
//
//    @Test(description = "Test for jms service availability check without service id")
//    public void testJMSServiceAvailabilityCheckWithoutJmsServiceId() {
//        BValue[] returns = Functions.invoke(bFile, "test");
//
//        Assert.assertEquals(returns.length, 1);
//
//        BBoolean actionReturned = (BBoolean) returns[0];
//        Assert.assertSame(actionReturned.getClass(), BBoolean.class,
//                          "Invalid class type returned.");
//        Assert.assertEquals(actionReturned.booleanValue(), true, "action named action1 failed");
//    }
//}
