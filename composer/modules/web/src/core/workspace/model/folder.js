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
 *
 */

import _ from 'lodash';
import { getPathSeperator } from 'api-client/api-client';

/**
 * Represents a Folder opened in workspace
 */
class Folder {

    /**
     * Folder Constructor
     * @param {Object} args Folder Details
     */
    constructor({ fullPath, children, properties }) {
        this._parent = '';
        this._name = '';
        this.fullPath = fullPath || `temp${getPathSeperator()}untitled`;
        this._children = children || [];
        this._props = properties || {};
    }

    /**
     * Returns fullPath
     */
    get fullPath() {
        return this._fullPath;
    }

    /**
     * Sets fullPath
     */
    set fullPath(fullPath) {
        this._fullPath = fullPath;
        const pathSegments = fullPath.split(getPathSeperator());
        this._name = fullPath.endsWith(getPathSeperator()) ? _.nth(pathSegments, pathSegments.length - 2)
                            : _.last(pathSegments);
        this._parent = _.dropRight(fullPath.split(getPathSeperator()), 1).join(getPathSeperator());
    }

    /**
     * Returns parent
     */
    get parent() {
        return this._parent;
    }

    /**
     * Returns name
     */
    get name() {
        return this._name;
    }


    /**
     * Returns children
     */
    get children() {
        return this._children;
    }

    /**
     * Sets children
     */
    set children(children) {
        this._children = children;
    }

    /**
     * Gets custom properties of the folder.
     *
     */
    get properties() {
        return this._props;
    }

    /**
     * Get the value of given property.
     *
     * @param {string} propertyName
     */
    getProperty(propertyName) {
        return this._props[propertyName];
    }

    /**
     * Set the value of given property.
     *
     * @param {string} propertyName
     * @param {any} propertyValue
     */
    setProperty(propertyName, propertyValue) {
        this._props[propertyName] = propertyValue;
    }


}

export default Folder;
