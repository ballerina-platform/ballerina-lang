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
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.SystemParameter;
import org.wso2.siddhi.annotation.util.AnnotationValidationException;

import java.text.MessageFormat;
import java.util.regex.Pattern;

/**
 * Parent class for extension annotation validation processors.
 * CORE_PACKAGE_PATTERN: regex pattern for check the core package name while validation.
 * PARAMETER_NAME_PATTERN: regex pattern for check the @Extension / @Parameters / name format validation.
 * extensionClassFullName: holds the extension class full name.
 * CAMEL_CASE_PATTERN: regex pattern for check the camelCase naming convention.
 */
public class AbstractAnnotationProcessor {
    protected static final Pattern CORE_PACKAGE_PATTERN = Pattern.compile("^org.wso2.siddhi.core.");
    protected static final Pattern PARAMETER_NAME_PATTERN = Pattern.compile("^[a-z][a-z0-9]*(\\.[a-z][a-z0-9]*)*$");
    protected static final Pattern CAMEL_CASE_PATTERN = Pattern.compile("^[a-z]+([A-Z][a-z0-9]+)*$");
    protected String extensionClassFullName;

    public AbstractAnnotationProcessor(String extensionClassFullName) {
        this.extensionClassFullName = extensionClassFullName;
    }

    /**
     * Basic @Extension annotation elements validation.
     *
     * @param name        name of the @Extension which needs to be validate.
     * @param description description of the @Extension  which needs to be validate.
     * @param namespace   namespace of the @Extension  which needs to be validate.
     * @throws AnnotationValidationException whenever if the validate rule violate, throws the annotation validate
     *                                       exception with proper message.
     */
    public void basicParameterValidation(String name, String description, String namespace)
            throws AnnotationValidationException {
        //Check if the @Extension name is empty.
        if (name.isEmpty()) {
            throw new AnnotationValidationException(MessageFormat.format("The @Extension -> name " +
                    " annotated in class {0} is null or empty.", extensionClassFullName));
        }
        //Check if the @Extension description is empty.
        if (description.isEmpty()) {
            throw new AnnotationValidationException(MessageFormat.format("The @Extension -> description " +
                    "annotated in class {0} is null or empty.", extensionClassFullName));
        }
        //Check if the @Extension namespace is empty.
        if (namespace.isEmpty()) {
            //The namespace cannot be null or empty if @Extension is not in core package.
            //Extract core package name by using CORE_PACKAGE_PATTERN pattern.
            if (!CORE_PACKAGE_PATTERN.matcher(extensionClassFullName).find()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> namespace " +
                        "annotated in class {0} is null or empty.", extensionClassFullName));
            }
        }
    }

    /**
     * This method uses for validate @Extension / @Parameter element.
     *
     * @param parameters parameter array which needs to be validate.
     * @throws AnnotationValidationException whenever if the validate rule violate, throws the annotation validate
     *                                       exception with proper message.
     */
    public void parameterValidation(Parameter[] parameters) throws AnnotationValidationException {
        for (Parameter parameter : parameters) {
            String parameterName = parameter.name();
            //Check if the @Parameter name is empty.
            if (parameterName.isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter -> " +
                        "name annotated in class {0} is null or empty.", extensionClassFullName));
            } else if (!PARAMETER_NAME_PATTERN.matcher(parameterName).find()) {
                //Check if the @Parameter name is in a correct format 'abc.def.ghi' using regex pattern.
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter -> " +
                                "name {0} annotated in class {1} is not in proper format ''abc.def.ghi''.",
                        parameterName, extensionClassFullName));
            }
            //Check if the @Parameter description is empty.
            if (parameter.description().isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter -> " +
                                "name:{0} -> description annotated in class {1} is null or empty.", parameterName,
                        extensionClassFullName));
            }
            //Check if the @Parameter type is empty.
            if (parameter.type().length == 0) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter -> " +
                                "name:{0} -> type annotated in class {1} is null or empty.", parameterName,
                        extensionClassFullName));
            }
            if (parameter.optional()) {
                if (parameter.defaultValue().isEmpty()) {
                    throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter -> " +
                                    "name:{0} -> defaultValue annotated in class {1} cannot be null or empty for the " +
                                    "optional parameter.", parameterName, extensionClassFullName));
                }
            }
        }
    }

    /**
     * This method uses for validate @Extension / @ReturnAttribute elements.
     *
     * @param returnAttributes returnA attributes array which needs to be validate.
     * @throws AnnotationValidationException whenever if the validate rule violate, throws the annotation validate
     *                                       exception with proper message.
     */
    public void returnAttributesValidation(ReturnAttribute[] returnAttributes) throws AnnotationValidationException {
        for (ReturnAttribute returnAttribute : returnAttributes) {
            String returnAttributeName = returnAttribute.name();
            //Check if the @ReturnAttributes name is empty.
            if (returnAttributeName.isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                        "@ReturnAttribute -> name annotated in class {0} is null or empty.", extensionClassFullName));
            } else if (!CAMEL_CASE_PATTERN.matcher(returnAttributeName).find()) {
                //Check if the @Extension -> @ReturnAttribute -> name is in a correct camelCase
                // format using regex pattern.
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                                "@ReturnAttribute -> name {0} annotated in class {1} is not in camelCase format.",
                        returnAttributeName, extensionClassFullName));
            }
            //Check if the @ReturnAttributes description is empty.
            if (returnAttribute.description().isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                                "@ReturnAttribute -> name:{0} -> description annotated in class {1} is null or empty.",
                        returnAttributeName, extensionClassFullName));
            }
            //Check if the @ReturnAttributes type is empty.
            if (returnAttribute.type().length == 0) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                                "@ReturnAttribute -> name:{0} -> type annotated in class {1} is null or empty.",
                        returnAttributeName, extensionClassFullName));
            }
        }
    }

    /**
     * This method uses for validate @Extension / @SystemParameter elements.
     *
     * @param systemParameters system property array which needs to be validate.
     * @throws AnnotationValidationException whenever if the validate rule violate, throws the annotation validate
     *                                       exception with proper message.
     */
    public void systemParametersValidation(SystemParameter[] systemParameters) throws AnnotationValidationException {
        // Iterate over all @SystemParameter annotated elements.
        for (SystemParameter systemParameter : systemParameters) {
            String systemParameterName = systemParameter.name();
            //Check if the @SystemParameter name is empty.
            if (systemParameterName.isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                        "@SystemParameter -> name annotated in class {0} is null or empty.", extensionClassFullName));
            }
            //Check if the @SystemParameter description is empty.
            if (systemParameter.description().isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                                "@SystemParameter -> name:{0} -> description annotated in class {1} is null or empty.",
                        systemParameterName, extensionClassFullName));
            }
            //Check if the @SystemParameter defaultValue is empty.
            if (systemParameter.defaultValue().isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                                "@SystemParameter -> name:{0} -> defaultValue annotated in class {1} is null or empty.",
                        systemParameterName, extensionClassFullName));
            }
            //Check if the @SystemParameter possibleParameters is empty.
            if (systemParameter.possibleParameters().length == 0) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                        "@SystemParameter -> name:{0} -> possibleParameters annotated in class {1} is " +
                        "null or empty.", systemParameterName, extensionClassFullName));
            }
        }
    }

    /**
     * This method uses for validate @Extension / @Example elements.
     *
     * @param examples examples array which needs to be validate.
     * @throws AnnotationValidationException whenever if the validate rule violate, throws the annotation validate
     *                                       exception with proper message.
     */
    public void examplesValidation(Example[] examples) throws AnnotationValidationException {
        //Check if the @Example annotated in all the @Extension classes.
        if (examples.length == 0) {
            throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Example " +
                    "annotated in class {0} is null or empty.", extensionClassFullName));
        } else {
            for (Example example : examples) {
                //Check if the @Example syntax is empty.
                if (example.syntax().isEmpty()) {
                    throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                            "@Example -> syntax annotated in class {0} is null or empty.", extensionClassFullName));
                }
                //Check if the @Example description is empty.
                if (example.description().isEmpty()) {
                    throw new AnnotationValidationException(MessageFormat.format("The @Extension -> " +
                                    "@Example -> description annotated in class {0} is null or empty.",
                            extensionClassFullName));
                }
            }
        }
    }
}
