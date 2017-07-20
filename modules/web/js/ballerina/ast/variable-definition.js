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

class VariableDefinition extends ASTNode {
    constructor(args) {
        super('VariableDefinition');
        this._name = _.get(args, 'name', 'newIdentifier');
        this._typeName = _.get(args, 'typeName', 'newTypeName');
        this._pkgName = _.get(args, 'pkgName');
        this._isPublic = _.get(args, 'isPublic', false);
        this._isArray = _.get(args, 'isArray', false);
        this._dimensions = _.get(args, 'dimensions', 0);
        this._typeConstraint = _.get(args, 'typeConstraint');
    }

    /**
     * set the name of the variable def
     * @param name
     * @param options
     */
    setName(name, options) {
        if (!_.isNil(name)) {
            this.setAttribute('_name', name, options);
        } else {
            log.error('Invalid Name [' + name + '] Provided');
            throw 'Invalid Name [' + name + '] Provided';
        }
    }

    /**
     * set the type of the variable def
     * @param typeName
     * @param options
     */
    setTypeName(typeName, options) {
        if (!_.isNil(typeName)) {
            this.setAttribute('_typeName', typeName, options);
        } else {
            log.error('Invalid Type Name [' + typeName + '] Provided');
            throw 'Invalid Type Name [' + typeName + '] Provided';
        }
    }

    /**
     * returns the  name
     * @returns {*}
     */
    getName() {
        return this._name;
    }

    /**
     * returns the  Type Name
     * @returns {*}
     */
    getTypeName() {
        return this._typeName;
    }

    /**
     * set the pkg path of the variable def
     * @param pkgName
     * @param options
     */
    setPkgName(pkgName, options) {
        if (!_.isNil(pkgName)) {
            this.setAttribute('_pkgName', pkgName, options);
        }
    }

    /**
     * returns the  Pkg path
     * @returns {string}
     */
    getPkgName() {
        return this._pkgName;
    }

    /**
     * set whether the variable def is public
     * @param isPublic
     * @param options
     */
    setArrayType(isPublic, options) {
        if (!_.isNil(isPublic)) {
            this.setAttribute('_isPublic', isPublic, options);
        } else {
            log.error('Invalid array type [' + isPublic + '] Provided');
            throw 'Invalid array type [' + isPublic + '] Provided';
        }
    }

    /**
     * returns the  isPublic
     * @returns {boolean}
     */
    isPublic() {
        return this._isPublic;
    }

    /**
     * Gets the variable definition as a string.
     * @return {string} - Variable definition as string.
     */
    getVariableDefinitionAsString() {
        return this._typeName + ' ' + this._name + ';';
    }

    initFromJson(jsonNode) {
        this.setName(jsonNode.variable_name, { doSilently: true });
        this.setTypeName(jsonNode.variable_type, { doSilently: true });
        this.setPkgName(jsonNode.package_name, { doSilently: true });
        this.setIsArray(jsonNode.is_array_type);
        this.setDimensions(jsonNode.dimensions);
        this.setTypeConstraint(jsonNode.type_constraint);

        _.each(jsonNode.children, (childNode) => {
            const child = this.getFactory().createFromJson(childNode);
            this.addChild(child);
            child.initFromJson(childNode);
        });
    }

    /**
     * Get the dimensions
     * @return {number} - number of dimensions
     */
    getDimensions() {
        return this._dimensions;
    }

    /**
     * Set the dimensions
     * @param {number} - number of dimensions
     */
    setDimensions(dimensions) {
        this._dimensions = dimensions;
    }

    /**
     * Get is array
     * @return {boolean} - whether is array or not
     */
    isArray() {
        return this._isArray;
    }

    /**
     * Set is array
     * @param {boolean} - whether is array or not
     */
    setIsArray(isArray) {
        this._isArray = isArray;
    }

    /**
     * Sets type constraint
     * @param {string} typeConstraint type constraint
     * @memberof VariableDefinition
     */
    setTypeConstraint(typeConstraint) {
        this._typeConstraint = typeConstraint;
    }

    /**
     * Gets type constraint
     * @returns {string} type constraint
     * @memberof VariableDefinition
     */
    getTypeConstraint() {
        return this._typeConstraint;
    }
}

export default VariableDefinition;
