import _ from 'lodash';
import uuid from 'uuid/v1';
import EventChannel from 'event_channel';
import { EVENTS } from './../constants';

/**
 * Represents a file opened in workspace
 */
class File extends EventChannel {

    /**
     * File Constructor
     * @param {Object} args File Details
     */
    constructor({ id, fullPath, path, name, packageName, extension, content, isPersisted, lastPersisted, isDirty }) {
        super();
        this._id = id || uuid();
        this._fullPath = fullPath || this._id;
        this._path = path || 'temp';
        this._name = name || 'untitled';
        this._packageName = packageName || '.';
        this._ext = extension || 'bal';
        this._content = content || '';
        this._isPersisted = !_.isNil(isPersisted) ? isPersisted : false;
        this._lastPersisted = lastPersisted || _.now();
        this._isDirty = !_.isNil(isDirty) ? isDirty : true;
        this._lastUpdate = _.now();
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
        this.trigger(EVENTS.FILE_UPDATED);
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
        this._fullPath = newPath;
        this.trigger(EVENTS.FILE_UPDATED);
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
        this.trigger(EVENTS.FILE_UPDATED);
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
        this.trigger(EVENTS.FILE_UPDATED);
    }

     /**
     * Returns package name
     */
    get packageName() {
        return this._packageName;
    }

    /**
     * Sets package name
     */
    set packageName(packageName) {
        this._packageName = packageName;
        this.trigger(EVENTS.FILE_UPDATED);
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
        };
        this.trigger(EVENTS.CONTENT_MODIFIED, evt);
        // if the new content is not equal to old content
        // set file dirty
        if (!_.isEqual(oldContent, newContent)) {
            this.isDirty = true;
        }
        this.trigger(EVENTS.FILE_UPDATED);
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
        this.trigger(EVENTS.FILE_UPDATED);
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
        this.trigger(EVENTS.FILE_UPDATED);
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
        this.trigger(EVENTS.FILE_UPDATED);
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
        this.trigger(EVENTS.FILE_UPDATED);
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
        this.trigger(EVENTS.FILE_UPDATED);
    }
}

export default File;
