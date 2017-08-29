import React from 'react';
import PropTypes from 'prop-types';
import Tree, { TreeNode } from 'rc-tree';
import 'rc-tree/assets/index.css';
import {  } from 'api-client/api-client';

/**
 * File Tree
 */
class FileTree extends React.Component {

    /**
     * Load data asynchronusly
     * @param {String} treeNode Node to load data
     */
    onLoadData(treeNode) {
        return new Promise((resolve, reject) => {
            
        });
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="file-tree">
                <Tree
                    showLine="true"
                    defaultExpandAll={false}
                    defaultExpandedKeys={['p1']}
                    loadData={this.onLoadData}
                >
                    <TreeNode title="parent 1" key="p1">
                        <TreeNode key="p10" title="leaf" />
                        <TreeNode title="parent 1-1" key="p11">
                            <TreeNode title="parent 2-1" key="p21">
                                <TreeNode title="leaf" />
                                <TreeNode title="leaf" />
                            </TreeNode>
                            <TreeNode key="p22" title="leaf" />
                        </TreeNode>
                    </TreeNode>
                </Tree>
            </div>
        );
    }
}

FileTree.propTypes = {
    root: PropTypes.string.isRequired,
};

export default FileTree;
