/**
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
define(['lodash', './node'], function (_, ASTNode) {

    var TypeConverterDefinition = function (source, variableDeclarations, returnType) {
        this.source = source;
        this.variableDeclarations = variableDeclarations || [];
        this.returnType = returnType;
    };

    TypeConverterDefinition.prototype = Object.create(ASTNode.prototype);
    TypeConverterDefinition.prototype.constructor = TypeConverterDefinition;

    TypeConverterDefinition.prototype.setSource = function (source) {
        if (!_.isEmpty(source)) {
            this.source = source;
        }
    };

    TypeConverterDefinition.prototype.setVariableDeclarations = function (variableDeclarations) {
        if (!_.isEmpty(variableDeclarations)) {
            this.variableDeclarations = variableDeclarations;
        }
    };

    TypeConverterDefinition.prototype.setReturnType = function (returnType) {
        if (!_.isEmpty(returnType)) {
            this.returnType = returnType;
        }
    };

    TypeConverterDefinition.prototype.getSource = function () {
        return this.source;
    };

    TypeConverterDefinition.prototype.getvariableDeclarations = function () {
        return this.variableDeclarations;
    };

    TypeConverterDefinition.prototype.getReturnType = function () {
        return this.returnType;
    };

});