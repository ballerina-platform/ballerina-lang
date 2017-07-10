/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import React from 'react';
import ReactDOM from 'react-dom';
import $ from 'jquery';
import * as d3 from 'd3';
import _ from 'lodash';
import PropTypes from 'prop-types';
import DragDropManager from '../tool-palette/drag-drop-manager';
import Tool from './tool';
import ToolGroup from './tool-group';

/**
 * Tool Component which render a tool in tool palette.
 *
 * @class Tool
 * @extends {React.Component}
 */
class ToolView extends React.Component {

    /**
     * We use component did mount to initialize dragable.
     *
     * @memberof Tool
     */
    componentDidMount() {
        const toolDiv = ReactDOM.findDOMNode(this);
        $(toolDiv).draggable({
            helper: _.isUndefined(this.createCloneCallback)
                ? 'clone'
                : this.createCloneCallback(self),
            cursor: 'move',
            cursorAt: {
                left: 30,
                top: -10,
            },
            containment: 'document',
            zIndex: 10001,
            stop: this.createHandleDragStopEvent(),
            start: this.createHandleDragStartEvent(),
            drag: this.createHandleOnDragEvent(),
        });
    }

    /**
     * JQuery dragable drag stop event handler.
     *
     * @returns {function} handler.
     * @memberof Tool
     */
    createHandleDragStopEvent() {
        return () => {
            if (this.context.dragDropManager.isAtValidDropTarget()) {
                const indexForNewNode = this.context.dragDropManager.getDroppedNodeIndex();
                const nodeBeingDragged = this.context.dragDropManager.getNodeBeingDragged();
                if (indexForNewNode >= 0) {
                    this.context.dragDropManager.getActivatedDropTarget()
                        .addChild(nodeBeingDragged, indexForNewNode, false, false, true);
                } else {
                    this.context.dragDropManager.getActivatedDropTarget()
                        .addChild(nodeBeingDragged, undefined, false, false, true);
                }
            }
            this.context.dragDropManager.reset();
            this._$disabledIcon = undefined;
            this._$draggedToolIcon = undefined;
        };
    }

    /**
     * JQuery dragable drag start event handler.
     *
     * @returns {function} handler.
     * @memberof Tool
     */
    createHandleDragStartEvent() {
        return () => {
            // Get the meta information/ arguments to create the particular tool
            const meta = this.props.tool.get('meta') || {};
            this.context.dragDropManager.setNodeBeingDragged(this.props.tool.nodeFactoryMethod(meta, true));
        };
    }

    /**
     * JQuery dragable on drag event handler.
     *
     * @returns {function} handler.
     * @memberof Tool
     */
    createHandleOnDragEvent() {
        return () => {
            if (!this.context.dragDropManager.isAtValidDropTarget()) {
                this._$disabledIcon.show();
                this._$draggedToolIcon.css('opacity', 0.3);
            } else {
                this._$disabledIcon.hide();
                this._$draggedToolIcon.css('opacity', 1);
            }
        };
    }

    /**
     * JQuery dragable drag clone event handler.
     *
     * @returns {function} handler.
     * @memberof Tool
     */
    createCloneCallback() {
        const icon = $(`<i class="${this.props.tool.get('cssClass')}" style="font-size:50px" />`).get(0);
        return () => {
            const div = this.createContainerForDraggable();
            div.node().appendChild(icon);
            this._$draggedToolIcon = $(icon);
            return div.node();
        };
    }

    /**
     * This function will create an icon image which will draged when a tool is draged.
     *
     * @returns {element} html div.
     * @memberof Tool
     */
    createContainerForDraggable() {
        const body = d3.select('body');
        const div = body.append('div').attr('id', 'draggingToolClone').classed('tool-drag-container', true);
        // For validation feedback
        const disabledIconDiv = div.append('div').classed('disabled-icon-container', true);
        disabledIconDiv.append('i').classed('fw fw-lg fw-block tool-disabled-icon', true);
        this._$disabledIcon = $(disabledIconDiv.node());
        this._$disabledIcon.css('top', 30 + 10);
        this._$disabledIcon.css('left', 30);
        return div;
    }

    /**
     * Handles mouse click on open documentation icon
     *
     * @param {any} e - Mouse event of icon click
     * @memberof ToolView
     */
    handleClickOpenDocumentation(e) {
        e.stopPropagation();
        const { tool, group } = this.props;
        const functionName = tool.get('title') + (tool.parent ? tool.parent : '');
        this.context.editor.openDocumentation(group.get('toolGroupName'), functionName);
    }

    /**
     * Render tool view.
     *
     * @returns {ReactElement} render tool view.
     *
     * @memberof Tool
     */
    render() {
        const tool = this.props.tool;
        let toolTip = '';
        if (this.props.toolOrder === 'horizontal') {
            toolTip = tool.get('name');
            return (
                <div
                    className="tool-block tool-container"
                    title={toolTip}
                    data-placement="bottom"
                    data-toggle="tooltip"
                    id={toolTip}
                >
                    <i className={tool.get('cssClass')} />
                    <span className="tool-title-wrap" />
                    <span className="tool-title-wrap">
                        <p className="tool-title">{toolTip}</p>
                    </span>
                </div>
            );
        }
        toolTip = tool.get('title');
        if (tool.get('_parameters')) {
            toolTip += '(';

            tool.get('_parameters').forEach((param, index) => {
                if (index !== 0) {
                    toolTip += ',';
                }

                toolTip += param.type + ' ' + param.name;
            });

            toolTip += ')';
        }
        if (tool.get('_returnParams')) {
            toolTip += '(';
            tool.get('_returnParams').forEach((param, index) => {
                if (index !== 0) {
                    toolTip += ',';
                }

                toolTip += param.type + ' ' + (param.name ? param.name : param.type.substr(0, 1));
            });

            toolTip += ')';
        }
        return (
            <div
                id={`${tool.id}-tool`}
                className={`tool-block tool-container-vertical ${tool.get('classNames')}`}
                ref={(c) => { this.tool = c; }}
            >
                <div
                    className="tool-container-vertical-icon"
                    data-placement="bottom"
                    data-toggle="tooltip"
                    title={toolTip}
                >
                    <i className={tool.get('cssClass')} />
                </div>
                <div
                    className="tool-container-vertical-title"
                    data-placement="bottom"
                    data-toggle="tooltip"
                    title={toolTip}
                >
                    {tool.get('title')}
                </div>
                <p className="tool-title">{tool.get('name')}</p>
                <a onClick={e => this.handleClickOpenDocumentation(e)} className="pull-right">
                    <span className="fw fw-document" />
                </a>
            </div>
        );
    }
}

ToolView.defaultProps = {
    toolOrder: 'vertical',
};

ToolView.propTypes = {
    toolOrder: PropTypes.string,
    tool: PropTypes.instanceOf(Tool).isRequired,
    group: PropTypes.instanceOf(ToolGroup).isRequired,
};

ToolView.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default ToolView;
