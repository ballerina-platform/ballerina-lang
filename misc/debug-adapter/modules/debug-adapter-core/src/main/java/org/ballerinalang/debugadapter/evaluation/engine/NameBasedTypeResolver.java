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
import io.ballerina.compiler.api.symbols.Symbol;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.TYPE_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_TYPE_INSTANCE_PREFIX;

/**
 * Ballerina type resolver implementation for resolving ballerina runtime types from type name.
 * <p>
 * Todo - try to convert into a symbol API based resolver, once
 *  https://github.com/ballerina-platform/ballerina-lang/pull/31589 is merged.
 *
 * @since 2.0.0
 */
public class NameBasedTypeResolver extends EvaluationTypeResolver<String> {

    static final String ARRAY_TYPE_SUFFIX = "\\[]$";
    private static final String UNION_TYPE_SEPARATOR_REGEX = "\\|";
    private static final String QUALIFIED_NAME_REF_SEPARATOR = ":";

    public NameBasedTypeResolver(EvaluationContext context) {
        super(context);
    }

    /**
     * Returns the runtime type instance which is resolved from the type name.
     *
     * @param typeDescriptor type descriptor string
     * @return a collection of resolved types
     * @throws EvaluationException if unsupported type(s) found
     */
    @Override
    public List<Value> resolve(String typeDescriptor) throws EvaluationException {
        List<Value> resolvedTypes = new LinkedList<>();
        // If the type is a union, resolves each sub type iteratively.
        if (typeDescriptor.contains(UNION_TYPE_SEPARATOR_REGEX)) {
            String[] unionTypes = typeDescriptor.split(UNION_TYPE_SEPARATOR_REGEX);
            for (String typeName : unionTypes) {
                resolvedTypes.add(resolveSingleType(typeName.trim()));
            }
        } else {
            resolvedTypes.add(resolveSingleType(typeDescriptor.trim()));
        }
        return resolvedTypes;
    }

    private Value resolveSingleType(String typeName) throws EvaluationException {
        boolean arrayTypeDetected = false;
        if (typeName.endsWith(ARRAY_TYPE_SUFFIX)) {
            arrayTypeDetected = true;
            typeName = typeName.replaceAll(ARRAY_TYPE_SUFFIX, "");
        }
        // Checks if the type name matches with ballerina predefined types.
        Optional<Value> result = resolveInbuiltType(typeName);
        // If any predefined type is not found, falls back to named types resolving.
        if (result.isEmpty()) {
            if (typeName.contains(QUALIFIED_NAME_REF_SEPARATOR)) {
                String[] nameRefParts = typeName.split(QUALIFIED_NAME_REF_SEPARATOR);
                result = resolveQualifiedType(nameRefParts[0].trim(), nameRefParts[1].trim());
            } else {
                result = resolveUserDefinedType(typeName);
            }
        }
        if (result.isEmpty()) {
            throw createEvaluationException(TYPE_RESOLVING_ERROR, typeName);
        }

        return arrayTypeDetected ? createBArrayType(result.get()) : result.get();
    }

    private Optional<Value> resolveUserDefinedType(String typeName) {
        Optional<Symbol> typeDefinition = getModuleTypeDefinitionSymbol(typeName);
        if (typeDefinition.isEmpty()) {
            return Optional.empty();
        }

        String packageInitClass = PackageUtils.getQualifiedClassName(context, PackageUtils.INIT_CLASS_NAME);
        List<ReferenceType> classRef = context.getAttachedVm().classesByName(packageInitClass);
        if (classRef.isEmpty()) {
            return Optional.empty();
        }

        Field typeField = classRef.get(0).fieldByName(INIT_TYPE_INSTANCE_PREFIX + typeName);
        if (typeField == null) {
            return Optional.empty();
        }
        return Optional.of(classRef.get(0).getValue(typeField));
    }
}
