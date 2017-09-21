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
        const isFSRoot = this.props.root === FS_ROOT;
        const loadData = isFSRoot ? getFSRoots() : listFiles(this.props.root);
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
                node.loading = true;
                listFiles(node.id, ['.bal'])
                    .then((data) => {
                        node.loading = false;
                        if (_.isEmpty(data)) {
                            delete node.children;
                            node.collapsed = false;
                        } else {
                            node.children = data;
                        }
                        this.forceUpdate();
                    });
            }
        }
        this.forceUpdate();
    }

    /**
     * @inheritdoc
     */
    render() {
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
                    onNodeUpdate={() => {
                        this.forceUpdate();
                    }}
                    onNodeDelete={(targetNode) => {
                        if (!_.isNil(parentNode)) {
                            _.remove(parentNode.children, ['id', targetNode.id]);
                        } else {
                            _.remove(this.state.data, ['id', targetNode.id]);
                        }
                        this.forceUpdate();
                    }}
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
                {this.state.data.map((childNode) => {
                    return renderNode(childNode, undefined);
                })}
            </div>
        );
    }
}

FileTree.propTypes = {
    enableContextMenu: PropTypes.bool,
    onLoadData: PropTypes.func,
    onOpen: PropTypes.func,
    onSelect: PropTypes.func,
    root: PropTypes.string,
};

FileTree.defaultProps = {
    enableContextMenu: false,
    onLoadData: () => {},
    onOpen: () => {},
    onSelect: () => {},
    root: FS_ROOT,
};

export default FileTree;
