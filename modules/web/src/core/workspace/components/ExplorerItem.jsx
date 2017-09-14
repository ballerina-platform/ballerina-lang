import React from 'react';
import classnames from 'classnames';
import _ from 'lodash';
import { Collapse } from 'react-bootstrap';
import { getPathSeperator } from 'api-client/api-client';
import PropTypes from 'prop-types';
import './styles.scss';

import FileTree from './../../view/FileTree';
import { withContextMenu } from './../../view/utils';

const TREE_NODE_TYPE = 'root';

/**
 * Represents a root item workspace explorer
 */
class ExplorerItem extends React.Component {
    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.state = {
            open: false,
            node: {
                id: this.props.folderPath,
                type: TREE_NODE_TYPE,
                active: false,
                label: _.last(this.props.folderPath.split(getPathSeperator())),
            },
        };
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
        return (
            <div className="explorer-item">
                <div
                    className={classnames('root', 'unseletable-content', { active: this.state.node.active })}
                    onClick={() => {
                        this.state.node.active = true;
                        this.setState({ open: !this.state.open });
                        // un-select child nodes when clicked on root
                        this.props.onSelect(this.state.node);
                    }
                    }
                >
                    <i className={classnames('fw', 'fw-start', 'arrow', { open: this.state.open })} />
                    <i className="fw fw-folder icon" />
                    <span className="root-label">{this.state.node.label}</span>
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
