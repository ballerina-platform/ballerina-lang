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
package org.ballerinalang.stdlib.utils;


import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.repository.CompiledPackage;
import org.wso2.ballerinalang.compiler.BinaryFileWriter;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_EXT;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.DOT_BALLERINA_DIR_NAME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.MANIFEST_FILE_NAME;

/**
 * Class providing utility methods to generate balx from bal.
 *
 * @since 0.967.0
 */
public class GenerateBalo {

    private static final String MANIFEST = "[project]\n" +
                                           "org-name = \"ballerina\"\n" +
                                           "version = \"0.0.0\"\n";

    public static void main(String[] args) throws IOException {
        String sourceRoot = args[0];
        String targetDir = args[1];

        Path sourceRootDir = Paths.get(sourceRoot);
        Files.write(sourceRootDir.resolve(MANIFEST_FILE_NAME), MANIFEST.getBytes(StandardCharsets.UTF_8));
        Files.createDirectories(sourceRootDir.resolve(DOT_BALLERINA_DIR_NAME));
        Files.createDirectories(Paths.get(targetDir));

        CompilerContext context = new CompilerContext();
        context.put(SourceDirectory.class, new MvnSourceDirectory(sourceRoot, targetDir));

        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(OFFLINE, Boolean.TRUE.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());

        SymbolTable symbolTable = SymbolTable.getInstance(context);

        Compiler compiler = Compiler.getInstance(context);
        compiler.build();

        BinaryFileWriter writer = BinaryFileWriter.getInstance(context);
        BPackageSymbol symbol = symbolTable.builtInPackageSymbol;
        writer.writeLibraryPackage(symbol, Names.BUILTIN_PACKAGE.getValue());
    }

    private static class MvnSourceDirectory extends FileSystemProjectDirectory {

        private final String targetDir;

        MvnSourceDirectory(String sourceRoot, String targetDir) {
            super(Paths.get(sourceRoot));
            this.targetDir = targetDir;
        }

        @Override
        public void saveCompiledPackage(CompiledPackage compiledPackage,
                                        Path dirPath,
                                        String fileName) throws IOException {
            String dirName = fileName.endsWith(BLANG_COMPILED_PKG_EXT) ?
                             fileName.substring(0, fileName.length() - BLANG_COMPILED_PKG_EXT.length()) :
                             fileName;
            Path path = Paths.get(targetDir, dirName, Names.DEFAULT_VERSION.getValue());
            super.saveCompiledPackage(compiledPackage, path, fileName);
        }
    }
}
