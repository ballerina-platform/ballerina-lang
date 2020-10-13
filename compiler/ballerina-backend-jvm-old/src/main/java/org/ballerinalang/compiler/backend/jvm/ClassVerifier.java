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

import io.ballerina.runtime.BallerinaErrors;
import io.ballerina.runtime.values.ArrayValue;
import io.ballerina.runtime.values.ErrorValue;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.SimpleVerifier;
import org.objectweb.asm.util.CheckClassAdapter;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Class verifier checks whether a given class is valid. It will check each method call individually,
 * based only on its arguments, but does not check the sequence of method calls. This verifier provides
 * some basic type checking for each method instruction, and also capable of detecting any invalid
 * references to classes.
 * 
 * @since 1.0
 */
public class ClassVerifier {

    /**
     * Verifies the validity of classes.
     * 
     * @param jarEntries Content of all classes that needs to be verified.
     * @param jarPath path to the jar to be loaded.
     * @return An optional error, if there are verification errors.
     */
    public static Optional<ErrorValue> verify(Map<String, ArrayValue> jarEntries, String jarPath) {
        try {
            ClassLoader cl = new URLClassLoader(new URL[]{Paths.get(jarPath).toUri().toURL()});
            for (Map.Entry<String, ArrayValue> entries : jarEntries.entrySet()) {
                Optional<ErrorValue> result = verify(entries.getValue().getBytes(), cl);
                if (result.isPresent()) {
                    return result;
                }
            }
            return Optional.empty();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * This method is an extension of
     * {@code jdk.internal.org.objectweb.asm.util.CheckClassAdapter#verify(ClassReader, boolean, java.io.PrintWriter)}.
     * 
     * @param bytes Bytes stream of the class to be verified.
     * @return An optional error, if there are verification errors.
     */
    private static Optional<ErrorValue> verify(byte[] bytes, ClassLoader classLoader) {
        ClassReader classReader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        classReader.accept(new CheckClassAdapter(Opcodes.ASM7, classNode, false) {
        }, ClassReader.SKIP_DEBUG);

        Type syperType = classNode.superName == null ? null : Type.getObjectType(classNode.superName);
        List<MethodNode> methods = classNode.methods;

        List<Type> interfaces = new ArrayList<>();
        for (String interfaceName : classNode.interfaces) {
            interfaces.add(Type.getObjectType(interfaceName));
        }

        for (MethodNode method : methods) {
            SimpleVerifier verifier = new SimpleVerifier(Type.getObjectType(classNode.name), syperType, interfaces,
                    (classNode.access & Opcodes.ACC_INTERFACE) != 0);
            Analyzer<BasicValue> analyzer = new Analyzer<>(verifier);
            if (classLoader != null) {
                verifier.setClassLoader(classLoader);
            }
            try {
                analyzer.analyze(classNode.name, method);
            } catch (AnalyzerException e) {
                return Optional.of(BallerinaErrors.createError(e.getMessage()));
            }
        }

        return Optional.empty();
    }
}
