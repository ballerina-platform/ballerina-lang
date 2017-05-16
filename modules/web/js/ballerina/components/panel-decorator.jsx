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
import ImageUtil from './image-util';
import PropTypes from 'prop-types';
import ASTNode from '../ast/node';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import DragDropManager from '../tool-palette/drag-drop-manager';
import EditableText from './editable-text';
import './panel-decorator.css';
import {panel} from '../configs/designer-defaults.js';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import {getComponentForNodeArray} from './utils';

class PanelDecorator extends React.Component {

    constructor(props) {
        super(props);
        this.state = {dropZoneActivated: false, dropZoneDropNotAllowed: false, titleEditing: false};
    }

    onCollapseClick() {
        this.props.model.setAttribute('viewState.collapsed', !this.props.model.viewState.collapsed);
    }

    onAnnotaionCollapseClick() {
        this.props.model.setAttribute('viewState.annotationViewCollapsed', !this.props.model.viewState.annotationViewCollapsed);
    }

    onDelete() {
        this.props.model.remove();
    }

    onTitleClick() {
        this.setState({titleEditing: true})
    }

    onTitleInputBlur() {
        this.setState({titleEditing: false})
    }

    onTitleInputChange(e) {
        const modelType = this.props.model.type.replace('Definition', '');

        // Setter functions take form 'setModelTypeName'. eg: setServiceName
        this.props.model[`set${modelType}Name`](e.target.value);
    }

    render() {
        const bBox = this.props.bBox;
        const titleHeight = panel.heading.height;
        const iconSize = 14;
        const collapsed = this.props.model.viewState.collapsed || false;
        const annotationViewCollapsed = this.props.model.viewState.annotationViewCollapsed || false;
        const dropZoneActivated = this.state.dropZoneActivated;
        const dropZoneDropNotAllowed = this.state.dropZoneDropNotAllowed;
        const dropZoneClassName = ((!dropZoneActivated) ? "panel-body-rect drop-zone" : "panel-body-rect drop-zone active")
            + ((dropZoneDropNotAllowed) ? " block" : "");
        const panelBodyClassName = "panel-body" + ((dropZoneActivated) ? " drop-zone active" : "");

        const annotationBodyClassName = "annotation-body";
        let annotationBodyHeight = 0;

        // TODO: Fix Me
        if (!_.isNil(this.props.model.viewState.components.annotation)) {
            annotationBodyHeight = this.props.model.viewState.components.annotation.h;
        }
        let titleComponents = this.getTitleComponents(this.props.titleComponentData);
        let annotations = this.props.model.getChildren().filter(function (child) {
            return BallerinaASTFactory.isAnnotation(child);
        });
        let annotationString = this.getAnnotationsString(annotations);
        let annotationComponents = this.getAnnotationComponents(annotations, bBox, titleHeight);

        return (<g className="panel">
            <g className="panel-header">
                <rect x={bBox.x} y={bBox.y + annotationBodyHeight} width={bBox.w} height={titleHeight} rx="0" ry="0"
                      className="headingRect" data-original-title="" title=""></rect>
                <EditableText
                    x={bBox.x + titleHeight} y={bBox.y + titleHeight / 2 + annotationBodyHeight}
                    onBlur={() => { this.onTitleInputBlur() }}
                    onClick={() => { this.onTitleClick() }}
                    editing={this.state.titleEditing}
                    onChange={e => {this.onTitleInputChange(e)}}>
                    {this.props.title}
                </EditableText>
                <image x={bBox.x + 5} y={bBox.y + 5 + annotationBodyHeight} width={iconSize} height={iconSize}
                       xlinkHref={ImageUtil.getSVGIconString(this.props.icon)}/>
                {titleComponents}
                <g className="panel-header-controls">
                    <rect x={bBox.x + bBox.w - 54} y={bBox.y + annotationBodyHeight} width={iconSize} height={iconSize}
                          className="panel-header-controls-wrapper"/>
                    <image x={bBox.x + bBox.w - 44.5} y={bBox.y + 5.5 + annotationBodyHeight} width={iconSize}
                           height={iconSize} className="control"
                           xlinkHref={ImageUtil.getSVGIconString('delete')} onClick={() => this.onDelete()}/>
                    <image x={bBox.x + bBox.w - 19.5} y={bBox.y + 5.5 + annotationBodyHeight} width={iconSize}
                           height={iconSize} className="control"
                           xlinkHref={(collapsed) ? ImageUtil.getSVGIconString('down') : ImageUtil.getSVGIconString('up')}
                           onClick={() => this.onCollapseClick()}/>
                </g>
            </g>
            <g className={panelBodyClassName}>
                <CSSTransitionGroup
                    component="g"
                    transitionName="panel-slide"
                    transitionEnterTimeout={300}
                    transitionLeaveTimeout={300}>
                    {!collapsed &&
                    <rect x={bBox.x} y={bBox.y + titleHeight + annotationBodyHeight} width={bBox.w}
                          height={bBox.h - titleHeight - annotationBodyHeight}
                          rx="0" ry="0" fill="#fff"
                          className={dropZoneClassName}
                          onMouseOver={(e) => this.onDropZoneActivate(e)}
                          onMouseOut={(e) => this.onDropZoneDeactivate(e)}/>
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
                dropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget()
            });
            dragDropManager.once('drop-target-changed', () => {
                this.setState({dropZoneActivated: false, dropZoneDropNotAllowed: false});
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
                this.setState({dropZoneActivated: false, dropZoneDropNotAllowed: false});
            }
        }
        e.stopPropagation();
    }

    getTitleComponents(titleComponentData) {
        let components = [];
        if (!_.isUndefined(titleComponentData)) {
            for (let componentData of titleComponentData) {
                if (componentData.isNode) {
                    components.push(getComponentForNodeArray([componentData.model])[0]);
                }else{
                    components.push(componentData.model);
                }
            }
        }
        return components;
    }

    getAnnotationComponents(annotationComponentData, bBox, titleHeight) {
        let components = [];
        let possitionY = bBox.y + titleHeight / 2 + 5;
        if (!_.isUndefined(annotationComponentData)) {
            for (let componentData of annotationComponentData) {
                let modelComponents = [];
                components.push(<g key={componentData.getID()}>
                    <text className="annotation-text" x={bBox.x + 5} y={possitionY}>{componentData.toString()}</text>
                </g>);
                possitionY = possitionY + 25;
            }
        }
        return components;
    }


    getAnnotationsString(annotations) {
        let annotationString = '';
        // TODO: Fix Me
        if (!_.isNil(annotations)) {
            annotations.forEach(function (annotation) {
                annotationString = annotationString + annotation.toString() + '  ';
            });
        }
        return annotationString;
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
    dropSourceValidateCB: PropTypes.func
};

PanelDecorator.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired
};

export default PanelDecorator;
