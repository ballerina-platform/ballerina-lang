/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * {@code ModuleConfig} contains necessary configuration elements required to create an instance of a {@code Module}.
 *
 * @since 2.0.0
 */
public class ModuleConfig {
    // This class should contain project-agnostic information
    private final ModuleId moduleId;
    private final ModuleName moduleName;
    private final Path moduleDirPath;
    private final List<DocumentConfig> srcDocs;
    private final List<DocumentConfig> testSrcDocs;

    public byte[] birBytes = new byte[0];

    private ModuleConfig(ModuleId moduleId,
                         ModuleName moduleName,
                         Path moduleDirPath,
                         List<DocumentConfig> srcDocs,
                         List<DocumentConfig> testSrcDocs) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
        this.moduleDirPath = moduleDirPath;
        this.srcDocs = srcDocs;
        this.testSrcDocs = testSrcDocs;
    }

    public static ModuleConfig from(ModuleId moduleId,
                                    ModuleName moduleName,
                                    Path moduleDirPath,
                                    List<DocumentConfig> srcDocs,
                                    List<DocumentConfig> testSrcDocs) {
        return new ModuleConfig(moduleId, moduleName, moduleDirPath, srcDocs, testSrcDocs);
    }

    public ModuleId moduleId() {
        return moduleId;
    }

    public ModuleName moduleName() {
        return moduleName;
    }

    public boolean isDefaultModule() {
        return moduleName.isDefaultModuleName();
    }

    public Optional<Path> moduleDirectoryPath() {
        return Optional.ofNullable(moduleDirPath);
    }

    public List<DocumentConfig> sourceDocs() {
        return srcDocs;
    }

    public List<DocumentConfig> testSourceDocs() {
        return testSrcDocs;
    }
}
