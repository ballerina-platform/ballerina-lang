import React from 'react';
import PropTypes from 'prop-types';
import View from 'core/view/view';
import { VIEWS } from './../constants';

/**
 * Debugger
 */
class DebuggerPanel extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.DEBUGGER_PANEL;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                Debugger
            </div>
        );
    }
}

export default DebuggerPanel;
