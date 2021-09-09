/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.utils;

import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.ballerinalang.bindgen.exceptions.BindgenException;
import org.ballerinalang.bindgen.model.JClass;
import org.ballerinalang.bindgen.model.JError;
import org.ballerinalang.formatter.core.Formatter;
import org.ballerinalang.formatter.core.FormatterException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BOOLEAN;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BOOLEAN_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BYTE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BYTE_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CHAR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DOUBLE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.FLOAT;
import static org.ballerinalang.bindgen.utils.BindgenConstants.FLOAT_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.HANDLE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.INT;
import static org.ballerinalang.bindgen.utils.BindgenConstants.INT_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.LONG;
import static org.ballerinalang.bindgen.utils.BindgenConstants.SHORT;

/**
 * Class containing the util methods of the Ballerina Bindgen CLI tool.
 *
 * @since 1.2.0
 */
public class BindgenUtils {

    private BindgenUtils() {
    }

    private static PrintStream errStream;
    private static PrintStream outStream;
    private static final String HAS_DIAGNOSTICS_ERROR = "syntax tree generated contains diagnostic errors";

    public static void outputSyntaxTreeFile(JError jError, BindgenEnv bindgenEnv,
                                            String outPath, Boolean append) throws BindgenException {

        SyntaxTree syntaxTree = new BindgenFileGenerator(bindgenEnv).generate(jError);
        if (syntaxTree.hasDiagnostics()) {
            bindgenEnv.setFailedClassGens(jError.getShortExceptionName(), HAS_DIAGNOSTICS_ERROR);
        } else {
            printOutputFile(syntaxTree.toSourceCode(), outPath, append);
        }
    }

    public static void outputSyntaxTreeFile(JClass jClass, BindgenEnv bindgenEnv,
                                            String outPath, Boolean append) throws BindgenException {

        SyntaxTree syntaxTree = new BindgenFileGenerator(bindgenEnv).generate(jClass);
        if (syntaxTree.hasDiagnostics()) {
            bindgenEnv.setFailedClassGens(jClass.getCurrentClass().getName(), HAS_DIAGNOSTICS_ERROR);
        } else {
            printOutputFile(syntaxTree.toSourceCode(), outPath, append);
        }
    }

    private static void printOutputFile(String content, String outPath, boolean append) throws BindgenException {
        PrintWriter writer = null;
        FileWriterWithEncoding fileWriter = null;
        try {
            fileWriter = new FileWriterWithEncoding(outPath, StandardCharsets.UTF_8, append);
            writer = new PrintWriter(fileWriter);
            writer.println(Formatter.format(content));
            fileWriter.close();
        } catch (IOException | FormatterException e) {
            throw new BindgenException("error: unable to create the file: " + outPath + " " + e.getMessage(), e);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void createDirectory(String path) throws BindgenException {
        File directory = new File(path);
        if (!directory.exists()) {
            try {
                final boolean mkdirResult = directory.mkdirs();
                if (!mkdirResult) {
                    throw new BindgenException("error: unable to create the directory: " + path);
                }
            } catch (SecurityException e) {
                throw new BindgenException("error: unable to create the directory: " + path + " " + e.getMessage(), e);
            }
        }
    }

    public static Set<String> addImportedPackage(Class type, Set<String> importedPackages) {
        if (type.isArray()) {
            type = type.getComponentType();
        }
        if (!type.isPrimitive() && type != String.class) {
            importedPackages.add(type.getPackageName());
        }
        return importedPackages;
    }

    public static boolean isPublicConstructor(Constructor constructor) {
        int modifiers = constructor.getModifiers();
        return Modifier.isPublic(modifiers);
    }

    public static boolean isPublicField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isPublic(modifiers);
    }

    public static boolean isPublicMethod(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isPublic(modifiers);
    }

    public static boolean isPublicClass(Class javaClass) {
        int modifiers = javaClass.getModifiers();
        return Modifier.isPublic(modifiers);
    }

    public static boolean isStaticField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isStatic(modifiers);
    }

    public static boolean isStaticMethod(Method method) {
        int modifiers = method.getModifiers();
        return Modifier.isStatic(modifiers);
    }

    public static boolean isFinalField(Field field) {
        int modifiers = field.getModifiers();
        return Modifier.isFinal(modifiers);
    }

    public static String getBallerinaParamType(Class javaType, Map<String, String> aliases) {
        if (javaType.isArray() && javaType.getComponentType().isPrimitive()) {
            return getPrimitiveArrayBalType(javaType.getComponentType().getSimpleName());
        } else {
            String returnType = getBalType(getAlias(javaType, aliases));
            if (returnType.equals(HANDLE)) {
                return getAlias(javaType, aliases);
            }
            return returnType;
        }
    }

    public static String getBallerinaHandleType(Class javaType) {
        String type = javaType.getSimpleName();
        String returnType = getBalType(type);
        if (type.equals(JAVA_STRING) || type.equals(JAVA_STRING_ARRAY)) {
            returnType = HANDLE;
        }
        return returnType;
    }

    public static String getJavaType(Class javaType) {
        if (javaType.isArray()) {
            javaType = javaType.getComponentType();
        }
        if (javaType.isPrimitive()) {
            return javaType.getSimpleName();
        } else if (javaType.getSimpleName().equals(JAVA_STRING)) {
            return BALLERINA_STRING;
        } else {
            return HANDLE;
        }
    }

    private static String getBalType(String type) {
        switch (type) {
            case INT:
                return INT;
            case FLOAT:
                return FLOAT;
            case BOOLEAN:
                return BOOLEAN;
            case BYTE:
                return BYTE;
            case SHORT:
                return INT;
            case CHAR:
                return INT;
            case DOUBLE:
                return FLOAT;
            case LONG:
                return INT;
            case JAVA_STRING:
                return BALLERINA_STRING;
            case JAVA_STRING_ARRAY:
                return BALLERINA_STRING_ARRAY;
            default:
                return HANDLE;
        }
    }

    private static String getPrimitiveArrayBalType(String type) {
        switch (type) {
            case INT:
                return INT_ARRAY;
            case FLOAT:
                return FLOAT_ARRAY;
            case BOOLEAN:
                return BOOLEAN_ARRAY;
            case BYTE:
                return BYTE_ARRAY;
            case SHORT:
                return INT_ARRAY;
            case CHAR:
                return INT_ARRAY;
            case DOUBLE:
                return FLOAT_ARRAY;
            case LONG:
                return INT_ARRAY;
            default:
                return HANDLE;
        }
    }

    public static String getPrimitiveArrayType(String type) {
        switch (type) {
            case "[C":
            case "[S":
            case "[J":
                return INT_ARRAY;
            case "[D":
                return FLOAT_ARRAY;
            case "[B":
                return BYTE_ARRAY;
            case "[I":
                return INT_ARRAY;
            case "[F":
                return FLOAT_ARRAY;
            case "[Z":
                return BOOLEAN_ARRAY;
            default:
                return type;
        }
    }

    public static URLClassLoader getClassLoader(Set<String> jarPaths, ClassLoader parent) throws BindgenException {
        URLClassLoader classLoader;
        List<URL> urls = new ArrayList<>();
        try {
            List<String> classPaths = new ArrayList<>();
            List<String> failedClassPaths = new ArrayList<>();
            for (String path : jarPaths) {
                File file = FileSystems.getDefault().getPath(path).toFile();
                if (file.isDirectory()) {
                    File[] paths = file.listFiles();
                    if (paths != null) {
                        for (File filePath: paths) {
                            if (isJarFile(filePath)) {
                                urls.add(filePath.toURI().toURL());
                                classPaths.add(filePath.getName());
                            }
                        }
                    } else {
                        failedClassPaths.add(path);
                    }
                } else {
                    if (isJarFile(file)) {
                        urls.add(file.toURI().toURL());
                        classPaths.add(file.getName());
                    } else {
                        failedClassPaths.add(file.toString());
                    }
                }
            }
            if (!classPaths.isEmpty()) {
                outStream.println("\nFollowing jars were added to the classpath:");
                for (String path : classPaths) {
                    outStream.println("\t" + path);
                }
            }
            if (!failedClassPaths.isEmpty()) {
                errStream.println("\nFailed to add the following to classpath:");
                for (String path : failedClassPaths) {
                    outStream.println("\t" + path);
                }
            }
            classLoader = (URLClassLoader) AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () ->
                    new ChildFirstClassLoader(urls.toArray(new URL[0]), parent));
        } catch (RuntimeException e) {
            throw new BindgenException("error: unable to load the provided classpaths: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new BindgenException("error: unable to process the provided classpaths: " + e.getMessage(), e);
        }
        return classLoader;
    }

    private static boolean isJarFile(File file) {
        String fileName = file.getName();
        return file.isFile() && fileName.substring(fileName.lastIndexOf('.')).equals(".jar");
    }

    public static void setErrStream(PrintStream errStream) {
        BindgenUtils.errStream = errStream;
    }

    public static void setOutStream(PrintStream outStream) {
        BindgenUtils.outStream = outStream;
    }

    public static String getAlias(Class className, Map<String, String> aliases) {
        if (!aliases.containsKey(className.getName())) {
            int i = 2;
            boolean notAdded = true;
            String simpleName = className.getSimpleName();
            String alias = simpleName;
            if (!aliases.containsValue(alias)) {
                aliases.put(className.getName(), alias);
            } else {
                while (notAdded) {
                    if (className.isArray()) {
                        int insertInto = simpleName.toCharArray().length - 2;
                        alias = simpleName.substring(0, insertInto) + i + simpleName.substring(insertInto);
                    } else {
                        alias = simpleName + i;
                    }
                    if (!aliases.containsValue(alias)) {
                        aliases.put(className.getName(), alias);
                        notAdded = false;
                    }
                    i++;
                }
            }
        }
        return aliases.get(className.getName());
    }
}
