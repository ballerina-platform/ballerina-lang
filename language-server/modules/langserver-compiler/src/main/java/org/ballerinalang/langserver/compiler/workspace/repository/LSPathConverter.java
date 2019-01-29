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

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.CompilerInput;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Language Server Path Converter.
 */
public class LSPathConverter extends PathConverter {
    private WorkspaceDocumentManager documentManager;

    public LSPathConverter(Path root, WorkspaceDocumentManager documentManager) {
        super(root);
        this.documentManager = documentManager;
    }

    @Override
    public Stream<CompilerInput> finalize(Path path, PackageID id) {
        // Returns an In-memory source entry with backing-off capability to read from the FileSystem
        return Stream.of(new LSInMemorySourceEntry(path, this.start(), id, documentManager));
    }
}
