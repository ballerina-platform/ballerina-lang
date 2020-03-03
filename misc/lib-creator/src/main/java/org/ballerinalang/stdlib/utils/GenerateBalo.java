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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.tool.util.CompileResult;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticListener;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.bir.BackendDriver;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
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
        String jvmTarget = args[5]; //TODO temp fix, remove this - rajith

        String originalShouldCompileBalOrg = System.getProperty(COMPILE_BALLERINA_ORG_PROP);
        String originalIsBuiltin = System.getProperty(LOAD_BUILTIN_FROM_SOURCE_PROP);
        String originalHome = System.getProperty(BALLERINA_INSTALL_DIR_PROP);
        try {
            System.setProperty(COMPILE_BALLERINA_ORG_PROP, "true");
            boolean isBuiltin = Boolean.parseBoolean(isBuiltinFlag);
            System.setProperty(LOAD_BUILTIN_FROM_SOURCE_PROP, Boolean.toString(isBuiltin));
            System.setProperty(BALLERINA_INSTALL_DIR_PROP, libDir);

            boolean reportWarnings = !skipReportingWarnings;

            genBalo(targetDir, sourceDir, reportWarnings, Boolean.parseBoolean(jvmTarget));
        } finally {
            unsetProperty(COMPILE_BALLERINA_ORG_PROP, originalShouldCompileBalOrg);
            unsetProperty(LOAD_BUILTIN_FROM_SOURCE_PROP, originalIsBuiltin);
            unsetProperty(BALLERINA_INSTALL_DIR_PROP, originalHome);
        }
    }

    private static void unsetProperty(String key, String val) {
        if (val == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, val);
        }
    }

    private static void genBalo(String targetDir, String sourceRootDir, boolean reportWarnings, boolean jvmTarget)
    throws IOException {
        Files.createDirectories(Paths.get(targetDir));

        CompilerContext context = new CompilerContext();

        CompileResult.CompileResultDiagnosticListener diagListner = new CompileResult.CompileResultDiagnosticListener();
        context.put(DiagnosticListener.class, diagListner);

        context.put(SourceDirectory.class, new MvnSourceDirectory(sourceRootDir, targetDir));

        CompilerPhase compilerPhase = CompilerPhase.CODE_GEN;

        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootDir);
        options.put(OFFLINE, Boolean.TRUE.toString());
        options.put(COMPILER_PHASE, compilerPhase.toString());
        options.put(SKIP_TESTS, Boolean.TRUE.toString());
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());


        Compiler compiler = Compiler.getInstance(context);
        List<BLangPackage> buildPackages = compiler.compilePackages(false);
        BackendDriver backendDriver = BackendDriver.getInstance(context);

        List<Diagnostic> diagnostics = diagListner.getDiagnostics();
        printErrors(reportWarnings, diagListner, diagnostics);

        compiler.write(buildPackages);

        for (BLangPackage pkg : buildPackages) {
            String suffix = "";
            String bStringProp = System.getProperty("ballerina.bstring");
            if (bStringProp != null && !"".equals(bStringProp)) {
                suffix = "-bstring";
            }
            Path jarOutput = Paths.get("./build/generated-bir-jar/" + pkg.packageID.name + suffix + ".jar");
            Path parent = jarOutput.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            backendDriver.execute(pkg.symbol.bir, false, jarOutput, readModuleDependencies());
            printErrors(reportWarnings, diagListner, diagnostics);
        }
    }

    private static void printErrors(boolean reportWarnings, CompileResult.CompileResultDiagnosticListener diagListner, List<Diagnostic> diagnostics) {
        if (diagListner.getErrorCount() > 0 || (reportWarnings && diagListner.getWarnCount() > 0)) {
            StringJoiner sj = new StringJoiner("\n  ");
            diagnostics.forEach(e -> sj.add(e.toString()));
            String warnMsg = reportWarnings ? " and " + diagListner.getWarnCount() + " warning(s)" : "";
            throw new BLangCompilerException("Compilation failed with " + diagListner.getErrorCount() +
                                             " error(s)" + warnMsg + " " + "\n  " + sj.toString());
        }
    }

    private static HashSet<Path> readModuleDependencies() throws IOException {
        HashSet<Path> moduleDependencies = new HashSet<>();
        try (BufferedReader br = new BufferedReader(new FileReader("build/interopJars.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                moduleDependencies.add(Paths.get(line));
            }
        }
        return moduleDependencies;
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
