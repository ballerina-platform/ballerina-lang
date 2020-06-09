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
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaFunction;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
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
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

/**
 * This represents the Ballerina annotation processor, which checks for specific annotations
 * in the Ballerina related classes, and generate source code and configuration files.
 * 
 * @since 0.94
 */
@SupportedAnnotationTypes({ "org.ballerinalang.annotation.JavaSPIService",
        "org.ballerinalang.natives.annotations.BallerinaFunction" })
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
    
    private void populateNativeFunctions(RoundEnvironment roundEnv, List<NativeElementCodeDef> nativeDefs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BallerinaFunction.class);
        for (Element element : elements) {
            nativeDefs.add(this.functionToDef(element.getAnnotation(BallerinaFunction.class), element));
        }
    }
    
    private void populateNativeActions(RoundEnvironment roundEnv, List<NativeElementCodeDef> nativeDefs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BallerinaAction.class);
        for (Element element : elements) {
            nativeDefs.add(this.actionToDef(element.getAnnotation(BallerinaAction.class), element));
        }
    }
    
    private void processNativeEntities(RoundEnvironment roundEnv) {
        List<NativeElementCodeDef> nativeDefs = new ArrayList<>();
        this.populateNativeFunctions(roundEnv, nativeDefs);
        this.populateNativeActions(roundEnv, nativeDefs);
        if (nativeDefs.isEmpty()) {
            return;
        }
        this.generateNativeEntityProviderSource(nativeDefs);
        this.generateNativeMap(nativeDefs);
    }

    private void generateNativeMap(List<NativeElementCodeDef> nativeDefs) {
        Writer writer = null;
        try {
            Filer filer = this.processingEnv.getFiler();
            String mappingPathInJar = "META-INF/this.map.json";
            FileObject javaFile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", mappingPathInJar);
            writer = javaFile.openWriter();
            this.writeNativeMapJson(writer, nativeDefs);
        } catch (IOException e) {
            throw new RuntimeException("Error processing native functions: " + e.getMessage(), e);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ignore) {
            }
        }
    }

    private void writeNativeMapJson(Writer writer, List<NativeElementCodeDef> nativeDefs) throws IOException {
        writer.append("{");
        for (int i = 0; i < nativeDefs.size(); i++) {
            NativeElementCodeDef nativeDef = nativeDefs.get(i);
            if (i != 0) {
                writer.append(" ,");
            }
            writer.append("\n\"");
            NativeFunctionCodeDef funcDef = (NativeFunctionCodeDef) nativeDef;
            writer.append(funcDef.org).append("/").append(funcDef.pkg.replace('.', '_')).append("/")
                    .append(funcDef.version.replace('.', '_')).append("/").append(funcDef.name);
            writer.append("\" : \"");
            writer.append(funcDef.className.replace('.', '/'));
            writer.append("\"");
        }
        writer.append("\n}");
    }

    private void generateNativeEntityProviderSource(List<NativeElementCodeDef> nativeDefs) {
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
    
    private NativeElementCodeDef functionToDef(BallerinaFunction func, Element element) {
        NativeFunctionCodeDef def = new NativeFunctionCodeDef();
        def.org = func.orgName();
        def.pkg = func.packageName();
        def.version = func.version();
        if (func.receiver().type() == TypeKind.OBJECT) {
            def.name = func.receiver().structType() + "." + func.functionName();
        } else {
            def.name = func.functionName();
        }
        def.className = this.extractClassName(element);
        Arrays.stream(func.args()).forEach(e -> def.argTypes.add(e.type()));
        Arrays.stream(func.returnType()).forEach(e -> def.retTypes.add(e.type()));
        return def;
    }
    
    private NativeElementCodeDef actionToDef(BallerinaAction action, Element element) {
        NativeActionCodeDef def = new NativeActionCodeDef();
        def.org = action.orgName();
        def.pkg = action.packageName();
        def.version = action.version();
        def.connectorName = action.connectorName();
        def.name = action.actionName();
        def.className = this.extractClassName(element);
        Arrays.stream(action.args()).forEach(e -> def.argTypes.add(e.type()));
        Arrays.stream(action.returnType()).forEach(e -> def.retTypes.add(e.type()));
        return def;
    }
    
    private void processJavaSPIServices(RoundEnvironment roundEnv) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(JavaSPIService.class);
        Map<String, List<String>> entries = new HashMap<>();
        String interfaceName;
        for (Element element : elements) {
            interfaceName = element.getAnnotation(JavaSPIService.class).value();
            List<String> implClasses = entries.computeIfAbsent(interfaceName, k -> new ArrayList<>());
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
            List<NativeElementCodeDef> elements) {
        try {
            writer.write("package " + pkgName + ";\n\n");
            writer.write("import org.ballerinalang.annotation.JavaSPIService;\n");
            writer.write("import org.ballerinalang.model.types.TypeKind;\n");
            writer.write("import org.ballerinalang.natives.NativeElementRepository;\n");
            writer.write("import org.ballerinalang.natives.NativeElementRepository.NativeActionDef;\n");
            writer.write("import org.ballerinalang.natives.NativeElementRepository.NativeFunctionDef;\n");
            writer.write("import org.ballerinalang.spi.NativeElementProvider;\n\n");
            writer.write("@JavaSPIService (\"org.ballerinalang.spi.NativeElementProvider\")\n");
            writer.write("public class " + className + " implements NativeElementProvider {\n\n");
            writer.write("\t@Override\n");
            writer.write("\tpublic void populateNatives(NativeElementRepository repo) {\n");
            for (NativeElementCodeDef element : elements) {
                writer.write("\t\trepo." + element.code() + ";\n");
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
    private interface NativeElementCodeDef {
        
        String code();
        
    }
    
    /**
     * @since 0.94
     */
    private class NativeFunctionCodeDef implements NativeElementCodeDef {

        public String org;

        public String pkg;

        public String version;
        
        public String name;
        
        public String className;
        
        public List<TypeKind> argTypes = new ArrayList<>();
        
        public List<TypeKind> retTypes = new ArrayList<>();
        
        protected String typeArrayToCode(List<TypeKind> types) {
            List<String> vals = new ArrayList<>();
            for (TypeKind type : types) {
                vals.add("TypeKind." + type.name());
            }
            return "new TypeKind[] { " + String.join(", ", vals) + " }";
        }
        
        public String code() {
            return "registerNativeFunction(new NativeFunctionDef(\"" + this.org + "\", \"" + this.pkg + "\", \"" +
                    this.version + "\", \"" + this.name + "\", " + this.typeArrayToCode(this.argTypes) + ", " +
                    this.typeArrayToCode(this.retTypes) + ", \"" + this.className + "\"))";
        }
        
    }
    
    /**
     * @since 0.94
     */
    private class NativeActionCodeDef extends NativeFunctionCodeDef {
        
        public String connectorName;
        
        public String code() {
            return "registerNativeAction(new NativeActionDef(\"" + this.org + "\", \""
                    + this.pkg + "\", \"" + this.version + "\", \"" + this.connectorName + "\", \""
                    + this.name + "\", " + this.typeArrayToCode(this.argTypes) + ", "
                    + this.typeArrayToCode(this.retTypes) + ", \"" + this.className + "\"))";
        }
        
    }
    
}
