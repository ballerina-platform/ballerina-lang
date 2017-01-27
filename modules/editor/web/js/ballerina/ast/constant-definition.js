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
define(['lodash', 'log', './variable-declaration'], function (_, log, VariableDeclaration) {

    /**
     * Constructor for constant declaration
     * @param {Object} args - Arguments to create the Constant Declaration
     * @constructor
     * @augments VariableDeclaration
     */
    var ConstantDefinition = function(args) {

        if(_.isNil(_.get(args, "bType")) || _.isEmpty(_.get(args, "bType"))) {
            log.error("A constant requires to have a type.");
            throw "A constant requires to have a type.";
        }

        if(_.isNil(_.get(args, "identifier")) || _.isEmpty(_.get(args, "identifier"))) {
            log.error("A constant requires an identifier.");
            throw "A constant requires an identifier.";
        }

        if(_.isNil(_.get(args, "value")) || _.isEmpty(_.get(args, "value"))) {
            log.error("A constant requires a value.");
            throw "A constant requires a value.";
        }

        VariableDeclaration.call(this, {
            type: "Constant-Declaration",
            bType: _.get(args, "bType"),
            identifier: _.get(args, "identifier")
        });
        this._value = _.get(args, "value");
    };

    ConstantDefinition.prototype = Object.create(VariableDeclaration.prototype);
    ConstantDefinition.prototype.constructor = ConstantDefinition;

    ConstantDefinition.prototype.setValue = function (value) {
        if (_.isNil(value) || _.isEmpty(value)) {
            log.error("A constant requires to have a type.");
            throw "A constant requires to have a type.";
        } else {
            this._value = value;
        }
    };

    ConstantDefinition.prototype.getValue = function () {
        return this._value;
    };

    ConstantDefinition.prototype.getConstantDefinitionAsString = function() {
        return "const " + this._type + " " + this._identifier + " = " + this._value;
    };

    return ConstantDefinition;
});