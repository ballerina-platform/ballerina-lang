/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import java.lang.annotation.Annotation;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Annotation processor for validating the siddhi annotations added for documenting the processors at compile time.
 */
public class ValidationAnnotationProcessor extends AbstractProcessor {
    private static final Pattern corePackagePattern = Pattern.compile("^org.wso2.siddhi.core.");
    private static final Pattern parameterNamePattern = Pattern.compile("^[a-z0-9]+(\\.[a-z0-9]+)*$");

    private static final String SINK_MAPPER_SUPER_CLASS = "org.wso2.siddhi.core.stream.output.sink.SinkMapper";
    private static final String SINK_SUPER_CLASS =
            "org.wso2.siddhi.core.stream.output.sink.Sink";
    private static final String FUNCTION_EXECUTOR_SUPER_CLASS =
            "org.wso2.siddhi.core.executor.function.FunctionExecutor";
    private static final String AGGREGATION_ATTRIBUTE_SUPER_CLASS =
            "org.wso2.siddhi.core.query.selector.attribute.aggregator.AttributeAggregator";
    private static final String DISTRIBUTION_STRATEGY_SUPER_CLASS =
            "org.wso2.siddhi.core.stream.output.sink.distributed.DistributionStrategy";
    private static final String STREAM_PROCESSOR_SUPER_CLASS =
            "org.wso2.siddhi.core.query.processor.stream.StreamProcessor";
    private static final String STREAM_FUNCTION_PROCESSOR_SUPER_CLASS =
            "org.wso2.siddhi.core.query.processor.stream.function.StreamFunctionProcessor";
    private static final String STORE_SUPER_CLASS = "org.wso2.siddhi.core.table.Table";
    private static final String SOURCE_SUPER_CLASS = "org.wso2.siddhi.core.stream.input.source.Source";
    private static final String SOURCE_MAPPER_SUPER_CLASS = "org.wso2.siddhi.core.stream.input.source.SourceMapper";

    private List<Class<? extends Annotation>> annotationsClasses;
    private final Map<String, String> namespaceReservedSuperClassMap = new HashMap<>();
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment env) {
        super.init(env);
        messager = env.getMessager();

        // Populating the reserved namespace for super classes.
        namespaceReservedSuperClassMap.put(DISTRIBUTION_STRATEGY_SUPER_CLASS, "distributionStrategy");
        namespaceReservedSuperClassMap.put(STORE_SUPER_CLASS, "store");
        namespaceReservedSuperClassMap.put(SOURCE_SUPER_CLASS, "source");
        namespaceReservedSuperClassMap.put(SOURCE_MAPPER_SUPER_CLASS, "sourceMapper");
        namespaceReservedSuperClassMap.put(SINK_SUPER_CLASS, "sink");
        namespaceReservedSuperClassMap.put(SINK_MAPPER_SUPER_CLASS, "sinkMapper");

        // Populating the supported annotations class.
        annotationsClasses = new ArrayList<>();
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
                Extension annotation = element.getAnnotation(Extension.class);
                String name = annotation.name();
                String description = annotation.description();
                String namespace = annotation.namespace();
                String extensionClassFullName = element.asType().toString();
                Parameter[] parameters = annotation.parameters();
                ReturnAttribute[] returnAttributes = annotation.returnAttributes();
                SystemParameter[] systemParameters = annotation.systemParameter();
                Example[] examples = annotation.examples();
                String namespaceReservedSuperClass = validateSuperClassInheritance(element,
                        new String[]{DISTRIBUTION_STRATEGY_SUPER_CLASS, STORE_SUPER_CLASS, SOURCE_SUPER_CLASS,
                                SOURCE_MAPPER_SUPER_CLASS, SINK_SUPER_CLASS, SINK_MAPPER_SUPER_CLASS});
                //Check if the @Extension name is empty.
                if (name.isEmpty()) {
                    showBuildError(element, MessageFormat.format("The name annotated in class {0} is null or empty.",
                            extensionClassFullName));
                }
                //Check if the @Extension description is empty.
                if (description.isEmpty()) {
                    showBuildError(element, MessageFormat.format("The description annotated in class {0} is " +
                            "null or empty.", extensionClassFullName));
                }
                //Check if the @Extension namespace is empty.
                if (namespace.isEmpty()) {
                    //The namespace cannot be null or empty if @Extension is not in core package or
                    //namespace reserved super class.
                    //Extract core package name by using corePackagePattern pattern.
                    if (!corePackagePattern.matcher(extensionClassFullName).find() ||
                            !namespaceReservedSuperClass.isEmpty()) {
                        showBuildError(element, MessageFormat.format("The namespace annotated in class {0} " +
                                "is null or empty.", extensionClassFullName));
                    }
                } else {
                    //Check if super class is namespace reserved super class.
                    if (!namespaceReservedSuperClass.isEmpty()) {
                        String reservedNamespace = namespaceReservedSuperClassMap.get(namespaceReservedSuperClass);
                        //Check if namespace provided matches with the reserved namespace.
                        if (!namespace.contains(reservedNamespace)) {
                            showBuildError(element, MessageFormat.format("The namespace provided {0} should be " +
                                            "corrected as {1} annotated in class {2}.", namespace, reservedNamespace,
                                    extensionClassFullName));
                        }
                    }
                }
                if (parameters.length > 0) {
                    //Check if the parameters defined in distributed strategy inherited class, as it
                    // cannot have any parameters.
                    if (!validateSuperClassInheritance(element,
                            new String[]{DISTRIBUTION_STRATEGY_SUPER_CLASS}).isEmpty()) {
                        showBuildError(element, MessageFormat.format("The Parameter cannot be annotated " +
                                "in class {0}.", extensionClassFullName));
                    } else {
                        for (Parameter parameter : parameters) {
                            String parameterName = parameter.name();
                            //Check if the @Parameter name is empty.
                            if (parameterName.isEmpty()) {
                                showBuildError(element, MessageFormat.format("The Parameter name annotated in " +
                                        "class {0} is null or empty.", extensionClassFullName));
                            } else if (!parameterNamePattern.matcher(parameterName).find()) {
                                //Check if the @Parameter name is in a correct format 'abc.def.ghi' using regex pattern.
                                showBuildError(element, MessageFormat.format("The Parameter name {0} annotated " +
                                                "in class {1} is not in proper format ''abc.def.ghi''.",
                                        parameterName, extensionClassFullName));
                            }
                            //Check if the @Parameter description is empty.
                            if (parameter.description().isEmpty()) {
                                showBuildError(element, MessageFormat.format("The Parameter {0} description " +
                                                "annotated in class {1} is null or empty.", parameterName,
                                        extensionClassFullName));
                            }
                            //Check if the @Parameter type is empty.
                            if (parameter.type().length == 0) {
                                showBuildError(element, MessageFormat.format("The Parameter {0} type annotated in " +
                                        "class {1} is null or empty.", parameterName, extensionClassFullName));
                            }
                            //Check if the @Parameter dynamic property false or empty in the classes extending
                            //super classes except the Sink & SinkMapper.
                            if (validateSuperClassInheritance(
                                    element, new String[]{
                                            SINK_MAPPER_SUPER_CLASS, SINK_SUPER_CLASS
                                    }).isEmpty()) {
                                if (parameter.dynamic()) {
                                    showBuildError(element, MessageFormat.format("The Parameter {0} dynamic property " +
                                                    "cannot be true annotated in class {1}.", parameterName,
                                            extensionClassFullName));
                                }
                            }
                        }
                    }
                }
                //Check if the classes extending FunctionExecutor or AttributeAggregator
                if (!validateSuperClassInheritance(
                        element, new String[]{
                                FUNCTION_EXECUTOR_SUPER_CLASS, AGGREGATION_ATTRIBUTE_SUPER_CLASS
                        }).isEmpty()) {
                    if (returnAttributes.length == 0) {
                        //Throw error if the @ReturnAttributes empty.
                        showBuildError(element, MessageFormat.format("The ReturnAttribute annotated in class {0} " +
                                "is null or empty.", extensionClassFullName));
                    } else if (returnAttributes.length == 1) {
                        String returnAttributeName = returnAttributes[0].name();
                        //Check if the @ReturnAttributes name is empty.
                        if (!returnAttributeName.isEmpty()) {
                            showBuildError(element, MessageFormat.format("The ReturnAttribute name" +
                                    "cannot be annotated in class {1}.", extensionClassFullName));
                        }
                        //Check if the @ReturnAttributes description is empty.
                        if (returnAttributes[0].description().isEmpty()) {
                            showBuildError(element, MessageFormat.format("The ReturnAttribute {0} " +
                                            "description annotated in class {1} is null or empty.",
                                    returnAttributeName, extensionClassFullName));
                        }
                        //Check if the @ReturnAttributes type is empty.
                        if (returnAttributes[0].type().length == 0) {
                            showBuildError(element, MessageFormat.format("The ReturnAttribute {0} " +
                                            "type annotated in class {1} is null or empty.",
                                    returnAttributeName, extensionClassFullName));
                        }
                    } else {
                        //Throw error if the @ReturnAttributes count is more than one.
                        showBuildError(element, MessageFormat.format("Only one ReturnAttribute can be " +
                                "annotated in class {0}.", extensionClassFullName));
                    }
                } else {
                    //Check if the @ReturnAttributes count is more than one.
                    if (returnAttributes.length > 0) {
                        //Check if the @ReturnAttributes in the classes extending
                        //StreamProcessor or StreamFunctionProcessor.
                        if (!validateSuperClassInheritance(
                                element, new String[]{
                                        STREAM_PROCESSOR_SUPER_CLASS, STREAM_FUNCTION_PROCESSOR_SUPER_CLASS
                                }).isEmpty()) {
                            for (ReturnAttribute returnAttribute : returnAttributes) {
                                String returnAttributeName = returnAttribute.name();
                                //Check if the @ReturnAttributes name is empty.
                                if (returnAttributeName.isEmpty()) {
                                    showBuildError(element, MessageFormat.format("The ReturnAttribute name " +
                                            "annotated in class {0} is null or empty.", extensionClassFullName));
                                }
                                //Check if the @ReturnAttributes description is empty.
                                if (returnAttribute.description().isEmpty()) {
                                    showBuildError(element, MessageFormat.format("The ReturnAttribute {0} " +
                                                    "description annotated in class {1} is null or empty.",
                                            returnAttributeName, extensionClassFullName));
                                }
                                //Check if the @ReturnAttributes type is empty.
                                if (returnAttribute.type().length == 0) {
                                    showBuildError(element, MessageFormat.format("The ReturnAttribute {0} " +
                                                    "type annotated in class {1} is null or empty.",
                                            returnAttributeName, extensionClassFullName));
                                }
                            }
                        } else {
                            //Throw error for other classes as only in the classes extending
                            //StreamProcessor or StreamFunctionProcessor allowed to have more than one ReturnAttribute.
                            showBuildError(element, MessageFormat.format("The ReturnAttribute cannot be annotated " +
                                    "in class {0}.", extensionClassFullName));
                        }
                    }
                }
                // Iterate over all @SystemParameter annotated elements.
                for (SystemParameter systemParameter : systemParameters) {
                    String systemParameterName = systemParameter.name();
                    //Check if the @SystemParameter name is empty.
                    if (systemParameterName.isEmpty()) {
                        showBuildError(element, MessageFormat.format("The SystemParameter name annotated in " +
                                "class {0} is null or empty.", extensionClassFullName));
                    }
                    //Check if the @SystemParameter description is empty.
                    if (systemParameter.description().isEmpty()) {
                        showBuildError(element, MessageFormat.format("The SystemParameter {0} description annotated " +
                                "in class {1} is null or empty.", systemParameterName, extensionClassFullName));
                    }
                    //Check if the @SystemParameter defaultValue is empty.
                    if (systemParameter.defaultValue().isEmpty()) {
                        showBuildError(element, MessageFormat.format("The SystemParameter {0} defaultValue annotated " +
                                "in class {1} is null or empty.", systemParameterName, extensionClassFullName));
                    }
                    //Check if the @SystemParameter possibleParameters is empty.
                    if (systemParameter.possibleParameters().length == 0) {
                        showBuildError(element, MessageFormat.format("The SystemParameter {0} defaultValue annotated " +
                                "in class {1} is null or empty.", systemParameterName, extensionClassFullName));
                    }
                }
                //Check if the @Example annotated in all the @Extension classes.
                if (examples.length == 0) {
                    showBuildError(element, MessageFormat.format("The Example annotated in class {0} " +
                            "is null or empty.", extensionClassFullName));
                } else {
                    for (Example example : examples) {
                        //Check if the @Example value is empty.
                        if (example.value().isEmpty()) {
                            showBuildError(element, MessageFormat.format("The Example value annotated in " +
                                    "class {0} is null or empty.", extensionClassFullName));
                        }
                    }
                }
            } else {
                //Throw error if the element returned is method or package.
                showBuildError(
                        element, MessageFormat.format("Only classes can be annotated with @{0}.",
                                Extension.class.getCanonicalName()));
            }
        }
        return false;   // Returning false since this processor only validates
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

    /**
     * Show build showBuildError
     *
     * @param element Element for which the showBuildError should be shown
     * @param message The showBuildError message to be shown.
     */
    private void showBuildError(Element element, String message) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    /**
     * Validate if the element specified is inherited from the super class specified.
     *
     * @param elementToValidate The element to validate if extended from one of the superclasses.
     * @param superClassNames   The super classes one of which the element should inherit.
     * @return matching superClass one of the element inherited.
     */
    private String validateSuperClassInheritance(Element elementToValidate, String[] superClassNames) {
        TypeMirror superType = ((TypeElement) elementToValidate).getSuperclass();
        String superClass = "";
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
}
