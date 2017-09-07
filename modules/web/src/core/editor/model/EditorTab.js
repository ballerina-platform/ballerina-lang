import EventChannel from 'event_channel';
import { EVENTS } from './../constants';

/**
 * Class to represent an editor tab
 */
class EditorTab extends EventChannel {

    /**
     * Creates an editor tab
     * @param {File} file Target file
     * @param {Object} editor Editor Definition
     */
    constructor(file, editor) {
        super();
        this._file = file;
        this._editor = editor;
        this._customTitleClass = editor.tabTitleClass || '';
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
     * Returns editor definition
     */
    get editor() {
        return this._editor;
    }

    /**
     * Sets editor
     */
    set editor(editor) {
        this._editor = editor;
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

export default EditorTab;
