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
import org.wso2.ballerinalang.compiler.CompilerDriver;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Class providing utility methods to generate balx from bal.
 *
 * @since 0.967.0
 */
public class GenerateBalo {

    public static void main(String[] args) throws IOException {
        String sourceRoot = args[0];
        String targetDir = args[1];
        String fileName = args[2];
        Files.createDirectories(Paths.get(sourceRoot).resolve(".ballerina"));

        CompilerContext context = new CompilerContext();
        context.put(SourceDirectory.class, new MvnSourceDirectory(sourceRoot, targetDir));

        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRoot);
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());

        SymbolTable symbolTable = SymbolTable.getInstance(context);
        CompilerDriver driver = CompilerDriver.getInstance(context);

        driver.loadBuiltinPackage();
        BinaryFileWriter writer = BinaryFileWriter.getInstance(context);
        BPackageSymbol symbol = symbolTable.builtInPackageSymbol;
        writer.writeLibraryPackage(symbol, fileName);
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
            super.saveCompiledPackage(compiledPackage, Paths.get(targetDir), fileName);
        }
    }
}
