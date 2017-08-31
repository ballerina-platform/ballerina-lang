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
        const trees = [];
        this.props.folders.forEach((folder) => {
            trees.push((
                <FileTree root={folder} key={folder} />
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
    folders: PropTypes.arrayOf(PropTypes.string),
};

WorkspaceExplorer.defaultProps = {
    folders: [],
};

WorkspaceExplorer.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default WorkspaceExplorer;
