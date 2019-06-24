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

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.model.values.BValueArray;
import org.ballerinalang.spi.CompilerBackendCodeGenerator;
import org.ballerinalang.util.codegen.ProgramFile;
import org.ballerinalang.util.codegen.ProgramFileReader;
import org.ballerinalang.util.exceptions.BLangRuntimeException;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.wso2.ballerinalang.compiler.util.ProjectDirConstants.BLANG_SOURCE_EXT;

/**
 * Ballerina compiler JVM backend.
 *
 * @since 0.955.0
 */
@JavaSPIService("org.ballerinalang.spi.CompilerBackendCodeGenerator")
public class JVMCodeGen implements CompilerBackendCodeGenerator {

    private static final String BALLERINA_HOME = "BALLERINA_HOME";
    private static final String UPDATE_CLASSPATH = "UPDATE_CLASSPATH";
    private static final String EXEC_RESOURCE_FILE_NAME = "compiler_backend_jvm.balx";
    private static final String PKG_ENTRIES = "pkgEntries";
    private static final String MANIFEST_ENTRIES = "manifestEntries";

    @Override
    public Optional<Object> generate(Object... args) {
        boolean dumpBIR = (boolean) args[0];
        BLangPackage bLangPackage = (BLangPackage) args[1];
        String entryBir = (String) args[2];
        String birCachePath = (String) args[3];
        String jarOutputPath = (String) args[4];
        String importsPath = (String) args[5];

        String bootstrapHome = "/media/manu/cd66d0ab-52b1-4647-8d52-1b88a47e9db2/checkout/ballerina-lang/distribution/bootstrapper/build/dist/pack3/ballerina-0.992.0-m1";
        generateJarBinary(bootstrapHome, entryBir, birCachePath, jarOutputPath, importsPath);
        return Optional.empty();
    }

    private static void generateJarBinary(String bootstrapHome, String entryBir, String birCachePath,
                                          String importsPath, String jarOutputPath) {
    }

    private static Path getPathToBIR(BLangPackage bLangPackage, Path sourceRootPath) {
        // If bir is for a package, get the bir from .ballerina folder
        // If bir is for a single bal file, get the bir from the tmp directory
        // TODO: Use a random tmp directory instead of java.io.tmpdir
        return bLangPackage.packageID.isUnnamed ?
                Paths.get(System.getProperty("java.io.tmpdir"))
                        .resolve(bLangPackage.packageID.orgName.value)
                        .resolve(bLangPackage.packageID.version.value)
                        .resolve(bLangPackage.packageID.name.value) :
                sourceRootPath
                        .resolve(".ballerina")
                        .resolve("repo")
                        .resolve(bLangPackage.packageID.orgName.value)
                        .resolve(bLangPackage.packageID.name.value)
                        .resolve(bLangPackage.packageID.version.value);
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

            if (!entries.containsKey(PKG_ENTRIES)) {
                throw new BLangCompilerException("no class file entries found in the record");
            }
            Map<String, BValue> jarEntries = ((BMap<String, BValue>) entries.get(PKG_ENTRIES)).getMap();
            for (String entryName : jarEntries.keySet()) {
                byte[] entryContent = ((BValueArray) jarEntries.get(entryName)).getBytes();
                JarEntry entry = new JarEntry(entryName);
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }
        }

        return baos.toByteArray();
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

    private static String cleanUpFilename(String updatedFileName) {
        if (updatedFileName.endsWith(BLANG_SOURCE_EXT)) {
            updatedFileName = updatedFileName.substring(0,
                    updatedFileName.length() - BLANG_SOURCE_EXT.length());
        }
        return updatedFileName;
    }

    private static byte[] decompressBIR(String pkgName, String libPath) {
        try (InputStream in = getCompiledBIRBinary(pkgName, libPath);
             ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
//            while (in.getNextJarEntry() != null) {
//                in.read(buffer);
//            }
            int len;
            while ((len = in.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, len);
            }
            // Create binary array from the Serialized the BIR model.
            return byteArrayOutputStream.toByteArray();
            // Set the return value
        } catch (IOException e) {
            throw new BLangRuntimeException("error decompressing executable " + libPath + File.separator + pkgName);
        }
    }

    private static InputStream getCompiledBIRBinary(String pkgName, String libPath) throws IOException {
        Path path = Paths.get(libPath);
        Path resolve = path.resolve(pkgName + ".jar");
        FileInputStream is = new FileInputStream(resolve.toString());
        return is;
    }
}
