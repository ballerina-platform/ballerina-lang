import React from 'react';
import classnames from 'classnames';
import _ from 'lodash';
import { Collapse } from 'react-bootstrap';
import { getPathSeperator } from 'api-client/api-client';
import PropTypes from 'prop-types';
import { COMMANDS } from './../constants';
import ContextMenuTrigger from './../../view/context-menu/ContextMenuTrigger';
import './styles.scss';

import FileTree from './../../view/tree-view/FileTree';
import { getContextMenuItems } from './../../view/tree-view/menu';

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
            disableToolTip: false,
            node: {
                collapsed: true,
                id: this.props.folderPath,
                type: TREE_NODE_TYPE,
                active: false,
                label: _.last(this.props.folderPath.split(getPathSeperator())),
            },
        };
        this.fileTree = undefined;
        this.onOpen = this.onOpen.bind(this);
        this.onRemoveProjectFolderClick = this.onRemoveProjectFolderClick.bind(this);
        this.onRefreshProjectFolderClick = this.onRefreshProjectFolderClick.bind(this);
        this.refresh = this.refresh.bind(this);
        this.isDOMElementVisible = this.isDOMElementVisible.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const { command: { on } } = this.context;
        on(COMMANDS.REFRESH_EXPLORER, this.refresh);
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        const { command: { off } } = this.context;
        off(COMMANDS.REFRESH_EXPLORER, this.refresh);
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
     * On Remove Project Folder
     */
    onRemoveProjectFolderClick(e) {
        this.props.workspaceManager.removeFolder(this.props.folderPath);
        e.stopPropagation();
        e.preventDefault();
    }

    /**
     * On Refresh Project Folder
     */
    onRefreshProjectFolderClick(e) {
        this.refresh();
        e.stopPropagation();
        e.preventDefault();
    }

    /**
     * Checks whether the given html element is visible ATM
     * @param {HTMLElement} ref Refence to native node
     */
    isDOMElementVisible(ref) {
        if (!ref) {
            return false;
        }
        const { containerHeight } = this.props;
        const { offsetParent, offsetTop, offsetLeft } = ref;
        // TODO verify that offsetParent is panel-content-scroll-container
        return offsetTop < containerHeight;
    }

    /**
     * Refresh file tree
     */
    refresh() {
        if (this.fileTree) {
            this.fileTree.loadData();
        }
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="explorer-item">
                <ContextMenuTrigger
                    id={this.state.node.id}
                    menu={getContextMenuItems(
                            this.state.node,
                            undefined,
                            this.context.command,
                            () => {
                                this.forceUpdate();
                            },
                            () => {
                                this.forceUpdate();
                            })
                        }
                    onShow={() => {
                        this.setState({
                            disableToolTip: true,
                        });
                    }}
                    onHide={() => {
                        this.setState({
                            disableToolTip: false,
                        });
                    }}
                >
                    <div
                        data-placement="bottom"
                        data-toggle="tooltip"
                        title={this.state.node.id}
                        className={classnames('root', 'unseletable-content', { active: this.state.node.active })}
                        onClick={() => {
                            this.state.node.active = true;
                            this.state.node.collapsed = !this.state.node.collapsed;
                            this.forceUpdate();
                            // un-select child nodes when clicked on root
                            this.props.onSelect(this.state.node);
                        }
                        }
                    >
                        <div className={classnames('arrow', { collapsed: this.state.node.collapsed })} />
                        <i className="fw fw-folder icon" />
                        <span className="root-label">{this.state.node.label}</span>
                        <span className="root-actions">
                            <i className="fw fw-refresh2 action" onClick={this.onRefreshProjectFolderClick} />
                            <i className="fw fw-close action" onClick={this.onRemoveProjectFolderClick} />
                        </span>
                    </div>
                </ContextMenuTrigger>
                <Collapse in={!this.state.node.collapsed}>
                    <div className="file-tree">
                        <FileTree
                            ref={(ref) => {
                                this.fileTree = ref;
                            }
                            }
                            enableContextMenu
                            onLoadData={(data) => {
                                this.state.node.children = data;
                            }}
                            root={this.props.folderPath}
                            onOpen={this.onOpen}
                            onSelect={this.props.onSelect}
                            panelResizeInProgress={this.props.panelResizeInProgress}
                            isDOMElementVisible={this.isDOMElementVisible}
                        />
                    </div>
                </Collapse>
            </div>
        );
    }
}

ExplorerItem.propTypes = {
    containerHeight: PropTypes.number.isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    onSelect: PropTypes.func,
    folderPath: PropTypes.string.isRequired,
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

ExplorerItem.defaultProps = {
    onSelect: () => {},
};

ExplorerItem.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
};

export default ExplorerItem;
