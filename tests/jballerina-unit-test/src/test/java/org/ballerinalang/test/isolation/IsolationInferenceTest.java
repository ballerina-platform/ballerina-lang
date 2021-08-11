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
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BVarSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.tree.BLangVariable;
import org.wso2.ballerinalang.util.Flags;
import org.wso2.ballerinalang.util.Lists;

import java.util.ArrayList;
import java.util.List;

import static org.ballerinalang.test.BAssertUtil.validateError;
import static org.ballerinalang.test.BAssertUtil.validateWarning;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Test cases related to isolation inference.
 *
 * @since 2.0.0
 */
public class IsolationInferenceTest {

    private static final String NON_ISOLATED_SERVICE_AND_METHOD_WARNING = "concurrent calls will not be made to this " +
            "method since the service and the method are not 'isolated'";

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
                "testFunctionsAccessingModuleLevelVarsIsolatedInference",
                "testFunctionCallingFunctionWithIsolatedParamAnnotatedParam"
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

    @Test(dataProvider = "testIsolatedInferenceWithObjectsNegativeFiles")
    public void testIsolatedInferenceWithObjectsNegative(String fileName) {
        CompileResult result = BCompileUtil.compile("test-src/isolation-analysis/" + fileName);
        BRunUtil.invoke(result, "testIsolatedInference");
    }

    @DataProvider
    private Object[] testIsolatedInferenceWithObjectsNegativeFiles() {
        return new String[]{
                "isolation_inference_with_objects_runtime_negative_1.bal",
                "isolation_inference_with_objects_runtime_negative_2.bal"
        };
    }

    @Test
    public void testNotSettingIsolatedFlagToFinalAndIsolatedObjectOrReadOnlyVariable() {
        CompileResult result = BCompileUtil.compile("test-src/isolation-analysis/isolation_inference_flags.bal");

        long lnFlags = 0;
        long xFlags = 0;
        long yFlags = 0;
        long zFlags = 0;

        for (BLangVariable variable : result.getAST().getGlobalVariables()) {
            BVarSymbol symbol = variable.symbol;
            String name = symbol.name.value;
            switch (name) {
                case "ln":
                    lnFlags = symbol.flags;
                    continue;
                case "x":
                    xFlags = symbol.flags;
                    continue;
                case "y":
                    yFlags = symbol.flags;
                    continue;
                case "z":
                    zFlags = symbol.flags;
            }
        }

        Assert.assertTrue(Symbols.isFlagOn(lnFlags, Flags.FINAL));
        Assert.assertTrue(Symbols.isFlagOn(lnFlags, Flags.LISTENER));
        Assert.assertTrue(Symbols.isFlagOn(xFlags, Flags.FINAL));
        Assert.assertTrue(Symbols.isFlagOn(yFlags, Flags.FINAL));
        Assert.assertTrue(Symbols.isFlagOn(zFlags, Flags.FINAL));
        // Variable shouldn't be inferred as isolated, since it is a final variable of an isolated object or readonly
        // type it can be accessed outside a lock statement.
        Assert.assertFalse(Symbols.isFlagOn(lnFlags, Flags.ISOLATED));
        Assert.assertFalse(Symbols.isFlagOn(xFlags, Flags.ISOLATED));
        Assert.assertFalse(Symbols.isFlagOn(yFlags, Flags.ISOLATED));
        Assert.assertFalse(Symbols.isFlagOn(zFlags, Flags.ISOLATED));
    }

    @Test(dataProvider = "testIsolatedInferenceWithVariablesFiles")
    public void testIsolatedInferenceWithVariables(String fileName) {
        CompileResult result = BCompileUtil.compile("test-src/isolation-analysis/" + fileName);
        BRunUtil.invoke(result, "testIsolatedInference");
    }

    @DataProvider
    private Object[] testIsolatedInferenceWithVariablesFiles() {
        return new String[]{
                "isolation_inference_with_variables_1.bal",
                "isolation_inference_with_variables_2.bal"
        };
    }

    @Test
    public void testIsolatedInferenceForObjectInitMethod() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_inference_for_object_init.bal");
        BRunUtil.invoke(result, "testIsolatedInference");
    }

    @Test
    public void testIsolatedNonInferencePubliclyExposedConstructs() {
        CompileResult result = BCompileUtil.compile(
                "test-src/isolation-analysis/isolation_non_inference_with_publicly_exposed_constructs.bal");
        int i = 0;
        validateError(result, i++, getAttemptToExposeSymbolError("NonPubliclyExposedInferredClassUsedInNonPublicTypes"),
                93, 5);
        validateError(result, i++, getAttemptToExposeSymbolError("NonPubliclyExposedInferredClassUsedInNonPublicTypes"),
                95, 43);
        validateError(result, i++, getAttemptToExposeSymbolError(
                "NonPubliclyExposedInferredReadOnlyClassUsedInNonPublicTypes"), 96, 45);
        validateError(result, i++, getAttemptToExposeSymbolError("ClassPubliclyExposedViaVariable"), 141, 1);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 147, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 153, 5);
        validateError(result, i++, getAttemptToExposeSymbolError("PubliclyExposedInferableClassUsedInRecord"), 158, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 165, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 171, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 184, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 190, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 200, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 206, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 214, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 220, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 235, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 241, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 249, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 255, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 263, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 269, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 277, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 283, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 293, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 299, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 310, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 316, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 326, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 332, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 342, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 348, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 358, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 364, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 381, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 387, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 395, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 401, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 409, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 415, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 423, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 429, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 437, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 443, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 451, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 457, 5);
        validateError(result, i++, getAttemptToExposeSymbolError("PubliclyExposedInferableClassUsedInClassMethodParam"),
                464, 5);
        validateError(result, i++, getAttemptToExposeSymbolError("PubliclyExposedInferableClassUsedInClassMethodParam"),
                466, 43);
        validateError(result, i++, getAttemptToExposeSymbolError(
                "PubliclyExposedInferableClassUsedInClassMethodReturnType"), 467, 45);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 474, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 480, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 488, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 494, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 505, 5);
        validateWarning(result, i++, NON_ISOLATED_SERVICE_AND_METHOD_WARNING, 511, 5);
        validateError(result, i++, getAttemptToExposeSymbolError("PubliclyExposedInferableClassUsedInFunctionParam"),
                515, 1);
        validateError(result, i++, getAttemptToExposeSymbolError("PubliclyExposedInferableClassUsedInFunctionParam"),
                517, 39);
        validateError(result, i++, getAttemptToExposeSymbolError(
                "PubliclyExposedInferableClassUsedInFunctionReturnType"), 518, 41);
        assertEquals(result.getDiagnostics().length, i);
    }

    private String getAttemptToExposeSymbolError(String symbol) {
        return "attempt to expose non-public symbol '" + symbol + "'";
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

    public static void f2(BObject val) {
    }
}
