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
define(['lodash', './expression'], function (_, Expression) {

    var TypeCastExpression = function (args) {
        this._name = _.get(args, 'name', 'newTypeCastName');
        this._pkgName = _.get(args, 'pkgName', 'newPkgName');
        this._pkgPath = _.get(args, 'pkgPath', 'newPkgPath');
        Expression.call(this, 'TypeCastExpression');
    };

    TypeCastExpression.prototype = Object.create(Expression.prototype);
    TypeCastExpression.prototype.constructor = TypeCastExpression;

    /**
     * set the name of the Type Cast Expression
     * @param name
     * @param options
     */
    TypeCastExpression.prototype.setName = function (name, options) {
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
    TypeCastExpression.prototype.getName = function () {
        return this._name;
    };

    /**
     * set the Pkg Name of the Type Cast Expression
     * @param pkgName
     * @param options
     */
    TypeCastExpression.prototype.setPkgName = function (pkgName, options) {
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
    TypeCastExpression.prototype.getPkgName = function () {
        return this._pkgName;
    };

    /**
     * set the pkg path of the Type Cast Expression
     * @param pkgPath
     * @param options
     */
    TypeCastExpression.prototype.setPkgPath = function (pkgPath, options) {
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
    TypeCastExpression.prototype.getPkgPath = function () {
        return this._pkgPath;
    };

    TypeCastExpression.prototype.initFromJson = function (jsonNode) {
        var self = this;
        this.setName(jsonNode.target_type, {doSilently: true});

        _.each(jsonNode.children, function (childNode) {
            var child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    };
    
    return TypeCastExpression;
});