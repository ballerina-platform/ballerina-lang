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
        this.undoManager = new UndoManager();
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

}

export default Editor;
