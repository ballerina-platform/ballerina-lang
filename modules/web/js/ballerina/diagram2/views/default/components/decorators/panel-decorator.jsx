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
import _ from 'lodash';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import { panel } from './../../designer-defaults';
import ImageUtil from '../../../../image-util';
import SimpleBBox from './../../../../../model/view/simple-bounding-box';
import PanelDecoratorButton from './panel-decorator-button';
import EditableText from './editable-text';
import { util } from '../../sizing-util_bk';
import { getComponentForNodeArray } from './../../../../diagram-util';
import Node from '../../../../../model/tree/node';
import DropZone from '../../../../../drag-drop/DropZone';
import './panel-decorator.css';
import ArgumentParameterDefinitionHolder from './../nodes/argument-parameter-definition-holder';
import ReturnParameterDefinitionHolder from './../nodes/return-parameter-definition-holder';
import TreeUtils from './../../../../../model/tree-util';
import FragmentUtils from './../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';

/* TODOX
import ASTFactory from '../../../../ast/ast-factory';
import SuggestionsText from './suggestions-text';
*/

class PanelDecorator extends React.Component {

    constructor(props) {
        super(props);

        this.state = {
            titleEditing: false,
            editingTitle: this.props.title,
            showProtocolSelect: false,
        };

        this.handleProtocolClick = this.handleProtocolClick.bind(this);
        this.handleProtocolBlur = this.handleProtocolBlur.bind(this);
        this.handleProtocolEnter = this.handleProtocolBlur.bind(this);
        this.togglePublicPrivateFlag = this.togglePublicPrivateFlag.bind(this);
        this.toggleAnnotations = this.toggleAnnotations.bind(this);
    }

    handleProtocolClick() {
        this.setState({ showProtocolSelect: true });
    }

    handleProtocolBlur(value) {
        value = (typeof value === 'string') ? value : value.currentTarget.textContent;
        value = (value === '') ? 'http' : value;
        this.props.model.setProtocolPkgName(value);
        this.setState({ showProtocolSelect: false });
    }

    handleProtocolEnter(value) {
        this.setState({ showProtocolSelect: false });
    }

    onCollapseClick() {
        this.props.model.viewState.collapsed = !this.props.model.viewState.collapsed;
        this.context.editor.update();
    }

    onDelete() {
        this.props.model.remove();
    }

    togglePublicPrivateFlag() {
        this.props.model.public = !this.props.model.public;
        this.props.model.trigger('tree-modified', {
            origin: this.props.model,
            type: 'modify-node',
            title: '',
            data: {
                node: this.props.model,
            },
        });
    }

    toggleAnnotations() {
        this.props.model.viewState.showAnnotationContainer = !this.props.model.viewState.showAnnotationContainer;
        this.context.editor.update();
    }

    onTitleClick() {
        const model = this.props.model;
        if (TreeUtils.isMainFunction(model)) {
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
        if (this.state.editingTitle) {
            const fragment = FragmentUtils.createExpressionFragment(this.state.editingTitle);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            if (!parsedJson.error) {
                const identifierNode = TreeBuilder.build(parsedJson);
                if (TreeUtils.isSimpleVariableRef(identifierNode.getVariable().getInitialExpression())) {
                    this.props.model.setName(identifierNode.getVariable().getInitialExpression().getVariableName());
                }
            }
        }
        this.setState({
            titleEditing: false,
            editingTitle: this.props.model.getName().value,
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
        if (!TreeUtils.isTransformer(this.props.model)) {
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
        }

        // Creating delete button.
        const deleteButtonProps = {
            bBox: {
                x: x - (width * (staticButtons.length + 1)),
                y,
                height,
                width,
            },
            icon: ImageUtil.getSVGIconString('delete'),
            tooltip: 'Delete',
            onClick: () => this.onDelete(),
            key: `${this.props.model.getID()}-delete-button`,
        };

        staticButtons.push(React.createElement(PanelDecoratorButton, deleteButtonProps, null));

        // Creating show annotation button.
        if (!TreeUtils.isTransformer(this.props.model)) {
            const annotationButtonProps = {
                bBox: {
                    x: x - (width * (staticButtons.length + 1)),
                    y,
                    height,
                    width,
                },
                icon: ImageUtil.getSVGIconString(this.props.model.viewState.showAnnotationContainer ?
                    'annotation-clicked' : 'annotation'),
                tooltip: this.props.model.viewState.showAnnotationContainer ? 'Hide Annotations' : 'Show Annotation',
                onClick: () => this.toggleAnnotations(),
                key: `${this.props.model.getID()}-show-annotation-button`,
            };

            staticButtons.push(React.createElement(PanelDecoratorButton, annotationButtonProps, null));
        }

        if ((!TreeUtils.isMainFunction(this.props.model) && TreeUtils.isFunction(this.props.model)) ||
            TreeUtils.isStruct(this.props.model) || TreeUtils.isConnector(this.props.model) ||
            TreeUtils.isTransformer(this.props.model)) {
            // Toggle button for public/private flag
            const publicPrivateFlagButtonProps = {
                bBox: {
                    x: x - (width * (staticButtons.length + 1)),
                    y,
                    height,
                    width,
                },
                icon: ImageUtil.getSVGIconString(this.props.model.public ? 'lock' : 'open-lock'),
                tooltip: this.props.model.public ? 'Make private' : 'Make public',
                onClick: () => this.togglePublicPrivateFlag(),
                key: `${this.props.model.getID()}-publicPrivateFlag-button`,
            };

            staticButtons.push(React.createElement(PanelDecoratorButton, publicPrivateFlagButtonProps, null));
        }
        // Dynamic buttons
        const dynamicButtons = this.props.rightComponents.map((rightComponent, index) => {
            rightComponent.props.bBox = {
                x: x - ((index + staticButtons.length + 1) * width),
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
        let annotationBodyHeight = 0;

        // TODO: Fix Me
        if (!_.isNil(this.props.model.viewState.components.annotation)) {
            annotationBodyHeight = this.props.model.viewState.components.annotation.h;
        }
        // const titleComponents = this.getTitleComponents(this.props.titleComponentData);
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

        let rightHeadingButtons = null;
        const lambda = this.props.model.lambda;
        if (!lambda) {
            rightHeadingButtons =
                this.getRightHeadingButtons(bBox.x + bBox.w, bBox.y + annotationBodyHeight, 27.5, titleHeight);
        }

        const isResourceDef = TreeUtils.isResource(this.props.model);
        const wsResourceDef = (isResourceDef && this.props.packageIdentifier === 'ws');

        let protocolOffset = 0;
        if (this.props.protocol) {
            protocolOffset = 40;
        }
        let publicPrivateFlagoffset = 0;
        let receiverOffset = 14;
        if (this.props.receiver) {
            receiverOffset = this.props.model.viewState.components.receiver.w;
        }
        let allowPublicPrivateFlag = false;
        if ((!TreeUtils.isMainFunction(this.props.model) && TreeUtils.isFunction(this.props.model)) ||
            TreeUtils.isStruct(this.props.model) || TreeUtils.isConnector(this.props.model) ||
            TreeUtils.isTransformer(this.props.model)) {
            allowPublicPrivateFlag = true;
            if (this.props.model.public) {
                publicPrivateFlagoffset = 50;
            }
        }

        return (<g className="panel">
            <g className="panel-header">
                <rect
                    x={bBox.x}
                    y={bBox.y + annotationBodyHeight}
                    width={bBox.w}
                    height={titleHeight}
                    rx="0"
                    ry="0"
                    className="headingRect"
                    data-original-title=""
                    title=""
                />
                <rect x={bBox.x - 1} y={bBox.y + annotationBodyHeight} height={titleHeight} rx="0" ry="0" className="panel-heading-decorator" />
                {allowPublicPrivateFlag && <g>
                    <rect
                        className="publicPrivateRectHolder"
                        x={bBox.x + 8}
                        y={bBox.y + annotationBodyHeight}
                        width='45'
                        height='30'
                    />
                    <text
                        x={bBox.x + 15}
                        y={bBox.y + titleHeight / 2 + annotationBodyHeight + 4}
                        className="publicPrivateText"
                    >{this.props.model.public ? 'public' : null}</text>
                </g>}
                <image
                    x={bBox.x + 15 + publicPrivateFlagoffset}
                    y={bBox.y + 8 + annotationBodyHeight}
                    width={iconSize}
                    height={iconSize}
                    xlinkHref={ImageUtil.getSVGIconString(this.props.icon)}
                />
                {wsResourceDef && <g>
                    <rect
                        x={bBox.x + titleHeight + iconSize + 15 + protocolOffset + publicPrivateFlagoffset}
                        y={bBox.y + titleHeight / 2 + annotationBodyHeight}
                        width={titleWidth.w}
                    />
                    <text
                        x={bBox.x + titleHeight + iconSize + 15 + protocolOffset + publicPrivateFlagoffset + 5}
                        y={bBox.y + titleHeight / 2 + annotationBodyHeight + 5}
                        className="resourceName"
                    >{titleWidth.text}</text>
                </g>}
                {!wsResourceDef && !lambda &&
                <EditableText
                    x={bBox.x + titleHeight + iconSize + protocolOffset + publicPrivateFlagoffset + receiverOffset}
                    y={bBox.y + titleHeight / 2 + annotationBodyHeight}
                    width={titleWidth.w}
                    onBlur={() => {
                        this.onTitleInputBlur();
                    }}
                    onClick={() => {
                        this.onTitleClick();
                    }}
                    editing={this.state.titleEditing}
                    onChange={(e) => {
                        this.onTitleInputChange(e);
                    }}
                    displayText={titleWidth.text}
                    onKeyDown={(e) => {
                        this.onTitleKeyDown(e);
                    }}
                >
                    {this.state.editingTitle}
                </EditableText>}
                { this.props.headerComponent &&
                    <this.props.headerComponent
                        x={bBox.x + titleHeight + iconSize + protocolOffset + publicPrivateFlagoffset}
                        y={bBox.y + annotationBodyHeight}
                    />
                }
                {rightHeadingButtons}
                { this.props.argumentParams &&
                    <ArgumentParameterDefinitionHolder
                        model={this.props.model}
                    >
                        {this.props.argumentParams}
                    </ArgumentParameterDefinitionHolder>
                }
                { this.props.returnParams &&
                <ReturnParameterDefinitionHolder
                    model={this.props.model}
                >
                    {this.props.returnParams}
                </ReturnParameterDefinitionHolder>
                }
            </g>
            <g className="panel-body">
                <CSSTransitionGroup
                    component="g"
                    transitionName="panel-slide"
                    transitionEnterTimeout={300}
                    transitionLeaveTimeout={300}
                >
                    <rect
                        x={panelBBox.x}
                        y={panelBBox.y}
                        width={panelBBox.w}
                        height={panelBBox.h}
                        style={panelRectStyles}
                        className="panel-body-rect"
                    />
                    {!collapsed &&
                    <DropZone
                        x={panelBBox.x}
                        y={panelBBox.y}
                        width={panelBBox.w}
                        height={panelBBox.h}
                        baseComponent="rect"
                        dropTarget={this.props.dropTarget}
                        canDrop={this.props.canDrop}
                        enableDragBg
                    />
                    }
                    {!collapsed && this.props.children}
                </CSSTransitionGroup>
            </g>
        </g>);
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
    model: PropTypes.instanceOf(Node).isRequired,
    dropTarget: PropTypes.instanceOf(Node),
    canDrop: PropTypes.func,
    rightComponents: PropTypes.arrayOf(PropTypes.shape({
        component: PropTypes.func.isRequired,
        props: PropTypes.object.isRequired,
    })),
};

PanelDecorator.defaultProps = {
    rightComponents: [],
    dropTarget: undefined,
    canDrop: undefined,
};

PanelDecorator.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default PanelDecorator;
