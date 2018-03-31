/*
 *
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.ballerinalang.siddhi.annotation.classindex;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Access to the compile-time generated index of classes.
 * <p>
 * Use &#064;{@link IndexAnnotated} and &#064;{@link IndexSubclasses} annotations to force the classes to be indexed.
 * </p>
 * <p>
 * Keep in mind that the class is indexed only when it is compiled with
 * classindex.jar file in classpath.
 * </p>
 *
 * Also to preserve class-index data when creating shaded jar you should use the following
 * Maven configuration:
 * <pre>
 * {@code
 * <build>
 *   <plugins>
 *     <plugin>
 *       <groupId>org.apache.maven.plugins</groupId>
 *       <artifactId>maven-shade-plugin</artifactId>
 *       <version>1.4</version>
 *       <executions>
 *         <execution>
 *           <phase>package</phase>
 *           <goals>
 *             <goal>shade</goal>
 *           </goals>
 *           <configuration>
 *             <transformers>
 *               <transformer implementation="org.ballerinalang.siddhi.annotation.classindex.ClassIndexTransformer"/>
 *             </transformers>
 *           </configuration>
 *         </execution>
 *       </executions>
 *       <dependencies>
 *         <groupId>org.atteo.classindex</groupId>
 *         <artifactId>classindex-transformer</artifactId>
 *       </dependencies>
 *     </plugin>
 *   </plugins>
 * </build>
 * }
 * </pre>
 */
public class ClassIndex {
    public static final String SUBCLASS_INDEX_PREFIX = "META-INF/services/";
    public static final String ANNOTATED_INDEX_PREFIX = "META-INF/annotations/";
    public static final String PACKAGE_INDEX_NAME = "jaxb.index";
    public static final String JAVADOC_PREFIX = "META-INF/javadocs/";

    private ClassIndex() {

    }

    /**
     * Retrieves a list of subclasses of the given class.
     * <p>
     * The class must be annotated with {@link IndexSubclasses} for it's subclasses to be indexed
     * at compile-time by {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     * @param superClass superClass class to find subclasses for
     * @param <T> super class type
     * @return list of subclasses
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<Class<? extends T>> getSubclasses(Class<T> superClass) {
        return getSubclasses(superClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Retrieves a list of subclasses of the given class.
     * <p>
     * The class must be annotated with {@link IndexSubclasses} for it's subclasses to be indexed
     * at compile-time by {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param superClass  class to find subclasses for
     * @param classLoader classloader for loading classes
     * @param <T> super class type
     * @return list of subclasses
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<Class<? extends T>> getSubclasses(Class<T> superClass, ClassLoader classLoader) {
        Iterable<String> entries = getSubclassesNames(superClass, classLoader);
        Set<Class<?>> classes = new HashSet<>();
        findClasses(classLoader, classes, entries);
        List<Class<? extends T>> subclasses = new ArrayList<>();

        for (Class<?> klass : classes) {
            if (superClass.isAssignableFrom(klass)) {
                subclasses.add((Class<? extends T>) klass);
            }
        }

        return subclasses;
    }

    /**
     * Retrieves names of subclasses of the given class.
     * <p>
     * The class must be annotated with {@link IndexSubclasses} for it's subclasses to be indexed
     * at compile-time by {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param superClass class to find subclasses for
     * @param <T> super class type
     * @return names of subclasses
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<String> getSubclassesNames(Class<T> superClass) {
        return getSubclassesNames(superClass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Retrieves names of subclasses of the given class.
     * <p>
     * The class must be annotated with {@link IndexSubclasses} for it's subclasses to be indexed
     * at compile-time by {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param superClass  class to find subclasses for
     * @param classLoader classloader for loading index file
     * @param <T> super class type
     * @return names of subclasses
     */
    @SuppressWarnings("unchecked")
    public static <T> Iterable<String> getSubclassesNames(Class<T> superClass, ClassLoader classLoader) {
        return readIndexFile(classLoader, SUBCLASS_INDEX_PREFIX + superClass.getCanonicalName());
    }

    /**
     * Retrieves a list of classes from given package.
     * <p>
     * The package must be annotated with {@link IndexSubclasses} for the classes inside
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param packageName name of the package to search classes for
     * @return list of classes from package
     */
    public static Iterable<Class<?>> getPackageClasses(String packageName) {
        return getPackageClasses(packageName, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Retrieves a list of classes from given package.
     * <p>
     * The package must be annotated with {@link IndexSubclasses} for the classes inside
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param packageName name of the package to search classes for
     * @param classLoader classloader for loading classes
     * @return list of classes from package
     */
    public static Iterable<Class<?>> getPackageClasses(String packageName, ClassLoader classLoader) {
        Iterable<String> entries = readIndexFile(classLoader,
                packageName.replace(".", "/") + "/" + PACKAGE_INDEX_NAME);

        Set<Class<?>> classes = new HashSet<>();
        findClassesInPackage(classLoader, packageName, classes, entries);

        // also execute findClasses because old ClassIndex versions where creating
        // this file in incorrect format with class full name instead of simple name
        findClasses(classLoader, classes, entries);
        return classes;
    }

    /**
     * Retrieves names of classes from given package.
     * <p>
     * The package must be annotated with {@link IndexSubclasses} for the classes inside
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param packageName name of the package to search classes for
     * @return names of classes from package
     */
    public static Iterable<String> getPackageClassesNames(String packageName) {
        return getPackageClassesNames(packageName, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Retrieves names of classes from given package.
     * <p>
     * The package must be annotated with {@link IndexSubclasses} for the classes inside
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param packageName name of the package to search classes for
     * @param classLoader classloader for loading index file
     * @return names of classes from package
     */
    public static Iterable<String> getPackageClassesNames(String packageName, ClassLoader classLoader) {
        Iterable<String> entries = readIndexFile(classLoader,
                packageName.replace(".", "/") + "/" + PACKAGE_INDEX_NAME);
        List<String> result = new ArrayList<>();
        for (String simpleName : entries) {
            result.add(packageName + "." + simpleName);
        }
        return result;
    }

    /**
     * Retrieves a list of classes annotated by given annotation.
     * <p>
     * The annotation must be annotated with {@link IndexAnnotated} for annotated classes
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param annotation annotation to search class for
     * @return list of annotated classes
     */
    public static Iterable<Class<?>> getAnnotated(Class<? extends Annotation> annotation) {
        return getAnnotated(annotation, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Retrieves a list of classes annotated by given annotation.
     * <p>
     * The annotation must be annotated with {@link IndexAnnotated} for annotated classes
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     *
     * @param annotation  annotation to search class for
     * @param classLoader classloader for loading classes
     * @return list of annotated classes
     */
    public static Iterable<Class<?>> getAnnotated(Class<? extends Annotation> annotation, ClassLoader classLoader) {
        Iterable<String> entries = getAnnotatedNames(annotation, classLoader);
        Set<Class<?>> classes = new HashSet<>();
        findClasses(classLoader, classes, entries);
        return classes;
    }

    /**
     * Retrieves names of classes annotated by given annotation.
     * <p>
     * The annotation must be annotated with {@link IndexAnnotated} for annotated classes
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     * <p>
     * Please note there is no verification if the class really exists. It can be missing when incremental
     * compilation is used. Use {@link #getAnnotated(Class) } if you need the verification.
     * </p>
     *
     * @param annotation annotation to search class for
     * @return names of annotated classes
     */
    public static Iterable<String> getAnnotatedNames(Class<? extends Annotation> annotation) {
        return getAnnotatedNames(annotation, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Retrieves names of classes annotated by given annotation.
     * <p>
     * The annotation must be annotated with {@link IndexAnnotated} for annotated classes
     * to be indexed at compile-time by
     * {@link org.ballerinalang.siddhi.annotation.classindex.processor.ClassIndexProcessor}.
     * </p>
     * <p>
     * Please note there is no verification if the class really exists. It can be missing when incremental
     * compilation is used. Use {@link #getAnnotated(Class, ClassLoader) } if you need the verification.
     * </p>
     *
     * @param annotation  annotation to search class for
     * @param classLoader classloader for loading the index file
     * @return names of annotated classes
     */
    public static Iterable<String> getAnnotatedNames(Class<? extends Annotation> annotation, ClassLoader classLoader) {
        return readIndexFile(classLoader, ANNOTATED_INDEX_PREFIX + annotation.getCanonicalName());
    }

    /**
     * Returns the Javadoc summary for given class.
     * <p>
     * Javadoc summary is the first sentence of a Javadoc.
     * </p>
     * <p>
     * You need to use {@link IndexSubclasses} or {@link IndexAnnotated} with {@link IndexAnnotated#storeJavadoc()}
     * set to true.
     * </p>
     *
     * @param klass class to retrieve summary for
     * @return summary for given class, or null if it does not exists
     * @see "http://www.oracle.com/technetwork/java/javase/documentation/index-137868.html#
     * writingdoccomments"
     */
    public static String getClassSummary(Class<?> klass) {
        return getClassSummary(klass, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Returns the Javadoc summary for given class.
     * <p>
     * Javadoc summary is the first sentence of a Javadoc.
     * </p>
     * <p>
     * You need to use {@link IndexSubclasses} or {@link IndexAnnotated} with {@link IndexAnnotated#storeJavadoc()}
     * set to true.
     * </p>
     *
     * @param klass       class to retrieve summary for
     * @param classLoader classloader for loading classes
     * @return summary for given class, or null if it does not exists
     * @see "http://www.oracle.com/technetwork/java/javase/documentation/index-137868.html#
     * writingdoccomments"
     */
    public static String getClassSummary(Class<?> klass, ClassLoader classLoader) {
        URL resource = classLoader.getResource(JAVADOC_PREFIX + klass.getCanonicalName());
        if (resource == null) {
            return null;
        }
        try {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(),
                    "UTF-8"))) {
                StringBuilder builder = new StringBuilder();
                String line = reader.readLine();
                while (line != null) {
                    int dotIndex = line.indexOf('.');
                    if (dotIndex == -1) {
                        builder.append(line);
                    } else {
                        builder.append(line.subSequence(0, dotIndex));
                        return builder.toString().trim();
                    }
                    line = reader.readLine();
                }
                return builder.toString().trim();
            } catch (FileNotFoundException e) {
                // catch this just in case some compiler actually throws that
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException("ClassIndex: Cannot read Javadoc index", e);
        }
    }

    private static Iterable<String> readIndexFile(ClassLoader classLoader, String resourceFile) {
        Set<String> entries = new HashSet<>();

        try {
            Enumeration<URL> resources = classLoader.getResources(resourceFile);

            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(),
                        "UTF-8"))) {

                    String line = reader.readLine();
                    while (line != null) {
                        entries.add(line);
                        line = reader.readLine();
                    }
                } catch (FileNotFoundException e) {
                    // When executed under Tomcat started from Eclipse with "Serve modules without
                    // publishing" option turned on, getResources() method above returns the same
                    // resource two times: first with incorrect path and second time with correct one.
                    // So ignore the one which does not exist.
                    // See: https://github.com/atteo/classindex/issues/5
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("ClassIndex: Cannot read class index", e);
        }
        return entries;
    }

    private static void findClasses(ClassLoader classLoader, Set<Class<?>> classes, Iterable<String> entries) {
        for (String entry : entries) {
            Class<?> klass;
            try {
                klass = classLoader.loadClass(entry);
            } catch (ClassNotFoundException e) {
                continue;
            }
            classes.add(klass);
        }
    }

    private static void findClassesInPackage(ClassLoader classLoader, String packageName, Set<Class<?>> classes,
                                             Iterable<String> entries) {
        for (String entry : entries) {
            if (entry.contains(".")) {
                continue;
            }
            Class<?> klass;
            try {
                klass = classLoader.loadClass(packageName + "." + entry);
            } catch (ClassNotFoundException e) {
                continue;
            }
            classes.add(klass);
        }
    }
}
