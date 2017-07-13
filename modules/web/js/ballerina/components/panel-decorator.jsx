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
import _ from 'lodash';
import PropTypes from 'prop-types';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import ImageUtil from './image-util';
import ASTNode from '../ast/node';
import DragDropManager from '../tool-palette/drag-drop-manager';
import EditableText from './editable-text';
import './panel-decorator.css';
import { panel } from '../configs/designer-defaults.js';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import { getComponentForNodeArray } from './utils';
import { util } from '../visitors/sizing-utils';
import SimpleBBox from '../ast/simple-bounding-box';
import PanelDecoratorButton from './panel-decorator-button';
import SuggestionsText from './suggestions-text';

class PanelDecorator extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            dropZoneActivated: false,
            dropZoneDropNotAllowed: false,
            titleEditing: false,
            editingTitle: this.props.title,
            showProtocolSelect: false
        };

        this.handleProtocolClick = this.handleProtocolClick.bind(this);
        this.handleProtocolBlur = this.handleProtocolBlur.bind(this);
        this.handleProtocolEnter = this.handleProtocolBlur.bind(this);

        // todo : another hack for now we need to move this to correct place and make it dynamic.
        this.availableProtocols = [{ name: 'http'}, {name: 'ws'}, {name: 'jms'}, {name: 'file'}];
    }

    handleProtocolClick() {
        this.setState({ showProtocolSelect : true });
    }

    handleProtocolBlur(value) {
        value = (typeof value === 'string') ? value : value.currentTarget.textContent;
        value = (value === '') ? 'http' : value;
        this.props.model.setProtocolPkgName(value);
        this.setState({ showProtocolSelect : false });
    }

    handleProtocolEnter(value) {
        this.setState({ showProtocolSelect : false });
    }

    onCollapseClick() {
        this.props.model.viewState.collapsed = !this.props.model.viewState.collapsed;
        this.context.editor.update();
    }

    onDelete() {
        this.props.model.remove();
    }

    onTitleClick() {
        if (this.props.model.getType() === 'FunctionDefinition' && this.props.model.getFunctionName() === 'main') {
            // should not edit main function name
            return;
        }
        this.setState({ titleEditing: true });
    }

    onTitleInputBlur() {
        this.setPropertyName();
    }

    onTitleInputChange(e) {
        this.setState({ editingTitle: e.target.value });
    }

    onTitleKeyDown(e) {
        if (e.keyCode === 13) {
            this.setPropertyName();
        }
    }

    setPropertyName() {
        const modelType = this.props.model.type.replace('Definition', '');

        // Setter functions take form 'setModelTypeName'. eg: setServiceName
        this.props.model[`set${modelType}Name`](this.state.editingTitle);
        this.setState({
            titleEditing: false,
        });
    }

    /**
     * Creates the panel heading buttons on the far right.
     *
     * @param {number} x The far x corner position on the heading.
     * @param {number} y The y position(The starting y position of the panel heading).
     * @param {number} width The width of a button.
     * @param {number} height The height of a button.
     * @returns {ReactElement[]} A list of buttons.
     * @memberof PanelDecorator
     */
    getRightHeadingButtons(x, y, width, height) {
        const staticButtons = [];

        const collapsed = this.props.model.viewState.collapsed || false;

        // Creating collapse button.
        const collapseButtonProps = {
            bBox: {
                x: x - width,
                y,
                height,
                width,
            },
            icon: (collapsed) ? ImageUtil.getSVGIconString('down') : ImageUtil.getSVGIconString('up'),
            onClick: () => this.onCollapseClick(),
            key: `${this.props.model.getID()}-collapse-button`,
        };

        staticButtons.push(React.createElement(PanelDecoratorButton, collapseButtonProps, null));

        // Creating delete button.
        const deleteButtonProps = {
            bBox: {
                x: x - (width * 2),
                y,
                height,
                width,
            },
            icon: ImageUtil.getSVGIconString('delete'),
            onClick: () => this.onDelete(),
            key: `${this.props.model.getID()}-delete-button`,
        };

        staticButtons.push(React.createElement(PanelDecoratorButton, deleteButtonProps, null));

        // Dynamic buttons
        const dynamicButtons = this.props.rightComponents.map((rightComponent, index) => {
            rightComponent.props.bBox = {
                x: x - ((index + 3) * width),
                y,
                width,
                height,
            };
            return React.createElement(rightComponent.component, rightComponent.props, null);
        });

        return [...staticButtons, ...dynamicButtons];
    }

    render() {
        const bBox = this.props.bBox;
        const titleHeight = panel.heading.height;
        const iconSize = 14;
        const collapsed = this.props.model.viewState.collapsed || false;
        const annotationViewCollapsed = this.props.model.viewState.annotationViewCollapsed || false;
        const dropZoneActivated = this.state.dropZoneActivated;
        const dropZoneDropNotAllowed = this.state.dropZoneDropNotAllowed;
        const dropZoneClassName = ((!dropZoneActivated) ? 'panel-body-rect drop-zone' : 'panel-body-rect drop-zone active')
            + ((dropZoneDropNotAllowed) ? ' block' : '');
        const panelBodyClassName = `panel-body${(dropZoneActivated) ? ' drop-zone active' : ''}`;

        const annotationBodyClassName = 'annotation-body';
        let annotationBodyHeight = 0;

        // TODO: Fix Me
        if (!_.isNil(this.props.model.viewState.components.annotation)) {
            annotationBodyHeight = this.props.model.viewState.components.annotation.h;
        }
        const titleComponents = this.getTitleComponents(this.props.titleComponentData);
        const annotations = this.props.model.getChildren().filter(child => BallerinaASTFactory.isAnnotation(child));
        const annotationString = this.getAnnotationsString(annotations);
        const annotationComponents = this.getAnnotationComponents(annotations, bBox, titleHeight);

        const titleWidth = util.getTextWidth(this.state.editingTitle);

        // calculate the panel bBox;
        const panelBBox = new SimpleBBox();
        panelBBox.x = bBox.x;
        panelBBox.y = bBox.y + titleHeight + annotationBodyHeight;
        panelBBox.w = bBox.w;
        panelBBox.h = bBox.h - titleHeight - annotationBodyHeight;

        // following config is to style the panel rect, we use it to hide the top stroke line of the panel.
        const panelRectStyles = {
            strokeDasharray: `0, ${panelBBox.w}, ${panelBBox.h} , 0 , ${panelBBox.w} , 0 , ${panelBBox.h}`,
        };

        const rightHeadingButtons = this.getRightHeadingButtons(bBox.x + bBox.w, bBox.y + annotationBodyHeight, 27.5, titleHeight);

        let protocolOffset = 0;
        let protocolTextSize = 0;
        if (this.props.protocol) {
            protocolOffset = 50;
            protocolTextSize = util.getTextWidth(this.props.protocol, 0).w;
        }

        return (<g className="panel">
            <g className="panel-header">
                <rect
                    x={bBox.x} y={bBox.y + annotationBodyHeight} width={bBox.w} height={titleHeight} rx="0" ry="0"
                    className="headingRect" data-original-title="" title=""
                />
                <rect x={bBox.x - 1} y={bBox.y + annotationBodyHeight} height={titleHeight} rx="0" ry="0" className="panel-heading-decorator" />
                <EditableText
                    x={bBox.x + titleHeight + iconSize + 15 + protocolOffset } y={bBox.y + titleHeight / 2 + annotationBodyHeight}
                    width={titleWidth.w}
                    onBlur={() => { this.onTitleInputBlur(); }}
                    onClick={() => { this.onTitleClick(); }}
                    editing={this.state.titleEditing}
                    onChange={(e) => { this.onTitleInputChange(e); }}
                    displayText={titleWidth.text}
                    onKeyDown={(e) => { this.onTitleKeyDown(e); }}
                >
                    {this.state.editingTitle}
                </EditableText>
                {this.props.protocol &&
                    <g>
                        <rect
                            x={bBox.x + titleHeight + iconSize + 15 + 3 } y={bBox.y + annotationBodyHeight} width={protocolOffset - 3} height={titleHeight}
                            className="protocol-rect"
                            onClick={this.handleProtocolClick}
                        />
                        <text className="protocol-text" onClick={this.handleProtocolClick} x={bBox.x + titleHeight + iconSize + 15 + 3 + ((protocolOffset - protocolTextSize) / 2)} y={bBox.y + annotationBodyHeight + 15} style={{ dominantBaseline: 'central' }}>{this.props.protocol}</text>
                        <SuggestionsText
                            x={bBox.x + titleHeight + iconSize + 15 + 3} y={bBox.y + annotationBodyHeight} width={protocolOffset - 3} height={titleHeight}
                            suggestionsPool={this.availableProtocols}
                            show={this.state.showProtocolSelect}
                            onBlur={this.handleProtocolBlur}
                            onEnter={this.handleProtocolEnter}
                            onSuggestionSelected={this.handleProtocolBlur}
                        />
                    </g>
                }
                <image
                    x={bBox.x + 8} y={bBox.y + 8 + annotationBodyHeight} width={iconSize} height={iconSize}
                    xlinkHref={ImageUtil.getSVGIconString(this.props.icon)}
                />
                <rect
                    x={bBox.x + iconSize + 16} y={bBox.y + annotationBodyHeight} width={iconSize + 15} height={titleHeight - 3}
                    className="annotation-icon-wrapper"
                />
                <image
                    x={bBox.x + iconSize + 24} y={bBox.y + 8 + annotationBodyHeight} width={iconSize} height={iconSize}
                    xlinkHref={ImageUtil.getSVGIconString('annotation-black')} onClick={this.onAnnotationEditButtonClick.bind(this)}
                    className="annotation-icon"
                />
                {titleComponents}
                {rightHeadingButtons}
            </g>
            <g className={panelBodyClassName}>
                <CSSTransitionGroup
                    component="g"
                    transitionName="panel-slide"
                    transitionEnterTimeout={300}
                    transitionLeaveTimeout={300}
                >
                    {!collapsed &&
                    <rect
                        x={panelBBox.x}
                        y={panelBBox.y}
                        width={panelBBox.w}
                        height={panelBBox.h}
                        rx="0" ry="0" fill="#fff"
                        className={dropZoneClassName}
                        onMouseOver={e => this.onDropZoneActivate(e)}
                        onMouseOut={e => this.onDropZoneDeactivate(e)}
                        style={panelRectStyles}
                    />
                    }
                    {!collapsed && this.props.children}
                </CSSTransitionGroup>
            </g>
        </g>);
    }

    onDropZoneActivate(e) {
        const dragDropManager = this.context.dragDropManager,
            dropTarget = this.props.dropTarget,
            dropSourceValidateCB = this.props.dropSourceValidateCB;
        if (!_.isNil(dropTarget) && dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                return;
            }
            if (_.isNil(dropSourceValidateCB)) {
                dragDropManager.setActivatedDropTarget(dropTarget);
            } else if (_.isFunction(dropSourceValidateCB)) {
                dragDropManager.setActivatedDropTarget(dropTarget, dropSourceValidateCB);
            }
            this.setState({
                dropZoneActivated: true,
                dropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget(),
            });
            dragDropManager.once('drop-target-changed', () => {
                this.setState({ dropZoneActivated: false, dropZoneDropNotAllowed: false });
            });
        }
        e.stopPropagation();
    }

    onDropZoneDeactivate(e) {
        const dragDropManager = this.context.dragDropManager,
            dropTarget = this.props.model;
        if (!_.isNil(dropTarget) && dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                dragDropManager.clearActivatedDropTarget();
                this.setState({ dropZoneActivated: false, dropZoneDropNotAllowed: false });
            }
        }
        e.stopPropagation();
    }

    getTitleComponents(titleComponentData) {
        const components = [];
        if (!_.isUndefined(titleComponentData)) {
            for (const componentData of titleComponentData) {
                if (componentData.isNode) {
                    components.push(getComponentForNodeArray([componentData.model])[0]);
                } else {
                    components.push(componentData.model);
                }
            }
        }
        return components;
    }

    getAnnotationComponents(annotationComponentData, bBox, titleHeight) {
        const components = [];
        let possitionY = bBox.y + titleHeight / 2 + 5;
        if (!_.isUndefined(annotationComponentData)) {
            for (const componentData of annotationComponentData) {
                const modelComponents = [];
                components.push(<g key={componentData.getID()}>
                    <text className="annotation-text" x={bBox.x + 5} y={possitionY}>{componentData.toString()}</text>
                </g>);
                possitionY += 25;
            }
        }
        return components;
    }


    getAnnotationsString(annotations) {
        let annotationString = '';
        // TODO: Fix Me
        if (!_.isNil(annotations)) {
            annotations.forEach((annotation) => {
                annotationString = `${annotationString + annotation.toString()}  `;
            });
        }
        return annotationString;
    }

    onAnnotationEditButtonClick() {
        this.props.model.viewState.showAnnotationContainer = !this.props.model.viewState.showAnnotationContainer;
        this.context.editor.update();
    }
}


PanelDecorator.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    model: PropTypes.instanceOf(ASTNode).isRequired,
    dropTarget: PropTypes.instanceOf(ASTNode),
    dropSourceValidateCB: PropTypes.func,
    rightComponents: PropTypes.arrayOf(PropTypes.shape({
        component: PropTypes.func.isRequired,
        props: PropTypes.object.isRequired,
    })),
};

PanelDecorator.defaultProps = {
    rightComponents: [],
};

PanelDecorator.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
};

export default PanelDecorator;
