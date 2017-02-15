/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.annotation.processor;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAnnotation;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeMapper;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Read all class annotations of native functions, connectors, actions, type mappers.
 * Process them and generate a service provider class that will register all the annotated 
 * classes as {@link Symbol}s to the global symbol table, via java SPI.
 */
@SupportedAnnotationTypes({ "org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction",
                            "org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector",
                            "org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAction",
                            "org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeMapper"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedOptions({ "className", "packageName", "targetDir" })
public class BallerinaAnnotationProcessor extends AbstractProcessor {

    private static final String CLASS_NAME = "className";
    private static final String PACKAGE_NAME = "packageName";
    private static final String TARGET_DIR = "targetDir";
    private static final String IS_PROCESSED = "balAnnotationProcessed";
    private static final String TRUE = "true";
    
    Map<String, Connector> connectors = new HashMap<String, Connector>();
    
    public BallerinaAnnotationProcessor() throws IOException {
        super();
    }
    
    /**
     * Process the ballerina annotations and generate the construct provider class.
     * <br/>
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        
        // if the annotations in this environment are already processed, then skip.
        if (System.getProperty(IS_PROCESSED) != null && System.getProperty(IS_PROCESSED).equals(TRUE)) {
            return true;
        }
        
        Set<Element> balFunctionElements = (Set<Element>) roundEnv.getElementsAnnotatedWith(BallerinaFunction.class);
        Set<Element> balConnectorElements = (Set<Element>) roundEnv.getElementsAnnotatedWith(BallerinaConnector.class);
        Set<Element> balActionElements = (Set<Element>) roundEnv.getElementsAnnotatedWith(BallerinaAction.class);
        Set<Element> balTypeMapperElements =
                (Set<Element>) roundEnv.getElementsAnnotatedWith(BallerinaTypeMapper.class);
        
        // If all annotations are empty, should not do anything. Continue to the next plugin phases.
        if (balFunctionElements.isEmpty() && balConnectorElements.isEmpty() && balActionElements.isEmpty()
                    && balTypeMapperElements.isEmpty()) {
            return true;
        }
        
        Map<String, String> options = processingEnv.getOptions();
        Filer filer = processingEnv.getFiler();
        
        String classClassName = options.get(CLASS_NAME);
        if (classClassName == null) {
            throw new BallerinaException("class name for the generated construct-provider must be specified.");
        }
        
        String packageName = options.get(PACKAGE_NAME);
        if (packageName == null) {
            throw new BallerinaException("package name for the generated construct-provider must be specified.");
        }
        
        String targetDir = options.get(TARGET_DIR);
        if (targetDir == null) {
            throw new BallerinaException("target directory to store the generated ballerina files, must be specified.");
        }

        ConstructProviderClassBuilder classBuilder = new ConstructProviderClassBuilder(filer, packageName, 
                classClassName);
        NativeBallerinaFileBuilder nativeBallerinaFileBuilder = new NativeBallerinaFileBuilder(targetDir);
        
        // Process all native function, connectors, actions and type mappers
        processNativeFunctions(balFunctionElements, classBuilder, nativeBallerinaFileBuilder);
        processNativeConnectors(balConnectorElements, classBuilder, nativeBallerinaFileBuilder);
        processNativeActions(balActionElements, classBuilder, nativeBallerinaFileBuilder);
        processNativeTypeMappers(balTypeMapperElements, classBuilder, nativeBallerinaFileBuilder);
        
        classBuilder.addConnectors(connectors);
        classBuilder.build();
        nativeBallerinaFileBuilder.addConnectors(connectors.values());
        nativeBallerinaFileBuilder.build();
        System.setProperty(IS_PROCESSED, TRUE);
        
        return true;
    }

    /**
     * Process all {@link BallerinaFunction} annotations and append constructs to the class builder.
     * 
     * @param balFunctionElements   Elements annotated with {@link BallerinaFunction}
     * @param classBuilder          Builder to generate the source class
     * @param nativeBallerinaFileBuilder Builder to generate the native ballerina files
     */
    private void processNativeFunctions(Set<Element> balFunctionElements, ConstructProviderClassBuilder classBuilder,
            NativeBallerinaFileBuilder nativeBallerinaFileBuilder) {
        for (Element element : balFunctionElements) {
            BallerinaFunction balFunction = element.getAnnotation(BallerinaFunction.class);
            BallerinaAnnotation[] annot = getBallerinaAnnotations(element);
            String functionQualifiedName = getFunctionQualifiedName(balFunction);
            String packageName = balFunction.packageName();
            String className = getClassName(element);
            classBuilder.addNativeConstruct(packageName, balFunction.functionName(), functionQualifiedName, className,
                balFunction.args(), balFunction.returnType(), balFunction.args().length);
            nativeBallerinaFileBuilder.addNativeConstruct(packageName, balFunction, annot);
        }
    }

    private BallerinaAnnotation[] getBallerinaAnnotations(Element element) {
        BallerinaAnnotation[] annot = new BallerinaAnnotation[0];
        if (element.getAnnotationsByType(BallerinaAnnotation.class).length > 0) {
            annot = element.getAnnotationsByType(BallerinaAnnotation.class);
        }
        return annot;
    }
    
    /**
     * Process all {@link BallerinaConnector} annotations and append constructs to the class builder.
     * 
     * @param balConnectorElements  Elements annotated with {@link BallerinaConnector}
     * @param classBuilder          Builder to generate the source class
     * @param nativeBallerinaFileBuilder Builder to generate the native ballerina files
     */
    private void processNativeConnectors(Set<Element> balConnectorElements, 
            ConstructProviderClassBuilder classBuilder, NativeBallerinaFileBuilder nativeBallerinaFileBuilder) {
        for (Element element : balConnectorElements) {
            BallerinaConnector balConnector = element.getAnnotation(BallerinaConnector.class);
            String className = getClassName(element);
            Connector connector = new Connector(balConnector, className);
            connector.setAnnotations(getBallerinaAnnotations(element));
            connectors.put(balConnector.connectorName(), connector);
        }
    }
    
    /**
     * Process all {@link BallerinaAction} annotations and append constructs to the class builder.
     * 
     * @param balActionElements     Elements annotated with {@link BallerinaAction}
     * @param classBuilder          Builder to generate the source class
     * @param nativeBallerinaFileBuilder Builder to generate the native ballerina files
     */
    private void processNativeActions(Set<Element> balActionElements, ConstructProviderClassBuilder classBuilder,
            NativeBallerinaFileBuilder nativeBallerinaFileBuilder) {
        for (Element element : balActionElements) {
            BallerinaAction balAction = element.getAnnotation(BallerinaAction.class);
            String className = getClassName(element);
            Action action = new Action(balAction, className);
            action.setAnnotations(getBallerinaAnnotations(element));
            connectors.get(balAction.connectorName()).addAction(action);
        }
    }
    
    /**
     * Process all {@link BallerinaTypeMapper} annotations and append constructs to the class builder.
     * 
     * @param balTypeMapperElements  Elements annotated with {@link BallerinaTypeMapper}
     * @param classBuilder              Builder to generate the source class
     * @param nativeBallerinaFileBuilder Builder to generate the native ballerina files
     */
    private void processNativeTypeMappers(Set<Element> balTypeMapperElements,
            ConstructProviderClassBuilder classBuilder, NativeBallerinaFileBuilder nativeBallerinaFileBuilder) {
        for (Element element : balTypeMapperElements) {
            BallerinaTypeMapper balTypeMapper = element.getAnnotation(BallerinaTypeMapper.class);
            BallerinaAnnotation[] annot = getBallerinaAnnotations(element);
            String typeMapperQualifiedName = getTypeMapperQualifiedName(balTypeMapper);
            String packageName = balTypeMapper.packageName();
            String className = getClassName(element);
            classBuilder.addNativeConstruct(packageName, balTypeMapper.typeMapperName(),
                typeMapperQualifiedName, className, balTypeMapper.args(), balTypeMapper.returnType(),
                balTypeMapper.args().length);
            nativeBallerinaFileBuilder.addNativeConstruct(packageName, balTypeMapper, annot);
        }
    }
    
    /**
     * Get the fully qualified class name of a given element.
     * 
     * @param element   Element to get the class name
     * @return          Fully qualified class name of a given element
     */
    private String getClassName(Element element) {
        return ((TypeElement) element).getQualifiedName().toString();
    }
    
    /**
     * Get the fully qualified name of the ballerina function.
     * 
     * @param balFunction   Ballerina function annotation
     * @return              Fully qualified name
     */
    private String getFunctionQualifiedName(BallerinaFunction balFunction) {
        StringBuilder funcNameBuilder = new StringBuilder(balFunction.functionName());
        Argument[] args = balFunction.args();
        for (Argument arg : args) {
            // if the argument is arrayType, then append the element type to the method signature 
            if (arg.type() == TypeEnum.ARRAY && arg.elementType() != TypeEnum.EMPTY) {
                funcNameBuilder.append("." + arg.elementType().getName() + "[]");
            } else {
                funcNameBuilder.append("." + arg.type().getName());
            }
        }
        return funcNameBuilder.toString();
    }
    
    /**
     * Get the fully qualified name of the ballerina type mapper.
     * 
     * @param balTypeMapper  Ballerina type mapper annotation.
     * @return                  Fully qualified name
     */
    private String getTypeMapperQualifiedName(BallerinaTypeMapper balTypeMapper) {
        StringBuilder mapperNameBuilder = new StringBuilder();
        Argument[] args = balTypeMapper.args();
        ReturnType[] returnTypes = balTypeMapper.returnType();
        
        for (Argument arg : args) {
            mapperNameBuilder.append(".").append(arg.type().getName());
        }
        
        mapperNameBuilder.append("->");
        
        for (ReturnType returnType : returnTypes) {
            mapperNameBuilder.append(".").append(returnType.type().getName());
        }
        return mapperNameBuilder.toString();
    }
}
