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

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.github.jknack.handlebars.context.FieldValueResolver;
import com.github.jknack.handlebars.context.JavaBeanValueResolver;
import com.github.jknack.handlebars.context.MapValueResolver;
import com.github.jknack.handlebars.io.ClassPathTemplateLoader;
import com.github.jknack.handlebars.io.FileTemplateLoader;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.ballerinalang.bindgen.components.JMethod;
import org.ballerinalang.bindgen.exceptions.BindgenException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BALLERINA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BAL_EXTENSION;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BOOLEAN;
import static org.ballerinalang.bindgen.utils.BindgenConstants.BYTE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.CHAR;
import static org.ballerinalang.bindgen.utils.BindgenConstants.DOUBLE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.FLOAT;
import static org.ballerinalang.bindgen.utils.BindgenConstants.HANDLE;
import static org.ballerinalang.bindgen.utils.BindgenConstants.INT;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA_STRING;
import static org.ballerinalang.bindgen.utils.BindgenConstants.JAVA_STRING_ARRAY;
import static org.ballerinalang.bindgen.utils.BindgenConstants.LONG;
import static org.ballerinalang.bindgen.utils.BindgenConstants.MUSTACHE_FILE_EXTENSION;
import static org.ballerinalang.bindgen.utils.BindgenConstants.SHORT;
import static org.ballerinalang.bindgen.utils.BindgenConstants.TEMPLATES_DIR_PATH_KEY;

/**
 * Class containing the util methods of the Ballerina Bindgen CLI tool.
 */
public class BindgenUtils {

    private BindgenUtils() {

    }

    private static final PrintStream errStream = System.err;
    private static final PrintStream outStream = System.out;

    public static void writeOutputFile(Object object, String templateDir, String templateName, String outPath,
                                       Boolean append) throws BindgenException {

        PrintWriter writer = null;
        FileWriterWithEncoding fileWriter = null;
        try {
            Template template = compileTemplate(templateDir, templateName);
            Context context = Context.newBuilder(object).resolver(
                    MapValueResolver.INSTANCE,
                    JavaBeanValueResolver.INSTANCE,
                    FieldValueResolver.INSTANCE).build();
            fileWriter = new FileWriterWithEncoding(outPath, StandardCharsets.UTF_8, append);
            writer = new PrintWriter(fileWriter);
            writer.println(template.apply(context));
            fileWriter.close();
        } catch (IOException e) {
            throw new BindgenException("IOException while writing to the Ballerina file. " + e.getMessage(), e);
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

    private static Template compileTemplate(String defaultTemplateDir, String templateName)
            throws BindgenException {

        String templatesDirPath = System.getProperty(TEMPLATES_DIR_PATH_KEY, defaultTemplateDir);
        ClassPathTemplateLoader cpTemplateLoader = new ClassPathTemplateLoader((templatesDirPath));
        FileTemplateLoader fileTemplateLoader = new FileTemplateLoader(templatesDirPath);
        cpTemplateLoader.setSuffix(MUSTACHE_FILE_EXTENSION);
        fileTemplateLoader.setSuffix(MUSTACHE_FILE_EXTENSION);
        Handlebars handlebars = new Handlebars().with(cpTemplateLoader, fileTemplateLoader);
        handlebars.registerHelper("controlChars", (object, options) -> {
            if (object instanceof String) {
                return "\'" + object;
            }
            return "";
        });
        handlebars.registerHelper("getSimpleName", (object, options) -> {
            if (object instanceof String) {
                String className = (String) object;
                return className.substring(className.lastIndexOf('.') + 1);
            }
            return object;
        });
        handlebars.registerHelper("getType", (object, options) -> {
            if (object.equals("[C") || object.equals("[S") || object.equals("[J")) {
                return "byte[]|int[]|float[]";
            } else if (object.equals("[D")) {
                return "byte[]|float[]";
            } else if (object.equals("[B")) {
                return "byte[]";
            } else if (object.equals("[I")) {
                return "int[]";
            } else if (object.equals("[F")) {
                return "float[]";
            } else if (object.equals("[Z")) {
                return "boolean[]";
            } else {
                return object;
            }
        });
        try {
            return handlebars.compile(templateName);
        } catch (FileNotFoundException e) {
            throw new BindgenException("Code generation template file does not exist. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new BindgenException("IO error while compiling the template file. " + e.getMessage(), e);
        }
    }

    private static void listAllFiles(String directoryName, List<File> files) {

        File directory = new File(directoryName);
        File[] fileList = directory.listFiles();
        if (fileList != null) {
            for (File file : fileList) {
                if (file.isFile()) {
                    files.add(file);
                } else if (file.isDirectory()) {
                    listAllFiles(file.getAbsolutePath(), files);
                }
            }
        }
    }

    public static void notifyExistingDependencies(Set<String> classList, File dependencyPath) {

        if (dependencyPath.isDirectory()) {
            List<File> listOfFiles = new ArrayList<>();
            listAllFiles(dependencyPath.toString(), listOfFiles);
            if (listOfFiles.size() > 1) {
                for (String className : classList) {
                    for (File file : listOfFiles) {
                        String fileName = className.substring(className.lastIndexOf('.') + 1) + BAL_EXTENSION;
                        if (file.getName().equals(fileName)) {
                            outStream.println("\nPlease delete the existing dependency: " + file.getPath());
                        }
                    }
                }
            }
        }
    }

    public static Set<String> getUpdatedConstantsList(Path existingPath, Set<String> classList) {

        try {
            List<String> allLines = Files.readAllLines(Paths.get(existingPath.toString()));
            for (String className : classList) {
                for (String line : allLines) {
                    if (line.contains("\"" + className + "\"")) {
                        classList.remove(className);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            errStream.println("Error while accessing the existing constants file. " + e);
        }
        return classList;
    }

    public static void createDirectory(String path) throws BindgenException {

        File directory = new File(path);
        if (!directory.exists()) {
            try {
                final boolean mkdirResult = directory.mkdir();
                if (!mkdirResult) {
                    errStream.println("directory " + path + " could not be created");
                }
            } catch (SecurityException e) {
                throw new BindgenException("Unable to create the directory: " + path, e);
            }
        }
    }

    public static Set<String> getClassNamesInJar(String jarPath) throws IOException {

        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarPath));
        JarEntry jarEntry;
        Set<String> classes = new HashSet<>();
        try {
            while (true) {
                jarEntry = jarInputStream.getNextJarEntry();
                if (jarEntry == null) {
                    break;
                }
                if ((jarEntry.getName().endsWith(".class"))) {
                    String className = jarEntry.getName().replace("/", ".");
                    classes.add(className.substring(0, className.length() - ".class".length()));
                }
            }
            jarInputStream.close();
        } catch (IOException e) {
            throw new IOException("error while accessing the jar: ", e);
        } finally {
            jarInputStream.close();
        }
        return classes;
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

    public static boolean isAbstractClass(Class javaClass) {

        int modifiers = javaClass.getModifiers();
        return Modifier.isAbstract(modifiers);
    }

    public static String getModuleName(String jarPath) {

        String name = null;
        if (jarPath != null) {
            Path jarName = Paths.get(jarPath).getFileName();
            if (jarName != null) {
                name = jarName.toString().substring(0, jarName.toString()
                        .lastIndexOf('.'));
                name = name.replace('-', '_');
            }
        }
        return name;
    }

    public static void handleOverloadedMethods(List<JMethod> methodList) {

        Map<String, Integer> methodNames = new HashMap<>();
        for (JMethod method : methodList) {
            String mName = method.methodName;
            if (methodNames.containsKey(mName)) {
                methodNames.replace(mName, methodNames.get(mName) + 1);
            } else {
                methodNames.put(mName, 1);
            }
        }
        for (Map.Entry<String, Integer> entry : methodNames.entrySet()) {
            if (entry.getValue() > 1) {
                int i = 1;
                for (JMethod jMethod : methodList) {
                    if (jMethod.methodName.equals(entry.getKey())) {
                        jMethod.methodName = jMethod.methodName + i;
                        jMethod.params = true;
                        i++;
                    }
                }
            }
        }
    }

    public static String balType(String type) {

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

    public static URLClassLoader getClassLoader(Set<String> jarPaths, ClassLoader parent) throws BindgenException {

        URLClassLoader classLoader;
        List<URL> urls = new ArrayList<>();
        try {
            for (String path : jarPaths) {
                urls.add(FileSystems.getDefault().getPath(path).toFile().toURI().toURL());
            }
            classLoader = (URLClassLoader) AccessController.doPrivileged((PrivilegedAction) ()
                    -> new URLClassLoader(urls.toArray(new URL[urls.size()]), parent));
        } catch (Exception e) {
            throw new BindgenException("Error while processing the classpaths.", e);
        }
        return classLoader;
    }
}
