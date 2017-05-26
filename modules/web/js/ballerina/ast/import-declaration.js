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

import _ from 'lodash';
import ASTNode from './node';

/**
 * constructs ImportDeclaration
 * @param args
 * @constructor
 */
class ImportDeclaration extends ASTNode {
    constructor(args) {
        super("ImportDeclaration");
        this._packageName = _.get(args, 'packageName');
        this._importVersionNumber = _.get(args, 'importVersionName');
        this._identifier = _.get(args, 'identifier');
        this._asName = _.get(args, 'asName');
        this.whiteSpace.defaultDescriptor.regions =  {
            0: ' ',
            1: ' ',
            2: ' ',
            3: ' ',
            4: '\n'
        }
    }

    /**
     * setter for Package name
     * @param packageName
     */
    setPackageName(packageName, options) {
        if(!_.isNil(packageName)){
            this.setAttribute('_packageName', packageName, options);
        }
    }

    /**
     * setter for Import version number
     * @param importVersionNumber
     */
    setImportVersionNumber(importVersionNumber, options) {
        if(!_.isNil(importVersionNumber)){
            this.setAttribute('_importVersionNumber', importVersionNumber, options);
        }
    }

    /**
     * setter for Identifier
     * @param identifier
     */
    setIdentifier(identifier, options) {
        if(!_.isNil(identifier)){
            this.setAttribute('_identifier', identifier, options);
        }
    }

    /**
     * setter for As name
     * @param as name
     */
    setAsName(asName, options) {
        if(!_.isNil(asName)){
            this.setAttribute('_asName', asName, options);
        }
    }

    /**
     * getter for Package name
     * @returns {String}
     */
    getPackageName() {
        return this._packageName;
    }

    /**
     * getter for Import version number
     * @returns {String}
     */
    getImportVersionNumber() {
        return this._importVersionNumber;
    }

    /**
     * getter for Identifier
     * @returns {String}
     */
    getIdentifier() {
        return this._identifier;
    }

    /**
     * getter for Identifier
     * @returns {String}
     */
    getAsName() {
        return this._asName;
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        this.setPackageName(jsonNode.import_package_path, {doSilently: true});
        this.setIdentifier(jsonNode.import_package_name, {doSilently: true});
        this.setAsName(jsonNode.import_as_name, {doSilently: true});
    }
}

export default ImportDeclaration;
