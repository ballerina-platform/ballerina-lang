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
import io.ballerina.compiler.syntax.tree.ArrayTypeDescriptorNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.UnionTypeDescriptorNode;
import org.ballerinalang.debugadapter.EvaluationContext;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;
import org.ballerinalang.debugadapter.utils.PackageUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.ballerinalang.debugadapter.evaluation.EvaluationException.createEvaluationException;
import static org.ballerinalang.debugadapter.evaluation.EvaluationExceptionKind.TYPE_RESOLVING_ERROR;
import static org.ballerinalang.debugadapter.utils.PackageUtils.INIT_TYPE_INSTANCE_PREFIX;

/**
 * Ballerina type resolver implementation for resolving ballerina runtime types from the syntax nodes.
 *
 * @since 2.0.0
 */
public class NodeBasedTypeResolver extends EvaluationTypeResolver<Node> {

    public NodeBasedTypeResolver(EvaluationContext context) {
        super(context);
    }

    /**
     * Returns the runtime type instance which is resolved from the syntax tree node of the type descriptor.
     *
     * @param typeDescriptor type descriptor string
     * @return a collection of resolved types
     * @throws EvaluationException if unsupported type(s) found
     */
    @Override
    public List<Value> resolve(Node typeDescriptor) throws EvaluationException {
        List<Value> resolvedTypes = new ArrayList<>();
        if (typeDescriptor instanceof UnionTypeDescriptorNode unionTypeDesc) {
            // If the type is a union, resolves sub types recursively.
            resolvedTypes.addAll(resolve(unionTypeDesc.leftTypeDesc()));
            resolvedTypes.addAll(resolve(unionTypeDesc.rightTypeDesc()));
        } else if (typeDescriptor instanceof ArrayTypeDescriptorNode arrayTypeDesc) {
            Value elementType = resolveSingleType(arrayTypeDesc.memberTypeDesc());
            resolvedTypes.add(createBArrayType(elementType));
        } else {
            resolvedTypes.add(resolveSingleType(typeDescriptor));
        }

        return resolvedTypes;
    }

    private Value resolveSingleType(Node type) throws EvaluationException {
        if (type.kind() == SyntaxKind.SIMPLE_NAME_REFERENCE) {
            return resolveUserDefinedType((SimpleNameReferenceNode) type);
        } else if (type.kind() == SyntaxKind.QUALIFIED_NAME_REFERENCE) {
            return resolveQualifiedType((QualifiedNameReferenceNode) type);
        } else {
            // If the type is not related to above named type, tries to get the type by assuming as a in-built type.
            Optional<Value> result = resolveInbuiltType(type.toSourceCode().trim());
            if (result.isPresent()) {
                return result.get();
            }
        }

        throw createEvaluationException(TYPE_RESOLVING_ERROR, type.toSourceCode().trim());
    }

    private Value resolveUserDefinedType(SimpleNameReferenceNode node) throws EvaluationException {
        String typeName = node.name().text().trim();
        Optional<Symbol> typeDefinition = getModuleTypeDefinitionSymbol(typeName);
        if (typeDefinition.isEmpty()) {
            throw createEvaluationException(TYPE_RESOLVING_ERROR, typeName);
        }

        String packageInitClass = PackageUtils.getQualifiedClassName(context, PackageUtils.INIT_CLASS_NAME);
        List<ReferenceType> classRef = context.getAttachedVm().classesByName(packageInitClass);
        if (classRef.isEmpty()) {
            throw createEvaluationException(TYPE_RESOLVING_ERROR, typeName);
        }

        Field typeField = classRef.get(0).fieldByName(INIT_TYPE_INSTANCE_PREFIX + typeName);
        if (typeField == null) {
            throw createEvaluationException(TYPE_RESOLVING_ERROR, typeName);
        }
        return classRef.get(0).getValue(typeField);
    }

    private Value resolveQualifiedType(QualifiedNameReferenceNode qualifiedNameRef) throws EvaluationException {
        String modulePrefix = qualifiedNameRef.modulePrefix().text().trim();
        String typeName = qualifiedNameRef.identifier().text().trim();
        Optional<Value> qualifiedType = resolveQualifiedType(modulePrefix, typeName);
        if (qualifiedType.isEmpty()) {
            throw createEvaluationException(TYPE_RESOLVING_ERROR, qualifiedNameRef.toSourceCode().trim());
        }

        return qualifiedType.get();
    }
}
