import React from 'react';
import log from 'log';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { getPathSeperator } from 'api-client/api-client';
import classnames from 'classnames';
import ContextMenuTrigger from './../context-menu/ContextMenuTrigger';
import { getContextMenuItems } from './menu';
import { exists, create, move } from './../../workspace/fs-util';

export const EDIT_TYPES = {
    NEW: 'new',
    RENAME: 'rename',
};

export const NODE_TYPES = {
    FILE: 'file',
    FOLDER: 'folder',
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
            disableToolTip: false,
            editError: '',
            editTargetExists: false,
            inputValue: this.props.node.label,
        };
        this.nameInput = undefined;
        this.focusHighligher = undefined;
        this.onEditName = this.onEditName.bind(this);
        this.onEditComplete = this.onEditComplete.bind(this);
    }

    /**
     * @inheritdoc
     */
    componentDidMount() {
        if (this.props.node.enableEdit && !_.isNil(this.nameInput)) {
            if (_.isFunction(this.context.getScroller)) {
                const scroller = this.context.getScroller();
                this.focusHighligher.style.height = `${scroller.getScrollHeight()}px`;
            }
            this.nameInput.focus();
        }
    }

    /**
     * @inheritdoc
     */
    shouldComponentUpdate(nextProps, nextState) {
        return (nextProps.panelResizeInProgress && nextProps.node.active)
            || !nextProps.panelResizeInProgress;
    }

    /**
     * @inheritdoc
     */
    componentDidUpdate(prevProps, prevState) {
        if (!_.isNil(this.nameInput) && this.state.inputValue === this.props.node.label) {
            this.nameInput.focus();
            if (this.props.node.fileName) {
                this.nameInput.setSelectionRange(0, this.props.node.fileName.length);
            } else {
                this.nameInput.select();
            }
        }
    }

    /**
     * Upon name modifications
     */
    onEditName(e) {
        const inputValue = e.target.value;
        this.setState({
            inputValue,
        });
        const { parent, id } = this.props.node;
        const newFullPath = parent + getPathSeperator() + inputValue;
        if (newFullPath !== id && !_.isEmpty(inputValue)) {
            exists(newFullPath)
            .then((resp) => {
                let editError = '';
                let editTargetExists = false;
                if (resp.exists) {
                    editError = `A file or folder "${inputValue}" already exists at this location.
                    Please choose a different name`;
                    editTargetExists = true;
                }
                this.setState({
                    editError,
                    editTargetExists,
                });
            })
            .catch((error) => {
                log.error(error.message);
                this.setState({
                    editError: error.message,
                });
            });
        } else {
            this.setState({
                editError: '',
                editTargetExists: false,
            });
        }
    }

     /**
     * Upon escaping edit mode
     */
    onEditEscape() {
        const { node, node: { editType, label }, onNodeDelete } = this.props;
        if (editType === EDIT_TYPES.NEW) {
            onNodeDelete(node);
        } else if (editType === EDIT_TYPES.RENAME) {
            node.enableEdit = false;
            this.setState({
                inputValue: label,
            });
        }
    }

    /**
     * Upon name modification completion
     */
    onEditComplete() {
        const { node, node: { id, editType, parent, type }, onNodeDelete } = this.props;
        const newFullPath = parent + getPathSeperator() + this.state.inputValue;

        if (_.isEmpty(this.state.inputValue) && editType === EDIT_TYPES.NEW) {
            onNodeDelete(node);
        } else if (!_.isEmpty(this.state.inputValue) && editType === EDIT_TYPES.NEW) {
            if (!this.state.editTargetExists) {
                create(newFullPath, type)
                    .then((sucess) => {
                        if (sucess) {
                            this.props.onNodeRefresh(this.props.parentNode);
                        }
                    })
                    .catch((error) => {
                        log.error(error.message);
                        if (_.has(error, 'response.data.Error')) {
                            this.setState({
                                editError: error.response.data.Error,
                            });
                        }
                    });
            }
        } else if (!_.isEmpty(this.state.inputValue) && editType === EDIT_TYPES.RENAME) {
            // user didn't change the name, just disable edit mode.
            if (this.state.inputValue === node.label) {
                node.enableEdit = false;
                this.forceUpdate();
            } else {
                move(id, newFullPath)
                    .then((sucess) => {
                        if (sucess) {
                            this.props.onNodeRefresh(this.props.parentNode);
                        }
                    })
                    .catch((error) => {
                        log.error(error.message);
                        if (_.has(error, 'response.data.Error')) {
                            this.setState({
                                editError: error.response.data.Error,
                            });
                        }
                    });
            }
        }
    }

    /**
     * @inheritdoc
     */
    render() {
        const {
            node,
            node: {
                id,
                active,
                collapsed,
                enableEdit = false,
                editType = EDIT_TYPES.NEW,
                type,
                label,
            },
            parentNode,
            onClick,
            onDoubleClick,
            children,
            onNodeUpdate,
            onNodeRefresh,
            onNodeDelete,
        } = this.props;
        const treeNodeHeader = (
            <div
                data-placement="bottom"
                data-toggle="tooltip"
                title={id}
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
                            { 'fw-folder': type === NODE_TYPES.FOLDER },
                            { 'fw-document': type === NODE_TYPES.FILE }
                        )
                    }
                />
                {enableEdit &&
                    <div
                        className="tree-node-focus-highlighter"
                        onClick={this.onEditComplete}
                        ref={(ref) => {
                            this.focusHighligher = ref;
                        }}
                    />
                }
                {enableEdit &&
                    <div className={classnames('tree-node-name-input-wrapper', { error: !_.isEmpty(this.state.editError) })} >
                        <input
                            type="text"
                            className={classnames('tree-node-name-input')}
                            spellCheck={false}
                            value={this.state.inputValue}
                            onChange={this.onEditName}
                            onBlur={this.onEditComplete}
                            onKeyDown={(e) => {
                                if (e.key === 'Enter' && !this.state.editTargetExists) {
                                    this.onEditComplete();
                                } else if (e.key === 'Escape') {
                                    this.onEditEscape();
                                }
                            }}
                            ref={(nameInput) => {
                                this.nameInput = nameInput;
                            }}
                        />
                        {!_.isEmpty(this.state.editError) && this.nameInput &&
                            <div
                                className="tree-node-name-input-error"
                                style={{
                                    top: this.nameInput.offsetTop + this.nameInput.clientHeight,
                                    left: this.nameInput.offsetLeft,
                                    width: this.nameInput.offsetWidth,
                                }}
                            >
                                <p
                                    style={{
                                        width: this.nameInput.offsetWidth,
                                    }}
                                >
                                    {this.state.editError}
                                </p>
                            </div>
                        }
                    </div>
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
                    menu={getContextMenuItems(node, parentNode, this.context.command, onNodeUpdate, onNodeRefresh)}
                    onShow={() => {
                        this.setState({
                            disableToolTip: true,
                        });
                    }}
                    onHide={() => {
                        this.setState({
                            disableToolTip: false,
                        });
                    }}
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
        id: PropTypes.string.isRequired,
        parent: PropTypes.string,
        collapsed: PropTypes.bool.isRequired,
        type: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
        enableEdit: PropTypes.bool,

    }).isRequired,
    parentNode: PropTypes.objectOf(Object),
    onNodeUpdate: PropTypes.func,
    onNodeDelete: PropTypes.func,
    onNodeRefresh: PropTypes.func,
    enableContextMenu: PropTypes.bool,
    children: PropTypes.node,
    onClick: PropTypes.func,
    onDoubleClick: PropTypes.func,
    panelResizeInProgress: PropTypes.bool,
};

TreeNode.defaultProps = {
    panelResizeInProgress: false,
    enableContextMenu: false,
    onNodeDelete: () => {},
    onNodeUpdate: () => {},
    onNodeRefresh: () => {},
    onClick: () => {},
    isDOMElementVisible: () => false,
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
    getScroller: PropTypes.func,
};

export default TreeNode;
