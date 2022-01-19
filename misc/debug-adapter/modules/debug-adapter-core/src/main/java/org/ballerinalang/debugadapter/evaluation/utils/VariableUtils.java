/*
 * Copyright (c) 2021, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.debugadapter.evaluation.utils;

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.jdi.JdiProxyException;
import org.ballerinalang.debugadapter.jdi.LocalVariableProxyImpl;
import org.ballerinalang.debugadapter.utils.PackageUtils;
import org.ballerinalang.debugadapter.variable.BVariable;
import org.ballerinalang.debugadapter.variable.BVariableType;
import org.ballerinalang.debugadapter.variable.DebugVariableException;
import org.ballerinalang.debugadapter.variable.IndexedCompoundVariable;
import org.ballerinalang.debugadapter.variable.VariableFactory;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.VARIABLE_NOT_FOUND;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_DEBUGGER_RUNTIME_UTILS_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.GET_BMAP_TYPE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_VERSION_SEPARATOR_REGEX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.loadClass;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;
import static org.ballerinalang.debugadapter.variable.VariableUtils.UNKNOWN_VALUE;

/**
 * Expression evaluation related variable utils.
 *
 * @since 2.0.0
 */
public class VariableUtils {

    /**
     * Returns runtime value of the matching variable, for the given name. This method searches for a matching
     * variable in both local and global variable scopes.
     *
     * @param context suspended context
     * @param name    name of the variable to be retrieved
     * @return the JDI value instance of the Ballerina variable
     */
    public static Value fetchVariableValue(SuspendedContext context, String name) throws EvaluationException {
        Optional<BExpressionValue> bExpressionValue = searchLocalVariables(context, name);
        if (bExpressionValue.isEmpty()) {
            bExpressionValue = searchGlobalVariables(context, name);
        }
        if (bExpressionValue.isEmpty()) {
            throw createEvaluationException(VARIABLE_NOT_FOUND, name);
        }
        return bExpressionValue.get().getJdiValue();
    }

    /**
     * Returns runtime value of the matching local variable, for the given name.
     *
     * @param context       suspended context
     * @param nameReference name of the variable to be retrieved
     * @return the JDI value instance of the local variable
     */
    private static Optional<BExpressionValue> searchLocalVariables(SuspendedContext context, String nameReference) {
        try {
            LocalVariableProxyImpl jvmVar = context.getFrame().visibleVariableByName(nameReference);
            if (jvmVar != null) {
                return Optional.of(new BExpressionValue(context, context.getFrame().getValue(jvmVar)));
            }

            // As all the ballerina variables which are being used inside lambda functions are converted into maps
            // during the runtime code generation, such local variables should be accessed in a different manner.
            List<LocalVariableProxyImpl> lambdaParamMaps = context.getFrame().visibleVariables().stream()
                    .filter(org.ballerinalang.debugadapter.variable.VariableUtils::isLambdaParamMap)
                    .collect(Collectors.toList());

            Optional<Value> localVariableMatch = lambdaParamMaps.stream()
                    .map(localVariableProxy -> {
                        try {
                            Value varValue = context.getFrame().getValue(localVariableProxy);
                            BVariable mapVar = VariableFactory.getVariable(context, varValue);
                            if (mapVar == null || mapVar.getBType() != BVariableType.MAP
                                    || !(mapVar instanceof IndexedCompoundVariable)) {
                                return null;
                            }
                            return ((IndexedCompoundVariable) mapVar).getChildByName(nameReference);
                        } catch (JdiProxyException | DebugVariableException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .findAny();

            if (localVariableMatch.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(new BExpressionValue(context, localVariableMatch.get()));
        } catch (JdiProxyException e) {
            return Optional.empty();
        }
    }

    /**
     * Returns runtime value of the matching global variable, for the given name.
     *
     * @param context       suspended context
     * @param nameReference name of the variable to be retrieved
     * @return the JDI value instance of the global variable
     */
    private static Optional<BExpressionValue> searchGlobalVariables(SuspendedContext context, String nameReference) {
        String classQName = PackageUtils.getQualifiedClassName(context, INIT_CLASS_NAME);
        return getFieldValue(context, classQName, nameReference);
    }

    /**
     * Returns runtime value of the matching constant variable, for the given name.
     *
     * @param moduleSymbol  module symbol retrieved from the semantic API
     * @param nameReference name of the variable to be retrieved
     * @return the JDI value instance of the global variable
     */
    public static Optional<BExpressionValue> getModuleVariable(SuspendedContext context,
                                                                     ModuleSymbol moduleSymbol, String nameReference) {
        String classQName = getInitClassName(moduleSymbol);
        return getFieldValue(context, classQName, nameReference);
    }

    /**
     * Returns the constraint type for a given BMap value.
     *
     * @param context  suspended context
     * @param mapValue JDI value instance of the BMap
     * @return the constraint type for a given BMap value
     */
    public static String getMapType(SuspendedContext context, Value mapValue) {
        try {
            RuntimeStaticMethod getBMapType = EvaluationUtils.getRuntimeMethod(context, B_DEBUGGER_RUNTIME_UTILS_CLASS,
                    GET_BMAP_TYPE_METHOD, Collections.singletonList(EvaluationUtils.JAVA_OBJECT_CLASS));
            getBMapType.setArgValues(Collections.singletonList(mapValue));
            Value value = getBMapType.invokeSafely();
            return org.ballerinalang.debugadapter.variable.VariableUtils.getStringFrom(value);
        } catch (Exception e) {
            return UNKNOWN_VALUE;
        }
    }

    /**
     * Returns the fully-qualified name of the init class, for a given Ballerina module.
     *
     * @param moduleSymbol module symbol retrieved from the semantic API
     * @return fully-qualified class name
     */
    private static String getInitClassName(ModuleSymbol moduleSymbol) {
        StringJoiner classNameJoiner = new StringJoiner(".");
        classNameJoiner.add(moduleSymbol.id().orgName())
                .add(encodeModuleName(moduleSymbol.id().moduleName()))
                .add(moduleSymbol.id().version().split(MODULE_VERSION_SEPARATOR_REGEX)[0])
                .add(INIT_CLASS_NAME);
        return classNameJoiner.toString();
    }

    private static Optional<BExpressionValue> getFieldValue(SuspendedContext context, String qualifiedClassName,
                                                            String fieldName) {

        List<ReferenceType> classesRef = context.getAttachedVm().classesByName(qualifiedClassName);
        // Tries to load the required class instance using "java.lang.Class.forName()" method.
        if (classesRef == null || classesRef.isEmpty()) {
            try {
                classesRef = Collections.singletonList(loadClass(context, qualifiedClassName, ""));
            } catch (EvaluationException e) {
                return Optional.empty();
            }
        }
        if (classesRef.size() != 1) {
            return Optional.empty();
        }

        Field field = classesRef.get(0).fieldByName(fieldName);
        if (field == null) {
            return Optional.empty();
        }
        return Optional.of(new BExpressionValue(context, classesRef.get(0).getValue(field)));
    }

    private VariableUtils() {
    }
}
