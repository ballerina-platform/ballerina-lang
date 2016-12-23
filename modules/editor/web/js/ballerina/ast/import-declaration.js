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

    /**
     * constructs ImportDeclaration
     * @param args
     * @constructor
     */
    var ImportDeclaration = function (args) {
        this._packageName = _.get(args, 'packageName');
        this._importVersionNumber = _.get(args, 'importVersionName', "1.0.0");
        this._identifier = _.get(args, 'identifier', "");

        ASTNode.call(this);
    };

    ImportDeclaration.prototype = Object.create(ASTNode.prototype);
    ImportDeclaration.prototype.constructor = ImportDeclaration;

    /**
     * setter for Package name
     * @param packageName
     */
    ImportDeclaration.prototype.setPackageName = function (packageName) {
      if(!_.isNil(packageName)){
          this._packageName = packageName;
      }
    };

    /**
     * setter for Import version number
     * @param importVersionNumber
     */
    ImportDeclaration.prototype.setImportVersionNumber = function (importVersionNumber) {
        if(!_.isNil(importVersionNumber)){
            this._importVersionNumber = importVersionNumber;
        }
    };

    /**
     * setter for Identifier
     * @param identifier
     */
    ImportDeclaration.prototype.setIdentifier = function (identifier) {
        if(!_.isNil(identifier)){
            this._identifier = identifier;
        }
    };

    /**
     * getter for Package name
     * @returns {String}
     */
    ImportDeclaration.prototype.getPackageName = function () {
        return this._packageName;
    };

    /**
     * getter for Import version number
     * @returns {String}
     */
    ImportDeclaration.prototype.getImportVersionNumber = function () {
        return this._importVersionNumber;
    };

    /**
     * getter for Identifier
     * @returns {String}
     */
    ImportDeclaration.prototype.getIdentifier = function () {
        return this._identifier;
    };

    return ImportDeclaration;
});