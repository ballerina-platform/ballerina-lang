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

import io.ballerina.runtime.profiler.Main;
import io.ballerina.runtime.profiler.util.Constants;
import io.ballerina.runtime.profiler.util.ProfilerException;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static io.ballerina.runtime.profiler.util.Constants.CURRENT_DIR_KEY;
import static io.ballerina.runtime.profiler.util.Constants.ERROR_STREAM;
import static io.ballerina.runtime.profiler.util.Constants.OUT_STREAM;
import static io.ballerina.runtime.profiler.util.Constants.USER_DIR;

/**
 * This class is used as the method wrapper for the Ballerina profiler.
 *
 * @since 2201.8.0
 */
public class ProfilerMethodWrapper extends ClassLoader {

    public void invokeMethods(String debugArg) throws IOException, InterruptedException {
        String balJarArgs = Main.getBalJarArgs();
        List<String> commands = new ArrayList<>();
        commands.add(System.getenv("java.command"));
        commands.add("-jar");
        if (debugArg != null) {
            commands.add(debugArg);
        }
        commands.add(Paths.get(System.getProperty(USER_DIR), Constants.TEMP_JAR_FILE_NAME).toString());
        if (balJarArgs != null) {
            commands.add(balJarArgs);
        }
        ProcessBuilder processBuilder = new ProcessBuilder(commands);
        processBuilder.inheritIO();
        processBuilder.directory(new File(System.getenv(CURRENT_DIR_KEY)));
        Process process = processBuilder.start();
        OUT_STREAM.printf(Constants.ANSI_CYAN + "[5/6] Running executable..." + Constants.ANSI_RESET + "%n");
        try (InputStreamReader streamReader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader reader = new BufferedReader(streamReader)) {
            reader.lines().forEach(OUT_STREAM::println);
        }
        process.waitFor();
    }

    public String mainClassFinder(URLClassLoader manifestClassLoader) {
        try {
            URL manifestURL = manifestClassLoader.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestURL.openStream());
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Main-Class").replace(".$_init", "").replace(".", "/");
        } catch (Throwable throwable) {
            ERROR_STREAM.println(throwable + "%n");
            return null;
        }
    }

    public byte[] modifyMethods(InputStream inputStream, String className) {
        byte[] code;
        try {
            ClassReader reader = new ClassReader(inputStream);
            ClassWriter classWriter = new ProfilerClassWriter(reader, ClassWriter.COMPUTE_MAXS |
                    ClassWriter.COMPUTE_FRAMES);
            ClassVisitor change = new ProfilerClassVisitor(className, classWriter);
            reader.accept(change, ClassReader.EXPAND_FRAMES);
            code = classWriter.toByteArray();
            return code;
        } catch (Throwable e) {
            ERROR_STREAM.println(e + "%n");
        }
        return new byte[0]; // Return a zero-length byte array if the code was not modified
    }

    // Print out the modified class code
    public void printCode(String className, byte[] code, String balJarName) {
        int lastSlashIndex = className.lastIndexOf('/');
        String output;
        if (lastSlashIndex == -1) {
            output = balJarName;
            className = balJarName + "/" + className;
        } else {
            output = className.substring(0, lastSlashIndex);
        }
        File directory = new File(output);
        if (!directory.exists()) {
            boolean directoryCreated = directory.mkdirs();
            if (!directoryCreated) {
                return;
            }
        }
        try (FileOutputStream fos = new FileOutputStream(className)) {
            fos.write(code); // Write the code to the output stream
        } catch (IOException e) {
            throw new ProfilerException(e);
        }
    }
}
