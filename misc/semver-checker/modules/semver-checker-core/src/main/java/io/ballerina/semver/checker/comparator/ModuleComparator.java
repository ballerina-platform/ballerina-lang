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

import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.ModuleMemberDeclarationNode;
import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.Module;
import io.ballerina.semver.checker.diff.DiffExtractor;
import io.ballerina.semver.checker.diff.ModuleDiff;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getFunctionIdentifier;
import static io.ballerina.semver.checker.util.SyntaxTreeUtils.getServiceIdentifier;

/**
 * Comparator implementation for Ballerina modules.
 *
 * @since 2201.2.0
 */
public class ModuleComparator implements Comparator {

    private final Module newModule;
    private final Module oldModule;
    private final Map<String, FunctionDefinitionNode> newFunctions;
    private final Map<String, FunctionDefinitionNode> oldFunctions;
    private final Map<String, ServiceDeclarationNode> newServices;
    private final Map<String, ServiceDeclarationNode> oldServices;

    public ModuleComparator(Module newModule, Module oldModule) {
        this.newModule = newModule;
        this.oldModule = oldModule;
        this.newFunctions = new HashMap<>();
        this.oldFunctions = new HashMap<>();
        this.newServices = new HashMap<>();
        this.oldServices = new HashMap<>();
    }

    @Override
    public Optional<ModuleDiff> computeDiff() {
        ModuleDiff.Builder moduleDiffBuilder = new ModuleDiff.Builder(newModule, oldModule);
        extractModuleLevelDefinitions(newModule, true);
        extractModuleLevelDefinitions(oldModule, false);

        extractFunctionDiffs(moduleDiffBuilder);
        extractServiceDiffs(moduleDiffBuilder);
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
                        if (serviceName.isEmpty()) {
                            // services that does not contain a unique identifier(usually the base path),
                            // can not be compared and therefore will be ignored.
                            // Todo - throw a warning/improve detection
                            break;
                        } else {
                            if (isNewModule) {
                                newServices.put(serviceName.get(), serviceNode);
                            } else {
                                oldServices.put(serviceName.get(), serviceNode);
                            }
                        }
                        break;
                    case CLASS_DEFINITION:
                    case TYPE_DEFINITION:
                    case MODULE_VAR_DECL:
                    case CONST_DECLARATION:
                    case LIST_CONSTRUCTOR:
                    case ENUM_DECLARATION:
                    default:
                        // Todo: implement
                }
            }
        });
    }
}
