/*
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
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

package io.ballerina.architecturemodelgenerator.generators.service;

import io.ballerina.architecturemodelgenerator.generators.ModelGenerator;
import io.ballerina.architecturemodelgenerator.generators.service.nodevisitors.ServiceDeclarationNodeVisitor;
import io.ballerina.architecturemodelgenerator.model.service.Service;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Build service model based on a given Ballerina service.
 *
 * @since 2201.2.2
 */
public class ServiceModelGenerator extends ModelGenerator {

    public ServiceModelGenerator(SemanticModel semanticModel, Module module) {
        super(semanticModel, module);
    }

    public Map<String, Service> generate() {
        Map<String, Service> services = new HashMap<>();
        for (DocumentId documentId :getModule().documentIds()) {
            SyntaxTree syntaxTree = getModule().document(documentId).syntaxTree();
            Path filePath = getModuleRootPath().resolve(syntaxTree.filePath());
            ServiceDeclarationNodeVisitor serviceNodeVisitor = new ServiceDeclarationNodeVisitor(getSemanticModel(),
                    syntaxTree, getModule().packageInstance(), filePath);
            syntaxTree.rootNode().accept(serviceNodeVisitor);
            serviceNodeVisitor.getServices().forEach(service -> {
                services.put(service.getServiceId(), service);
            });
        }
        return services;
    }
}
