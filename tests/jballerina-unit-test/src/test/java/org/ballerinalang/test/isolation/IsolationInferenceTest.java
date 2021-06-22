/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.isolation;

import io.ballerina.runtime.api.TypeTags;
import io.ballerina.runtime.api.flags.SymbolFlags;
import io.ballerina.runtime.api.types.MethodType;
import io.ballerina.runtime.api.types.ObjectType;
import io.ballerina.runtime.api.types.ResourceMethodType;
import io.ballerina.runtime.api.types.ServiceType;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.types.TypedescType;
import io.ballerina.runtime.api.utils.TypeUtils;
import io.ballerina.runtime.api.values.BObject;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.types.BObjectType;
import org.ballerinalang.test.BCompileUtil;
import org.ballerinalang.test.BRunUtil;
import org.ballerinalang.test.CompileResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases related to isolation inference.
 *
 * @since 2.0.0
 */
public class IsolationInferenceTest {

    private CompileResult result;

    @BeforeClass
    public void setup() {
        result = BCompileUtil.compile("test-src/isolation-analysis/isolation_inference.bal");
    }

    @Test(dataProvider = "isolationInferenceTests")
    public void testIsolatedFunctions(String function) {
        BRunUtil.invoke(result, function);
    }

    @DataProvider(name = "isolationInferenceTests")
    public Object[] isolationInferenceTests() {
        return new Object[]{
                "testBasicFunctionIsolationInference",
                "testRecursiveFunctionIsolationInference",
                "testMethodIsolationInference",
                "testFunctionPointerIsolationInference",
                "testServiceClassMethodIsolationInference",
                "testObjectConstructorIsolatedInference",
                "testFunctionsAccessingModuleLevelVarsIsolatedInference"
        };
    }

    @Test
    public void testIsolationInferenceTypeCheckingNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_inference_type_checking_negative.bal");
        int i = 0;
        validateError(result, i++, "incompatible types: expected 'isolated function', found 'function () returns " +
                "(string)'", 21, 25);
        assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolationInferenceIsolationAnalysisNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_inference_isolation_analysis_negative.bal");
        int i = 0;
        validateError(result, i++, "invalid invocation of a non-isolated function in an 'isolated' function", 20, 18);
        assertEquals(result.getErrorCount(), i);
    }

    @Test
    public void testIsolatedInferenceWithObjects() {
        CompileResult result = BCompileUtil.compile("test-src/isolation-analysis/isolation_inference_with_objects.bal");
        BRunUtil.invoke(result, "testIsolatedInference");
    }

    @Test
    public void testIsolatedInferenceWithObjectsNegative() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_inference_with_objects_runtime_negative.bal");
        BRunUtil.invoke(result, "testIsolatedInference");
    }

    @Test
    public void testIsolatedInferenceWithVariables() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_inference_with_variables.bal");
        BRunUtil.invoke(result, "testIsolatedInference");
    }

    // This is called from the test file via the attach method of the listener.
    public static Object testServiceDeclarationMethodIsolationInference(BObject listener, BObject s, Object name) {
        assertTrue(isResourceIsolated(s, "get", "foo"));
        assertFalse(isResourceIsolated(s, "get", "bar"));
        assertTrue(isRemoteMethodIsolated(s, "baz"));
        assertFalse(isRemoteMethodIsolated(s, "qux"));
        assertTrue(isMethodIsolated(s, "quux"));
        assertFalse(isMethodIsolated(s, "quuz"));
        return null;
    }

    @AfterClass
    public void tearDown() {
        result = null;
    }

    public static boolean isResourceIsolated(Object val, BString resourceMethodName, BString resourcePath) {
        return isResourceIsolated(val, resourceMethodName.getValue(), resourcePath.getValue());
    }

    private static boolean isResourceIsolated(Object val, String resourceMethodNameString, String resourcePathString) {
        for (ResourceMethodType resourceMethodType : ((ServiceType) getType(val)).getResourceMethods()) {
            if (resourceMethodType.getAccessor().equals(resourceMethodNameString) &&
                    resourceMethodType.getResourcePath()[0].equals(resourcePathString)) {
                return SymbolFlags.isFlagOn(resourceMethodType.getFlags(), SymbolFlags.ISOLATED);
            }
        }

        return false;
    }

    public static boolean isRemoteMethodIsolated(Object val, BString methodName) {
        return isRemoteMethodIsolated(val, methodName.getValue());
    }

    public static boolean isRemoteMethodIsolated(Object val, String methodName) {
        ObjectType type = (ObjectType) getType(val);
        if (SymbolFlags.isFlagOn(type.getFlags(), SymbolFlags.SERVICE)) {
            return isIsolated(Lists.of(((ServiceType) type).getRemoteMethods()), methodName);
        }

        List<MethodType> remoteMethods = new ArrayList<>();
        for (MethodType method : type.getMethods()) {
            if (SymbolFlags.isFlagOn(method.getFlags(), SymbolFlags.REMOTE)) {
                remoteMethods.add(method);
            }
        }
        return isIsolated(remoteMethods, methodName);
    }

    public static boolean isMethodIsolated(Object val, BString methodName) {
        return isMethodIsolated(val, methodName.getValue());
    }

    public static boolean isMethodIsolated(Object val, String methodName) {
        BObjectType objectType = (BObjectType) getType(val);

        List<MethodType> methodTypes = Lists.of(objectType.getMethods());

        MethodType initializer = objectType.initializer;
        if (initializer != null) {
            methodTypes.add(initializer);
        }
        return isIsolated(methodTypes, methodName);
    }

    private static Type getType(Object val) {
        Type type = TypeUtils.getType(val);
        if (type.getTag() != TypeTags.TYPEDESC_TAG) {
            return type;
        }
        return ((TypedescType) type).getConstraint();
    }

    private static boolean isIsolated(List<MethodType> methods, String methodNameString) {
        for (MethodType methodType : methods) {
            if (methodType.getName().equals(methodNameString)) {
                return SymbolFlags.isFlagOn(methodType.getFlags(), SymbolFlags.ISOLATED);
            }
        }

        throw new RuntimeException("method not found: " + methodNameString);
    }
}
