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

import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.ReturnAttribute;
import org.wso2.siddhi.annotation.util.AnnotationValidationException;

import java.text.MessageFormat;

/**
 * This processor will extend the validation rules for validate Stream Function specific annotation contents.
 */
public class StreamFunctionProcessorValidationAnnotationProcessor extends AbstractAnnotationProcessor {
    public StreamFunctionProcessorValidationAnnotationProcessor(String extensionClassFullName) {
        super(extensionClassFullName);
    }

    @Override
    public void parameterValidation(Parameter[] parameters) throws AnnotationValidationException {
        for (Parameter parameter : parameters) {
            String parameterName = parameter.name();
            //Check if the @Parameter name is empty.
            if (parameterName.isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter ->" +
                        " name annotated in class {0} is null or empty.", extensionClassFullName));
            } else if (!PARAMETER_NAME_PATTERN.matcher(parameterName).find()) {
                //Check if the @Parameter name is in a correct format 'abc.def.ghi' using regex pattern.
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter ->" +
                                " name:{0} annotated in class {1} is not in proper format ''abc.def.ghi''.",
                        parameterName, extensionClassFullName));
            }
            //Check if the @Parameter description is empty.
            if (parameter.description().isEmpty()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter ->" +
                                " name:{0} -> description annotated in class {1} is null or empty.", parameterName,
                        extensionClassFullName));
            }
            //Check if the @Parameter type is empty.
            if (parameter.type().length == 0) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter ->" +
                                " name:{0} -> type annotated in class {1} is null or empty.", parameterName,
                        extensionClassFullName));
            }
            //Check if the @Parameter dynamic property false or empty in the classes extending
            //super classes except the Sink & SinkMapper.
            if (parameter.dynamic()) {
                throw new AnnotationValidationException(MessageFormat.format("The @Extension -> @Parameter ->" +
                                " name:{0} -> dynamic property cannot be true annotated in class {1}.", parameterName,
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

    @Override
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
                                "@ReturnAttribute -> name:{0}  -> description annotated in class {1} is null or empty.",
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
}
