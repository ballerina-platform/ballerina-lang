/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.tool.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.BinaryFileWriter;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.semantics.model.SymbolTable;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.StringJoiner;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_COMPILED_PKG_EXT;
import static org.wso2.ballerinalang.util.RepoUtils.BALLERINA_INSTALL_DIR_PROP;
import static org.wso2.ballerinalang.util.RepoUtils.COMPILE_BALLERINA_ORG_PROP;
import static org.wso2.ballerinalang.util.RepoUtils.LOAD_BUILTIN_FROM_SOURCE_PROP;

/**
 * Class providing utility methods to generate balx from bal.
 *
 * @since 0.967.0
 */
public class GenerateBalo {

    public static void main(String[] args) throws IOException {
        String isBuiltinFlag = args[0];
        String sourceDir = args[1];
        String targetDir = args[2];
        String libDir = args[3];
        boolean skipReportingWarnings = args.length > 4 && Boolean.parseBoolean(args[4]);

        String originalShouldCompileBalOrg = System.getProperty(COMPILE_BALLERINA_ORG_PROP);
        String originalIsBuiltin = System.getProperty(LOAD_BUILTIN_FROM_SOURCE_PROP);
        String originalHome = System.getProperty(BALLERINA_INSTALL_DIR_PROP);
        try {
            System.setProperty(COMPILE_BALLERINA_ORG_PROP, "true");
            boolean isBuiltin = Boolean.parseBoolean(isBuiltinFlag);
            System.setProperty(LOAD_BUILTIN_FROM_SOURCE_PROP, Boolean.toString(isBuiltin));
            System.setProperty(BALLERINA_INSTALL_DIR_PROP, libDir);

            boolean reportWarnings = !skipReportingWarnings;
            genBalo(targetDir, sourceDir, isBuiltin, reportWarnings);
        } finally {
            unsetProperty(COMPILE_BALLERINA_ORG_PROP, originalShouldCompileBalOrg);
            unsetProperty(LOAD_BUILTIN_FROM_SOURCE_PROP, originalIsBuiltin);
            unsetProperty(BALLERINA_INSTALL_DIR_PROP, originalHome);
        }
    }

    private static void unsetProperty(String key, String val) throws IOException {
        if (val == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, val);
        }
    }


    private static void genBalo(String targetDir, String sourceRootDir, boolean saveBuiltin, boolean reportWarnings)
            throws IOException {
        Files.createDirectories(Paths.get(targetDir));

        CompilerContext context = new CompilerContext();

        CompileResult.CompileResultDiagnosticListener diagListner = new CompileResult.CompileResultDiagnosticListener();
        context.put(DiagnosticListener.class, diagListner);

        context.put(SourceDirectory.class, new MvnSourceDirectory(sourceRootDir, targetDir));

        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootDir);
        options.put(OFFLINE, Boolean.TRUE.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(SKIP_TESTS, Boolean.TRUE.toString());
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());

        SymbolTable symbolTable = SymbolTable.getInstance(context);

        Compiler compiler = Compiler.getInstance(context);
        List<BLangPackage> buildPackages = compiler.compilePackages(false);

        List<Diagnostic> diagnostics = diagListner.getDiagnostics();
        if (diagListner.getErrorCount() > 0) {
            StringJoiner sj = new StringJoiner("\n  ");
            diagnostics.forEach(e -> sj.add(e.toString()));
            String warnMsg = reportWarnings ? " and " + diagListner.getWarnCount() + " warning(s)" : "";
            throw new BLangCompilerException("Compilation failed with " + diagListner.getErrorCount() +
                                             " error(s)" + warnMsg + " " + "\n  " + sj.toString());
        }

        compiler.write(buildPackages);

        BinaryFileWriter writer = BinaryFileWriter.getInstance(context);
        BPackageSymbol buitlinSymbol = symbolTable.builtInPackageSymbol;
        if (saveBuiltin && buitlinSymbol != null && buitlinSymbol.compiledPackage != null) {
            writer.writeLibraryPackage(buitlinSymbol, Names.BUILTIN_PACKAGE.getValue());
        }
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
