import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { getFSRoots, listFiles } from 'api-client/api-client';
import TreeView from 'react-treeview';
import 'react-treeview/react-treeview.css';
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
            });
    }

    /**
     * On Node Toggole
     * @param {Object} node node object
     * @param {Boolean} collapsed collapsed state
     */
    onToggle(node, collapsed) {
        node.active = true;
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
                        this.props.onSelect(node);
                    });
            } else {
                this.props.onSelect(node);
            }
        } else {
            this.props.onSelect(node);
        }
        this.forceUpdate();
    }

    /**
     * @inheritdoc
     */
    render() {
        const renderNode = (node) => {
            node.collapsed = _.isNil(node.collapsed) ? true : node.collapsed;
            const label = (
                <span
                    className="node"
                    onClick={() => this.onToggle(node, !node.collapsed)}
                >
                    {node.label}
                </span>
            );
            return (
                <TreeView
                    nodeLabel={label}
                    key={node.id}
                    collapsed={node.collapsed}
                    onClick={() => this.onToggle(node, !node.collapsed)}
                >
                    {
                        node.children
                            && _.isArray(node.children)
                            && node.children.map(renderNode)
                    }
                </TreeView>
            );
        };
        return (
            <div className="file-tree">
                {this.state.data.map(renderNode)}
            </div>
        );
    }
}

FileTree.propTypes = {
    onOpen: PropTypes.func,
    onSelect: PropTypes.func,
    root: PropTypes.string,
};

FileTree.defaultProps = {
    onOpen: () => {},
    onSelect: () => {},
    root: FS_ROOT,
};

export default FileTree;
