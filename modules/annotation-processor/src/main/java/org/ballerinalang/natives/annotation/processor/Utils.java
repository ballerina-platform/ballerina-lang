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
package org.ballerinalang.natives.annotation.processor;

import org.ballerinalang.model.types.TypeEnum;
import org.ballerinalang.natives.annotation.processor.holders.AnnotationHolder;
import org.ballerinalang.natives.annotations.Argument;
import org.ballerinalang.natives.annotations.Attribute;
import org.ballerinalang.natives.annotations.BallerinaAction;
import org.ballerinalang.natives.annotations.BallerinaAnnotation;
import org.ballerinalang.natives.annotations.BallerinaFunction;
import org.ballerinalang.natives.annotations.BallerinaTypeMapper;
import org.ballerinalang.natives.annotations.ReturnType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Utility class for annotation processor
 */
public class Utils {

    /**
     * Holds the annotations supported by API documentation.
     */
    static enum DocAnnotations {
        Description, Param, Return
    }

    /**
     * Appends input parameters
     * 
     * @param args input parameters
     * @param sb {@link StringBuilder} to append the input parameters
     */
    public static void getInputParams(Argument[] args, StringBuilder sb) {
        sb.append(" (");
        for (int i = 1; i <= args.length; i++) {
            Argument arg = args[i - 1];
            sb.append(getArgumentType(arg.type(), arg.elementType(), arg.structType())).append(" ").append(arg.name());
            if (i != args.length) {
                sb.append(", ");
            }
        }
        sb.append(")");
    }

    /**
     * Appends return parameters
     * 
     * @param args return parameters
     * @param sb {@link StringBuilder} to append the return parameters
     */
    public static void appendReturnParams(ReturnType[] args, StringBuilder sb) {
        if (args.length == 0) {
            return;
        }
        sb.append(" (");
        for (int i = 1; i <= args.length; i++) {
            ReturnType arg = args[i - 1];
            sb.append(getArgumentType(arg.type(), arg.elementType(), ""));
            if (i != args.length) {
                sb.append(", ");
            }
        }
        sb.append(")");
    }

    /**
     * Gets the argument type
     * 
     * @param argType type of the argument
     * @param argEltType type of the argument elements
     * @return argument type
     */
    public static String getArgumentType(TypeEnum argType, TypeEnum argEltType, String structType) {
        if (TypeEnum.ARRAY.equals(argType)) {
            if (TypeEnum.STRUCT.equals(argEltType)) {
                return structType + "[]";
            } else {
                return argEltType.getName() + "[]";
            }
        } else {
            if (TypeEnum.STRUCT.equals(argType)) {
                return structType;
            } else {
                return argType.getName();
            }
        }
    }

    /**
     * Convert {@link BallerinaAnnotation} to {@link AnnotationHolder}
     * 
     * @param annotations arrays of {@link BallerinaAnnotation}
     * @return list of {@link AnnotationHolder}
     */
    public static List<AnnotationHolder> getAnnotations(BallerinaAnnotation[] annotations) {
        List<AnnotationHolder> annotationList = new ArrayList<>();
        for (BallerinaAnnotation ballerinaAnnotation : annotations) {
            annotationList.add(new AnnotationHolder(ballerinaAnnotation));
        }
        return annotationList;
    }

    /**
     * Appends annotations and builds a string representation using the default delimiter new line
     * 
     * @param sb {@link StringBuilder} to append to
     * @param annotations list of {@link AnnotationHolder}
     */
    public static void appendAnnotationStrings(StringBuilder sb, List<AnnotationHolder> annotations) {
        appendAnnotationStrings(sb, annotations, "\n");
    }

    /**
     * Appends annotations and builds a string representation using a given delimiter
     * 
     * @param sb {@link StringBuilder} to append to
     * @param annotations list of {@link AnnotationHolder}
     * @param delimiter delimiter to concatenate {@link AnnotationHolder} strings
     */
    public static void appendAnnotationStrings(StringBuilder sb, List<AnnotationHolder> annotations, String delimiter) {
        sb.append(annotations.stream().map(p -> annotationToString(p)).collect(Collectors.joining(delimiter)));
    }

    /**
     * Returns a string representation of an {@link AnnotationHolder}
     * 
     * @param annotation {@link AnnotationHolder}
     * @return string representation of the given {@link AnnotationHolder}
     */
    public static String annotationToString(AnnotationHolder annotation) {
        return Arrays.stream(DocAnnotations.values()).filter(e -> e.name().equals(annotation.getName())).findAny()
                .map(p -> getDocAnnotation(annotation)).orElse(annotation.toString());
    }

    /**
     * Generate string representation of documentation annotations
     * 
     * @param annotation {@link AnnotationHolder}
     * @return string representation of the given documentation annotation.
     */
    private static String getDocAnnotation(AnnotationHolder annotation) {
        StringBuilder sb = new StringBuilder();
        sb.append("@doc:" + annotation.getName() + " (");
        List<Attribute> attributes = annotation.getAttributes();

        sb.append(DocAnnotations.Description.name().equals(annotation.getName()) ? "\"" + attributes.get(0).value()
                + "\"" : attributes.stream().map(p -> "\"" + p.name() + ": " + p.value() + "\" ")
                .collect(Collectors.joining(",")));
        sb.append(")");
        return sb.toString();
    }
    
    /**
     * Get the qualified name of a ballerina native action.
     * 
     * @param balAction Ballerina action annotation
     * @param connectorName Name of the connector this action belongs to
     * @param connectorPkg Package of the connector this action belongs to
     * 
     * @return Qualified name of a ballerina native action
     */
    public static String getActionQualifiedName(BallerinaAction balAction, String connectorName, String connectorPkg) {
        StringBuilder actionNameBuilder = new StringBuilder(balAction.connectorName() + "." + balAction.actionName());
        Argument[] args = balAction.args();
        for (Argument arg : args) {
            if (arg.type() == TypeEnum.CONNECTOR) {
                actionNameBuilder.append("." + connectorPkg + ":" + connectorName);
            } else if (arg.type() == TypeEnum.ARRAY && arg.elementType() != TypeEnum.EMPTY) {
                // if the argument is arrayType, then append the element type to the method signature
                if (arg.elementType() == TypeEnum.STRUCT) {
                    actionNameBuilder.append("." + connectorPkg + ":" + arg.structType() + "[]");
                } else {
                    actionNameBuilder.append("." + arg.elementType().getName() + "[]");
                }
            } else {
                if (arg.type() == TypeEnum.STRUCT) {
                    actionNameBuilder.append("." + arg.structType());
                } else {
                    actionNameBuilder.append("." + arg.type().getName());
                }
            }
        }
        return actionNameBuilder.toString();
    }
    
    /**
     * Get the fully qualified class name of a given element.
     * 
     * @param element Element to get the class name
     * @return Fully qualified class name of a given element
     */
    public static String getClassName(Element element) {
        return ((TypeElement) element).getQualifiedName().toString();
    }
    
    /**
     * Get the fully qualified name of the ballerina function.
     * 
     * @param balFunction Ballerina function annotation
     * @return Fully qualified name
     */
    public static String getFunctionQualifiedName(BallerinaFunction balFunction) {
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
     * Get the fully qualified name of the ballerina type convertor.
     * 
     * @param balTypeMapper Ballerina type convertor annotation.
     * @return Fully qualified name
     */
    public static String getTypeConverterQualifiedName(BallerinaTypeMapper balTypeMapper) {
        StringBuilder convertorNameBuilder = new StringBuilder();
        Argument[] args = balTypeMapper.args();
        ReturnType[] returnTypes = balTypeMapper.returnType();
        
        for (Argument arg : args) {
            convertorNameBuilder.append(".").append(arg.type().getName());
        }
        
        convertorNameBuilder.append("->");
        
        for (ReturnType returnType : returnTypes) {
            convertorNameBuilder.append(".").append(returnType.type().getName());
        }
        return convertorNameBuilder.toString();
    }
}
