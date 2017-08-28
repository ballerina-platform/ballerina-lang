import React from 'react';
import PropTypes from 'prop-types';
import Tree, { TreeNode } from 'rc-tree';
import 'rc-tree/assets/index.css';
import View from './../../view/view';
import { VIEWS } from './../constants';

/**
 * Woprkspace Explorer
 */
class WorkspaceExplorer extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEWS.EXPLORER;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
                <Tree
                    defaultExpandAll={false}
                    defaultExpandedKeys={['p1']}
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

export default WorkspaceExplorer;
