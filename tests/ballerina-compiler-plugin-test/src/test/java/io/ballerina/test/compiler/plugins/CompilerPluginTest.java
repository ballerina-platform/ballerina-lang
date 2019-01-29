/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.test.compiler.plugins;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class tests the compiler plugin implementation.
 */
public class CompilerPluginTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/compiler_plugin/compiler_plugin_test.bal");
    }

    @Test(description = "Test compiler plugin")
    public void testCompilerPlugin() {
        Assert.assertEquals(compileResult.getErrorCount(), 0, "There are compilation errors");
        Map<TestEvent.Kind, Set<TestEvent>> abcEventMap = ABCCompilerPlugin.testEventMap;
        Map<TestEvent.Kind, Set<TestEvent>> xyzEventMap = XYZCompilerPlugin.testEventMap;

        Assert.assertEquals(abcEventMap.size(), 11,
                "All the process methods haven't been invoked by the compiler plugin");

        // Test service events
        assertData(TestEvent.Kind.SERVICE_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.SERVICE_ANN, "routerService", 3));
                    add(new TestEvent(TestEvent.Kind.SERVICE_ANN, "routerService2", 2));
                }});

        // Test resource events
        assertData(TestEvent.Kind.RESOURCE_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.RESOURCE_ANN, "route", 2));
                    add(new TestEvent(TestEvent.Kind.RESOURCE_ANN, "route2", 1));
                }});

        // Test connector events
        assertData(TestEvent.Kind.CONNECTOR_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.CONNECTOR_ANN, "routeCon", 1));
                }});

        // Test action events
        assertData(TestEvent.Kind.ACTION_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.ACTION_ANN, "getRoutes", 1));
                }});

        assertData(TestEvent.Kind.ACTION_ANN,
                xyzEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.ACTION_ANN, "getRoutes", 1));
                }});

        // Test struct events
        assertData(TestEvent.Kind.STRUCT_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.STRUCT_ANN, "RouteConfig", 1));
                    add(new TestEvent(TestEvent.Kind.STRUCT_ANN, "Employee", 1));
                }});

        // Test function events
        assertData(TestEvent.Kind.FUNC_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.FUNC_ANN, "routerFunc", 1));
                }});

        assertData(TestEvent.Kind.FUNC_ANN,
                xyzEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.FUNC_ANN, "routerFunc", 1));
                }});

        // Test variable events
        assertData(TestEvent.Kind.VARIAVLE_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    // TODO Annotations cannot be attached to global variables at the moment.
//                    add(new TestEvent(TestEvent.Kind.VARIAVLE_ANN, "a", 1));
                    add(new TestEvent(TestEvent.Kind.VARIAVLE_ANN, "PI", 1));
                }});

        // Test annotation events
        assertData(TestEvent.Kind.ANNOTATION_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.ANNOTATION_ANN, "RouteData", 2));
                }});

        // Test transformer events
        assertData(TestEvent.Kind.TRANSFORM_ANN,
                abcEventMap,
                new ArrayList<TestEvent>() {{
                    add(new TestEvent(TestEvent.Kind.TRANSFORM_ANN, "setCityToNewYork", 2));
                }});
    }

    public void assertData(TestEvent.Kind kind,
                           Map<TestEvent.Kind, Set<TestEvent>> eventMap,
                           List<TestEvent> expectedEventList) {
        Set<TestEvent> actualEventSet = eventMap.get(kind);
        Assert.assertNotNull(actualEventSet, "All the " + kind.name + " nodes haven't been processed");
        Assert.assertEquals(actualEventSet.size(), expectedEventList.size(),
                "All the " + kind.name + " nodes haven't been processed");
        expectedEventList.forEach(expectedEvent ->
                Assert.assertEquals(actualEventSet.contains(expectedEvent), true,
                        "The " + kind.name + " '" + expectedEvent.nodeName + "' is not processed")
        );
    }
}
