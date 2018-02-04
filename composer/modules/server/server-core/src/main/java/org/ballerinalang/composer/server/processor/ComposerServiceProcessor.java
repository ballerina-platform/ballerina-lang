/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.composer.server.processor;

import org.ballerinalang.composer.server.spi.ComposerServiceProvider;
import org.ballerinalang.composer.server.spi.annotation.ComposerSPIServiceProvider;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;

/**
 * Process all services and generate necessary configurations.
 */
@SupportedAnnotationTypes({ "org.ballerinalang.composer.server.spi.annotation.ComposerSPIServiceProvider" })
public class ComposerServiceProcessor extends AbstractProcessor {
    private static final String JAVA_SPI_SERVICES_BASE_PATH = "META-INF/services/";
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        Set<? extends Element> spiElements = roundEnv.getElementsAnnotatedWith(ComposerSPIServiceProvider.class);
        List<String> serviceImplClasses = new ArrayList<>();
        for (Element element: spiElements) {
            if (element.getKind() == ElementKind.CLASS) {
                TypeElement serviceClass = (TypeElement) element;
                serviceImplClasses.add(serviceClass.getQualifiedName().toString());
            }
        }
        if (!serviceImplClasses.isEmpty()) {
            createServiceConfig(ComposerServiceProvider.class.getCanonicalName(), serviceImplClasses);
        }
        return true;
    }

    private void createServiceConfig(String interfaceName, List<String> implClasses) {
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
}
