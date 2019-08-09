/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.ballerinalang.compiler.backend.jvm;

import org.ballerinalang.bre.Context;
import org.ballerinalang.bre.bvm.BlockingNativeCallableUnit;
import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.ArrayValue;
import org.ballerinalang.jvm.values.ErrorValue;
import org.ballerinalang.jvm.values.MapValue;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.ballerinalang.model.types.TypeKind.RECORD;
import static org.ballerinalang.model.types.TypeKind.STRING;

/**
 * @since 0.995.0
 */
@BallerinaFunction(
        orgName = "ballerina", packageName = "compiler_backend_jvm",
        functionName = "writeExecutableJarToFile",
        args = {
                @Argument(name = "jarFile", type = RECORD),
                @Argument(name = "targetPath", type = STRING),
        }
)
public class WriteExecutableJarFile extends BlockingNativeCallableUnit {

    private static final String PKG_ENTRIES = "pkgEntries";
    private static final String MANIFEST_ENTRIES = "manifestEntries";

    @Override
    @Deprecated
    public void execute(Context context) {
        throw new UnsupportedOperationException("BVM not supported");
    }

    public static void writeExecutableJarToFile(Strand strand, MapValue oJarFile, String targetPath) {
        try {
            byte[] bytes = getJarContent(oJarFile);
            Optional<ErrorValue> result =
                    ClassVerifier.verify((Map<String, ArrayValue>) oJarFile.get(PKG_ENTRIES), bytes);
            if (result.isPresent()) {
                throw result.get();
            }

            writeJar(targetPath, bytes);
        } catch (IOException e) {
            throw new BLangCompilerException("jar file generation failed: " + e.getMessage(), e);
        }
    }

    private static void writeJar(String targetPath, byte[] bytes) throws IOException {
        try (FileOutputStream stream = new FileOutputStream(targetPath)) {
            stream.write(bytes);
        }
    }

    private static byte[] getJarContent(MapValue<String, MapValue> entries) throws IOException {
        Manifest manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

        if (entries.containsKey(MANIFEST_ENTRIES)) {
            MapValue<String, String> manifestEntries = entries.get(MANIFEST_ENTRIES);
            manifestEntries.forEach((k, v) -> manifest.getMainAttributes().put(new Attributes.Name(k), v));
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                JarOutputStream target = new JarOutputStream(baos, manifest)) {
            if (!entries.containsKey(PKG_ENTRIES)) {
                throw new BLangCompilerException("no class file entries found in the record");
            }

            Map<String, ArrayValue> jarEntries = entries.get(PKG_ENTRIES);
            for (Map.Entry<String, ArrayValue> keyVal : jarEntries.entrySet()) {
                byte[] entryContent = keyVal.getValue().getBytes();
                JarEntry entry = new JarEntry(keyVal.getKey());
                target.putNextEntry(entry);
                target.write(entryContent);
                target.closeEntry();
            }

            return baos.toByteArray();
        }
    }
}
