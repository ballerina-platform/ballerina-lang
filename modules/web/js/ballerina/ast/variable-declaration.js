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
import _ from 'lodash';
import log from 'log';
import ASTNode from './node';

class VariableDeclaration extends ASTNode {
    constructor(args) {
        super(_.get(args, 'type', 'VariableDeclaration'));
        this._bType = _.get(args, 'bType');
        this._identifier = _.get(args, 'identifier');

        // Validating the identifier.
        if (!_.isUndefined(this.identifier) && !ASTNode.isValidIdentifier(this.identifier)) {
            const exceptionString = "Invalid identifier: \'" + this.identifier + "\'. An identifier must match the " +
                'regex ^[a-zA-Z$_][a-zA-Z0-9$_]*$';
            log.error(exceptionString);
            throw exceptionString;
        }
    }

    setBType(type, options) {
        if (!_.isUndefined(type)) {
            this.setAttribute('_bType', type, options);
        } else {
            const exceptionString = 'A variable requires a type.';
            log.error(exceptionString);
            throw exceptionString;
        }
    }

    getBType() {
        return this._bType;
    }

    setIdentifier(identifier, options) {
        if (!_.isNil(identifier) && ASTNode.isValidIdentifier(identifier)) {
            this.setAttribute('_identifier', identifier, options);
        } else {
            const exceptionString = "Invalid identifier: \'" + identifier + "\'. An identifier must match the regex " +
                '^[a-zA-Z$_][a-zA-Z0-9$_]*$';
            log.error(exceptionString);
            throw exceptionString;
        }
    }

    getIdentifier() {
        return this._identifier;
    }

    /**
     * Gets the variable declaration as a string.
     * @return {string} - Variable declaration as string.
     */
    getVariableDeclarationAsString() {
        return this._bType + ' ' + this._identifier + ';';
    }

    /**
     * Initialize VariableDeclaration from json object
     * @param {Object} jsonNode - The JSON object.
     * @param {string} jsonNode.variable_type - The ballerina type.
     * @param {string} jsonNode.variable_name - The identifier of the variable.
     */
    initFromJson(jsonNode) {
        this.setBType(jsonNode.variable_type, { doSilently: true });
        this.setIdentifier(jsonNode.variable_name, { doSilently: true });
    }
}

export default VariableDeclaration;

