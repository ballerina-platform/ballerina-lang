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

import EventChannel from 'event_channel';
import { EVENTS } from './../constants';

/**
 * Class to represent a custom editor tab
 */
class CustomEditor extends EventChannel {

    /**
     * Creates a custom editor
     * @param {String} id 
     * @param {String} title 
     * @param {String} icon 
     * @param {Object} component 
     * @param {Function} propsProvider
     * @param {Function} additionalProps
     * @param {string} customTitleClass
     */
    constructor(id, title, icon, component, propsProvider, additionalProps, customTitleClass) {
        super();
        this._id = id;
        this._title = title;
        this._icon = icon;
        this._component = component;
        this._propsProvider = propsProvider;
        this._additionalProps = additionalProps;
        this._customTitleClass = customTitleClass;
        this._props = {};
    }

    get id() {
        return this._id;
    }

    get title() {
        return this._title;
    }

    get icon() {
        return this._icon;
    }

    get component() {
        return this._component;
    }

    get propsProvider() {
        return this._propsProvider;
    }

    set additionalProps(additionalProps) {
        this._additionalProps = additionalProps;
    }

    get additionalProps() {
        return this._additionalProps;
    }

    get customTitleClass() {
        return this._customTitleClass;
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

export default CustomEditor;
