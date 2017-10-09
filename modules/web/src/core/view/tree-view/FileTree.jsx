import React from 'react';
import classnames from 'classnames';
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
                this.loadNodeChildren(node)
                    .then(() => {
                        this.forceUpdate();
                    });
            }
        }
        this.forceUpdate();
    }

    /**
     * Load tree data
     */
    loadData() {
        const extensions = this.props.extensions;
        const isFSRoot = this.props.root === FS_ROOT;
        const loadData = isFSRoot ? getFSRoots(extensions) : listFiles(this.props.root, extensions);
        loadData
            .then((tree) => {
                const data = tree;
                this.setState({
                    data,
                });
                this.props.onLoadData(data);
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
                            this.loadNodeChildren(targetNode)
                                .then(() => {
                                    this.forceUpdate();
                                });
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
                    isDOMElementVisible={this.props.isDOMElementVisible}
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
    panelResizeInProgress: PropTypes.bool,
    enableContextMenu: PropTypes.bool,
    onLoadData: PropTypes.func,
    onOpen: PropTypes.func,
    onSelect: PropTypes.func,
    root: PropTypes.string,
    isDOMElementVisible: PropTypes.func.isRequired,
    extensions: PropTypes.arrayOf(PropTypes.string).isRequired,
};

FileTree.defaultProps = {
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
