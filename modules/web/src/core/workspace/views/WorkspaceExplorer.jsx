import React from 'react';
import PropTypes from 'prop-types';
import View from './../../view/view';
import { VIEWS } from './../constants';

/**
 * Woprkspace Explorer
 */
class WorkspaceExplorer extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.EXPLORER;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                Explorer
            </div>
        );
    }
}

export default WorkspaceExplorer;
