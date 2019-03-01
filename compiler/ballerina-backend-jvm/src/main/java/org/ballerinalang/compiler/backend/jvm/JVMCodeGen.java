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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.ballerinalang.compiler.CompilerOptionName.COMPILER_PHASE;
import static org.ballerinalang.compiler.CompilerOptionName.LOCK_ENABLED;
import static org.ballerinalang.compiler.CompilerOptionName.OFFLINE;
import static org.ballerinalang.compiler.CompilerOptionName.PROJECT_DIR;

/**
 * Ballerina compiler JVM backend.
 */
public class JVMCodeGen {
    private static final String EXEC_RESOURCE_FILE_NAME = "compiler_backend_jvm.balx";
    private String executableJarFileName = "DEFAULT.jar";
    private static JVMCodeGen jvmCodeGen = new JVMCodeGen();
    private static final PrintStream console = System.out;
    private static final String JAR_ENTRIES = "jarEntries";
    private static final String MANIFEST_ENTRIES = "manifestEntries";

    private JVMCodeGen() {}

    public static JVMCodeGen getInstance() {
        return jvmCodeGen;
    }

    public void generateJVMExecutable(Path projectPath, String progPath, Path outputPath) {
        executableJarFileName = progPath.substring(0, progPath.indexOf("."));
        CompilerContext context = getCompilerContext(projectPath);
        BLangPackage bLangPackage = compileProgram(context, progPath);
        if (bLangPackage.diagCollector.hasErrors()) {
            throw new BLangCompilerException("compilation contains errors");
        }

        PackageID packageID = bLangPackage.packageID;
        BIRNode.BIRPackage bir = bLangPackage.symbol.bir;

        BIREmitter birEmitterjvm = new BIREmitter();
        String birText = birEmitterjvm.emit(bir);
        console.println(birText);

        final String functionName = "genExecutableJar";
        URI resURI = getExecResourceURIFromThisJar();
        byte[] resBytes = readExecResource(resURI);
        ProgramFile programFile = loadProgramFile(resBytes);

        BValue[] args = new BValue[3];
        args[0] = BIRModuleUtils.createBIRContext(programFile, PackageCache.getInstance(context),
                Names.getInstance(context));
        args[1] = BIRModuleUtils.createModuleID(programFile, packageID.orgName.value,
                packageID.name.value, packageID.version.value, packageID.isUnnamed, packageID.sourceFileName.value);
        args[2] = new BString(executableJarFileName);

        // Generate the jar file
        try {
            Debugger debugger = new Debugger(programFile);
            programFile.setDebugger(debugger);
            FunctionInfo functionInfo = programFile.getEntryPackage().getFunctionInfo(functionName);
            BValue[] result = BVMExecutor.executeEntryFunction(programFile, functionInfo, args);
            LinkedHashMap<String, BValue> classes = ((BMap<String, BValue>) result[0]).getMap();
            generateJar(outputPath, classes);
        } catch (Exception e) {
            throw new BLangCompilerException("jvm class file generation failed: " + e.getMessage(), e);
        }
    }

    private void generateJar(Path outputPath, LinkedHashMap<String, BValue> entries) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        if (entries.containsKey(MANIFEST_ENTRIES)) {
            LinkedHashMap<String, BValue> manifestEntries = ((BMap<String, BValue>) entries.get(MANIFEST_ENTRIES)).
                    getMap();
            manifestEntries.forEach((k, v) -> {
                manifest.getMainAttributes().put(new Attributes.Name(k), v.stringValue());
            });
        }
        JarOutputStream target = new JarOutputStream(new FileOutputStream(outputPath.toString() + "/" +
                executableJarFileName + ".jar"), manifest);

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

        target.close();
    }

    private BLangPackage compileProgram(CompilerContext context, String progPath) {
        Compiler compiler = Compiler.getInstance(context);
        return compiler.build(progPath);
    }

    private CompilerContext getCompilerContext(Path projectPath) {
        CompilerContext context = new CompilerContext();
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(PROJECT_DIR, projectPath.toString());
        options.put(COMPILER_PHASE, CompilerPhase.BIR_GEN.toString());
        options.put(OFFLINE, Boolean.toString(true));
        options.put(LOCK_ENABLED, Boolean.toString(true));
        return context;
    }

    private URI getExecResourceURIFromThisJar() {
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

    private byte[] readExecResource(URI resURI) {
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

    private ProgramFile loadProgramFile(byte[] resBytes) {
        try (ByteArrayInputStream byteAIS = new ByteArrayInputStream(resBytes)) {
            ProgramFileReader programFileReader = new ProgramFileReader();
            return programFileReader.readProgram(byteAIS);
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
    }
}
