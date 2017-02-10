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

    var SimpleTypeName = function (args) {
        ASTNode.call(this, 'SimpleTypeName');
        this._name = _.get(args, 'name', 'newName');
        this._pkgName = _.get(args, 'pkgName', 'newPkgName');
        this._pkgPath = _.get(args, 'pkgPath', 'newPkgPath');
        this._isArrayType = _.get(args, 'isArrayType', false);
    };

    SimpleTypeName.prototype = Object.create(ASTNode.prototype);
    SimpleTypeName.prototype.constructor = SimpleTypeName;

    /**
     * set the name of the simple type name
     * @param name
     * @param options
     */
    SimpleTypeName.prototype.setName = function (name, options) {
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
    SimpleTypeName.prototype.getName = function () {
        return this._name;
    };

    /**
     * set the Pkg Name of the simple type name
     * @param pkgName
     * @param options
     */
    SimpleTypeName.prototype.setPkgName = function (pkgName, options) {
        if (!_.isNil(pkgName)) {
            this.setAttribute('_pkgName', pkgName, options);
        } else {
            log.error('Invalid Name [' + pkgName + '] Provided');
            throw 'Invalid Name [' + pkgName + '] Provided';
        }
    };

    /**
     * returns the  pkgName
     * @returns {string}
     */
    SimpleTypeName.prototype.getPkgName = function () {
        return this._pkgName;
    };

    /**
     * set the pkg path of the simple type name
     * @param pkgPath
     * @param options
     */
    SimpleTypeName.prototype.setPkgPath = function (pkgPath, options) {
        if (!_.isNil(pkgPath)) {
            this.setAttribute('_pkgPath', pkgPath, options);
        } else {
            log.error('Invalid pkg path [' + pkgPath + '] Provided');
            throw 'Invalid pkg path [' + pkgPath + '] Provided';
        }
    };

    /**
     * returns the  Pkg path
     * @returns {string}
     */
    SimpleTypeName.prototype.getPkgPath = function () {
        return this._pkgPath;
    };

    /**
     * set the isArrayType of the simple type name
     * @param pkgPath
     * @param options
     */
    SimpleTypeName.prototype.setArrayType = function (isArrayType, options) {
        if (!_.isNil(isArrayType)) {
            this.setAttribute('_isArrayType', isArrayType, options);
        } else {
            log.error('Invalid array type [' + isArrayType + '] Provided');
            throw 'Invalid array type [' + isArrayType + '] Provided';
        }
    };

    /**
     * returns the  isArrayType
     * @returns {boolean}
     */
    SimpleTypeName.prototype.isArrayType = function () {
        return this._isArrayType;
    };
    
    return SimpleTypeName;
});