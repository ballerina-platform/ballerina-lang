/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.siddhi.annotation.processor;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.AnnotationConstants;
import org.wso2.siddhi.annotation.util.AnnotationValidationException;

import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * The annotation processor for siddhi extension annotation validation. This will validate @Extension, @Parameter,
 * ReturnAttribute, @SystemParameter and @Example annotation and parameters. Note that each of the validation
 * processor select using the super class where the extension extends or implement.
 * <p>
 * annotationsClasses : holds all the supported annotations class.
 * messager : the messager used to report errors, warnings, and other notices when validation error throws..
 */
public class SiddhiAnnotationProcessor extends AbstractProcessor {
    private final List<Class<? extends Annotation>> annotationsClasses = new ArrayList<>();
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messager = env.getMessager();

        // Populating the supported annotations class.
        annotationsClasses.add(Extension.class);
        annotationsClasses.add(Parameter.class);
        annotationsClasses.add(ReturnAttribute.class);
        annotationsClasses.add(SystemParameter.class);
        annotationsClasses.add(Example.class);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // Iterate over all @Extension annotated elements.
        for (Element element : roundEnv.getElementsAnnotatedWith(Extension.class)) {
            // Check if a class has been annotated with @Extension.
            if (element.getKind() == ElementKind.CLASS) {
                String superClass = getMatchingSuperClass(element, new String[]{
                        AnnotationConstants.SINK_MAPPER_SUPER_CLASS,
                        AnnotationConstants.SINK_SUPER_CLASS,
                        AnnotationConstants.FUNCTION_EXECUTOR_SUPER_CLASS,
                        AnnotationConstants.AGGREGATION_ATTRIBUTE_SUPER_CLASS,
                        AnnotationConstants.DISTRIBUTION_STRATEGY_SUPER_CLASS,
                        AnnotationConstants.STREAM_PROCESSOR_SUPER_CLASS,
                        AnnotationConstants.STREAM_FUNCTION_PROCESSOR_SUPER_CLASS,
                        AnnotationConstants.STORE_SUPER_CLASS,
                        AnnotationConstants.SOURCE_SUPER_CLASS,
                        AnnotationConstants.SOURCE_MAPPER_SUPER_CLASS,
                        AnnotationConstants.WINDOW_PROCESSOR_CLASS,
                        AnnotationConstants.SCRIPT_SUPER_CLASS,
                        AnnotationConstants.INCREMENTAL_ATTRIBUTE_AGGREGATOR_SUPER_CLASS});
                AbstractAnnotationProcessor abstractAnnotationProcessor = null;
                Extension annotation = element.getAnnotation(Extension.class);
                String name = annotation.name();
                String description = annotation.description();
                String namespace = annotation.namespace();
                Parameter[] parameters = annotation.parameters();
                ReturnAttribute[] returnAttributes = annotation.returnAttributes();
                SystemParameter[] systemParameters = annotation.systemParameter();
                Example[] examples = annotation.examples();
                String extensionClassFullName = element.asType().toString();
                if (superClass != null) {
                    switch (superClass) {
                        case AnnotationConstants.DISTRIBUTION_STRATEGY_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new DistributionStrategyValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.SINK_MAPPER_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new SinkMapperValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.SINK_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new SinkValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.FUNCTION_EXECUTOR_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new FunctionExecutorValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.AGGREGATION_ATTRIBUTE_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new AggregationAttributeValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.STREAM_PROCESSOR_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new StreamProcessorValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.SOURCE_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new SourceValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.SOURCE_MAPPER_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new SourceMapperValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.STORE_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new StoreValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.STREAM_FUNCTION_PROCESSOR_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new StreamFunctionProcessorValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.WINDOW_PROCESSOR_CLASS:
                            abstractAnnotationProcessor =
                                    new WindowProcessorValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.SCRIPT_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new ScriptValidationAnnotationProcessor(extensionClassFullName);
                            break;
                        case AnnotationConstants.INCREMENTAL_ATTRIBUTE_AGGREGATOR_SUPER_CLASS:
                            abstractAnnotationProcessor =
                                    new IncrementalAggregationAttributeValidationAnnotationProcessor(
                                            extensionClassFullName);
                            break;
                        default:
                            //Throw error if no matching super class.
                            showBuildError(MessageFormat.format("Default switch case executed as there is no " +
                                            "matching super class option for @{0}.",
                                    superClass), element);
                            break;
                    }
                    if (abstractAnnotationProcessor != null) {
                        try {
                            abstractAnnotationProcessor.basicParameterValidation(name, description, namespace);
                            abstractAnnotationProcessor.parameterValidation(parameters);
                            abstractAnnotationProcessor.returnAttributesValidation(returnAttributes);
                            abstractAnnotationProcessor.systemParametersValidation(systemParameters);
                            abstractAnnotationProcessor.examplesValidation(examples);
                        } catch (AnnotationValidationException e) {
                            showBuildError(e.getMessage(), element);
                        }
                    } else {
                        showBuildError(MessageFormat.format("Error while validation, " +
                                "abstractAnnotationProcessor cannot be null.", superClass), element);
                    }
                } else {
                    //Throw error if no matching super class.
                    showBuildError("Class does not have a matching Siddhi Extension super class.", element);
                }
            } else {
                //Throw error if the element returned is method or package.
                showBuildError(MessageFormat.format("Only classes can be annotated with @{0}.",
                        Extension.class.getCanonicalName()), element);
            }
        }
        return false; // Returning false since this processor only validates.
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> annotationTypes = new HashSet<>();
        for (Class<? extends Annotation> annotationClass : annotationsClasses) {
            annotationTypes.add(annotationClass.getCanonicalName());
        }
        return annotationTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }

    private String getMatchingSuperClass(Element elementToValidate, String[] superClassNames) {
        TypeMirror superType = ((TypeElement) elementToValidate).getSuperclass();
        String superClass = null;
        // Looping the inheritance hierarchy to check if the element inherits at least one of the super
        // classes specified.
        while (!"none".equals(superType.toString())) {
            Element superTypeElement = ((DeclaredType) superType).asElement();

            if (Arrays.asList(superClassNames).contains(superTypeElement.toString())) {
                superClass = superTypeElement.toString();
                break;
            }

            superType = ((TypeElement) superTypeElement).getSuperclass();
        }
        return superClass;
    }

    public void showBuildError(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }
}
