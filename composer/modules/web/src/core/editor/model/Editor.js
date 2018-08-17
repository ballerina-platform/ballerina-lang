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
import UndoManager from './../undo-manager/undo-manager';

/**
 * Class to represent an editor tab
 */
class Editor extends EventChannel {

    /**
     * Creates an editor tab
     * @param {File} file Target file
     * @param {Object} definition Editor Definition
     */
    constructor(file, definition) {
        super();
        this._file = file;
        this._definition = definition;
        this._customTitleClass = definition.tabTitleClass || '';
        this._undoManager = new UndoManager();
        this._props = {};
    }

    /**
     * Gets current undo manager
     *
     * @returns {UndoManager} current undo manager
     */
    get undoManager() {
        return this._undoManager;
    }

    /**
     * Sets undo manager
     *
     * @param {UndoManager} undoManager
     */
    set undoManager(undoManager) {
        this._undoManager = undoManager;
    }


    /**
     * Returns id
     */
    get id() {
        return this._file.fullPath;
    }

    /**
     * Returns file
     */
    get file() {
        return this._file;
    }

    /**
     * Sets file
     */
    set file(file) {
        this._file = file;
    }

    /**
     * Returns isDirty
     */
    get isDirty() {
        return this._file.isDirty;
    }

    /**
     * Returns editor definition
     */
    get definition() {
        return this._definition;
    }

    /**
     * Sets editor definition
     */
    set definition(definition) {
        this._definition = definition;
    }

    /**
     * Returns custom class for title
     */
    get customTitleClass() {
        return this._customTitleClass;
    }

    /**
     * Sets custom class for title
     */
    set customTitleClass(customTitleClass) {
        this._customTitleClass = customTitleClass;
        this.trigger(EVENTS.UPDATE_TAB_TITLE, this);
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

export default Editor;
