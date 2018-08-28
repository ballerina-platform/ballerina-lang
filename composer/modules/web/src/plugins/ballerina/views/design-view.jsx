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
import { Scrollbars } from 'react-custom-scrollbars';
import BallerinaDiagram from 'plugins/ballerina/diagram/diagram';
import TransformerExpanded from 'plugins/ballerina/diagram/views/default/components/transformer/transformer-expanded';
import TreeUtil from 'plugins/ballerina/model/tree-util.js';
import DragLayer from './../drag-drop/drag-layer';
import CompilationUnitNode from './../model/tree/compilation-unit-node';
import { EVENTS } from '../constants';
import DesignViewErrorBoundary from './DesignViewErrorBoundary';
import DiagramMenu from './diagram-menu';
import GraphicalEditor from '../graphical-editor/graphical-editor';

class DesignView extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isTransformActive: false,
            dragging: false,
            zoom: 1,
        };
        this.zoomInHandle = this.zoomInHandle.bind(this);
        this.zoomOutHandle = this.zoomOutHandle.bind(this);
        this.zoomFitHandle = this.zoomFitHandle.bind(this);
        this.mouseUpHandle = this.mouseUpHandle.bind(this);
        this.mouseMoveHandle = this.mouseMoveHandle.bind(this);
        this.overlayContainer = undefined;
        this.diagramContainer = undefined;
        this.toolPaletteContainer = undefined;
        this.onScroll = this.onScroll.bind(this);
        this.setOverlayContainer = this.setOverlayContainer.bind(this);
        this.getOverlayContainer = this.getOverlayContainer.bind(this);
        this.setDiagramContainer = this.setDiagramContainer.bind(this);
        this.getDiagramContainer = this.getDiagramContainer.bind(this);
        this.setGraphicalEditorContainer = this.setGraphicalEditorContainer.bind(this);
        this.getGraphicalEditorContainer = this.getGraphicalEditorContainer.bind(this);
        this.setToolPaletteContainer = this.setToolPaletteContainer.bind(this);
        this.getToolPaletteContainer = this.getToolPaletteContainer.bind(this);
        this.props.commandProxy.on('go-to-node', (node) => {
            this.scrollbars.scrollTop(node.viewState.bBox.y);
        });
    }

    /**
     * @override
     * @memberof Diagram
     */
    getChildContext() {
        return {
            editMode: this.props.editMode,
            designView: this,
            getOverlayContainer: this.getOverlayContainer,
            getDiagramContainer: this.getDiagramContainer,
            getGraphicalEditorContainer: this.getGraphicalEditorContainer,
        };
    }

    componentDidMount() {
        window.addEventListener('mouseup', this.mouseUpHandle);
        window.addEventListener('mousemove', this.mouseMoveHandle);
    }

    shouldComponentUpdate(nextProps, nextState) {
        // Always re render when transform is active
        // Otherwise don't rerender for panel resizings
        return !nextProps.panelResizeInProgress || nextState.isTransformActive;
    }

    componentWillUnmount() {
        window.removeEventListener('mouseup', this.mouseUpHandle);
        window.removeEventListener('mousemove', this.mouseMoveHandle);
    }

    onScroll(e) {
        this.props.commandProxy.dispatch(EVENTS.SCROLL_DESIGN_VIEW, {
            scrollLeft: e.scrollLeft,
            scrollTop: e.scrollTop,
            scrollHeight: e.scrollHeight,
            scrollWidth: e.scrollWidth,
            clientHeight: e.clientHeight,
            clientWidth: e.clientWidth,
        });
    }

    setDiagramContainer(ref) {
        this.diagramContainer = ref;
    }

    /**
    * Get the transform expanded view active state.
    * @return {boolean} true if transform expanded view is active.
    * @memberof DesignView
    */
    getTransformActive() {
        return this.state.isTransformActive;
    }

    /**
    * Set the transform expanded view active state.
    * @param {boolean} isTransformActive - whether transform expanded view is active.
    * @param {ASTNode} activeTransformSignature - signature of the transformer node related to expanded transform
*                                                   statement.
    * @memberof DesignView
    */
    setTransformActive(isTransformActive, activeTransformSignature) {
        if (this.state.isTransformActive === isTransformActive &&
            this.state.activeTransformSignature === activeTransformSignature) {
            return;
        }

        this.setState({
            isTransformActive,
            activeTransformSignature,
        });
    }

    /**
    * Set the active transformer signature
    * @param {string} activeTransformSignature - signature of the transformer node related to expanded transformer
    *                                           statement.
    * @memberof DesignView
    */
    setActiveTransformerSignature(activeTransformSignature) {
        this.setState({
            activeTransformSignature,
        });
    }

    getDiagramContainer() {
        return this.diagramContainer;
    }

    getGraphicalEditorContainer() {
        return this.graphicalEditorContainer;
    }

    setGraphicalEditorContainer(ref) {
        this.graphicalEditorContainer = ref;
    }

    setOverlayContainer(ref) {
        this.overlayContainer = ref;
    }

    getOverlayContainer() {
        return this.overlayContainer;
    }

    setToolPaletteContainer(ref) {
        this.toolPaletteContainer = ref;
    }

    getToolPaletteContainer() {
        return this.toolPaletteContainer;
    }

    mouseUpHandle(event) {
        if (this.state.dragging) {
            this.diagramContainer.classList.remove('grabbing');
            this.setState({
                dragging: false,
            });
        }
    }

    mouseDownHandle(event) {
        if (!this.state.dragging) {
            this.setState({
                dragging: true,
            });
            this.lastClientX = event.clientX;
            this.lastClientY = event.clientY;

            this.diagramContainer.classList.add('grabbing');
            event.preventDefault();
        }
    }

    mouseMoveHandle(event) {
        if (this.state.dragging) {
            this.diagramContainer.classList.add('grabbing');

            const valueX = (this.scrollbars.getScrollLeft() - (-this.lastClientX + (this.lastClientX = event.clientX)));
            const valueY = (this.scrollbars.getScrollTop() - (-this.lastClientY + (this.lastClientY = event.clientY)));

            this.scrollbars.scrollLeft(valueX);
            this.scrollbars.scrollTop(valueY);
        }
    }

    zoomInHandle() {
        this.setState({
            zoom: this.state.zoom + 0.1,
        });
    }

    zoomFitHandle() {
        this.setState({
            zoom: 1,
        });
    }

    zoomOutHandle(event) {
        const scaleRatio = this.state.zoom - 0.1;

        if (scaleRatio >= 1) {
            this.setState({
                zoom: scaleRatio,
            });
        }
    }

    render() {
        const { isTransformActive, activeTransformSignature } = this.state;

        let activeTransformModel;
        if (isTransformActive) {
            activeTransformModel = this.props.model.filterTopLevelNodes(
                node => (TreeUtil.isTransformer(node))).find(node =>
                    (node.getSignature() === activeTransformSignature));
        }

        const shouldShowTransform = isTransformActive && activeTransformModel;

        const disabled = (this.props.disabled) ? 'design-view-disabled' : '';

        return (
            <div
                className={`design-view-container ${disabled}`}
                style={{ display: this.props.show ? 'block' : 'none' }}
            >
                <div className='outerCanvasDiv'>
                    <DragLayer />
                    <DiagramMenu
                        width={this.props.width}
                        model={this.props.model}
                        onModeChange={(data) => { this.props.onModeChange(data); }}
                        onCodeExpandToggle={(data) => { this.props.onCodeExpandToggle(data); }}
                        zoomInHandle={(e) => { this.zoomInHandle(e); }}
                        zoomOutHandle={(e) => { this.zoomOutHandle(e); }}
                        zoomFitHandle={(e) => { this.zoomFitHandle(e); }}
                        editMode={this.props.editMode}
                        mode={this.props.mode}
                    />
                    <Scrollbars
                        style={{
                            width: this.props.width,
                            height: this.props.height,
                        }}
                        renderThumbVertical={props => <div {...props} className='scrollbar-thumb-vertical' />}
                        renderTrackVertical={props => <div {...props} className='scrollbar scrollbar-track-vertical' />}
                        renderThumbHorizontal={props => <div {...props} className='scrollbar-thumb-horizontal' />}
                        renderTrackHorizontal={props => <div {...props} className='scrollbar scrollbar-track-horizontal' />}
                        ref={(scrollbars) => { this.scrollbars = scrollbars; }}
                        onScrollFrame={this.onScroll}
                        onMouseUp={(e) => { this.mouseUpHandle(e); }}
                        onMouseDown={(e) => { this.mouseDownHandle(e); }}
                    >
                        <div className='canvas-container'>
                            <div className='canvas-top-controls-container' />
                            <div className='html-overlay' ref={this.setOverlayContainer} />
                            <div className='graphical-editor' ref={this.setGraphicalEditorContainer} />
                            <div className='diagram root grabbable' ref={this.setDiagramContainer} >
                                {this.props.model &&
                                    <DesignViewErrorBoundary>
                                        <BallerinaDiagram
                                            model={this.props.model}
                                            mode={this.props.mode}
                                            width={this.props.width}
                                            height={this.props.height}
                                            disabled={this.props.disabled}
                                            editMode={this.props.editMode}
                                            zoom={this.state.zoom}
                                        />
                                        <GraphicalEditor
                                            model={this.props.model}
                                            mode={this.props.mode}
                                            editMode={this.props.editMode}
                                        />
                                    </DesignViewErrorBoundary>
                                }
                            </div>
                        </div>
                    </Scrollbars>
                    {shouldShowTransform &&
                        <TransformerExpanded
                            model={activeTransformModel}
                            panelResizeInProgress={this.props.panelResizeInProgress}
                            width={this.props.width}
                            height={this.props.height}
                        />
                    }
                </div>
            </div>
        );
    }
}

DesignView.propTypes = {
    show: PropTypes.bool,
    model: PropTypes.instanceOf(CompilationUnitNode),
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
        getCommands: PropTypes.func.isRequired,
    }),
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    disabled: PropTypes.bool.isRequired,
    editMode: PropTypes.bool,
    mode: PropTypes.string.isRequired,
};

DesignView.defaultProps = {
    show: true,
    model: undefined,
    commandProxy: {
        on: () => undefined,
        dispatch: () => undefined,
        getCommands: () => [],
    },
    disabled: false,
    editMode: true,
};

DesignView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    isPreviewViewEnabled: PropTypes.bool.isRequired,
};

DesignView.childContextTypes = {
    designView: PropTypes.instanceOf(DesignView).isRequired,
    getDiagramContainer: PropTypes.instanceOf(Object).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    getGraphicalEditorContainer: PropTypes.instanceOf(Object).isRequired,
    editMode: PropTypes.bool,
};

export default DesignView;
