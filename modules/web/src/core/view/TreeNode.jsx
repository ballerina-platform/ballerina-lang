import React from 'react';
import PropTypes from 'prop-types';
import classnames from 'classnames';

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
        return (
            <div
                className={classnames('tree-node', 'unseletable-content', {
                    collapsed, empty: !node.children }
                )}
            >
                <div
                    className={classnames('tree-node-header', { active })}
                    onClick={() => { onClick(node); }}
                    onDoubleClick={() => { onDoubleClick(node); }}
                >
                    <div className="tree-node-highlight-row" />
                    <div
                        className="tree-node-arrow"
                    />
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
    children: PropTypes.node,
    onClick: PropTypes.func,
    onDoubleClick: PropTypes.func,
};

TreeNode.defaultProps = {
    onClick: () => {},
    onDoubleClick: () => {},
};

export default TreeNode;
