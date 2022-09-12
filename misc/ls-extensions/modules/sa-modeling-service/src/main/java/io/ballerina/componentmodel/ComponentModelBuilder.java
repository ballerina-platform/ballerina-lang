/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.componentmodel;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import io.ballerina.componentmodel.ComponentModel.PackageId;
import io.ballerina.componentmodel.entitymodel.EntityModelGenerator;
import io.ballerina.componentmodel.entitymodel.components.Entity;
import io.ballerina.componentmodel.servicemodel.components.Service;
import io.ballerina.componentmodel.servicemodel.nodevisitors.ServiceDeclarationNodeVisitor;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;
import io.ballerina.projects.Project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Construct component model fpr project with multiple service.
 */
public class ComponentModelBuilder {

    public ComponentModel constructComponentModel(Project project) {
        List<Service> services = new ArrayList<>();
        Map<String, Entity> entities = new HashMap<>();

        // get project from the workspace
        Package currentPackage = project.currentPackage();

        String packageName = String.valueOf(currentPackage.packageName());
        String packageOrg = String.valueOf(currentPackage.packageOrg());
        String packageVersion = String.valueOf(currentPackage.packageVersion());

        PackageId packageId = new PackageId(packageName, packageOrg, packageVersion);

        // need to escape tests ? test directory is not getting listed as another module
        currentPackage.modules().forEach(module -> {
            Collection<DocumentId> documentIds = module.documentIds();
            SemanticModel currentSemanticModel = currentPackage.getCompilation().getSemanticModel(module.moduleId());
            for (DocumentId documentId : documentIds) {
                SyntaxTree syntaxTree = module.document(documentId).syntaxTree();
                ServiceDeclarationNodeVisitor serviceNodeVisitor = new
                        ServiceDeclarationNodeVisitor(currentSemanticModel);
                syntaxTree.rootNode().accept(serviceNodeVisitor);
                services.addAll(serviceNodeVisitor.getServices());
            }

            EntityModelGenerator entityModelConstructor = new EntityModelGenerator();
            entityModelConstructor.generateEntityModel(currentSemanticModel, entities, packageId);
        });
        return new ComponentModel(packageId, services, entities);
    }
}
