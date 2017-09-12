import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import View from './../../view/view';
import FileTree from './../../view/FileTree';
import { VIEWS, COMMANDS } from './../constants';

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
    constructor(props) {
        super(props);
        this.onOpen = this.onOpen.bind(this);
        this.onClickOpenProgramDir = this.onClickOpenProgramDir.bind(this);
    }

    /**
     * On double click on tree node
     * @param {Object} node tree node
     */
    onOpen(node) {
        if (node.type === 'file') {
            this.props.workspaceManager.openFile(node.id);
        }
    }

    /**
     * On click open program dir
     */
    onClickOpenProgramDir() {
        const { command: { dispatch } } = this.props.workspaceManager.appContext;
        dispatch(COMMANDS.SHOW_FOLDER_OPEN_WIZARD);
    }

    /**
     * @inheritdoc
     */
    render() {
        const trees = [];
        const { openedFolders } = this.props.workspaceManager;
        openedFolders.forEach((folder) => {
            trees.push((
                <FileTree root={folder} key={folder} onOpen={this.onOpen} />
            ));
        });
        return (
            <div className="workspace-explorer">
                {_.isEmpty(trees) &&
                    <div className="open-folder-btn-wrapper" onClick={this.onClickOpenProgramDir} > 
                        <span className="open-folder-button">
                            <i className="fw fw-folder-open"></i>Open Program Directory
                        </span>
                    </div>
                }
                {trees}
            </div>
        );
    }
}

WorkspaceExplorer.propTypes = {
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

WorkspaceExplorer.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default WorkspaceExplorer;
