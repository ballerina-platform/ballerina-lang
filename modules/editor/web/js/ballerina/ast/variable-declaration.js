/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['lodash', 'log', './node'], function(_, log, ASTNode){

    var VariableDeclaration = function (args) {
        ASTNode.call(this, _.get(args, "type", "VariableDeclaration"));
        this._type = _.get(args, "bType");
        this._identifier = _.get(args, "identifier");

        // Validating the identifier.
        if (!_.isUndefined(this.identifier) && !ASTNode.isValidIdentifier(this.identifier)) {
            var exceptionString = "Invalid identifier: \'" + this.identifier + "\'. An identifier must match the " +
                "regex ^[a-zA-Z$_][a-zA-Z0-9$_]*$";
            log.error(exceptionString);
            throw exceptionString;
        }
    };

    VariableDeclaration.prototype = Object.create(ASTNode.prototype);
    VariableDeclaration.prototype.constructor = VariableDeclaration;

    VariableDeclaration.prototype.setType = function (type, options) {
        if (!_.isUndefined(type)) {
            this.setAttribute('_type', type, options);
        } else {
            var exceptionString = "A variable requires a type.";
            log.error(exceptionString);
            throw exceptionString;
        }
    };

    VariableDeclaration.prototype.getType = function () {
        return this._type;
    };

    VariableDeclaration.prototype.setIdentifier = function (identifier, options) {
        if (!_.isNil(identifier) && ASTNode.isValidIdentifier(identifier)) {
            this.setAttribute('_identifier', identifier, options);
        } else {
            var exceptionString = "Invalid identifier: \'" + identifier + "\'. An identifier must match the regex " +
                "^[a-zA-Z$_][a-zA-Z0-9$_]*$";
            log.error(exceptionString);
            throw exceptionString;
        }
    };

    VariableDeclaration.prototype.getIdentifier = function(){
        return this._identifier;
    };

    /**
     * Gets the variable declaration as a string.
     * @return {string} - Variable declaration as string.
     */
    VariableDeclaration.prototype.getVariableDeclarationAsString = function() {
      return this._type + " " + this._identifier + ";";
    };

    /**
     * Initialize VariableDeclaration from json object
     * @param {Object} jsonNode - The JSON object.
     * @param {string} jsonNode.variable_type - The ballerina type.
     * @param {string} jsonNode.variable_name - The identifier of the variable.
     */
    VariableDeclaration.prototype.initFromJson = function (jsonNode) {
        this.setType(jsonNode.variable_type, {doSilently: true});
        this.setIdentifier(jsonNode.variable_name, {doSilently: true});
    };

    return VariableDeclaration;
});
