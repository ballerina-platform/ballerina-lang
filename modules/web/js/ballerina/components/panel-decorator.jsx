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
import './panel-decorator.css';

class PanelDecorator extends React.Component {

    constructor(props) {
        super(props);
        this.state = {dropZoneActivated: false, dropZoneDropNotAllowed: false};
    }

    onCollapseClick() {
        this.props.model.setAttribute('viewState.collapsed', !this.props.model.viewState.collapsed);
    }

    onDelete() {
        this.props.model.remove();
    }

    render() {
        const bBox = this.props.bBox;
        const titleHeight = 25;
        const iconSize = 14;
        const collapsed = this.props.model.viewState.collapsed|| false;
        const dropZoneActivated = this.state.dropZoneActivated;
        const dropZoneDropNotAllowed = this.state.dropZoneDropNotAllowed;
        const dropZoneClassName = ((!dropZoneActivated) ? "panel-body-rect drop-zone" : "panel-body-rect drop-zone active")
                          + ((dropZoneDropNotAllowed) ? " block" : "");
        return ( <g className="panel">
                     <g className="panel-header">
                         <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ titleHeight } rx="0" ry="0" className="headingRect" data-original-title="" title=""></rect>
                         <text x={ bBox.x + titleHeight } y={ bBox.y + titleHeight / 2 + 5 }>{this.props.title}</text>
                         <image x={bBox.x + 5} y={bBox.y + 5} width={iconSize} height={iconSize} xlinkHref={ImageUtil.getSVGIconString(this.props.icon)}/>
                         <g className="panel-header-controls">
                             <image x={ bBox.x + bBox.w - 44.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize } className="control"
                                  xlinkHref={ImageUtil.getSVGIconString('delete')} onClick={() => this.onDelete()}/>
                             <line x1={ bBox.x + bBox.w - 50} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 50} y2={ bBox.y + 20} className="operations-separator"></line>
                             <image x={ bBox.x + bBox.w - 19.5} y={ bBox.y + 5.5} width={ iconSize } height={ iconSize }  className="control"
                                  xlinkHref={(collapsed) ? ImageUtil.getSVGIconString('down') : ImageUtil.getSVGIconString('up')} onClick={() => this.onCollapseClick()}/>
                             <line x1={ bBox.x + bBox.w - 25} y1={ bBox.y + 5} x2={ bBox.x + bBox.w - 25} y2={ bBox.y + 20} className="operations-separator"></line>
                         </g>
                     </g>
                     <g className="panel-body">
                        <CSSTransitionGroup
                           component="g"
                           transitionName="panel-slide"
                           transitionEnterTimeout={300}
                           transitionLeaveTimeout={300}>
                              { !collapsed &&
                                   <rect x={ bBox.x } y={ bBox.y + titleHeight} width={ bBox.w } height={ bBox.h - titleHeight }
                                      rx="0" ry="0" fill="#fff"
                                      className={dropZoneClassName}
                                      onMouseOver={(e) => this.onDropZoneActivate(e)}
                                      onMouseOut={(e) => this.onDropZoneDeactivate(e)}/>
                              }
                              { !collapsed && this.props.children}
                        </CSSTransitionGroup>
                     </g>
                 </g>);
    }

    onDropZoneActivate (e) {
        const dragDropManager = this.context.dragDropManager,
              dropTarget = this.props.dropTarget,
              dropSourceValidateCB = this.props.dropSourceValidateCB;
        if(!_.isNil(dropTarget) && dragDropManager.isOnDrag()) {
            if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
                return;
            }
            if(_.isNil(dropSourceValidateCB)) {
                dragDropManager.setActivatedDropTarget(dropTarget);
            } else if (_.isFunction(dropSourceValidateCB)) {
                dragDropManager.setActivatedDropTarget(dropTarget, dropSourceValidateCB);
            }
            this.setState({dropZoneActivated: true,
                      dropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget()});
            dragDropManager.once('drop-target-changed', () => {
                this.setState({dropZoneActivated: false, dropZoneDropNotAllowed: false});
            });
        }
        e.stopPropagation();
    }

    onDropZoneDeactivate (e) {
        const dragDropManager = this.context.dragDropManager,
              dropTarget = this.props.model;
        if(!_.isNil(dropTarget) && dragDropManager.isOnDrag()){
            if(_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)){
                dragDropManager.clearActivatedDropTarget();
                this.setState({dropZoneActivated: false, dropZoneDropNotAllowed: false});
            }
        }
        e.stopPropagation();
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
}

PanelDecorator.contextTypes = {
	 dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired
};

export default PanelDecorator;
