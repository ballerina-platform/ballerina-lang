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

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

/**
 * This represents the Ballerina annotation processor, which checks for specific annotations
 * in the Ballerina related classes, and generate source code and configuration files.
 * 
 * @since 0.94
 */
@SupportedAnnotationTypes ("org.ballerinalang.annotation.JavaSPIService")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BallerinaAnnotationProcessor extends AbstractProcessor {
    
    private static final String JAVA_SPI_SERVICES_BASE_PATH = "META-INF/services/";
    
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        this.processJavaSPIServices(roundEnv);
        return true;
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
    
}
