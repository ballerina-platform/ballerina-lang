import React from 'react';
import PropTypes from 'prop-types';
import View from 'core/view/view';
import { VIEWS } from './../constants';

/**
 * Debugger Console View
 */
class DebuggerConsole extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.DEBUGGER_CONSOLE;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                Debugger Console
            </div>
        );
    }
}

export default DebuggerConsole;
