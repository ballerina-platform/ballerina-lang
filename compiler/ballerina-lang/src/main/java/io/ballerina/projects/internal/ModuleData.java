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
package io.ballerina.projects.internal;

import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * {@code ModuleFileData} represents a Ballerina module directory.
 *
 * @since 2.0.0
 */
public class ModuleData {
    // Just the module name without the package name
    private final Path moduleDirPath;
    private final String moduleName;
    private final List<DocumentData> srcDocs;
    private final List<DocumentData> testSrcDocs;
    private final DocumentData readmeMd;

    // TODO do we need to maintain resources and test resources

    private ModuleData(Path moduleDirPath,
                       String moduleName,
                       List<DocumentData> srcDocs,
                       List<DocumentData> testSrcDocs,
                       DocumentData readmeMd) {
        this.moduleDirPath = moduleDirPath;
        this.moduleName = moduleName;
        this.srcDocs = srcDocs;
        this.testSrcDocs = testSrcDocs;
        this.readmeMd = readmeMd;
    }

    public static ModuleData from(Path path,
                                  String moduleName,
                                  List<DocumentData> srcDocuments,
                                  List<DocumentData> testSrcDocuments,
                                  @Nullable DocumentData readmeMd) {
        return new ModuleData(path, moduleName, srcDocuments, testSrcDocuments, readmeMd);
    }

    public Path moduleDirectoryPath() {
        return moduleDirPath;
    }

    public String moduleName() {
        return moduleName;
    }

    public List<DocumentData> sourceDocs() {
        return srcDocs;
    }

    public void addSourceDocs(List<DocumentData> docs) {
        srcDocs.addAll(docs);
    }

    public List<DocumentData> testSourceDocs() {
        return testSrcDocs;
    }

    public void addTestSourceDocs(List<DocumentData> docs) {
        testSrcDocs.addAll(docs);
    }

    /**
     * Returns the ModuleMd document data.
     *
     * @return DocumentData optionally.
     * @deprecated use {@link #readmeMd()} instead.
     */
    @Deprecated (forRemoval = true)
    public Optional<DocumentData> moduleMd() {
        return Optional.ofNullable(this.readmeMd);
    }

    public Optional<DocumentData> readmeMd() {
        return Optional.ofNullable(this.readmeMd);
    }

}
