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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.Field;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.Value;
import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.Qualifiable;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.SuspendedContext;
import org.ballerinalang.debugadapter.evaluation.BExpressionValue;
import org.ballerinalang.debugadapter.evaluation.BImport;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.evaluation.IdentifierModifier;
import org.ballerinalang.debugadapter.evaluation.engine.invokable.RuntimeStaticMethod;
import org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.NON_PUBLIC_OR_UNDEFINED_ACCESS;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.TYPE_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeIdentifier;
import static org.ballerinalang.debugadapter.evaluation.IdentifierModifier.encodeModuleName;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_ARRAY_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_CREATOR_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.B_TYPE_UTILS_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_ARRAY_TYPE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.CREATE_UNION_TYPE_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.JAVA_STRING_CLASS;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.MODULE_VERSION_SEPARATOR_REGEX;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.VALUE_FROM_STRING_METHOD;
import static org.ballerinalang.debugadapter.evaluation.utils.EvaluationUtils.getRuntimeMethod;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_CLASS_NAME;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_TYPE_INSTANCE_PREFIX;

/**
 * Ballerina type resolver implementation for resolving ballerina runtime types using type descriptors.
 *
 * @param <T> Type descriptor type (e.g. syntax tree node, string)
 * @since 2.0.0
 */
public abstract class EvaluationTypeResolver<T> {

    protected final SuspendedContext context;
    protected final Map<String, BImport> resolvedImports;

    protected EvaluationTypeResolver(EvaluationContext evaluationContext) {
        this.context = evaluationContext.getSuspendedContext();
        this.resolvedImports = evaluationContext.getResolvedImports();
    }

    /**
     * Returns the runtime type instance which is resolved using the type information with constraint T (e.g. a type
     * descriptor node from the syntax tree, type name string, etc.).
     *
     * @param typeDescriptor type descriptor
     * @return a collection of resolved types
     * @throws EvaluationException if unsupported type(s) found
     */
    public abstract List<Value> resolve(T typeDescriptor) throws EvaluationException;

    /**
     * Creates a 'BUnionType' instance by combining all member types.
     *
     * @param resolvedTypes member types
     * @return a 'BUnionType' instance by combining all its member types
     */
    public Value getUnionTypeFrom(List<Value> resolvedTypes) throws EvaluationException {
        List<String> methodArgTypeNames = new ArrayList<>();
        methodArgTypeNames.add(B_TYPE_ARRAY_CLASS);
        RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_CREATOR_CLASS, CREATE_UNION_TYPE_METHOD,
                methodArgTypeNames);
        List<Value> methodArgs = new ArrayList<>(resolvedTypes);
        method.setArgValues(methodArgs);
        return method.invokeSafely();
    }

    protected Optional<Value> resolveInbuiltType(String typeName) {
        try {
            List<String> methodArgTypeNames = Collections.singletonList(JAVA_STRING_CLASS);
            RuntimeStaticMethod method = getRuntimeMethod(context, B_TYPE_UTILS_CLASS, VALUE_FROM_STRING_METHOD,
                    methodArgTypeNames);
            method.setArgValues(Collections.singletonList(context.getAttachedVm().mirrorOf(typeName)));
            return Optional.of(new BExpressionValue(context, method.invokeSafely()).getJdiValue());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    protected Optional<Symbol> getModuleTypeDefinitionSymbol(String typeName) {
        SemanticModel semanticContext = context.getDebugCompiler().getSemanticInfo();
        return semanticContext.moduleSymbols().stream()
                .filter(symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS)
                .filter(symbol -> encodeIdentifier(symbol.getName().get(), IdentifierModifier.IdentifierType.OTHER)
                        .equals(typeName))
                .findAny();
    }

    protected Optional<Symbol> getQualifiedTypeDefinitionSymbol(String modulePrefix, String typeName) {
        return this.resolvedImports.get(modulePrefix).getResolvedSymbol().allSymbols().stream()
                .filter(symbol -> symbol.kind() == SymbolKind.TYPE_DEFINITION || symbol.kind() == SymbolKind.CLASS)
                .filter(symbol -> encodeIdentifier(symbol.getName().get(), IdentifierModifier.IdentifierType.OTHER)
                        .equals(typeName))
                .findAny();
    }

    protected String constructInitClassNameFrom(Symbol typeSymbol) {
        ModuleID moduleMeta = typeSymbol.getModule().get().id();
        return new StringJoiner(".")
                .add(encodeModuleName(moduleMeta.orgName()))
                .add(encodeModuleName(moduleMeta.moduleName()))
                .add(moduleMeta.version().split(MODULE_VERSION_SEPARATOR_REGEX)[0])
                .add(INIT_CLASS_NAME)
                .toString();
    }

    protected Value createBArrayType(Value type) throws EvaluationException {
        List<String> argTypeNames = new ArrayList<>();
        argTypeNames.add(EvaluationUtils.B_TYPE_CLASS);
        RuntimeStaticMethod createArrayMethod = getRuntimeMethod(context, B_TYPE_CREATOR_CLASS,
                CREATE_ARRAY_TYPE_METHOD, argTypeNames);
        List<Value> methodArgs = new ArrayList<>();
        methodArgs.add(type);
        createArrayMethod.setArgValues(methodArgs);
        return createArrayMethod.invokeSafely();
    }

    protected Optional<Value> resolveQualifiedType(String modulePrefix, String typeName)
            throws EvaluationException {
        Optional<Symbol> typeDefinition = getQualifiedTypeDefinitionSymbol(modulePrefix, typeName);
        if (typeDefinition.isEmpty()) {
            throw createEvaluationException(NON_PUBLIC_OR_UNDEFINED_ACCESS, modulePrefix, typeName);
        } else if (!isPublicSymbol(typeDefinition.get())) {
            throw createEvaluationException(NON_PUBLIC_OR_UNDEFINED_ACCESS, modulePrefix, typeName);
        }

        String packageInitClass = constructInitClassNameFrom(typeDefinition.get());
        List<ReferenceType> classRef = context.getAttachedVm().classesByName(packageInitClass);
        if (classRef.isEmpty()) {
            throw createEvaluationException(TYPE_RESOLVING_ERROR, typeName);
        }

        Field typeField = classRef.get(0).fieldByName(INIT_TYPE_INSTANCE_PREFIX + typeName);
        if (typeField == null) {
            throw createEvaluationException(TYPE_RESOLVING_ERROR, typeName);
        }
        return Optional.ofNullable(classRef.get(0).getValue(typeField));
    }

    /**
     * Checks whether the given semantic API symbol contains 'public' qualifier.
     *
     * @param symbol semantic API symbol
     * @return true if the given semantic API symbol has 'public' qualifier
     */
    public static boolean isPublicSymbol(Symbol symbol) {
        if (symbol instanceof Qualifiable qualifiable) {
            return qualifiable.qualifiers().stream().anyMatch(qualifier -> qualifier == Qualifier.PUBLIC);
        }
        return true;
    }
}
