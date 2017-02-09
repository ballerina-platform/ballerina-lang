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
package org.wso2.ballerina.annotation.processor;

import org.wso2.ballerina.core.model.types.TypeEnum;
import org.wso2.ballerina.core.nativeimpl.annotations.Argument;
import org.wso2.ballerina.core.nativeimpl.annotations.Attribute;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAnnotation;
import org.wso2.ballerina.core.nativeimpl.annotations.ReturnType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
            sb.append(getArgumentType(arg.type(), arg.elementType())).append(" ").append(arg.name());
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
    public static void getReturnParams(ReturnType[] args, StringBuilder sb) {
        sb.append(" (");
        for (int i = 1; i <= args.length; i++) {
            ReturnType arg = args[i - 1];
            sb.append(getArgumentType(arg.type(), arg.elementType()));
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
    public static String getArgumentType(TypeEnum argType, TypeEnum argEltType) {
        if (TypeEnum.ARRAY.equals(argType)) {
            return argEltType.getName() + "[]";
        }
        return argType.getName();
    }

    /**
     * Convert {@link BallerinaAnnotation} to {@link Annotation}
     * 
     * @param annotations array of {@link BallerinaAnnotation}
     * @return list of {@link Annotation}
     */
    public static List<Annotation> getAnnotations(BallerinaAnnotation[] annotations) {
        List<Annotation> annotationList = new ArrayList<>();
        for (BallerinaAnnotation ballerinaAnnotation : annotations) {
            annotationList.add(new Annotation(ballerinaAnnotation));
        }
        return annotationList;
    }

    /**
     * Appends annotations and builds a string representation using the default delimiter new line
     * 
     * @param sb {@link StringBuilder} to append to
     * @param annotations list of {@link Annotation}
     */
    public static void appendAnnotationStrings(StringBuilder sb, List<Annotation> annotations) {
        appendAnnotationStrings(sb, annotations, "\n");
    }

    /**
     * Appends annotations and builds a string representation using a given delimiter
     * 
     * @param sb {@link StringBuilder} to append to
     * @param annotations list of {@link Annotation}
     * @param delimiter delimiter to concatenate {@link Annotation} strings
     */
    public static void appendAnnotationStrings(StringBuilder sb, List<Annotation> annotations, String delimiter) {
        sb.append(annotations.stream().map(p -> annotationToString(p)).collect(Collectors.joining(delimiter)));
    }

    /**
     * Returns a string representation of an {@link Annotation}
     * 
     * @param annotation {@link Annotation}
     * @return string representation of the given {@link Annotation}
     */
    public static String annotationToString(Annotation annotation) {
        return Arrays.stream(DocAnnotations.values()).filter(e -> e.name().equals(annotation.getName())).findAny()
                .map(p -> getDocAnnotation(annotation)).orElse(annotation.toString());
    }

    /**
     * Generate string representation of documentation annotations
     * 
     * @param annotation {@link Annotation}
     * @return string representation of the given documentation annotation.
     */
    private static String getDocAnnotation(Annotation annotation) {
        StringBuilder sb = new StringBuilder();
        sb.append("@" + annotation.getName() + " (");
        List<Attribute> attributes = annotation.getAttributes();

        sb.append(DocAnnotations.Description.name().equals(annotation.getName()) ? "\"" + attributes.get(0).value()
                + "\"" : attributes.stream().map(p -> "\"" + p.name() + ": " + p.value() + "\" ")
                .collect(Collectors.joining(",")));
        sb.append(")");
        return sb.toString();
    }
}
