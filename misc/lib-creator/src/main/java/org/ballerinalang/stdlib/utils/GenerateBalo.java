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

import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.docgen.docs.BallerinaDocGenerator;
import org.ballerinalang.docgen.model.ModuleDoc;
import org.ballerinalang.packerina.utils.EmptyPrintStream;
import org.ballerinalang.packerina.writer.JarFileWriter;
import org.ballerinalang.repository.CompiledPackage;
import org.ballerinalang.util.diagnostic.DiagnosticCode;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.FileSystemProjectDirectory;
import org.wso2.ballerinalang.compiler.SourceDirectory;
import org.wso2.ballerinalang.compiler.diagnostic.BLangDiagnostic;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.compiler.CompilerOptionName.BALO_GENERATION;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.util.diagnostic.DiagnosticCode.USAGE_OF_DEPRECATED_CONSTRUCT;
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

    private static final String LOG_ISOLATION_WARNINGS_PROP = "BALLERINA_DEV_LOG_ISOLATION_WARNINGS";
    private static int errorCount = 0;
    private static int warnCount = 0;

    public static void main(String[] args) throws IOException {
        String isBuiltinFlag = args[0];
        String sourceDir = args[1];
        String targetDir = args[2];
        String libDir = args[3];
        boolean skipReportingWarnings = args.length > 4 && Boolean.parseBoolean(args[4]);
        String jvmTarget = args[5]; //TODO temp fix, remove this - rajith
        String moduleFilter = args[6];

        String originalShouldCompileBalOrg = System.getProperty(COMPILE_BALLERINA_ORG_PROP);
        String originalIsBuiltin = System.getProperty(LOAD_BUILTIN_FROM_SOURCE_PROP);
        String originalHome = System.getProperty(BALLERINA_INSTALL_DIR_PROP);
        try {
            System.setProperty(COMPILE_BALLERINA_ORG_PROP, "true");
            boolean isBuiltin = Boolean.parseBoolean(isBuiltinFlag);
            System.setProperty(LOAD_BUILTIN_FROM_SOURCE_PROP, Boolean.toString(isBuiltin));
            System.setProperty(BALLERINA_INSTALL_DIR_PROP, libDir);

            boolean reportWarnings = !skipReportingWarnings;

            genBalo(targetDir, sourceDir, reportWarnings, Boolean.parseBoolean(jvmTarget),
                    new HashSet<>(Arrays.asList(moduleFilter.split(","))),
                    Boolean.parseBoolean(System.getenv(LOG_ISOLATION_WARNINGS_PROP)));
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

    private static void genBalo(String targetDir, String sourceRootDir, boolean reportWarnings, boolean jvmTarget,
                                Set<String> docModuleFilter, boolean logIsolationWarnings)
            throws IOException {
        Files.createDirectories(Paths.get(targetDir));

        CompilerContext context = new CompilerContext();
        context.put(SourceDirectory.class, new MvnSourceDirectory(sourceRootDir, targetDir));

        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootDir);
        options.put(OFFLINE, Boolean.TRUE.toString());
        options.put(BALO_GENERATION, Boolean.TRUE.toString());
        options.put(COMPILER_PHASE, CompilerPhase.CODE_GEN.toString());
        options.put(SKIP_TESTS, Boolean.TRUE.toString());
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.TRUE.toString());

        Compiler compiler = Compiler.getInstance(context);
        List<BLangPackage> buildPackages = compiler.compilePackages(false);
        BallerinaDocGenerator.setPrintStream(new EmptyPrintStream());

        List<Diagnostic> diagnostics = new ArrayList<>();

        for (BLangPackage buildPackage : buildPackages) {
            diagnostics.addAll(buildPackage.getDiagnostics());
            errorCount += buildPackage.getErrorCount();
            warnCount += buildPackage.getWarnCount();
        }

        printErrors(reportWarnings, diagnostics, logIsolationWarnings);

        compiler.write(buildPackages);

        JarFileWriter jarFileWriter = JarFileWriter.getInstance(context);

        for (BLangPackage pkg : buildPackages) {
            Path jarOutput = Paths.get("./build/generated-bir-jar/" + pkg.packageID.orgName + "-" + pkg.packageID.name +
                                               "-" + pkg.packageID.version + ".jar");
            Path parent = jarOutput.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            jarFileWriter.write(pkg, jarOutput);
        }

        // Generate api doc
        genApiDoc(sourceRootDir, docModuleFilter, buildPackages);
    }

    private static void genApiDoc(String sourceRootDir, Set<String> docModuleFilter, List<BLangPackage> buildPackages)
            throws IOException {
        Map<String, ModuleDoc> moduleDocMap =
                BallerinaDocGenerator.generateModuleDocs(sourceRootDir, buildPackages,
                                                         docModuleFilter);
        String apiDocPath = "./build/generated-apidocs/";
        Files.createDirectories(Paths.get(apiDocPath));
        BallerinaDocGenerator.writeAPIDocsForModules(moduleDocMap, apiDocPath, false);
    }

    private static void printErrors(boolean reportWarnings,
                                    List<Diagnostic> diagnostics, boolean logIsolationWarnings) {
        int deprecatedWarnCount = 0;
        if (reportWarnings && warnCount > 0) {
            for (Diagnostic diagnostic : diagnostics) {
                if (!(diagnostic instanceof BLangDiagnostic)) {
                    continue;
                }
                DiagnosticCode code = ((BLangDiagnostic) diagnostic).getCode();
                if (code != null && (code == USAGE_OF_DEPRECATED_CONSTRUCT ||
                        (!logIsolationWarnings && isIsolatedWarningLog(code)))) {
                    deprecatedWarnCount++;
                }
            }
        }
        if (errorCount > 0 ||
                (reportWarnings && (warnCount - deprecatedWarnCount) > 0)) {
            String warnMsg = reportWarnings ? " and " + warnCount + " warning(s)" : "";
            throw new BLangCompilerException("Compilation failed with " + errorCount +
                                             " error(s)" + warnMsg + " " + "\n  ");
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
            Path path = Paths.get(targetDir, dirName, compiledPackage.getPackageID().version.value);
            super.saveCompiledPackage(compiledPackage, path, fileName);
        }
    }

    private static boolean isIsolatedWarningLog(DiagnosticCode code) {
        switch (code) {
            case FUNCTION_CAN_BE_MARKED_ISOLATED:
            case WARNING_INVALID_MUTABLE_ACCESS_AS_RECORD_DEFAULT:
            case WARNING_INVALID_NON_ISOLATED_INVOCATION_AS_RECORD_DEFAULT:
            case WARNING_INVALID_NON_ISOLATED_INIT_EXPRESSION_AS_RECORD_DEFAULT:
                return true;
        }
        return false;
    }
}
