import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import View from './../../view/view';
import { VIEWS, COMMANDS } from './../constants';
import ExplorerItem from './../components/ExplorerItem';

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
        this.onSelectNode = this.onSelectNode.bind(this);
        this.onClickOpenProgramDir = this.onClickOpenProgramDir.bind(this);
    }

    /**
     * On any node in any tree is selected.
     * Enforces a single selection across multiple trees.
     *
     */
    onSelectNode(node) {
        this.props.workspaceManager.onNodeSelectedInExplorer(node);
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
                <ExplorerItem
                    folderPath={folder}
                    key={folder}
                    workspaceManager={this.props.workspaceManager}
                    onSelect={this.onSelectNode}
                />
            ));
        });
        return (
            <div className="workspace-explorer">
                {_.isEmpty(trees) &&
                    <div className="open-folder-btn-wrapper" onClick={this.onClickOpenProgramDir} >
                        <span className="open-folder-button">
                            <i className="fw fw-folder-open" />Open Program Directory
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
