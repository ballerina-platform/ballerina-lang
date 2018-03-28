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

package org.ballerinalang.siddhi.annotation.classindex.processor;

import org.ballerinalang.siddhi.annotation.classindex.ClassIndex;
import org.ballerinalang.siddhi.annotation.classindex.IndexAnnotated;
import org.ballerinalang.siddhi.annotation.classindex.IndexSubclasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner6;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

/**
 * Generates index files for {@link ClassIndex}.
 */
public class ClassIndexProcessor extends AbstractProcessor {
    private static final Logger log = LoggerFactory.getLogger(ClassIndexProcessor.class);
    private Map<String, Set<String>> subclassMap = new HashMap<>();
    private Map<String, Set<String>> annotatedMap = new HashMap<>();
    private Map<String, Set<String>> packageMap = new HashMap<>();
    private boolean annotationDriven = true;
    private Set<String> indexedAnnotations = new HashSet<>();
    private Set<String> indexedSuperclasses = new HashSet<>();
    private Set<String> indexedPackages = new HashSet<>();
    private Set<TypeElement> javadocAlreadyStored = new HashSet<>();
    private Types types;
    private Filer filer;
    private Elements elementUtils;
    private Messager messager;

    public ClassIndexProcessor() {
    }

    /**
     * Used when creating subclasses of the processor which will index some annotations
     * which cannot be itself annotated with {@link IndexAnnotated} or {@link IndexSubclasses}.
     *
     * @param classes list of classes which the processor will be indexing
     */
    protected ClassIndexProcessor(Class<?>... classes) {
        if (classes.length == 0) {
            return;
        }
        annotationDriven = false;
        for (Class<?> klass : classes) {
            indexedAnnotations.add(klass.getCanonicalName());
        }
    }

    private static void readOldIndexFile(Set<String> entries, Reader reader) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line = bufferedReader.readLine();
            while (line != null) {
                entries.add(line);
                line = bufferedReader.readLine();
            }
        }
    }

    /**
     * Adds given annotations for indexing.
     * @param classes classes to index annotations
     */
    protected final void indexAnnotations(Class<?>... classes) {
        for (Class<?> klass : classes) {
            indexedAnnotations.add(klass.getCanonicalName());
        }
        annotationDriven = false;
    }

    /**
     * Adds given classes for subclass indexing.
     * @param classes classes to index subclasses
     */
    protected final void indexSubclasses(Class<?>... classes) {
        for (Class<?> klass : classes) {
            indexedSuperclasses.add(klass.getCanonicalName());
        }
        annotationDriven = false;
    }

    /**
     * Adds given package for indexing.
     * @param packages packages to be indexed
     */
    protected final void indexPackages(String... packages) {
        Collections.addAll(indexedPackages, packages);
        annotationDriven = false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Collections.singleton("*");
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        types = processingEnv.getTypeUtils();
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        messager = processingEnv.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element element : roundEnv.getRootElements()) {
                if (!(element instanceof TypeElement)) {
                    continue;
                }
                final PackageElement packageElement = getPackage(element);
                element.accept(new ElementScanner6<Void, Void>() {
                    @Override
                    public Void visitType(TypeElement typeElement, Void o) {
                        try {
                            for (AnnotationMirror mirror : typeElement.getAnnotationMirrors()) {
                                final TypeElement annotationElement = (TypeElement)
                                        mirror.getAnnotationType().asElement();
                                storeAnnotation(annotationElement, typeElement);
                            }
                            indexSupertypes(typeElement, typeElement);
                            if (packageElement != null) {
                                storeClassFromPackage(packageElement, typeElement);
                            }
                        } catch (IOException e) {
                            messager.printMessage(Diagnostic.Kind.ERROR, "[ClassIndexProcessor] "
                                    + e.getMessage());
                        }
                        return super.visitType(typeElement, o);
                    }
                }, null);
            }

            if (!roundEnv.processingOver()) {
                return false;
            }

            writeIndexFiles(ClassIndex.SUBCLASS_INDEX_PREFIX, subclassMap);
            writeIndexFiles(ClassIndex.ANNOTATED_INDEX_PREFIX, annotatedMap);

            for (Map.Entry<String, Set<String>> entry : packageMap.entrySet()) {
                writeSimpleNameIndexFile(entry.getValue(), entry.getKey()
                        .replace(".", "/")
                        + "/" + ClassIndex.PACKAGE_INDEX_NAME);
            }
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, "[ClassIndexProcessor] Can't write index file: "
                    + e.getMessage());
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            messager.printMessage(Diagnostic.Kind.ERROR, "[ClassIndexProcessor] Internal error: " + e.getMessage());
        }

        return false;
    }

    private void writeIndexFiles(String prefix, Map<String, Set<String>> indexMap) throws IOException {
        for (Map.Entry<String, Set<String>> entry : indexMap.entrySet()) {
            writeSimpleNameIndexFile(entry.getValue(), prefix + entry.getKey());
        }
    }

    private FileObject readOldIndexFile(Set<String> entries, String resourceName) throws IOException {
        Reader reader = null;
        try {
            final FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
            reader = resource.openReader(true);
            readOldIndexFile(entries, reader);
            return resource;
        } catch (FileNotFoundException e) {
            /**
             * Ugly hack for Intellij IDEA incremental compilation.
             * The problem is that it throws FileNotFoundException on the files, if they were not created during the
             * current session of compilation.
             */
            final String realPath = e.getMessage();
            if (new File(realPath).exists()) {
                try (Reader fileReader = new InputStreamReader(new FileInputStream(realPath), "UTF-8")) {
                    readOldIndexFile(entries, fileReader);
                }
            }
        } catch (IOException e) {
            // Thrown by Eclipse JDT when not found
        } catch (UnsupportedOperationException e) {
            // Java6 does not support reading old index files
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return null;
    }

    private void writeIndexFile(Set<String> entries, String resourceName, FileObject overrideFile) throws IOException {
        FileObject file = overrideFile;
        if (file == null) {
            file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        }
        try (Writer writer = file.openWriter()) {
            for (String entry : entries) {
                writer.write(entry);
                writer.write("\n");
            }
        }
    }

    private void writeSimpleNameIndexFile(Set<String> elementList, String resourceName)
            throws IOException {
        FileObject file = readOldIndexFile(elementList, resourceName);
        if (file != null) {
            /**
             * Ugly hack for Eclipse JDT incremental compilation.
             * Eclipse JDT can't createResource() after successful getResource().
             * But we can file.openWriter().
             */
            try {
                writeIndexFile(elementList, resourceName, file);
                return;
            } catch (IllegalStateException e) {
                // Thrown by HotSpot Java Compiler
            }
        }
        writeIndexFile(elementList, resourceName, null);
    }

    private void writeFile(String content, String resourceName) throws IOException {
        FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        try (Writer writer = file.openWriter()) {
            writer.write(content);
        }
    }

    /**
     * Index super types for {@link IndexSubclasses} and any {@link IndexAnnotated}
     * additionally accompanied by {@link Inherited}.
     */
    private void indexSupertypes(TypeElement rootElement, TypeElement element) throws IOException {

        for (TypeMirror mirror : types.directSupertypes(element.asType())) {
            if (mirror.getKind() != TypeKind.DECLARED) {
                continue;
            }

            DeclaredType superType = (DeclaredType) mirror;
            TypeElement superTypeElement = (TypeElement) superType.asElement();
            storeSubclass(superTypeElement, rootElement);

            for (AnnotationMirror annotationMirror : superTypeElement.getAnnotationMirrors()) {
                TypeElement annotationElement = (TypeElement) annotationMirror.getAnnotationType()
                        .asElement();

                if (hasAnnotation(annotationElement, Inherited.class)) {
                    storeAnnotation(annotationElement, rootElement);
                }
            }

            indexSupertypes(rootElement, superTypeElement);
        }
    }

    private boolean hasAnnotation(TypeElement element, Class<? extends Annotation> inheritedClass) {
        try {
            for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
                if (annotationMirror.getAnnotationType().toString().equals(inheritedClass.getName())) {
                    return true;
                }
            }
        } catch (RuntimeException e) {
            if (!e.getClass().getName().equals("com.sun.tools.javac.code.Symbol$CompletionFailure")) {
                messager.printMessage(Diagnostic.Kind.ERROR, "[ClassIndexProcessor] Can't check annotation: "
                        + e.getMessage());
            }
        }
        return false;
    }

    private void storeAnnotation(TypeElement annotationElement, TypeElement rootElement) throws IOException {
        if (indexedAnnotations.contains(annotationElement.getQualifiedName().toString())) {
            putElement(annotatedMap, annotationElement.getQualifiedName().toString(), rootElement);
        } else if (annotationDriven) {
            IndexAnnotated indexAnnotated = annotationElement.getAnnotation(IndexAnnotated.class);
            if (indexAnnotated != null) {
                putElement(annotatedMap, annotationElement.getQualifiedName().toString(), rootElement);
                if (indexAnnotated.storeJavadoc()) {
                    storeJavadoc(rootElement);
                }
            }
        }
    }

    private void storeSubclass(TypeElement superTypeElement, TypeElement rootElement) throws IOException {
        if (indexedSuperclasses.contains(superTypeElement.getQualifiedName().toString())) {
            putElement(subclassMap, superTypeElement.getQualifiedName().toString(), rootElement);
        } else if (annotationDriven) {
            IndexSubclasses indexSubclasses = superTypeElement.getAnnotation(IndexSubclasses.class);
            if (indexSubclasses != null) {
                putElement(subclassMap, superTypeElement.getQualifiedName().toString(), rootElement);

                if (indexSubclasses.storeJavadoc()) {
                    storeJavadoc(rootElement);
                }
            }
        }
        if (indexedSuperclasses.contains(superTypeElement.getQualifiedName().toString())
                || (annotationDriven && superTypeElement.getAnnotation(IndexSubclasses.class) != null)) {
            putElement(subclassMap, superTypeElement.getQualifiedName().toString(), rootElement);
        }
    }

    private void storeClassFromPackage(PackageElement packageElement, TypeElement rootElement) throws IOException {
        if (indexedPackages.contains(packageElement.getQualifiedName().toString())) {
            putElement(packageMap, packageElement.getQualifiedName().toString(), rootElement);
        } else if (annotationDriven) {
            IndexSubclasses indexSubclasses = packageElement.getAnnotation(IndexSubclasses.class);
            if (indexSubclasses != null) {
                String simpleName = getShortName(rootElement);
                if (simpleName != null) {
                    putElement(packageMap, packageElement.getQualifiedName().toString(), simpleName);
                    if (indexSubclasses.storeJavadoc()) {
                        storeJavadoc(rootElement);
                    }
                }
            }
        }
    }

    private <K> void putElement(Map<K, Set<String>> map, K keyElement, TypeElement valueElement) {
        final String fullName = getFullName(valueElement);
        if (fullName != null) {
            putElement(map, keyElement, fullName);
        }
    }

    private <K> void putElement(Map<K, Set<String>> map, K keyElement, String valueElement) {
        Set<String> set = map.get(keyElement);
        if (set == null) {
            set = new TreeSet<>();
            map.put(keyElement, set);
        }
        set.add(valueElement);
    }

    private String getFullName(TypeElement typeElement) {
        switch (typeElement.getNestingKind()) {
            case TOP_LEVEL:
                return typeElement.getQualifiedName().toString();
            case MEMBER:
                final Element enclosingElement = typeElement.getEnclosingElement();
                if (enclosingElement instanceof TypeElement) {
                    final String enclosingName = getFullName(((TypeElement) enclosingElement));
                    if (enclosingName != null) {
                        return enclosingName + '$' + typeElement.getSimpleName().toString();
                    }
                }
                return null;
            case ANONYMOUS:
            case LOCAL:
            default:
                return null;
        }
    }

    private String getShortName(TypeElement typeElement) {
        switch (typeElement.getNestingKind()) {
            case TOP_LEVEL:
                return typeElement.getSimpleName().toString();
            case MEMBER:
                final Element enclosingElement = typeElement.getEnclosingElement();
                if (enclosingElement instanceof TypeElement) {
                    final String enclosingName = getShortName(((TypeElement) enclosingElement));
                    if (enclosingName != null) {
                        return enclosingName + '$' + typeElement.getSimpleName().toString();
                    }
                }
                return null;
            case ANONYMOUS:
            case LOCAL:
            default:
                return null;
        }
    }

    private PackageElement getPackage(Element typeElement) {
        Element element = typeElement;
        while (element != null) {
            if (element instanceof PackageElement) {
                return (PackageElement) element;
            }
            element = element.getEnclosingElement();
        }
        return null;
    }

    private void storeJavadoc(TypeElement element) throws IOException {
        if (javadocAlreadyStored.contains(element)) {
            return;
        }
        javadocAlreadyStored.add(element);

        String docComment = elementUtils.getDocComment(element);
        if (docComment == null) {
            return;
        }
        writeFile(docComment, ClassIndex.JAVADOC_PREFIX + element.getQualifiedName().toString());
    }


}
