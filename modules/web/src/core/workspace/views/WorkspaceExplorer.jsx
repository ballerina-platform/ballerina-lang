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

export default WorkspaceExplorer;
