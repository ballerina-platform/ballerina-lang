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
package io.ballerina.projects.directory;

import java.nio.file.Path;
import java.util.List;

/**
 * {@code ModuleFileData} represents a Ballerina module directory.
 *
 * @since 2.0.0
 */
public class ModuleFileData {
    // Just the module name without the package name
    private final Path moduleDirPath;
    private final List<DocumentFileData> srcDocs;
    private final List<DocumentFileData> testSrcDocs;
    // TODO do we need to maintain resources and test resources

    private ModuleFileData(Path moduleDirPath,
                           List<DocumentFileData> srcDocs,
                           List<DocumentFileData> testSrcDocs) {
        this.moduleDirPath = moduleDirPath;
        this.srcDocs = srcDocs;
        this.testSrcDocs = testSrcDocs;
    }

    public static ModuleFileData from(Path path,
                                      List<DocumentFileData> srcDocuments,
                                      List<DocumentFileData> testSrcDocuments) {
        return new ModuleFileData(path, srcDocuments, testSrcDocuments);
    }

    public Path moduleDirectoryPath() {
        return moduleDirPath;
    }

    public List<DocumentFileData> sourceDocs() {
        return srcDocs;
    }

    public List<DocumentFileData> testSourceDocs() {
        return testSrcDocs;
    }
}
