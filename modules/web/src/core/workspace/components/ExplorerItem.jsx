import React from 'react';
import classnames from 'classnames';
import _ from 'lodash';
import { Collapse } from 'react-bootstrap';
import { getPathSeperator } from 'api-client/api-client';
import PropTypes from 'prop-types';
import './styles.scss';

import FileTree from './../../view/FileTree';

/**
 * Represents a root item workspace explorer
 */
class ExplorerItem extends React.Component {
    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.state = { open: false };
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
        const label = _.last(this.props.folderPath.split(getPathSeperator()));
        return (
            <div className="explorer-item">
                <div
                    className="root unseletable-content"
                    onClick={() => {
                        this.setState({ open: !this.state.open });
                        // un-select child nodes when clicked on root
                        this.props.onSelect(undefined);
                    }
                    }
                >
                    <i className={classnames('fw', 'fw-start', 'arrow', { open: this.state.open })} />
                    <i className="fw fw-folder icon" />
                    <span className="root-label">{label}</span>
                </div>
                <Collapse in={this.state.open}>
                    <div className="file-tree">
                        <FileTree
                            root={this.props.folderPath}
                            onOpen={this.onOpen}
                            onSelect={this.props.onSelect}
                        />
                    </div>
                </Collapse>
            </div>
        );
    }
}

ExplorerItem.propTypes = {
    onSelect: PropTypes.func,
    folderPath: PropTypes.string.isRequired,
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

ExplorerItem.defaultProps = {
    onSelect: () => {},
};

export default ExplorerItem;
