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
package org.ballerinalang.langserver.workspace.repository;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageSourceEntry;
import org.wso2.ballerinalang.compiler.packaging.converters.FileSystemSourceEntry;
import org.wso2.ballerinalang.compiler.packaging.converters.PathConverter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * Language Server Path Converter.
 */
public class LSPathConverter extends PathConverter {
    public LSPathConverter(Path root) {
        super(root);
    }

    @Override
    public Stream<PackageSourceEntry> finalize(Path path, PackageID id) {
        // TODO: Inject unsaved/in-memory file content
        if (Files.isRegularFile(path)) {
            return Stream.of(new FileSystemSourceEntry(path, id));
        } else {
            return Stream.of(new LSInMemorySourceEntry("testEntryName", null));
        }
    }
}
