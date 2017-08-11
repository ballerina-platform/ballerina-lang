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
import ASTNode from './node';

/**
 * class for namespace declaration.
 * @class NamespaceDeclaration
 * */
class NamespaceDeclaration extends ASTNode {
    /**
     * constructor for NamespaceDeclaration
     * @param {object} args - arguments for creating NamespaceDeclaration
     * */
    constructor(args) {
        super(_.get(args, 'type', 'NamespaceDeclaration'));
    }

    /**
     * set the identifier
     * @param {string} identifier - identifier
     * @param {object} options - options for setAttribute
     * */
    setIdentifier(identifier, options) {
        if (!_.isNil(identifier) && ASTNode.isValidIdentifier(identifier)) {
            this.setAttribute('_identifier', identifier, options);
        }
    }

    /**
     * set the name
     * @param {string} name - name
     * @param {object} options - options for setAttribute
     * */
    setName(name, options) {
        this.setAttribute('_name', name, options);
    }

    /**
     * set the URI.
     * @param {string} uri - namespace URI.
     * @param {object} options - options for setAttribute
     * */
    setURI(uri, options) {
        this.setAttribute('_uri', uri, options);
    }

    /**
     * set the package path
     * @param {string} packagePath - package path
     * @param {object} options - options for setAttribute
     * */
    setPackagePath(packagePath, options) {
        this.setAttribute('_packagePath', packagePath, options);
    }

    /**
     * get the identifier.
     * @return {string} identifier.
     * */
    getIdentifier() {
        return this._identifier;
    }

    /**
     * get the name.
     * @return {string} name.
     * */
    getName() {
        return this._name;
    }

    /**
     * get the URI.
     * @return {string} namespace URI.
     * */
    getURI() {
        return this._uri;
    }

    /**
     * get the package path.
     * @return {string} package path.
     * */
    getPackagePath() {
        return this._packagePath;
    }

    /**
     * get the statement string.
     * @return {string} statement string.
     * */
    getStatementString() {
        let statementString = `xmlns "${this.getURI()}"`;
        if (this._identifier) {
            statementString += ` as ${this.getIdentifier()}`;
        }
        return statementString;
    }

    /**
     * initialize NamespaceDeclaration from the json.
     * @param {object} jsonNode - json node for NamespaceDeclaration
     * */
    initFromJson(jsonNode) {
        this.setIdentifier(jsonNode.namespace_identifier, { doSilently: true });
        this.setName(jsonNode.namespace_name, { doSilently: true });
        this.setURI(jsonNode.namespace_uri, { doSilently: true });
        this.setPackagePath(jsonNode.namespace_packagePath, { doSilently: true });
    }
}

export default NamespaceDeclaration;
