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
define(['lodash', 'log', './node'], function (_, log, ASTNode) {

    /**
     * Constructor for Argument
     * @param {Object} [args] - The arguments to create the Argument.
     * @param {string} [args.type=undefined] - Type of the argument.
     * @param {string} [args.identifier=undefined] - Identifier of the argument.
     * @class Argument
     * @constructor
     * @extends ASTNode
     */
    var Argument = function (args) {
        ASTNode.call(this, "Argument");
        this.type = _.get(args, "type","ArgumentType");
        this.identifier = _.get(args, "identifier","ArgumentIdentifier");

        // Validating the argument.
        if (!_.isUndefined(this.identifier) && !ASTNode.isValidIdentifier(this.identifier)) {
            var exceptionString = "Invalid identifier: \'" + this.identifier + "\'. An identifier must match the regex " +
                "^[a-zA-Z$_][a-zA-Z0-9$_]*$";
            log.error(exceptionString);
            throw exceptionString;
        }
    };

    Argument.prototype = Object.create(ASTNode.prototype);
    Argument.prototype.constructor = Argument;

    Argument.prototype.setType = function (type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('type', type, options);
        }
    };

    Argument.prototype.getType = function () {
        return this.type;
    };

    Argument.prototype.getArgumentAsString = function() {
        var argAsString = "";
        argAsString += this.type;
        argAsString += !_.isUndefined(this.identifier) ? " " + this.identifier : "";
        return argAsString;
    };

    Argument.prototype.setIdentifier = function (identifier, options) {
        if (_.isNil(identifier) || ASTNode.isValidIdentifier(identifier)) {
            this.setAttribute('identifier', identifier, options);
        } else {
            var exceptionString = "Invalid identifier: \'" + identifier + "\'. An identifier must match the regex " +
                "^[a-zA-Z$_][a-zA-Z0-9$_]*$";
            log.error(exceptionString);
            throw exceptionString;
        }
    };

    Argument.prototype.getType = function () {
        return this.type;
    };

    Argument.prototype.getIdentifier = function () {
        return this.identifier;
    };

    /**
     * initialize Argument from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} jsonNode.parameter_type - Type of the argument
     * @param {string} jsonNode.parameter_name - Identifier of the argument
     */
    Argument.prototype.initFromJson = function (jsonNode) {
        this.setType(jsonNode.parameter_type, {doSilently: true});
        this.setIdentifier(jsonNode.parameter_name, {doSilently: true});
    };

    Argument.prototype.toString = function () {
        return this.getArgumentAsString();
    };

    return Argument;
});
