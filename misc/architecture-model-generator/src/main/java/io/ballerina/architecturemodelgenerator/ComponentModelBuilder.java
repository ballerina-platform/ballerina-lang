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

package io.ballerina.architecturemodelgenerator;

import io.ballerina.architecturemodelgenerator.ComponentModel.PackageId;
import io.ballerina.architecturemodelgenerator.diagnostics.ComponentModelingDiagnostics;
import io.ballerina.architecturemodelgenerator.diagnostics.DiagnosticMessage;
import io.ballerina.architecturemodelgenerator.diagnostics.DiagnosticNode;
import io.ballerina.architecturemodelgenerator.generators.entity.EntityModelGenerator;
import io.ballerina.architecturemodelgenerator.generators.service.ServiceModelGenerator;
import io.ballerina.architecturemodelgenerator.model.entity.Entity;
import io.ballerina.architecturemodelgenerator.model.service.Service;
import io.ballerina.projects.Package;
import io.ballerina.projects.PackageCompilation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Construct component model fpr project with multiple service.
 *
 * @since 2201.2.2
 */
public class ComponentModelBuilder {

    public ComponentModel constructComponentModel(Package currentPackage) {
        return constructComponentModel(currentPackage, null);
    }

    public ComponentModel constructComponentModel(Package currentPackage, PackageCompilation packageCompilation) {
        Map<String, Service> services = new HashMap<>();
        // todo: Change to TypeDefinition
        Map<String, Entity> entities = new HashMap<>();
        List<ComponentModelingDiagnostics> diagnostics = new ArrayList<>();
        PackageId packageId = new PackageId(currentPackage);
        AtomicBoolean hasDiagnosticErrors = new AtomicBoolean(false);

        currentPackage.modules().forEach(module -> {
            PackageCompilation currentPackageCompilation = packageCompilation == null ?
                    currentPackage.getCompilation() : packageCompilation;
            if (currentPackageCompilation.diagnosticResult().hasErrors() && !hasDiagnosticErrors.get()) {
                hasDiagnosticErrors.set(true);
            }

            ServiceModelGenerator serviceModelGenerator = new ServiceModelGenerator(currentPackageCompilation, module);
            try {
                services.putAll(serviceModelGenerator.generate());
            } catch (Throwable e) {
                // Even though catching Throwable is not recommended,
                // we are doing it here to avoid any unexpected errors as this is critical.
                DiagnosticMessage message =
                        DiagnosticMessage.failedToGenerate(DiagnosticNode.SERVICES, e.getMessage());
                ComponentModelingDiagnostics diagnostic = new ComponentModelingDiagnostics(
                        message.getCode(), message.getDescription(), message.getSeverity(), null, null
                );
                diagnostics.add(diagnostic);
            }

            EntityModelGenerator entityModelGenerator = new EntityModelGenerator(currentPackageCompilation, module);
            try {
                entities.putAll(entityModelGenerator.generate());
            } catch (Throwable e) {
                DiagnosticMessage message =
                        DiagnosticMessage.failedToGenerate(DiagnosticNode.ENTITIES, e.getMessage());
                ComponentModelingDiagnostics diagnostic = new ComponentModelingDiagnostics(
                        message.getCode(), message.getDescription(), message.getSeverity(), null, null
                );
                diagnostics.add(diagnostic);
            }
        });

        return new ComponentModel(packageId, hasDiagnosticErrors.get(), diagnostics, services, entities);
    }
}
