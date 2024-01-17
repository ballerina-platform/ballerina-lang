/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.projects.internal.plugins;

import io.ballerina.compiler.internal.parser.tree.STAnnotationNode;
import io.ballerina.compiler.syntax.tree.AnnotationNode;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.plugins.CompilerPlugin;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * This class contains a set of utility method related to compiler plugin implementation.
 *
 * @since 2.0.0
 */
public class CompilerPlugins {
    static List<CompilerPlugin> builtInPlugins = new ArrayList<>();

    private CompilerPlugins() {
    }

    static {
        ServiceLoader<CompilerPlugin> pluginServiceLoader = ServiceLoader
                .load(CompilerPlugin.class, CompilerPlugins.class.getClassLoader());
        for (CompilerPlugin plugin : pluginServiceLoader) {
            builtInPlugins.add(plugin);
        }
    }

    public static List<CompilerPlugin> getBuiltInPlugins() {
        return builtInPlugins;
    }

    public static CompilerPlugin loadCompilerPlugin(String pluginClassName, List<Path> jarDependencyPaths) {
        ClassLoader classLoader = createClassLoader(jarDependencyPaths);
        Class<?> pluginClass = loadPluginClass(pluginClassName, classLoader);
        Class<CompilerPlugin> compilerPluginAbstractClass = CompilerPlugin.class;
        if (!compilerPluginAbstractClass.isAssignableFrom(pluginClass)) {
            throw new ProjectException("Specified class '" + pluginClassName + "' is not a subclass of '" +
                    compilerPluginAbstractClass.getName() + "'");
        }

        Constructor<?> defaultConstructor = getDefaultConstructor(pluginClass);
        try {
            return (CompilerPlugin) defaultConstructor.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ProjectException("Cannot create a new instance of the class '" +
                    pluginClassName + "', reason: " + e.getMessage());
        } catch (InvocationTargetException e) {
            throw new ProjectException("Cannot create a new instance of the class '" +
                    pluginClassName + "', reason: " + e.getTargetException());
        }
    }

    private static Constructor<?> getDefaultConstructor(Class<?> pluginClass) {
        try {
            return pluginClass.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            throw new ProjectException("Cannot find the default constructor in class: '" +
                    pluginClass.getName() + "'");
        }
    }

    private static Class<?> loadPluginClass(String pluginClassName, ClassLoader classLoader) {
        try {
            return classLoader.loadClass(pluginClassName);
        } catch (ClassNotFoundException e) {
            throw new ProjectException("Cannot find class '" + pluginClassName + "'");
        }
    }

    private static ClassLoader createClassLoader(List<Path> jarDependencyPaths) {
        return AccessController.doPrivileged(
                (PrivilegedAction<URLClassLoader>) () -> new URLClassLoader(getJarURLS(jarDependencyPaths),
                        CompilerPlugins.class.getClassLoader())
        );
    }

    private static URL[] getJarURLS(List<Path> jarDependencyPaths) {
        URL[] jarURLS = new URL[jarDependencyPaths.size()];
        for (int i = 0; i < jarDependencyPaths.size(); i++) {
            try {
                jarURLS[i] = jarDependencyPaths.get(i).toUri().toURL();
            } catch (MalformedURLException e) {
                throw new ProjectException(e.getMessage(), e);
            }
        }
        return jarURLS;
    }

    public static List<String> annotationsAsStr(NodeList<AnnotationNode> supportedAnnotations) {
        List<String> annotations = new ArrayList<>();
        StringBuilder id = new StringBuilder();
        for (AnnotationNode annotation : supportedAnnotations) {
            String annotationRef = ((STAnnotationNode) annotation.internalNode()).annotReference.toString()
                    .replaceAll("\\s", "");
            id.append(annotationRef);

            String annotationVal = ((STAnnotationNode) annotation.internalNode()).annotValue.toString()
                    .replaceAll("\\s", "");
            id.append(annotationVal);
            annotations.add(id.toString());
        }
        annotations.sort(Comparator.naturalOrder());
        return annotations;
    }
}
