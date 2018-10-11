/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

/**
 * Class for the object field.
 */
class ObjectField {

    /**
     * Constructor for the object field.
     *
     * @param args arguments to fill the field class
     */
    constructor(args) {
        this._name = _.get(args, 'name', '');
        this._type = _.get(args, 'type', '');
        this._defaultValue = _.get(args, 'defaultValue', '');
        this._fullPackageName = _.get(args, 'packageName', '');
    }

    /**
     * Get the name.
     *
     * @param name name to be added
     */
    setName(name) {
        this._name = name;
    }

    /**
     * Get the name.
     *
     * @returns {string} name of the field
     */
    getName() {
        return this._name;
    }

    /**
     * Set the type.
     *
     * @param type type to be added
     */
    setType(type) {
        this._type = type;
    }

    /**
     * Get the type of the field.
     *
     * @returns {type} type of the field
     */
    getType() {
        return this._type;
    }

    /**
     * Set the default value.
     *
     * @param defaultValue default value for the field
     */
    setDefaultValue(defaultValue) {
        this._defaultValue = defaultValue;
    }

    /**
     * Get the default value.
     *
     * @returns {string} default value of the field
     */
    getDefaultValue() {
        return this._defaultValue;
    }

    /**
     * Set the full package name.
     *
     * @param packageName package name of the field
     */
    setFullPackageName(packageName) {
        this._fullPackageName = packageName;
    }

    /**
     * Get the full package name.
     *
     * @returns {string} full package name
     */
    getFullPackageName() {
        return this._fullPackageName;
    }
}

export default ObjectField;