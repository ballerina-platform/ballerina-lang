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
import _ from 'lodash';
import PropTypes from 'prop-types';
import { getFSRoots, listFiles } from 'api-client/api-client';
import TreeNode from './TreeNode';

// A symbol to represent file system root
const FS_ROOT = '#';

/**
 * File Tree
 */
class FileTree extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.state = {
            data: [],
            activeNode: undefined,
        };
        this.onToggle = this.onToggle.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        this.loadData();
    }

    /**
     * @inheritdoc
     */
    componentWillReceiveProps(newProps) {
        if (this.props.activeKey !== newProps.activeKey) {
            this.loadGivenPath(newProps.activeKey);
        }
    }

    /**
     * On Node Toggole
     * @param {Object} node node object
     * @param {Boolean} collapsed collapsed state
     */
    onToggle(node, collapsed) {
        const { activeNode } = this.state;
        if (activeNode) {
            activeNode.active = false;
        }
        node.active = true;
        this.props.onSelect(node);
        if (node.children) {
            node.collapsed = collapsed;
            if (_.isEmpty(node.children)) {
                this.loadNodeChildren(node);
            }
        }
        this.setState({
            activeNode: node,
        });
    }

    /**
     * Load tree data
     */
    loadData() {
        const { extensions, root, onLoadData } = this.props;
        const loadExts = extensions || this.context.editor.getSupportedExtensions();
        const loadData = root === FS_ROOT ? getFSRoots(loadExts) : listFiles(root, loadExts);
        loadData
            .then((tree) => {
                const data = tree;
                this.setState({
                    data,
                });
                onLoadData(data);
                if (!_.isNil(this.props.activeKey)) {
                    this.loadActiveNode();
                }
            });
    }

    /**
     * Load and activate the given active key in tree
     */
    loadActiveNode() {
        const { activeKey } = this.props;
        this.loadGivenPath(activeKey);
    }

    /**
     * Recursively load given path
     *
     * @param {Node} root root Node to load from
     * @param {String} path Path to load
     */
    loadGivenPath(path, root) {
        if (_.isNil(path)) {
            return;
        }
        let resolveNodeChildren = Promise.resolve(this.state.data);
        if (root) {
            root.collapsed = false;
            if (_.isBoolean(root.children)) {
                if (root.children) {
                    resolveNodeChildren = this.loadNodeChildren(root)
                                            .then(loadedNode => loadedNode.children);
                } else {
                    resolveNodeChildren = Promise.resolve([]);
                }
            } else if (_.isArray(root.children)) {
                resolveNodeChildren = Promise.resolve(root.children);
            }
        }
        const resolveTargetChild = resolveNodeChildren
            .then((children) => {
                if (_.isArray(children)) {
                    return _.find(children, child => path.startsWith(child.id));
                } else {
                    return null;
                }
            });
        resolveTargetChild
            .then((targetChild) => {
                if (targetChild) {
                    // this is the target
                    if (path === targetChild.id) {
                        this.onToggle(targetChild, false);
                    } else if (targetChild.children) {
                        this.loadGivenPath(path, targetChild);
                    }
                }
            });
    }

    /**
     * Load children of given node
     *
     * @param {Object} node Tree Node
     */
    loadNodeChildren(node) {
        node.loading = true;
        delete node.children;
        this.forceUpdate();
        const loadExts = this.props.extensions || this.context.editor.getSupportedExtensions();
        return listFiles(node.id, loadExts)
                .then((data) => {
                    node.loading = false;
                    if (_.isEmpty(data)) {
                        node.children = false;
                        node.collapsed = false;
                    } else {
                        node.children = data;
                    }
                    return node;
                })
                .then(() => {
                    this.forceUpdate();
                    return node;
                });
    }

    /**
     * @inheritdoc
     */
    render() {
        const files = _.filter(this.state.data, ['type', 'file']);
        const folders = _.filter(this.state.data, ['type', 'folder']);
        const renderNode = (node, parentNode) => {
            if (_.isNil(node.collapsed)) {
                node.collapsed = true;
            }
            if (_.isNil(node.active)) {
                node.active = false;
            }
            if (_.isNil(node.loading)) {
                node.loading = false;
            }
            return (
                <TreeNode
                    node={node}
                    key={node.id}
                    onClick={() => this.onToggle(node, !node.collapsed)}
                    onDoubleClick={this.props.onOpen}
                    enableContextMenu={this.props.enableContextMenu}
                    onNodeUpdate={(targetNode) => {
                        this.forceUpdate();
                    }}
                    onNodeRefresh={(targetNode) => {
                        if (_.isNil(targetNode)) {
                            this.loadData();
                        } else {
                            this.loadNodeChildren(targetNode);
                        }
                    }}
                    onNodeDelete={(targetNode) => {
                        if (!_.isNil(parentNode)) {
                            _.remove(parentNode.children, ['id', targetNode.id]);
                        } else {
                            _.remove(this.state.data, ['id', targetNode.id]);
                        }
                        this.forceUpdate();
                    }}
                    parentNode={parentNode}
                    panelResizeInProgress={this.props.panelResizeInProgress}
                >
                    {
                        node.children
                            && _.isArray(node.children)
                            && node.children.map((childNode) => {
                                return renderNode(childNode, node);
                            })
                    }
                </TreeNode>
            );
        };
        return (
            <div className='file-tree'>
                {folders.map((childNode) => {
                    return renderNode(childNode, undefined);
                })}
                {files.map((childNode) => {
                    return renderNode(childNode, undefined);
                })}
            </div>
        );
    }
}

FileTree.propTypes = {
    activeKey: PropTypes.string,
    panelResizeInProgress: PropTypes.bool,
    enableContextMenu: PropTypes.bool,
    onLoadData: PropTypes.func,
    onOpen: PropTypes.func,
    onSelect: PropTypes.func,
    root: PropTypes.string,
    extensions: PropTypes.arrayOf(PropTypes.string),
};

FileTree.defaultProps = {
    activeKey: undefined,
    panelResizeInProgress: false,
    enableContextMenu: false,
    onLoadData: () => {},
    onOpen: () => {},
    onSelect: () => {},
    root: FS_ROOT,
    isDOMElementVisible: () => false,
    extensions: undefined,
};

FileTree.contextTypes = {
    editor: PropTypes.shape({
        getSupportedExtensions: PropTypes.func.isRequired,
    }).isRequired,
};

export default FileTree;
