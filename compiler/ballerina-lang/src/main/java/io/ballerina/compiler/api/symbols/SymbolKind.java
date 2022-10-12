/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package io.ballerina.compiler.api.symbols;

/**
 * Represents the types of Symbols.
 *
 * @since 2.0.0
 */
public enum SymbolKind {
    MODULE,
    XMLNS,
    FUNCTION,
    METHOD,
    RESOURCE_METHOD,
    CONSTANT,
    TYPE_DEFINITION,
    TYPE,
    VARIABLE,
    SERVICE_DECLARATION,
    CLIENT_DECLARATION,
    CLASS,
    WORKER,
    ANNOTATION,
    RECORD_FIELD,
    OBJECT_FIELD,
    CLASS_FIELD,
    ENUM,
    ENUM_MEMBER,
    PARAMETER,
    PATH_PARAMETER,
    ANNOTATION_ATTACHMENT
}
