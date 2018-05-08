/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

import React from 'react';
import classnames from 'classnames';
import PropTypes from 'prop-types';
import ContextMenuTrigger from 'core/view/context-menu/ContextMenuTrigger';
import FileTree from 'core/view/tree-view/FileTree';
import { getContextMenuItems } from 'core/view/tree-view/menu';
import { COMMANDS } from './../constants';
import Folder from '../model/folder';
import './styles.scss';

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
            forceCollapse: false,
            node: {
                collapsed: true,
                id: this.props.folder.fullPath,
                type: TREE_NODE_TYPE,
                active: false,
                label: this.props.folder.name,
            },
        };
        this.fileTree = undefined;
        this.onOpen = this.onOpen.bind(this);
        this.onRemoveProjectFolderClick = this.onRemoveProjectFolderClick.bind(this);
        this.onRefreshProjectFolderClick = this.onRefreshProjectFolderClick.bind(this);
        this.refresh = this.refresh.bind(this);
        this.isDOMElementVisible = this.isDOMElementVisible.bind(this);
        this.refreshPathInExplorer = this.refreshPathInExplorer.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        const { command: { on } } = this.context;
        on(COMMANDS.REFRESH_EXPLORER, this.refresh);
        on(COMMANDS.REFRESH_PATH_IN_EXPLORER, this.refreshPathInExplorer);
    }

    /**
     * @inheritdoc
     */
    componentWillReceiveProps(newProps) {
        if (newProps.activeKey) {
            const node = this.state.node;
            node.collapsed = this.state.forceCollapse;
            this.setState({
                node,
            });
        }
    }

    /**
     * @inheritdoc
     */
    componentWillUnmount() {
        const { command: { off } } = this.context;
        off(COMMANDS.REFRESH_EXPLORER, this.refresh);
        off(COMMANDS.REFRESH_PATH_IN_EXPLORER, this.refreshPathInExplorer);
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
        this.props.workspaceManager.removeFolder(this.props.folder.fullPath);
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
     * On Refresh Project Folder
     */
    refreshPathInExplorer({ filePath }) {
        if (filePath.startsWith(this.props.folder.fullPath)) {
            this.refresh();
        }
    }

    /**
     * Checks whether the given html element is visible ATM
     * @param {HTMLElement} ref Refence to native node
     */
    isDOMElementVisible(ref) {
        if (!ref) {
            return false;
        }
        const { containerHeight, getScroller } = this.props;
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
            <div className='explorer-item'>
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
                            },
                            this.context)
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
                        data-placement='bottom'
                        data-toggle='tooltip'
                        title={this.state.node.id}
                        className={classnames('root', 'unseletable-content', { active: this.state.node.active })}
                        onClick={() => {
                            this.state.node.active = true;
                            this.state.node.collapsed = !this.state.node.collapsed;
                            this.state.forceCollapse = this.state.node.collapsed;
                            // un-select child nodes when clicked on root
                            this.props.onSelect(this.state.node);
                        }
                        }
                    >
                        <div className={classnames('arrow', { collapsed: this.state.node.collapsed })} />
                        <i className='fw fw-folder' />
                        <span className='root-label'>{this.state.node.label}</span>
                        <span className='root-actions'>
                            <i
                                className='fw fw-refresh2 action'
                                title='Refresh'
                                onClick={this.onRefreshProjectFolderClick}
                            />
                            <i
                                className='fw fw-close action'
                                title='Remove Project Directory'
                                onClick={this.onRemoveProjectFolderClick}
                            />
                        </span>
                    </div>
                </ContextMenuTrigger>
                <div className={classnames('file-tree', { collapsed: this.state.node.collapsed })}>
                    <FileTree
                        ref={(ref) => {
                            this.fileTree = ref;
                        }
                        }
                        enableContextMenu
                        onLoadData={(data) => {
                            this.state.node.children = data;
                        }}
                        root={this.props.folder.fullPath}
                        activeKey={this.props.activeKey}
                        onOpen={this.onOpen}
                        onSelect={this.props.onSelect}
                        panelResizeInProgress={this.props.panelResizeInProgress}
                    />
                </div>
            </div>
        );
    }
}

ExplorerItem.propTypes = {
    panelResizeInProgress: PropTypes.bool.isRequired,
    onSelect: PropTypes.func,
    activeKey: PropTypes.string,
    folder: PropTypes.instanceOf(Folder).isRequired,
    workspaceManager: PropTypes.objectOf(Object).isRequired,
};

ExplorerItem.defaultProps = {
    activeKey: '',
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
    editor: PropTypes.shape({
        isFileOpenedInEditor: PropTypes.func,
        getEditorByID: PropTypes.func,
        setActiveEditor: PropTypes.func,
        getActiveEditor: PropTypes.func,
    }).isRequired,
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
    }).isRequired,
};

export default ExplorerItem;
