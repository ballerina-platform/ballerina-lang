/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.openapi.validator;

/**
 * Container for error messages of the OpenAPI validator plugin.
 */
class ErrorMessages {

    static String invalidFilePath(String path) {
        return String.format("OpenAPI contract doesn't exist in the given location:%n%s", path);
    }

    static String invalidFile() {
        return "Invalid file type. Provide either a .yaml or .json file.";
    }

    static String parserException(String path) {
        return String.format("Couldn't read the OpenAPI contract from the given file: %s", path);
    }

    static String undocumentedResourcePath(String path) {
        return String.format("Ballerina service contains a Resource that is not"
                + " documented in the OpenAPI contract."
                + " Error Resource path '%s'", path);
    }

    static String undocumentedResourceParameter(String paramName, String method, String path) {
        return String.format("'%s' parameter for the method '%s' " +
                "of the resource associated with the path '%s' " +
                "is not documented in the OpenAPI contract", paramName, method, path);
    }

    static String undocumentedResourceMethods(String methods, String path) {
        return String.format("OpenAPI contract doesn't contain the" +
                " documentation for http method(s) '%s' for the path '%s'", methods, path);
    }

    static String unimplementedOpenAPIPath(String path) {
        return String.format("Couldn't find a Ballerina service resource for the path '%s' " +
                "which is documented in the OpenAPI contract", path);
    }

    static String unimplementedOpenAPIOperationsForPath(String methods, String path) {
        return String.format("Couldn't find Ballerina service resource(s) for http method(s) '%s' " +
                "for the path '%s' which is documented in the OpenAPI contract", methods, path);
    }

    static String unimplementedParameterForOperation(String paramName, String method, String path) {
        return String.format("Couldn't find '%s' parameter in the Ballerina service resource for the method '%s' " +
                "of the path '%s' which is documented in the OpenAPI contract", paramName, method, path);
    }

    static String undocumentedFieldInRecordParam(String fieldName, String paramName, String method, String path) {
        return String.format("The '%s' field in the record type of the parameter '%s' " +
                        "is not documented in the OpenAPI contract for the method '%s' of the path '%s'",
                fieldName, paramName, method, path);
    }

    static String unimplementedFieldInOperation(String fieldName, String paramName, String operation, String path) {
        return String.format("Couldn't find the '%s' field in the record type of the parameter '%s' " +
                        "for the method '%s' of the path '%s' which is documented in the OpenAPI contract",
                fieldName, paramName, operation, path);
    }

    static String tagFilterEnable() {
        return String.format("Both Tags and excludeTags fields include the same tag(s). Make sure to use one" +
                " field of tag filtering when using the openapi annotation. ");
    }

    static String operationFilterEnable() {
        return String.format("Both Operations and excludeOperations fields include" +
                " the same operation(s). Make sure to use one field of operation filtering" +
                " when using the openapi annotation.");
    }
}
