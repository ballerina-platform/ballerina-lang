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

    var TypeElement = function (type, identifier) {
        this.type = type;
        this.identifier = identifier;
        this.type = "TypeElement";
    };

    TypeElement.prototype = Object.create(ASTNode.prototype);
    TypeElement.prototype.constructor = TypeElement;


    TypeElement.prototype.setType = function (type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('type', type, options);
        }
    };

    TypeElement.prototype.setIdentifier = function (identifier, options) {
        if (!_.isNil(identifier)) {
            this.setAttribute('identifier', identifier, options);
        }
    };

    TypeElement.prototype.getType = function () {
        return this.type;
    };

    TypeElement.prototype.getIdentifier = function () {
        return this.identifier;
    };

    return TypeElement;
});
