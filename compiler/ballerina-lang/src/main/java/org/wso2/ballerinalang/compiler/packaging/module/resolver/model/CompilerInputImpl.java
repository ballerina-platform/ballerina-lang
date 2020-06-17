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

package org.wso2.ballerinalang.compiler.packaging.module.resolver.model;

import io.ballerinalang.compiler.syntax.tree.SyntaxTree;
import io.ballerinalang.compiler.text.TextDocuments;
import org.ballerinalang.repository.CompilerInput;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

/**
 * CompilerInput implementation used in module resolver.
 */
public class CompilerInputImpl implements CompilerInput {

    private final byte[] code;
    private final Path path;
    private Path packageRoot;
    private SyntaxTree tree = null;

    CompilerInputImpl(byte[] code, Path path) {
        this.code = code;
        this.path = path;
    }

    CompilerInputImpl(byte[] code, Path path, Path packageRoot) {
        this.code = code;
        this.path = path;
        this.packageRoot = packageRoot;
    }

    @Override
    public String getEntryName() {
        // We need to return the file path relative to the package root.
        // This is to distinguish files with the same name but in different folders.
        String entryName;
        if (packageRoot != null) {
            File pkgRoot = new File(packageRoot.toString());
            File file = new File(path.toString());
            // Find the file path relative to the package root.
            entryName = pkgRoot.toURI().relativize(file.toURI()).getPath();
        } else {
            entryName = path.toString();
        }
        return entryName;
    }

    @Override
    public byte[] getCode() {
        byte[] codeCopy = this.code;
        return codeCopy;
    }

    @Override
    public SyntaxTree getTree() {
        if (this.tree != null) {
            return this.tree;
        }
        this.tree = SyntaxTree.from(TextDocuments.from(new String(getCode(), StandardCharsets.UTF_8)));
        return this.tree;
    }

    public Path getPath() {
        return this.path;
    }
}
