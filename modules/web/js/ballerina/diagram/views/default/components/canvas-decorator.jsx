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
import ASTNode from '../../../../ast/node';
import AnnotationContainer from './annotation-container';
import DragDropManager from '../../../../tool-palette/drag-drop-manager';
import MessageManager from './../../../../visitors/message-manager';
import './canvas-decorator.css';
import { setCanvasOverlay } from '../../../../configs/app-context';
import ArrowDecorator from './arrow-decorator';
import BackwardArrowDecorator from './backward-arrow-decorator';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import './properties-form.css';
/**
 * React component for a canvas decorator.
 *
 * @class CanvasDecorator
 * @extends {React.Component}
 */
class CanvasDecorator extends React.Component {

    /**
     * Creates an instance of CanvasDecorator.
     * @param {Object} props React properites.
     * @memberof CanvasDecorator
     */
    constructor(props) {
        super(props);
        this.state = { dropZoneActivated: false, dropZoneDropNotAllowed: false };
    }

    /**
     * Event for when dropzone is activated.
     *
     * @param {Object} e event.
     * @returns {void}
     * @memberof CanvasDecorator
     */
    onDropZoneActivate(e) {
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.dropTarget;
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                return;
            }
            dragDropManager.setActivatedDropTarget(dropTarget);
            this.setState({ dropZoneActivated: true,
                dropZoneDropNotAllowed: !dragDropManager.isAtValidDropTarget() });
            dragDropManager.once('drop-target-changed', () => {
                this.setState({ dropZoneActivated: false, dropZoneDropNotAllowed: false });
            });
        }
        e.stopPropagation();
    }

    /**
     * Event fo when dropzone is deactivated.
     *
     * @param {Object} e The event.
     * @memberof CanvasDecorator
     */
    onDropZoneDeactivate(e) {
        const dragDropManager = this.context.dragDropManager;
        const dropTarget = this.props.dropTarget;
        if (dragDropManager.isOnDrag()) {
            if (_.isEqual(dragDropManager.getActivatedDropTarget(), dropTarget)) {
                dragDropManager.clearActivatedDropTarget();
                this.setState({ dropZoneActivated: false, dropZoneDropNotAllowed: false });
            }
        }
        e.stopPropagation();
    }

    /**
     * Renders view for a canvas.
     *
     * @returns {ReactElement} The view.
     * @memberof CanvasDecorator
     */
    render() {
        const dropZoneActivated = this.state.dropZoneActivated;
        const dropZoneDropNotAllowed = this.state.dropZoneDropNotAllowed;
        const canvasClassName = `svg-container${dropZoneActivated ? ' drop-zone active' : ''}`;
        const arrowStart = {
            x: 0,
            y: 0,
        };
        const arrowEnd = {
            x: 0,
            y: 0,
        };

        const dropZoneClassName = (dropZoneActivated ? 'drop-zone active' : 'drop-zone ')
                        + (dropZoneDropNotAllowed ? ' blocked' : '');
        return (
            <div className="grid-background" style={{ width: this.props.bBox.w }} >
                <div ref={(x) => { setCanvasOverlay(x); }}>
                    {/* This space is used to render html elements over svg*/ }
                </div>
                {(this.props.annotations && this.props.annotations.length > 0) ? this.props.annotations : null }
                <CSSTransitionGroup
                    transitionName="propWindow"
                    transitionAppear
                    transitionAppearTimeout={800}
                    transitionEnter
                    transitionEnterTimeout={800}
                    transitionLeave
                    transitionLeaveTimeout={800}
                >
                    {(this.props.serverConnectorPropsViews && this.props.serverConnectorPropsViews.length > 0) ?
                    this.props.serverConnectorPropsViews : null }
                    {(this.props.connectorPropsViews && this.props.connectorPropsViews.length > 0)
                    ? this.props.connectorPropsViews : null }
                    {(this.props.wsResourceViews && this.props.wsResourceViews.length > 0)
                        ? this.props.wsResourceViews : null }
                </CSSTransitionGroup>

                <svg className={canvasClassName} width={this.props.bBox.w} height={this.props.bBox.h}>
                    <rect
                        x="0"
                        y="0"
                        width="100%"
                        height="100%"
                        className={dropZoneClassName}
                        onMouseOver={e => this.onDropZoneActivate(e)}
                        onMouseOut={e => this.onDropZoneDeactivate(e)}
                    />
                    {this.props.children}
                    <ArrowDecorator start={arrowStart} end={arrowEnd} enable moveWithMessageManager />
                    <BackwardArrowDecorator start={arrowStart} end={arrowEnd} enable moveWithMessageManager />
                </svg>
            </div>
        );
    }
}

CanvasDecorator.propTypes = {
    bBox: PropTypes.shape({
        h: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
    }).isRequired,
    children: PropTypes.node.isRequired,
    dropTarget: PropTypes.instanceOf(ASTNode).isRequired,
    annotations: PropTypes.arrayOf(PropTypes.element),
    serverConnectorPropsViews: PropTypes.arrayOf(PropTypes.element),
    connectorPropsViews: PropTypes.arrayOf(PropTypes.element),
    wsResourceViews: PropTypes.arrayOf(PropTypes.element),
};

CanvasDecorator.defaultProps = {
    annotations: [],
    serverConnectorPropsViews: [],
    connectorPropsViews: [],
    wsResourceViews: [],
};

CanvasDecorator.contextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

export default CanvasDecorator;
