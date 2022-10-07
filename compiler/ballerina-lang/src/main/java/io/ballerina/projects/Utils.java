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

package io.ballerina.projects;

import io.ballerina.projects.internal.DocumentData;
import io.ballerina.projects.internal.ModuleData;
import io.ballerina.projects.internal.ProjectFiles;
import io.ballerina.projects.util.ProjectConstants;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Compiler plugin related util methods. We have to introduce this due to the
 * usage of package-private methods from ModuleContext
 *
 * @since 2.3.0
 */
class Utils {

    static void writeModule(Module module, Path modulesRoot) throws IOException {
        Path moduleDirPath = modulesRoot.resolve(module.descriptor().name().moduleNamePart());
        Files.createDirectories(moduleDirPath);
        for (DocumentId documentId : module.documentIds()) {
            Document document = module.document(documentId);
            Files.writeString(moduleDirPath.resolve(document.name()), document.syntaxTree().toString());
        }

        Path moduleTestDirPath = moduleDirPath.resolve(ProjectConstants.TEST_DIR_NAME);
        for (DocumentId documentId : module.testDocumentIds()) {
            Files.createDirectories(moduleTestDirPath);
            Document document = module.document(documentId);
            Files.writeString(moduleTestDirPath.resolve(document.name()), document.syntaxTree().toString());
        }

        Path moduleResourcesDirPath = moduleDirPath.resolve(ProjectConstants.RESOURCE_DIR_NAME);
        for (DocumentId resourceId : module.resourceIds()) {
            Files.createDirectories(moduleTestDirPath);
            Resource resource = module.resource(resourceId);
            Files.write(moduleResourcesDirPath.resolve(resource.name()), resource.content());
        }
    }

    static ModuleConfig createModuleConfig (String moduleName, Project project) {
        ModuleData moduleData = ProjectFiles.loadModule(
                project.sourceRoot().resolve(ProjectConstants.GENERATED_MODULES_ROOT).resolve(moduleName));
        ModuleId moduleId = ModuleId.create(moduleName, project.currentPackage().packageId());
        List<DocumentConfig> documentConfigs = new ArrayList<>();
        List<DocumentConfig> testDocumentConfigs = new ArrayList<>();
        for (DocumentData sourceDoc : moduleData.sourceDocs()) {
            DocumentId documentId = DocumentId.create(sourceDoc.name(), moduleId);
            documentConfigs.add(DocumentConfig.from(documentId, sourceDoc.content(), sourceDoc.name()));
        }
        for (DocumentData sourceDoc : moduleData.testSourceDocs()) {
            DocumentId documentId = DocumentId.create(sourceDoc.name(), moduleId);
            testDocumentConfigs.add(DocumentConfig.from(documentId, sourceDoc.content(), sourceDoc.name()));
        }
        ModuleDescriptor moduleDescriptor = ModuleDescriptor.from(
                ModuleName.from(project.currentPackage().packageName(), moduleName),
                project.currentPackage().descriptor());
        return ModuleConfig.from(
                moduleId, moduleDescriptor, documentConfigs, testDocumentConfigs, null, new ArrayList<>(),
                ModuleKind.COMPILER_GENERATED);
    }
}
