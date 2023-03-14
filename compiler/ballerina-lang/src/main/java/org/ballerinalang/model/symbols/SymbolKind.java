/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model.symbols;

/**
 * @since 0.94
 */
public enum SymbolKind {
    PACKAGE,
    STRUCT,
    OBJECT,
    RECORD,
    CONNECTOR,
    ACTION,
    SERVICE,
    RESOURCE,
    FUNCTION,
    WORKER,
    ANNOTATION,
    ANNOTATION_ATTRIBUTE,
    CONSTANT,
    VARIABLE,
    PACKAGE_VARIABLE,
    TRANSFORMER,
    TYPE_DEF,
    ENUM,
    ERROR,

    PARAMETER,
    PATH_PARAMETER,
    PATH_REST_PARAMETER,
    LOCAL_VARIABLE,
    SERVICE_VARIABLE,
    CONNECTOR_VARIABLE,

    CAST_OPERATOR,
    CONVERSION_OPERATOR,
    TYPEOF_OPERATOR,

    XMLNS,
    SCOPE,
    OTHER,

    INVOKABLE_TYPE,

    RESOURCE_PATH_IDENTIFIER_SEGMENT,
    RESOURCE_PATH_PARAM_SEGMENT,
    RESOURCE_PATH_REST_PARAM_SEGMENT,
    RESOURCE_ROOT_PATH_SEGMENT
}
