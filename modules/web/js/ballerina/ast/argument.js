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
import _ from 'lodash';
import log from 'log';
import ASTNode from './node';

/**
 * Constructor for Argument
 * @param {Object} [args] - The arguments to create the Argument.
 * @param {string} [args.type=undefined] - Type of the argument.
 * @param {string} [args.identifier=undefined] - Identifier of the argument.
 * @class Argument
 * @constructor
 * @extends ASTNode
 */
class Argument extends ASTNode {
    constructor(args) {
        super('Argument');
        this.bType = _.get(args, 'type', 'ArgumentType');
        this.identifier = _.get(args, 'identifier');

        // Validating the argument.
        if (!_.isUndefined(this.identifier) && !ASTNode.isValidIdentifier(this.identifier)) {
            const exceptionString = 'Invalid identifier: \'' + this.identifier + '\'. An identifier must match the regex ' +
                '^[a-zA-Z$_][a-zA-Z0-9$_]*$';
            log.error(exceptionString);
            throw exceptionString;
        }
    }

    setBType(type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('bType', type, options);
        }
    }

    getBType() {
        return this.bType;
    }

    getArgumentAsString() {
        let argAsString = '';
        argAsString += this.bType;
        argAsString += !_.isUndefined(this.identifier) ? ' ' + this.identifier : '';
        return argAsString.trim();
    }

    setIdentifier(identifier, options) {
        if (_.isNil(identifier) || ASTNode.isValidIdentifier(identifier)) {
            this.setAttribute('identifier', identifier, options);
        } else {
            const exceptionString = 'Invalid identifier: \'' + identifier + '\'. An identifier must match the regex ' +
                '^[a-zA-Z$_][a-zA-Z0-9$_]*$';
            log.error(exceptionString);
            throw exceptionString;
        }
    }

    // TODO: This function is to temporary fix the issue of function drag and drop until the model refactor
    getParameterAsString() {
        let paramAsString = '';
        paramAsString += '' + this.getBType() + ' ';
        paramAsString += this.getIdentifier();

        return paramAsString.trim();
    }

    getIdentifier() {
        return this.identifier;
    }

    /**
     * initialize Argument from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} jsonNode.parameter_type - Type of the argument
     * @param {string} jsonNode.parameter_name - Identifier of the argument
     */
    initFromJson(jsonNode) {
        this.setBType(jsonNode.parameter_type, { doSilently: true });
        this.setIdentifier(jsonNode.parameter_name, { doSilently: true });
    }

    toString() {
        return this.getArgumentAsString();
    }
}

export default Argument;

