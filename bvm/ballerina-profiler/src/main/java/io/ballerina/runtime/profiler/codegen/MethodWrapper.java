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

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import static io.ballerina.runtime.profiler.Main.*;

public class MethodWrapper extends ClassLoader {

    public static void invokeMethods() throws IOException, InterruptedException {
        String[] command = {"java", "-jar", TEMP_JAR_FILE_NAME};
        if (balJarArgs != null) {
            command = Arrays.copyOf(command, command.length + 1);
            command[3] = balJarArgs;
        }
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        System.out.println(ANSI_CYAN + "[5/6] Running Executable..." + ANSI_RESET);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        reader.lines().forEach(System.out::println);
        process.waitFor();
    }

    public static String mainClassFinder(URLClassLoader manifestClassLoader) {
        try {
            URL manifestURL = manifestClassLoader.findResource("META-INF/MANIFEST.MF");
            Manifest manifest = new Manifest(manifestURL.openStream());
            Attributes attributes = manifest.getMainAttributes();
            return attributes.getValue("Main-Class").replace(".$_init", "").replace(".", "/");
        } catch (Exception | Error throwable) {
            System.err.println(throwable);
            return null;
        }
    }

    public static byte[] modifyMethods(InputStream inputStream) {
        byte[] code;
            try {
                ClassReader reader = new ClassReader(inputStream); //Create a ClassReader object using the inputStream
                ClassWriter classWriter = new CustomClassWriter(reader, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES); //Create a BallerinaClassWriter object using the classReader with both COMPUTE_MAXS and COMPUTE_FRAMES
                ClassVisitor change = new CustomClassVisitor(classWriter); //Create a ClassVisitor object to make changes to the class
                reader.accept(change, ClassReader.EXPAND_FRAMES); //Accept the changes using the classReader
                code = classWriter.toByteArray(); //Convert the changed code into a Byte Array
                return code; //Return the Byte Array
            } catch (Exception | Error e) {
                e.printStackTrace(); //Print the stack trace of the exception or error
            }
        return null; //Return null if the code was not modified
    }

    // Print out the modified class code
    public static void printCode(String className, byte[] code) {
        int lastSlashIndex = className.lastIndexOf('/');
        String output = className.substring(0, lastSlashIndex);
        new File(output).mkdirs();
        try {
            //Create a FileOutputStream object using the className
            FileOutputStream fos = new FileOutputStream(className);
            fos.write(code); //Write the code to the output stream
            fos.close(); //Close the output stream
        } catch (IOException ignore) {}
    }
}