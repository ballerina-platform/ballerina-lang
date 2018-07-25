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
import uuid from 'uuid/v4';
import EventChannel from 'event_channel';
import { getPathSeperator } from 'api-client/api-client';
import { isClientOnWindows } from 'core/utils/client-info';
import { EVENTS } from './../constants';

/**
 * Represents a file opened in workspace
 */
class File extends EventChannel {

    /**
     * File Constructor
     * @param {Object} args File Details
     */
    constructor({ id, fullPath, path, name, extension, content, isPersisted, lastPersisted, isDirty, properties }) {
        super();
        // FIXME: Get a valid temp dir from backend to fix this properly for both cloud & desktop
        const tempRootDir = isClientOnWindows() ? 'c:\\' : '/';
        this._id = id || `${tempRootDir}temp${getPathSeperator()}${uuid()}${getPathSeperator()}untitled.bal`;
        this._fullPath = fullPath || this._id;
        this._path = path || 'temp';
        this._name = name || 'untitled';
        this._ext = extension || 'bal';
        this._content = content || '';
        this._isPersisted = !_.isNil(isPersisted) ? isPersisted : false;
        this._lastPersisted = lastPersisted || _.now();
        this._isDirty = !_.isNil(isDirty) ? isDirty : true;
        this._lastUpdated = _.now();
        this._props = properties || {};
    }

    /**
     * Returns id
     */
    get id() {
        return this._id;
    }

    /**
     * Sets id
     */
    set id(newID) {
        this._id = newID;
        this.trigger(EVENTS.FILE_UPDATED, this);
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
    set fullPath(newPath) {
        const oldPath = this._fullPath;
        this._fullPath = newPath;
        this.trigger(EVENTS.FILE_PATH_CHANGED, { oldPath, newPath });
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Returns path
     */
    get path() {
        return this._path;
    }

    /**
     * Sets path
     */
    set path(newPath) {
        this._path = newPath;
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Returns name
     */
    get name() {
        return this._name;
    }

    /**
     * Sets name
     */
    set name(name) {
        this._name = name;
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Returns content
     */
    get content() {
        return this._content;
    }

    /**
     * Set content of the file
     * @param {string} newContent new file content
     * @param {Object} originEvt Originating change event
     *
     * @emits File#content-modified
     */
    setContent(newContent, originEvt) {
        const oldContent = this._content;
        this._content = newContent;
        this.lastUpdated = _.now();

        /**
         * Fired when a change is made to file content
         * @event File#content-modified
         *
         */
        const evt = {
            oldContent,
            newContent,
            originEvt,
            file: this,
        };
        this.trigger(EVENTS.CONTENT_MODIFIED, evt);
        // if the new content is not equal to old content
        // set file dirty
        if (!_.isEqual(oldContent, newContent)) {
            this.isDirty = true;
        }
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Returns isPersisted
     */
    get isPersisted() {
        return this._isPersisted;
    }

    /**
     * Sets isPersisted
     */
    set isPersisted(isPersisted) {
        this._isPersisted = isPersisted;
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Returns lastPersisted
     */
    get lastPersisted() {
        return this._lastPersisted;
    }

    /**
     * Sets lastPersisted
     */
    set lastPersisted(lastPersisted) {
        this._lastPersisted = lastPersisted;
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Returns lastUpdated
     */
    get lastUpdated() {
        return this._lastUpdated;
    }

    /**
     * Sets lastUpdated
     */
    set lastUpdated(lastUpdated) {
        this._lastUpdated = lastUpdated;
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Returns isDirty
     */
    get isDirty() {
        return this._isDirty;
    }

    /**
     * Sets isDirty
     */
    set isDirty(isDirty) {
        this._isDirty = isDirty;
        this.trigger(EVENTS.DIRTY_STATE_CHANGE, isDirty);
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

     /**
     * Returns extension
     */
    get extension() {
        return this._ext;
    }

    /**
     * Sets extension
     */
    set extension(ext) {
        this._ext = ext;
        this.trigger(EVENTS.FILE_UPDATED, this);
    }

    /**
     * Gets custom properties of the given file.
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

    /**
     * Gives the URI of file.
     */
    toURI() {
        const pathSep = getPathSeperator();
        const path = !this.fullPath.startsWith(pathSep)
            ? pathSep + this.fullPath
            : this.fullPath;
        return `file://${path.replace(/\\/g, '/')}`;
    }

}

export default File;
