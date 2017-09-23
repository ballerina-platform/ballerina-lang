import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import classnames from 'classnames';
import ContextMenuTrigger from './../context-menu/ContextMenuTrigger';
import { getContextMenuItems } from './menu';

export const EDIT_TYPES = {
    NEW: 'new',
    RENAME: 'rename',
};

/**
 * Class to represent a tree node
 */
class TreeNode extends React.Component {

    /**
     * @inheritdoc
     */
    constructor(...args) {
        super(...args);
        this.state = {
            inputValue: this.props.node.label,
        };
        this.nameInput = undefined;
        this.onEditName = this.onEditName.bind(this);
        this.onEditComplete = this.onEditComplete.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        if (!_.isNil(this.nameInput)) {
            this.nameInput.focus();
        }
    }

    /**
     * Upon name modifications
     */
    onEditName(e) {
        this.setState({
            inputValue: e.target.value,
        });
    }

    /**
     * Upon name modification completion
     */
    onEditComplete() {
        const { node, node: { editType }, onNodeDelete } = this.props;
        if (_.isEmpty(this.state.inputValue) && editType === EDIT_TYPES.NEW) {
            onNodeDelete(node);
        } else {
            node.label = this.state.inputValue;
            node.enableEdit = false;
            this.forceUpdate();
            // FIX ME: Implement logic to rename file/folder  or to create file/folder
            // using BE services along with error handling
        }
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            node,
            node: {
                active,
                collapsed,
                enableEdit = false,
                editType = EDIT_TYPES.NEW,
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
                onClick={() => {
                    if (!enableEdit) {
                        onClick(node);
                    }
                }}
                onDoubleClick={() => {
                    if (!enableEdit) {
                        onDoubleClick(node);
                    }
                }}
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
                {enableEdit && <div className="tree-node-focus-highlighter" onClick={this.onEditComplete} />}
                {enableEdit &&
                    <input
                        type="text"
                        className="tree-node-name-input"
                        value={this.state.value}
                        onChange={this.onEditName}
                        onBlur={this.onEditComplete}
                        ref={(nameInput) => {
                            this.nameInput = nameInput;
                        }}
                    />
                }
                {!enableEdit &&
                    <span className="tree-node-label" >
                        {label}
                    </span>
                }
            </div>
        );
        return (
            <div
                className={classnames('tree-node', 'unseletable-content', {
                    collapsed: node.loading || collapsed, empty: !node.children }
                )}
            >
                {this.props.enableContextMenu && !enableEdit &&
                <ContextMenuTrigger
                    id={node.id}
                    menu={getContextMenuItems(node, this.context.command, (targetNode) => {
                        this.props.onNodeUpdate(targetNode);
                    })}
                >
                    {treeNodeHeader}
                </ContextMenuTrigger>
                }
                {(!this.props.enableContextMenu || enableEdit) && treeNodeHeader}
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
        enableEdit: PropTypes.bool,

    }).isRequired,
    onNodeUpdate: PropTypes.func,
    onNodeDelete: PropTypes.func,
    enableContextMenu: PropTypes.bool,
    children: PropTypes.node,
    onClick: PropTypes.func,
    onDoubleClick: PropTypes.func,
};

TreeNode.defaultProps = {
    enableContextMenu: false,
    onNodeDelete: () => {},
    onNodeUpdate: () => {},
    onClick: () => {},
    onDoubleClick: () => {},
};

TreeNode.contextTypes = {
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }),
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }),
};

export default TreeNode;
