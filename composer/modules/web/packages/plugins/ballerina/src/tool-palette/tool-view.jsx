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
import PropTypes from 'prop-types';
import { getEmptyImage } from 'react-dnd-html5-backend';
import ImageUtils from './../diagram/image-util';
import { withDragEnabled } from './../drag-drop/drag-source';
import { TOOL, TOOL_GROUP } from './spec';

/**
 * Tool Component which render a tool in tool palette.
 *
 * @class Tool
 * @extends {React.Component}
 */
class ToolView extends React.Component {

    /**
     * @inheritdoc
     */
    componentDidMount() {
        // Use empty image as a drag preview so browsers don't draw it
        // and we can draw whatever we want on the custom drag layer instead.
        this.props.connectDragPreview(getEmptyImage(), {
            // IE fallback: specify that we'd rather screenshot the node
            // when it already knows it's being dragged so we can hide it with CSS.
            captureDraggingState: true,
        });
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
        const functionName = tool.title
            + (tool.factoryArgs.actionConnectorName ? tool.factoryArgs.actionConnectorName : '');
        this.context.editor.openDocumentation(group.name, functionName);
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
        let toolDesc = '';
        if (this.props.order === 'horizontal') {
            toolTip = tool.name;
            toolDesc = tool.description || '';
            return this.props.connectDragSource(
                <div
                    className='tool-block tool-container'
                    title={toolTip + '\n' + toolDesc}
                    data-placement='bottom'
                    data-toggle='tooltip'
                    id={toolTip}
                >

                    <i className={`icon fw fw-${tool.icon}`} />
                    <span className='tool-title-wrap' />
                    <span className='tool-title-wrap'>
                        <p className='tool-title'>{toolTip}</p>
                    </span>

                </div>);
        }
        // delete(string path, Request req) : Response b
        toolTip = tool.title;
        if (tool.parameters) {
            toolTip += '(';
            tool.parameters.forEach((param, index) => {
                if (index !== 0) {
                    toolTip += ' , ';
                }

                toolTip += param.type + ' ' + param.name;
            });
            toolTip += ')';
        }
        if (tool.returnParams) {
            toolTip += ' : ( ';
            tool.returnParams.forEach((param, index) => {
                if (index !== 0) {
                    toolTip += ' , ';
                }

                toolTip += param.type + ' ' + (param.name ? param.name : param.type.substr(0, 1));
            });
            toolTip += ')';
        }
        let imageIcon;
        if (tool.id === 'ClientConnector') {
            const iconBytes = ImageUtils.getConnectorIcon(tool.factoryArgs.packageName);
            imageIcon = <img alt='client connector icon' src={iconBytes} />;
        } else {
            imageIcon = <i className={`icon fw fw-${tool.icon}`} />;
        }

        return this.props.connectDragSource(
            <div
                id={`${tool.id}-tool`}
                className={`tool-block tool-container-vertical ${tool.classNames}`}
                ref={(c) => { this.tool = c; }}
            >
                <div
                    className='tool-container-vertical-icon'
                    data-placement='bottom'
                    data-toggle='tooltip'
                    title={toolTip + '\n' + toolDesc}
                >
                    {imageIcon}
                </div>
                <div
                    className='tool-container-vertical-title'
                    data-placement='bottom'
                    data-toggle='tooltip'
                    title={toolTip + '\n' + toolDesc}
                >
                    {tool.name}
                </div>
                <p className='tool-title'>{tool.title}</p>
                {this.props.isDocEnable &&
                <a onClick={e => this.handleClickOpenDocumentation(e)} className='pull-right'>
                    <span className='fw fw-document' />
                </a>
                }
            </div>);
    }
}

ToolView.defaultProps = {
    order: 'vertical',
};

ToolView.propTypes = {
    connectDragSource: PropTypes.func.isRequired,
    connectDragPreview: PropTypes.func.isRequired,
    isDragging: PropTypes.bool.isRequired,
    order: PropTypes.string,
    tool: TOOL.isRequired,
    group: TOOL_GROUP.isRequired,
};

ToolView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default withDragEnabled(ToolView);
