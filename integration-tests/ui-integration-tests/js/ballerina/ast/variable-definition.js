/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash','log', './node'], function (_, log, ASTNode) {

    var VariableDefinition = function (args) {
        ASTNode.call(this, 'VariableDefinition');
        this._name = _.get(args, 'name', 'newIdentifier');
        this._typeName = _.get(args, 'typeName', 'newTypeName');
        this._pkgPath = _.get(args, 'pkgPath', 'newPkgPath');
        this._isPublic = _.get(args, 'isPublic', false);
    };

    VariableDefinition.prototype = Object.create(ASTNode.prototype);
    VariableDefinition.prototype.constructor = VariableDefinition;

    /**
     * set the name of the variable def
     * @param name
     * @param options
     */
    VariableDefinition.prototype.setName = function (name, options) {
        if (!_.isNil(name)) {
            this.setAttribute('_name', name, options);
        } else {
            log.error('Invalid Name [' + name + '] Provided');
            throw 'Invalid Name [' + name + '] Provided';
        }
    };

    /**
     * set the type of the variable def
     * @param typeName
     * @param options
     */
    VariableDefinition.prototype.setTypeName = function (typeName, options) {
        if (!_.isNil(typeName)) {
            this.setAttribute('_typeName', typeName, options);
        } else {
            log.error('Invalid Type Name [' + typeName + '] Provided');
            throw 'Invalid Type Name [' + typeName + '] Provided';
        }
    };

    /**
     * returns the  name
     * @returns {*}
     */
    VariableDefinition.prototype.getName = function () {
        return this._name;
    };

    /**
     * returns the  Type Name
     * @returns {*}
     */
    VariableDefinition.prototype.getTypeName = function () {
        return this._typeName;
    };

    /**
     * set the pkg path of the variable def
     * @param pkgPath
     * @param options
     */
    VariableDefinition.prototype.setPkgPath = function (pkgPath, options) {
        if (!_.isNil(pkgPath)) {
            this.setAttribute('_pkgPath', pkgPath, options);
        } else {
            log.error('Invalid pkg path [' + pkgPath + '] Provided');
            throw 'Invalid pkg path [' + pkgPath + '] Provided';
        }
    };

    /**
     * returns the  Pkg path
     * @returns {string}
     */
    VariableDefinition.prototype.getPkgPath = function () {
        return this._pkgPath;
    };

    /**
     * set whether the variable def is public
     * @param isPublic
     * @param options
     */
    VariableDefinition.prototype.setArrayType = function (isPublic, options) {
        if (!_.isNil(isPublic)) {
            this.setAttribute('_isPublic', isPublic, options);
        } else {
            log.error('Invalid array type [' + isPublic + '] Provided');
            throw 'Invalid array type [' + isPublic + '] Provided';
        }
    };

    /**
     * returns the  isPublic
     * @returns {boolean}
     */
    VariableDefinition.prototype.isPublic = function () {
        return this._isPublic;
    };

    /**
     * Gets the variable definition as a string.
     * @return {string} - Variable definition as string.
     */
    VariableDefinition.prototype.getVariableDefinitionAsString = function() {
        return this._typeName + " " + this._name + ";";
    };

    VariableDefinition.prototype.initFromJson = function (jsonNode) {
        var self = this;
        this.setName(jsonNode.variable_name, {doSilently: true});
        this.setTypeName(jsonNode.variable_type, {doSilently: true});

        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };

    return VariableDefinition;
});