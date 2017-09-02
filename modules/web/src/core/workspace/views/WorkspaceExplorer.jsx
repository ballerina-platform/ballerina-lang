import React from 'react';
import PropTypes from 'prop-types';
import View from './../../view/view';
import FileTree from './../../view/FileTree';
import { VIEWS, EVENTS } from './../constants';

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
        props.workspaceManager.on(EVENTS.OPEN_FOLDER, () => {
            this.forceUpdate();
        });
        this.onOpen = this.onOpen.bind(this);
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
            <div>
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
