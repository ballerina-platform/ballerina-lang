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
     * @param {string} customTitleClass
     */
    constructor(id, title, icon, component, propsProvider, customTitleClass) {
        super();
        this._id = id;
        this._title = title;
        this._icon = icon;
        this._component = component;
        this._propsProvider = propsProvider;
        this._customTitleClass = customTitleClass;
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

    get customTitleClass() {
        return this._customTitleClass;
    }
}

export default CustomEditor;
