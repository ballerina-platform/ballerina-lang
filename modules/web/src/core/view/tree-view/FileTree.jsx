import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { getFSRoots, listFiles } from 'api-client/api-client';
import TreeNode from './TreeNode';
import './file-tree.scss';

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
     * On Node Toggole
     * @param {Object} node node object
     * @param {Boolean} collapsed collapsed state
     */
    onToggle(node, collapsed) {
        node.active = true;
        this.props.onSelect(node);
        if (node.children) {
            node.collapsed = collapsed;
            if (_.isEmpty(node.children)) {
                this.loadNodeChildren(node);
            }
        }
        this.forceUpdate();
    }

    /**
     * Load tree data
     */
    loadData() {
        const { extensions, root, onLoadData, activeKey } = this.props;
        const loadData = root === FS_ROOT ? getFSRoots(extensions) : listFiles(root, extensions);
        loadData
            .then((tree) => {
                const data = tree;
                this.setState({
                    data,
                });
                onLoadData(data);
                if (!_.isNil(activeKey)) {
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
    loadGivenPath(path = '', root) {
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
                        targetChild.active = true;
                        this.forceUpdate();
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
        return listFiles(node.id, this.props.extensions)
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
            <div className="file-tree">
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
    extensions: PropTypes.arrayOf(PropTypes.string).isRequired,
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
    extensions: ['bal'],
};

export default FileTree;
