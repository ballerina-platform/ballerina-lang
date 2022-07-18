/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.semver.checker.comparator;

import io.ballerina.compiler.syntax.tree.ClassDefinitionNode;
import io.ballerina.compiler.syntax.tree.ConstantDeclarationNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.compiler.syntax.tree.TypeDefinitionNode;
import io.ballerina.projects.Module;
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.ModuleDiff;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getClassIdentifier;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getConstIdentifier;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getFunctionIdentifier;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getModuleVarIdentifier;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getServiceIdentifier;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getTypeDefIdentifier;

/**
 * Comparator implementation for Ballerina modules.
 *
 * @since 2201.2.0
 */
public class ModuleComparator implements Comparator {

    private final Module newModule;
    private final Module oldModule;
    private final Map<String, FunctionDefinitionNode> newFunctions = new HashMap<>();
    private final Map<String, FunctionDefinitionNode> oldFunctions = new HashMap<>();
    private final Map<String, ServiceDeclarationNode> newServices = new HashMap<>();
    private final Map<String, ServiceDeclarationNode> oldServices = new HashMap<>();
    private final Map<String, ModuleVariableDeclarationNode> newVars = new HashMap<>();
    private final Map<String, ModuleVariableDeclarationNode> oldVars = new HashMap<>();
    private final Map<String, ConstantDeclarationNode> newConstants = new HashMap<>();
    private final Map<String, ConstantDeclarationNode> oldConstants = new HashMap<>();
    private final Map<String, ClassDefinitionNode> newClasses = new HashMap<>();
    private final Map<String, ClassDefinitionNode> oldClasses = new HashMap<>();
    private final Map<String, TypeDefinitionNode> newTypes = new HashMap<>();
    private final Map<String, TypeDefinitionNode> oldTypes = new HashMap<>();

    public ModuleComparator(Module newModule, Module oldModule) {
        this.newModule = newModule;
        this.oldModule = oldModule;
    }

    @Override
    public Optional<ModuleDiff> computeDiff() {
        ModuleDiff.Builder moduleDiffBuilder = new ModuleDiff.Builder(newModule, oldModule);
        extractModuleLevelDefinitions(newModule, true);
        extractModuleLevelDefinitions(oldModule, false);

        extractFunctionDiffs(moduleDiffBuilder);
        extractServiceDiffs(moduleDiffBuilder);
        extractModuleVarDiffs(moduleDiffBuilder);
        extractConstantDiffs(moduleDiffBuilder);
        extractClassDiffs(moduleDiffBuilder);
        extractTypeDefinitionDiffs(moduleDiffBuilder);
        // Todo: implement analyzers for other module-level definitions
        return moduleDiffBuilder.build();
    }

    private void extractFunctionDiffs(ModuleDiff.Builder diffModifier) {
        DiffExtractor<FunctionDefinitionNode> funcDiffExtractor = new DiffExtractor<>(newFunctions, oldFunctions);
        funcDiffExtractor.getAdditions().forEach((name, function) -> diffModifier.withFunctionAdded(function));
        funcDiffExtractor.getRemovals().forEach((name, function) -> diffModifier.withFunctionRemoved(function));
        funcDiffExtractor.getCommons().forEach((name, functions) -> diffModifier.withFunctionChanged(functions.getKey(),
                functions.getValue()));
    }

    private void extractServiceDiffs(ModuleDiff.Builder diffModifier) {
        DiffExtractor<ServiceDeclarationNode> serviceDiffExtractor = new DiffExtractor<>(newServices, oldServices);
        serviceDiffExtractor.getAdditions().forEach((name, service) -> diffModifier.withServiceAdded(service));
        serviceDiffExtractor.getRemovals().forEach((name, service) -> diffModifier.withServiceRemoved(service));
        serviceDiffExtractor.getCommons().forEach((name, service) -> diffModifier.withServiceChanged(service.getKey(),
                service.getValue()));
    }

    private void extractModuleVarDiffs(ModuleDiff.Builder diffModifier) {
        DiffExtractor<ModuleVariableDeclarationNode> moduleVarDiffExtractor = new DiffExtractor<>(newVars, oldVars);
        moduleVarDiffExtractor.getAdditions().forEach((name, var) -> diffModifier.withModuleVarAdded(var));
        moduleVarDiffExtractor.getRemovals().forEach((name, var) -> diffModifier.withModuleVarRemoved(var));
        moduleVarDiffExtractor.getCommons().forEach((name, var) -> diffModifier.withModuleVarChanged(var.getKey(),
                var.getValue()));
    }

    private void extractConstantDiffs(ModuleDiff.Builder diffModifier) {
        DiffExtractor<ConstantDeclarationNode> constDiffExtractor = new DiffExtractor<>(newConstants, oldConstants);
        constDiffExtractor.getAdditions().forEach((name, constant) -> diffModifier.withConstantAdded(constant));
        constDiffExtractor.getRemovals().forEach((name, constant) -> diffModifier.withConstantRemoved(constant));
        constDiffExtractor.getCommons().forEach((name, constant) -> diffModifier.withConstantChanged(constant.getKey(),
                constant.getValue()));
    }

    private void extractClassDiffs(ModuleDiff.Builder diffModifier) {
        DiffExtractor<ClassDefinitionNode> constDiffExtractor = new DiffExtractor<>(newClasses, oldClasses);
        constDiffExtractor.getAdditions().forEach((name, clazz) -> diffModifier.withClassAdded(clazz));
        constDiffExtractor.getRemovals().forEach((name, clazz) -> diffModifier.withClassRemoved(clazz));
        constDiffExtractor.getCommons().forEach((name, clazz) -> diffModifier.withClassModified(clazz.getKey(),
                clazz.getValue()));
    }

    private void extractTypeDefinitionDiffs(ModuleDiff.Builder diffModifier) {
        DiffExtractor<TypeDefinitionNode> constDiffExtractor = new DiffExtractor<>(newTypes, oldTypes);
        constDiffExtractor.getAdditions().forEach((name, type) -> diffModifier.withTypeDefAdded(type));
        constDiffExtractor.getRemovals().forEach((name, type) -> diffModifier.withTypeDefRemoved(type));
        constDiffExtractor.getCommons().forEach((name, types) -> diffModifier.withTypeDefModified(types.getKey(),
                types.getValue()));
    }

    private void extractModuleLevelDefinitions(Module module, boolean isNewModule) {
        module.documentIds().forEach(documentId -> {
            SyntaxTree documentST = module.document(documentId).syntaxTree();
            if (documentST.rootNode() == null || (documentST.rootNode().kind() != SyntaxKind.MODULE_PART)) {
                return;
            }

            NodeList<ModuleMemberDeclarationNode> members = ((ModulePartNode) documentST.rootNode()).members();
            for (ModuleMemberDeclarationNode member : members) {
                switch (member.kind()) {
                    case FUNCTION_DEFINITION:
                        FunctionDefinitionNode funcNode = (FunctionDefinitionNode) member;
                        if (isNewModule) {
                            newFunctions.put(getFunctionIdentifier(funcNode), funcNode);
                        } else {
                            oldFunctions.put(getFunctionIdentifier(funcNode), funcNode);
                        }
                        break;
                    case SERVICE_DECLARATION:
                        ServiceDeclarationNode serviceNode = (ServiceDeclarationNode) member;
                        Optional<String> serviceName = getServiceIdentifier(serviceNode);
                        if (serviceName.isPresent()) {
                            if (isNewModule) {
                                newServices.put(serviceName.get(), serviceNode);
                            } else {
                                oldServices.put(serviceName.get(), serviceNode);
                            }
                        } else {
                            // services that does not contain a unique identifier(usually the base path),
                            // can not be compared and therefore will be ignored.
                            // Todo - throw a warning/improve detection
                        }
                        break;
                    case MODULE_VAR_DECL:
                        ModuleVariableDeclarationNode varNode = (ModuleVariableDeclarationNode) member;
                        if (isNewModule) {
                            newVars.put(getModuleVarIdentifier(varNode), varNode);
                        } else {
                            oldVars.put(getModuleVarIdentifier(varNode), varNode);
                        }
                        break;
                    case CONST_DECLARATION:
                        ConstantDeclarationNode constNode = (ConstantDeclarationNode) member;
                        if (isNewModule) {
                            newConstants.put(getConstIdentifier(constNode), constNode);
                        } else {
                            oldConstants.put(getConstIdentifier(constNode), constNode);
                        }
                        break;
                    case CLASS_DEFINITION:
                        ClassDefinitionNode classNode = (ClassDefinitionNode) member;
                        if (isNewModule) {
                            newClasses.put(getClassIdentifier(classNode), classNode);
                        } else {
                            oldClasses.put(getClassIdentifier(classNode), classNode);
                        }
                        break;
                    case TYPE_DEFINITION:
                        TypeDefinitionNode typeNode = (TypeDefinitionNode) member;
                        if (isNewModule) {
                            newTypes.put(getTypeDefIdentifier(typeNode), typeNode);
                        } else {
                            oldTypes.put(getTypeDefIdentifier(typeNode), typeNode);
                        }
                        break;
                    case LISTENER_DECLARATION:
                    case ENUM_DECLARATION:
                    default:
                        // Todo: implement
                }
            }
        });
    }
}
