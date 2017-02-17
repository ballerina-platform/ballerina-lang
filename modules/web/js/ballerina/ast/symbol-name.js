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
define(['lodash', './node'], function (_, ASTNode) {

    var SymbolName = function (args) {
        ASTNode.call(this, 'SymbolName');
        this._name = _.get(args, 'name', 'newName');
        this._pkgPath = _.get(args, 'pkgPath', 'newPkgPath');
    };

    SymbolName.prototype = Object.create(ASTNode.prototype);
    SymbolName.prototype.constructor = SymbolName;

    /**
     * set the name of the symbol
     * @param name
     * @param options
     */
    SymbolName.prototype.setName = function (name, options) {
        if (!_.isNil(name)) {
            this.setAttribute('_name', name, options);
        } else {
            log.error('Invalid Name [' + name + '] Provided');
            throw 'Invalid Name [' + name + '] Provided';
        }
    };

    /**
     * returns the  name
     * @returns {*}
     */
    SymbolName.prototype.getName = function () {
        return this._name;
    };

    /**
     * set the Pkg path of the symbol
     * @param pkgName
     * @param options
     */
    SymbolName.prototype.setPkgPath = function (pkgPath, options) {
        if (!_.isNil(pkgPath)) {
            this.setAttribute('_pkgPath', pkgPath, options);
        } else {
            log.error('Invalid Name [' + pkgPath + '] Provided');
            throw 'Invalid Name [' + pkgPath + '] Provided';
        }
    };

    /**
     * returns the  pkgName
     * @returns {string}
     */
    SymbolName.prototype.getPkgPath = function () {
        return this._pkgPath;
    };
    
    return SymbolName;
});