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

    var TypeConverterDefinition = function (args) {
        ASTNode.call(this, 'TypeConverterDefinition');
        this._typeConverterName = _.get(args, 'typeConverterName', 'TypeConverter1');
        this.BallerinaASTFactory = this.getFactory();
    };

    TypeConverterDefinition.prototype = Object.create(ASTNode.prototype);
    TypeConverterDefinition.prototype.constructor = TypeConverterDefinition;

    TypeConverterDefinition.prototype.setTypeConverterName = function (typeConverterName) {
        if (!_.isNil(typeConverterName)) {
            this._typeConverterName = typeConverterName;
        }
    };

    TypeConverterDefinition.prototype.getTypeConverterName = function () {
        return this._typeConverterName;
    };

    TypeConverterDefinition.prototype.setVariableDeclarations = function (variableDeclarations) {
        if (!_.isNil(variableDeclarations)) {
            this.variableDeclarations = variableDeclarations;
        }
    };

    TypeConverterDefinition.prototype.getVariableDeclarations = function () {
        var variableDeclarations = [];
        var self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.BallerinaASTFactory.isVariableDeclaration(child)) {
                variableDeclarations.push(child);
            }
        });
        return variableDeclarations;
    };

    return TypeConverterDefinition;
});