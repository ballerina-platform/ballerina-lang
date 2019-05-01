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

import org.ballerinalang.BLangProgramRunner;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.nativeimpl.bir.BIRModuleUtils;
import org.ballerinalang.util.codegen.FunctionInfo;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.BIREmitter;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.FileUtils;
import org.wso2.ballerinalang.compiler.util.Names;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.ballerinalang.util.BLangConstants.BALLERINA_HOME;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_BRE;
import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BALLERINA_HOME_LIB;

/**
 * Ballerina compiler JVM backend.
 *
 * @since 0.955.0
 */
public class JVMCodeGen {

    private static final PrintStream console = System.out;
    private static final String EXEC_RESOURCE_FILE_NAME = "compiler_backend_jvm.balx";
    private static final String functionName = "generateJarBinary";
    private static final String JAR_ENTRIES = "jarEntries";
    private static final String MANIFEST_ENTRIES = "manifestEntries";
    private static final String BALLERINA_RUNTIME_JAR_NAME = "ballerina-runtime";

    public static byte[] generateJarBinary(boolean dumpBIR, BLangPackage bLangPackage, CompilerContext context,
                                           String packagePath) {
        PackageID packageID = bLangPackage.packageID;
        URI resURI = getExecResourceURIFromThisJar();
        byte[] resBytes = readExecResource(resURI);
        ProgramFile programFile = loadProgramFile(resBytes);

        BValue[] args = new BValue[4];
        args[0] = new BBoolean(dumpBIR);
        args[1] = BIRModuleUtils.createBIRContext(programFile, PackageCache.getInstance(context),
                Names.getInstance(context));
        args[2] = BIRModuleUtils.createModuleID(programFile, packageID.orgName.value,
                packageID.name.value, packageID.version.value, packageID.isUnnamed,
                packageID.sourceFileName != null ? packageID.sourceFileName.value : packageID.name.value);
        args[3] = new BString(FileUtils.cleanupFileExtension(packagePath));

        FunctionInfo functionInfo = programFile.getEntryPackage().getFunctionInfo(functionName);
        BValue result = BLangProgramRunner.runProgram(programFile, functionInfo, args);

        Map<String, BValue> jarEntries = ((BMap<String, BValue>) result).getMap();
        try {
            return getJarContent(jarEntries);
        } catch (IOException e) {
            throw new BLangCompilerException("jar file generation failed: " + e.getMessage(), e);
        }
    }

    private static byte[] getJarContent(Map<String, BValue> entries) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        if (entries.containsKey(MANIFEST_ENTRIES)) {
            Map<String, BValue> manifestEntries = ((BMap<String, BValue>) entries.get(MANIFEST_ENTRIES)).
                    getMap();
            manifestEntries.forEach((k, v) -> manifest.getMainAttributes().
                    put(new Attributes.Name(k), v.stringValue()));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (JarOutputStream target = new JarOutputStream(baos, manifest)) {

            if (!entries.containsKey(JAR_ENTRIES)) {
                throw new BLangCompilerException("no class file entries found in the record");
            }
            Map<String, BValue> jarEntries = ((BMap<String, BValue>) entries.get(JAR_ENTRIES)).getMap();
            for (String entryName : jarEntries.keySet()) {
                byte[] entryContent = ((BValueArray) jarEntries.get(entryName)).getBytes();
                JarEntry entry = new JarEntry(entryName);
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }
            writeBallerinaRuntimeDependency(target);
        }

        return baos.toByteArray();
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
        if (resURI.getScheme().equals("jar")) {
            initFileSystem(resURI);
        }

        byte[] resBytes;

        try {
            resBytes = Files.readAllBytes(Paths.get(resURI));
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }

        return resBytes;
    }

    private static void initFileSystem(URI uri) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        try {
            FileSystems.newFileSystem(uri, env);
        } catch (Exception ignore) {
        }
    }


    private static ProgramFile loadProgramFile(byte[] resBytes) {
        try (ByteArrayInputStream byteAIS = new ByteArrayInputStream(resBytes)) {
            ProgramFileReader programFileReader = new ProgramFileReader();
            return programFileReader.readProgram(byteAIS);
        } catch (IOException e) {
            throw new BLangCompilerException("failed to load embedded executable resource: ", e);
        }
    }

    public static void emitBIRText(BIRNode.BIRPackage bir) {
        BIREmitter birEmitter = new BIREmitter();
        String birText = birEmitter.emit(bir);
        console.println(birText);
    }
}
