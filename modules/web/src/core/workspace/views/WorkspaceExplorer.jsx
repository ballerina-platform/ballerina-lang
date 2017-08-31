import React from 'react';
import PropTypes from 'prop-types';
import View from './../../view/view';
import FileTree from './../../view/FileTree';
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
                <FileTree />
            </div>
        );
    }
}

WorkspaceExplorer.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default WorkspaceExplorer;
