import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import ContextMenuTrigger from './../context-menu/ContextMenuTrigger';
import { getContextMenuItems } from './menu';

/**
 * Class to represent a tree node
 */
class TreeNode extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        const {
            node,
            node: {
                active,
                collapsed,
                type,
                label,
            },
            onClick,
            onDoubleClick,
            children,
        } = this.props;

        const treeNodeHeader = (
            <div
                className={classnames('tree-node-header', { active })}
                onClick={() => { onClick(node); }}
                onDoubleClick={() => { onDoubleClick(node); }}
            >
                <div className="tree-node-highlight-row" />
                {!node.loading && <div className="tree-node-arrow" />}
                {node.loading && <i className="tree-node-loading fw fw-loader4 fw-spin" />}
                <i
                    className={
                        classnames(
                            'tree-node-icon',
                            'fw',
                            { 'fw-folder': type === 'folder' },
                            { 'fw-document': type === 'file' }
                        )
                    }
                />
                <span className="tree-node-label" >
                    {label}
                </span>
            </div>
        );
        return (
            <div
                className={classnames('tree-node', 'unseletable-content', {
                    collapsed: node.loading || collapsed, empty: !node.children }
                )}
            >
                {this.props.enableContextMenu &&
                <ContextMenuTrigger
                    id={node.id}
                    menu={getContextMenuItems(node, this.context.command)}
                >
                    {treeNodeHeader}
                </ContextMenuTrigger>
                }
                {!this.props.enableContextMenu && treeNodeHeader}
                <div className="tree-node-children">
                    {collapsed ? null : children}
                </div>
            </div>
        );
    }

}

TreeNode.propTypes = {
    node: PropTypes.shape({
        collapsed: PropTypes.bool.isRequired,
        type: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,

    }).isRequired,
    enableContextMenu: PropTypes.bool,
    children: PropTypes.node,
    onClick: PropTypes.func,
    onDoubleClick: PropTypes.func,
};

TreeNode.defaultProps = {
    enableContextMenu: false,
    onClick: () => {},
    onDoubleClick: () => {},
};

TreeNode.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
};

export default TreeNode;
