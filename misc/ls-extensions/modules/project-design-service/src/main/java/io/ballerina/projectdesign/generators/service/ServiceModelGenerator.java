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

package io.ballerina.projectdesign.generators.service;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.projectdesign.ComponentModel;
import io.ballerina.projectdesign.generators.service.nodevisitors.ServiceDeclarationNodeVisitor;
import io.ballerina.projectdesign.model.service.Service;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Package;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Build service model based on a given Ballerina service.
 *
 * @since 2201.2.2
 */
public class ServiceModelGenerator {

    private final SemanticModel semanticModel;
    private final ComponentModel.PackageId packageId;
    private final Path moduleRootPath;

    public ServiceModelGenerator(SemanticModel semanticModel, ComponentModel.PackageId packageId,
                                 Path moduleRootPath) {

        this.semanticModel = semanticModel;
        this.packageId = packageId;
        this.moduleRootPath = moduleRootPath;
    }

    public Map<String, Service> generate(Collection<DocumentId> documentIds, Module module, Package currentPackage) {
        Map<String, Service> services = new HashMap<>();
        for (DocumentId documentId : documentIds) {
            SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
            Path filePath = moduleRootPath.resolve(syntaxTree.filePath());
            ServiceDeclarationNodeVisitor serviceNodeVisitor = new
                    ServiceDeclarationNodeVisitor(semanticModel, currentPackage, packageId, filePath);
            syntaxTree.rootNode().accept(serviceNodeVisitor);
            serviceNodeVisitor.getServices().forEach(service -> {
                services.put(service.getServiceId(), service);

            });
        }
        return services;
    }
}
