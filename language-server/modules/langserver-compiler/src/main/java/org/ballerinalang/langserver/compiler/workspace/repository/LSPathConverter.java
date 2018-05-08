/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler.workspace.repository;

import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.packaging.converters.FileSystemSourceInput;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Language Server Path Converter.
 */
class LSPathConverter extends PathConverter {
    private WorkspaceDocumentManager documentManager;
    private LSContext lsContext;
    
    LSPathConverter(Path root, WorkspaceDocumentManager documentManager, LSContext lsContext) {
        super(root);
        this.documentManager = documentManager;
        this.lsContext = lsContext;
    }

    @Override
    public Stream<CompilerInput> finalize(Path path, PackageID id) {
        if (documentManager.isFileOpen(path) || !Files.isRegularFile(path)) {
            // TODO: Remove passing completion context after introducing a proper fix for _=.... issue
            return Stream.of(new LSInMemorySourceEntry(path, id, documentManager, lsContext));
        } else {
            return Stream.of(new FileSystemSourceInput(path));
        }
    }

    /**
     * Reset the LS context instance.
     * @param lsContext     New LSContext instance
     */
    public void resetLSContext(LSContext lsContext) {
        this.lsContext = lsContext;
    }
}
