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
        if (!_.isUndefined(this.identifier) && !VariableDeclaration.isValidIdentifier(this.identifier)) {
            var exceptionString = "Invalid identifier: \'" + this.identifier + "\'. An identifier must match the " +
                "regex ^[a-zA-Z$_][a-zA-Z0-9$_]*$";
            log.error(exceptionString);
            throw exceptionString;
        }
    };

    VariableDeclaration.prototype = Object.create(ASTNode.prototype);
    VariableDeclaration.prototype.constructor = VariableDeclaration;

    VariableDeclaration.prototype.setType = function (type) {
        if(!_.isUndefined(type)){
            this._type = type;
        }
    };

    VariableDeclaration.prototype.getType = function () {
        return this._type;
    };

    VariableDeclaration.prototype.setIdentifier = function (identifier) {
        if (!_.isNil(identifier) && VariableDeclaration.isValidIdentifier(identifier)) {
            this.identifier = identifier;
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
     * initialize VariableDeclaration from json object
     * @param {Object} jsonNode to initialize from
     */
    VariableDeclaration.prototype.initFromJson = function (jsonNode) {
        this.setType(jsonNode.variable_type);
        this.setIdentifier(jsonNode.variable_name);
    };

    /**
     * Checks whether the identifier is valid or not.
     * @param {string} identifier - The identifier
     * @return {boolean} - True if valid identifier, else false.
     * @static
     */
    VariableDeclaration.isValidIdentifier = function (identifier) {
        return identifier === undefined ? false : /^[a-zA-Z$_][a-zA-Z0-9$_]*$/.test(identifier);
    };

    return VariableDeclaration;
});