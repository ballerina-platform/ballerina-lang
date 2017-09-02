import _ from 'lodash';
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
    constructor({ fullPath, path, name, extension, content, isPersisted, lastPersisted, isDirty }) {
        super();
        this._fullPath = fullPath || 'temp/untitled';
        this._path = path || 'temp';
        this._name = name || 'untitled';
        this._ext = extension || 'bal';
        this._content = content || '';
        this._isPersisted = !_.isNil(isPersisted) ? isPersisted : false;
        this._lastPersisted = lastPersisted || _.now();
        this._isDirty = !_.isNil(isDirty) ? isDirty : true;
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
    }

    /**
     * Returns content
     */
    get content() {
        return this._content;
    }

    /**
     * Set content of the file
     * @param {string} content new file content
     * @param {Object} changeEvt Originating change event
     *
     * @emits File#content-modified
     */
    setContent(content, changeEvt) {
        const oldContent = this._content;
        this._content = content;

        /**
         * Fired when a change is made to file content
         * @event File#content-modified
         *
         */
        const evt = {
            oldContent,
            content,
            changeEvt,
        };
        this.trigger(EVENTS.CONTENT_MODIFIED, evt);
        // if the new content is not equal to old content
        // set file dirty
        if (!_.isEqual(oldContent, content)) {
            this.isDirty = true;
        }
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
    }
}

export default File;
