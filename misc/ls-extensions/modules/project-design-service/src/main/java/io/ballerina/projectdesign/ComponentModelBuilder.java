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

package io.ballerina.projectdesign;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projectdesign.ComponentModel.PackageId;
import io.ballerina.projectdesign.generators.entity.EntityModelGenerator;
import io.ballerina.projectdesign.generators.service.ServiceModelGenerator;
import io.ballerina.projectdesign.model.entity.Entity;
import io.ballerina.projectdesign.model.service.Service;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Package;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Construct component model fpr project with multiple service.
 *
 * @since 2201.2.2
 */
public class ComponentModelBuilder {

    public ComponentModel constructComponentModel(Package currentPackage) {

        Map<String, Service> services = new HashMap<>();
        // todo: Change to TypeDefinition
        Map<String, Entity> entities = new HashMap<>();

        PackageId packageId = new PackageId(currentPackage);

        currentPackage.modules().forEach(module -> {
            Path moduleRootPath = module.project().sourceRoot().toAbsolutePath();
            if (module.moduleName().moduleNamePart() != null) {
                moduleRootPath = moduleRootPath.resolve(module.moduleName().moduleNamePart());
            }
            Collection<DocumentId> documentIds = module.documentIds();
            SemanticModel currentSemanticModel =
                    currentPackage.getCompilation().getSemanticModel(module.moduleId());
            // todo : Check project diagnostics
            ServiceModelGenerator serviceModelGenerator = new ServiceModelGenerator(
                    currentSemanticModel, packageId, moduleRootPath);
            services.putAll(serviceModelGenerator.generate(documentIds, module, currentPackage));

            EntityModelGenerator entityModelGenerator = new EntityModelGenerator(
                    currentSemanticModel, packageId, moduleRootPath);
            entities.putAll(entityModelGenerator.generate());
        });

        return new ComponentModel(packageId, services, entities);
    }
}
