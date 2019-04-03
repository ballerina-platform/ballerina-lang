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

import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentException;
import org.ballerinalang.langserver.compiler.workspace.WorkspaceDocumentManager;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.packaging.converters.FileSystemSourceInput;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * LSInMemorySourceEntry.
 */
class LSInMemorySourceEntry extends FileSystemSourceInput {

    private String hashValue;
    private byte[] sourceBytes;

    LSInMemorySourceEntry(Path path, Path root, PackageID pkgId, WorkspaceDocumentManager documentManager) {
        super(path, root.resolve(Paths.get(pkgId.name.value)));
        try {
            this.sourceBytes = documentManager.getFileContent(this.getPath()).getBytes(StandardCharsets.UTF_8);
            byte[] bytes = documentManager.getFileContent(this.getPath()).getBytes(StandardCharsets.UTF_8);
            this.hashValue = this.getMD5Hash(bytes);
        } catch (WorkspaceDocumentException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error in loading package source entry '" + getPath() +
                    "': " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] getCode() {
        return this.sourceBytes;
    }

    private String getMD5Hash(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] hashInBytes = md.digest(bytes);

        StringBuilder sb = new StringBuilder();
        for (byte b : hashInBytes) {
            sb.append(String.format("%02x", b));
        }

        return sb.toString();
    }

    @Override
    public String getHashValue() {
        return this.hashValue;
    }
}
