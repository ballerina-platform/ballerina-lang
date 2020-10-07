///*
// *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// *  WSO2 Inc. licenses this file to you under the Apache License,
// *  Version 2.0 (the "License"); you may not use this file except
// *  in compliance with the License.
// *  You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// *  Unless required by applicable law or agreed to in writing,
// *  software distributed under the License is distributed on an
// *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// *  KIND, either express or implied.  See the License for the
// *  specific language governing permissions and limitations
// *  under the License.
// */
//package io.ballerina.test.compiler.plugins;
//
//import org.ballerinalang.test.balo.BaloCreator;
//import org.ballerinalang.test.util.BAssertUtil;
//import org.ballerinalang.test.util.BCompileUtil;
//import org.ballerinalang.test.util.CompileResult;
//import org.testng.Assert;
//import org.testng.annotations.BeforeClass;
//import org.testng.annotations.Test;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * This class tests the compiler plugin implementation.
// */
//public class CompilerPluginTest {
//
//    private CompileResult compileResult;
//
//    @BeforeClass
//    public void setup() {
//        BaloCreator.cleanCacheDirectories();
//        BaloCreator.createAndSetupBalo("test-src/test-project", "testorg", "functions");
//        BaloCreator.createAndSetupBalo("test-src/test-project", "testorg", "services");
//        BaloCreator.createAndSetupBalo("test-src/test-project", "testorg", "types");
//        compileResult = BCompileUtil.compile("test-src/compiler_plugin_test.bal");
//    }
//
//    @Test(description = "Test compiler plugin")
//    public void testCompilerPlugin() {
//
//        Assert.assertEquals(compileResult.getErrorCount(), 0, "There are compilation errors");
//        Assert.assertEquals(compileResult.getWarnCount(), 1);
//        BAssertUtil.validateWarning(compileResult, 0, "compiler plugin crashed", 6, 1);
//
//        Map<TestEvent.Kind, Set<TestEvent>> allEvents = TestCompilerPlugin.testEventMap;
//        Map<TestEvent.Kind, Set<TestEvent>> funcEvents = FunctionsTestCompilerPlugin.testEventMap;
//
//        Assert.assertEquals(allEvents.size(), 6,
//                "All the process methods haven't been invoked by the compiler plugin");
//
//        assertData(TestEvent.Kind.PLUGIN_START,
//                allEvents,
//                new ArrayList<TestEvent>() {{
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_START, "testOrg/functions:1.0.0", 1));
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_START, "testOrg/services:1.0.0", 1));
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_START, "testOrg/types:1.0.0", 1));
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_START, ".", 1));
//                }});
//
//        assertData(TestEvent.Kind.PLUGIN_COMPLETE,
//                allEvents,
//                new ArrayList<TestEvent>() {{
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_COMPLETE, "testOrg/functions:1.0.0", 1));
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_COMPLETE, "testOrg/services:1.0.0", 1));
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_COMPLETE, "testOrg/types:1.0.0", 1));
//                    add(new TestEvent(TestEvent.Kind.PLUGIN_COMPLETE, ".", 1));
//                }});
//
//        // Test service events
//        assertData(TestEvent.Kind.SERVICE_ANN,
//                allEvents,
//                new ArrayList<TestEvent>() {{
//                    add(new TestEvent(TestEvent.Kind.SERVICE_ANN, "routerService", 1));
//                }});
//
//        // Test struct events
//        assertData(TestEvent.Kind.TYPEDEF_ANN,
//                allEvents,
//                new ArrayList<TestEvent>() {{
//                    add(new TestEvent(TestEvent.Kind.TYPEDEF_ANN, "routeCon", 1));
//                    add(new TestEvent(TestEvent.Kind.TYPEDEF_ANN, "RouteConfig", 1));
//                    add(new TestEvent(TestEvent.Kind.TYPEDEF_ANN, "Employee", 1));
//                }});
//
//        // Test function events
//        assertData(TestEvent.Kind.FUNC_ANN,
//                allEvents,
//                new ArrayList<TestEvent>() {{
//                    add(new TestEvent(TestEvent.Kind.FUNC_ANN, "routerFunc", 1));
//                }});
//
//        assertData(TestEvent.Kind.FUNC_ANN,
//                funcEvents,
//                new ArrayList<TestEvent>() {{
//                    add(new TestEvent(TestEvent.Kind.FUNC_ANN, "routerFunc", 1));
//                }});
//
//    }
//
//    public void assertData(TestEvent.Kind kind,
//                           Map<TestEvent.Kind, Set<TestEvent>> eventMap,
//                           List<TestEvent> expectedEventList) {
//        Set<TestEvent> actualEventSet = eventMap.get(kind);
//        Assert.assertNotNull(actualEventSet, "All the " + kind.name + " nodes haven't been processed");
//        Assert.assertEquals(actualEventSet.size(), expectedEventList.size(),
//                "All the " + kind.name + " nodes haven't been processed");
//        expectedEventList.forEach(expectedEvent ->
//                Assert.assertEquals(actualEventSet.contains(expectedEvent), true,
//                        "The " + kind.name + " '" + expectedEvent.nodeName + "' is not processed")
//        );
//    }
//}
