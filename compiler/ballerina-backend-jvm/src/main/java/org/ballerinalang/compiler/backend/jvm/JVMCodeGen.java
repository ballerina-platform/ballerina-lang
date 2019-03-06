/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.compiler.backend.jvm;

import org.ballerinalang.bre.bvm.BVMExecutor;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.nativeimpl.bir.BIRModuleUtils;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.debugger.Debugger;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.BIREmitter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ballerinalang.compiler.CompilerOptionName.BUILD_COMPILED_MODULE;
import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.EXPERIMENTAL_FEATURES_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;
import static org.ballerinalang.compiler.CompilerOptionName.SKIP_TESTS;
import static org.ballerinalang.compiler.CompilerOptionName.TEST_ENABLED;
import static org.ballerinalang.util.BLangConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * Ballerina compiler JVM backend.
 */
public class JVMCodeGen {

    private static final PrintStream console = System.out;
    private static final String EXEC_RESOURCE_FILE_NAME = "compiler_backend_jvm.balx";
    private static final String JAR_ENTRIES = "jarEntries";
    private static final String MANIFEST_ENTRIES = "manifestEntries";
    private static final String JAR_EXT = ".jar";
    private static final String functionName = "generateExecutableJar";
    private static final String BALLERINA_RUNTIME_JAR_NAME = "ballerina-runtime";

    public static void generateExecutableJar(Path sourceRootPath,
                                             String packagePath,
                                             String targetPath,
                                             boolean buildCompiledPkg,
                                             boolean offline,
                                             boolean lockEnabled,
                                             boolean skipTests,
                                             boolean enableExperimentalFeatures) {
        CompilerContext context = getCompilerContext(sourceRootPath, buildCompiledPkg, offline, lockEnabled, skipTests,
                enableExperimentalFeatures);

        BLangPackage bLangPackage = compileProgram(context, packagePath);

        if (bLangPackage.diagCollector.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }

        PackageID packageID = bLangPackage.packageID;
        BIRNode.BIRPackage bir = bLangPackage.symbol.bir;
        emitBIRText(bir);
        URI resURI = getExecResourceURIFromThisJar();
        byte[] resBytes = readExecResource(resURI);
        ProgramFile programFile = loadProgramFile(resBytes);

        BValue[] args = new BValue[3];
        args[0] = BIRModuleUtils.createBIRContext(programFile, PackageCache.getInstance(context),
                Names.getInstance(context));
        args[1] = BIRModuleUtils.createModuleID(programFile, packageID.orgName.value,
                packageID.name.value, packageID.version.value, packageID.isUnnamed,
                packageID.sourceFileName != null ? packageID.sourceFileName.value : packageID.name.value);
        args[2] = new BString(cleanupFileExtension(packagePath));

        // Generate the jar file
        try {
            Debugger debugger = new Debugger(programFile);
            programFile.setDebugger(debugger);
            FunctionInfo functionInfo = programFile.getEntryPackage().getFunctionInfo(functionName);
            BValue[] result = BVMExecutor.executeEntryFunction(programFile, functionInfo, args);
            LinkedHashMap<String, BValue> classes = ((BMap<String, BValue>) result[0]).getMap();
            generateJar(sourceRootPath, targetPath, classes);
        } catch (IOException e) {
            throw new BLangCompilerException("jvm jar file generation failed: " + e.getMessage(), e);
        }
    }

    private static void generateJar(Path outputPath, String targetFileName, LinkedHashMap<String, BValue> entries)
            throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        if (entries.containsKey(MANIFEST_ENTRIES)) {
            LinkedHashMap<String, BValue> manifestEntries = ((BMap<String, BValue>) entries.get(MANIFEST_ENTRIES)).
                    getMap();
            manifestEntries.forEach((k, v) -> manifest.getMainAttributes().
                    put(new Attributes.Name(k), v.stringValue()));
        }
        JarOutputStream target = new JarOutputStream(new FileOutputStream(outputPath.toString() + "/" +
                cleanupFileExtension(targetFileName) + JAR_EXT), manifest);

        if (entries.containsKey(JAR_ENTRIES)) {
            LinkedHashMap<String, BValue> jarEntries = ((BMap<String, BValue>) entries.get(JAR_ENTRIES)).getMap();
            for (String entryName : jarEntries.keySet()) {
                byte[] entryContent = ((BValueArray) jarEntries.get(entryName)).getBytes();
                JarEntry entry = new JarEntry(entryName);
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }
        }

        writeBallerinaRuntimeDependency(target);

        target.close();
    }

    private static void writeBallerinaRuntimeDependency(JarOutputStream target) throws IOException {
        String ballerinaHome = System.getProperty(BALLERINA_HOME);
        Path ballerinaRuntimeLib = Paths.get(ballerinaHome, BALLERINA_HOME_BRE, BALLERINA_HOME_LIB);

        File ballerinaRuntimeJar =  Arrays.stream(Objects.requireNonNull(ballerinaRuntimeLib.toFile().listFiles()))
                .filter(file -> file.getName().contains(BALLERINA_RUNTIME_JAR_NAME) && !file.getName().contains("api"))
                .findFirst()
                .orElseThrow(() -> new BLangCompilerException("ballerina runtime jar is not found"));

        ZipFile zipFile = new ZipFile(ballerinaRuntimeJar);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry jarEntry = entries.nextElement();

            if (jarEntry == null) {
                break;
            }

            if (jarEntry.getName().startsWith("META-INF") || !jarEntry.getName().endsWith(".class")) {
                continue;
            }

            target.putNextEntry(jarEntry);

            try (InputStream stream = zipFile.getInputStream(jarEntry)) {
                int nRead;
                byte[] data = new byte[1024];
                while ((nRead = stream.read(data, 0, data.length)) != -1) {
                    target.write(data, 0, nRead);
                }
                target.closeEntry();
            }
        }
    }

    private static BLangPackage compileProgram(CompilerContext context, String progPath) {
        Compiler compiler = Compiler.getInstance(context);
        return compiler.build(progPath);
    }

    private static CompilerContext getCompilerContext(Path sourceRootPath,
                                                      boolean buildCompiledPkg,
                                                      boolean offline,
                                                      boolean lockEnabled,
                                                      boolean skipTests,
                                                      boolean enableExperimentalFeatures) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, sourceRootPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(BUILD_COMPILED_MODULE, Boolean.toString(buildCompiledPkg));
        options.put(OFFLINE, Boolean.toString(offline));
        options.put(LOCK_ENABLED, Boolean.toString(lockEnabled));
        options.put(SKIP_TESTS, Boolean.toString(skipTests));
        options.put(TEST_ENABLED, Boolean.toString(true));
        options.put(EXPERIMENTAL_FEATURES_ENABLED, Boolean.toString(enableExperimentalFeatures));
        return context;
    }

    private static URI getExecResourceURIFromThisJar() {
        URI resURI;
        try {
            URL resourceURL = JVMCodeGen.class.getClassLoader().getResource("META-INF/ballerina/" +
                    EXEC_RESOURCE_FILE_NAME);
            if (resourceURL == null) {
                throw new BLangCompilerException("missing embedded executable resource: " + EXEC_RESOURCE_FILE_NAME);
            }
            resURI = resourceURL.toURI();
        } catch (URISyntaxException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
        return resURI;
    }

    private static byte[] readExecResource(URI resURI) {
        byte[] resBytes;
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try (FileSystem ignored = FileSystems.newFileSystem(resURI, env)) {
            resBytes = Files.readAllBytes(Paths.get(resURI));
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
        return resBytes;
    }

    private static ProgramFile loadProgramFile(byte[] resBytes) {
        try (ByteArrayInputStream byteAIS = new ByteArrayInputStream(resBytes)) {
            ProgramFileReader programFileReader = new ProgramFileReader();
            return programFileReader.readProgram(byteAIS);
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
    }

    private static String cleanupFileExtension(String targetFileName) {
        String updatedFileName = targetFileName;
        if (updatedFileName == null || updatedFileName.isEmpty()) {
            throw new IllegalArgumentException("invalid target file name");
        }

        if (updatedFileName.endsWith(BLANG_SOURCE_EXT)) {
            updatedFileName = updatedFileName.substring(0, updatedFileName.length() - BLANG_SOURCE_EXT.length());
        }
        return updatedFileName;
    }

    private static void emitBIRText(BIRNode.BIRPackage bir) {
        BIREmitter birEmitter = new BIREmitter();
        String birText = birEmitter.emit(bir);
        console.println(birText);
    }
}
