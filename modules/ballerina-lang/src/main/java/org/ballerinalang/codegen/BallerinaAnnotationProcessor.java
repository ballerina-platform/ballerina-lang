/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.codegen;

import org.ballerinalang.annotation.JavaSPIService;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * This represents the Ballerina annotation processor, which checks for specific annotations
 * in the Ballerina related classes, and generate source code and configuration files.
 * 
 * @since 0.94
 */
@SupportedAnnotationTypes({ "org.ballerinalang.annotation.JavaSPIService",
        "org.ballerinalang.annotation.natives.BallerinaFunction" })
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({ "nativeEntityProviderPackage", "nativeEntityProviderClass" })
public class BallerinaAnnotationProcessor extends AbstractProcessor {
    
    private static final String NATIVE_ENTITY_PROVIDER_PACKAGE_NAME = "nativeEntityProviderPackage";
    
    private static final String NATIVE_ENTITY_PROVIDER_CLASS_NAME = "nativeEntityProviderClass";

    private static final String JAVA_SPI_SERVICES_BASE_PATH = "META-INF/services/";
    
    private ProcessingEnvironment processingEnv;
    
    @Override
    public void init(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.processJavaSPIServices(roundEnv);
        this.processNativeEntities(roundEnv);
        return true;
    }
    
    private void populateNativeFunctions(RoundEnvironment roundEnv, List<NativeElementDef> nativeDefs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BallerinaFunction.class);
        for (Element element : elements) {
            nativeDefs.add(this.functionToDef(element.getAnnotation(BallerinaFunction.class), element));
        }
    }
    
    private void processNativeEntities(RoundEnvironment roundEnv) {
        List<NativeElementDef> nativeDefs = new ArrayList<>();
        this.populateNativeFunctions(roundEnv, nativeDefs);
        if (nativeDefs.isEmpty()) {
            return;
        }
        this.generateNativeEntityProviderSource(nativeDefs);
    }
    
    private void generateNativeEntityProviderSource(List<NativeElementDef> nativeDefs) {
        Map<String, String> options = this.processingEnv.getOptions();
        String targetPackageName = options.get(NATIVE_ENTITY_PROVIDER_PACKAGE_NAME);
        String targetClassName = options.get(NATIVE_ENTITY_PROVIDER_CLASS_NAME);
        Writer writer = null;
        try {
            JavaFileObject javaFile = this.processingEnv.getFiler().createSourceFile(
                    targetPackageName + "." + targetClassName);
            writer = javaFile.openWriter();
            this.generateNativeElementProviderCode(writer, targetPackageName, targetClassName, nativeDefs);
        } catch (IOException e) {
            throw new RuntimeException("Error processing native functions: " + e.getMessage(), e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ignore) { }
        }
    }
    
    private NativeElementDef functionToDef(BallerinaFunction func, Element element) {
        NativeFunctionDef def = new NativeFunctionDef();
        def.pkg = func.packageName();
        def.name = func.functionName();
        def.className = this.extractClassName(element);
        return def;
    }
    
    private void processJavaSPIServices(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(JavaSPIService.class);
        Map<String, List<String>> entries = new HashMap<>();
        String interfaceName;
        for (Element element : elements) {
            interfaceName = element.getAnnotation(JavaSPIService.class).value();
            List<String> implClasses = entries.get(interfaceName);
            if (implClasses == null) {
                implClasses = new ArrayList<>();
                entries.put(interfaceName, implClasses);
            }
            implClasses.add(this.extractClassName(element));
        }
        if (!entries.isEmpty()) {
            entries.forEach(this::createServiceFile);
        }
    }
    
    private void createServiceFile(String interfaceName, List<String> implClasses) {
        Filer filer = this.processingEnv.getFiler();
        Writer writer = null;
        try {
            writer = filer.createResource(StandardLocation.CLASS_OUTPUT, "", JAVA_SPI_SERVICES_BASE_PATH + 
                    interfaceName).openWriter();
            writer.write(String.join("\n", implClasses));
        } catch (IOException e) {
            throw new RuntimeException("Error creating Java SPI services file: " + e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ignore) { }
            }
        }
    }
    
    private String extractClassName(Element element) {
        return ((TypeElement) element).getQualifiedName().toString();
    }
    
    private void generateNativeElementProviderCode(Writer writer, String pkgName, String className, 
            List<NativeElementDef> elements) {
        try {
            writer.write("package " + pkgName + ";\n\n");
            writer.write("import org.ballerinalang.annotation.JavaSPIService;\n");
            writer.write("import org.ballerinalang.natives.NativeElementKey;\n");
            writer.write("import org.ballerinalang.natives.NativeElementRepository;\n");
            writer.write("import org.ballerinalang.natives.NativeElementType;\n");
            writer.write("import org.ballerinalang.spi.NativeElementProvider;\n\n");
            writer.write("@JavaSPIService (\"org.ballerinalang.spi.NativeElementProvider\")\n");
            writer.write("public class " + className + " implements NativeElementProvider {\n\n");
            writer.write("\t@Override\n");
            writer.write("\tpublic void populateNatives(NativeElementRepository repo) {\n");
            for (NativeElementDef element : elements) {
                writer.write("\t\trepo.addEntry(new NativeElementKey(" + element.code() + ");\n");
            }
            writer.write("\t}\n\n");
            writer.write("}\n");
        } catch (IOException e) {
            throw new RuntimeException("Error in generating native element definitions: " + e.getMessage(), e);
        }
    }
    
    /**
     * @since 0.94
     */
    private interface NativeElementDef {
        
        String code();
        
    }
    
    /**
     * @since 0.94
     */
    private class NativeFunctionDef implements NativeElementDef {
        
        public String pkg;
        
        public String name;
        
        public String className;
        
        public String code() {
            return "NativeElementType.FUNCTION, \"" + this.pkg + 
                    "\", \"" + this.name + "\"), \"" + this.className + "\"";
        }
        
    }
    
}
