/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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
 */

package io.ballerina.runtime.profiler.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static io.ballerina.runtime.profiler.Main.ANSI_CYAN;
import static io.ballerina.runtime.profiler.Main.ANSI_RESET;
import static io.ballerina.runtime.profiler.Main.TEMPJARFILENAME;
import static io.ballerina.runtime.profiler.Main.getBalJarArgs;

/**
 * This class is used as the method wrapper for the ballerina profiler.
 *
 * @since 2201.7.0
 */
public class MethodWrapper extends ClassLoader {
    public static void invokeMethods() throws IOException, InterruptedException {
        String balJarArgs = getBalJarArgs();
        String[] command = {"java", "-jar", TEMPJARFILENAME};
        if (balJarArgs != null) {
            command = Arrays.copyOf(command, command.length + 1);
            command[3] = balJarArgs;
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        System.out.printf(ANSI_CYAN + "[5/6] Running Executable..." + ANSI_RESET + "%n");
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            reader.lines().forEach(System.out::println);
        }
        process.waitFor();
    }

    public static String mainClassFinder(URLClassLoader manifestClassLoader) {
        try {
            URL manifestURL = manifestClassLoader.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestURL.openStream());
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Main-Class").replace(".$_init", "").replace(".", "/");
        } catch (Exception | Error throwable) {
            System.err.printf(throwable + "%n");
            return null;
        }
    }

    public static byte[] modifyMethods(InputStream inputStream) {
        byte[] code;
        try {
            ClassReader reader = new ClassReader(inputStream);
            ClassWriter classWriter = new CustomClassWriter(reader,
                    ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
            ClassVisitor change = new CustomClassVisitor(classWriter);
            reader.accept(change, ClassReader.EXPAND_FRAMES);
            code = classWriter.toByteArray();
            return code;
        } catch (Exception | Error e) {
            System.out.printf(e + "%n");
        }
        return new byte[0]; // Return a zero-length byte array if the code was not modified
    }

    // Print out the modified class code
    public static void printCode(String className, byte[] code) {
        int lastSlashIndex = className.lastIndexOf('/');
        String output = className.substring(0, lastSlashIndex);
        File directory = new File(output);

        if (!directory.exists()) {
            boolean directoryCreated = directory.mkdirs();
            if (!directoryCreated) {
                return;
            }
        }

        try (FileOutputStream fos = new FileOutputStream(className)) {
            fos.write(code); // Write the code to the output stream
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
