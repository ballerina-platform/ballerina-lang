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
import { TOOL_PALETTE_WIDTH, ZOOM_LEVELS } from './constants';
import { EVENTS } from '../constants';
import DesignViewErrorBoundary from './DesignViewErrorBoundary';
import ViewButton from './view-button';

const zoomLevels = [ZOOM_LEVELS.FIT_TO_SCREEN, ZOOM_LEVELS.ACTION, ZOOM_LEVELS.DEFAULT];

class DesignView extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isTransformActive: false,
        };
        this.overlayContainer = undefined;
        this.diagramContainer = undefined;
        this.toolPaletteContainer = undefined;
        this.onScroll = this.onScroll.bind(this);
        this.setOverlayContainer = this.setOverlayContainer.bind(this);
        this.getOverlayContainer = this.getOverlayContainer.bind(this);
        this.setDiagramContainer = this.setDiagramContainer.bind(this);
        this.getDiagramContainer = this.getDiagramContainer.bind(this);
        this.setToolPaletteContainer = this.setToolPaletteContainer.bind(this);
        this.getToolPaletteContainer = this.getToolPaletteContainer.bind(this);
        this.zoomIn = this.zoomIn.bind(this);
        this.zoomOut = this.zoomOut.bind(this);
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
            fitToScreen: zoomLevels[this.props.zoomLevel] === ZOOM_LEVELS.FIT_TO_SCREEN,
            designView: this,
            getOverlayContainer: this.getOverlayContainer,
            getDiagramContainer: this.getDiagramContainer,
        };
    }

    shouldComponentUpdate(nextProps, nextState) {
        // Always re render when transform is active
        // Otherwise don't rerender for panel resizings
        return !nextProps.panelResizeInProgress || nextState.isTransformActive;
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

    zoomIn() {
        let newLevel = this.props.zoomLevel + 1;
        newLevel = (newLevel >= zoomLevels.length) ? zoomLevels.length - 1 : newLevel;
        this.props.setZoom(newLevel);
    }

    zoomOut() {
        let newLevel = this.props.zoomLevel - 1;
        newLevel = (newLevel < 0) ? 0 : newLevel;
        this.props.setZoom(newLevel);
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
        // For now, fit to screen mode is same as action for components
        const mode = zoomLevels[this.props.zoomLevel] === ZOOM_LEVELS.FIT_TO_SCREEN
                        ? 'action'
                        : zoomLevels[this.props.zoomLevel];
        return (
            <div
                className={`design-view-container ${disabled}`}
                style={{ display: this.props.show ? 'block' : 'none' }}
            >
                <div className='outerCanvasDiv'>
                    <DragLayer />
                    <Scrollbars
                        style={{
                            width: this.props.width - TOOL_PALETTE_WIDTH,
                            height: this.props.height,
                            marginLeft: TOOL_PALETTE_WIDTH,
                        }}
                        ref={(scrollbars) => { this.scrollbars = scrollbars; }}
                        onScrollFrame={this.onScroll}
                    >
                        <div className='canvas-container'>
                            <div className='canvas-top-controls-container' />
                            <div className='html-overlay' ref={this.setOverlayContainer} />
                            <div className='diagram root' ref={this.setDiagramContainer} >
                                {this.props.model &&
                                    <DesignViewErrorBoundary>
                                        <BallerinaDiagram
                                            model={this.props.model}
                                            mode={mode}
                                            width={this.props.width - TOOL_PALETTE_WIDTH}
                                            height={this.props.height}
                                            disabled={this.props.disabled}
                                        />
                                    </DesignViewErrorBoundary>
                                }
                            </div>
                        </div>
                    </Scrollbars>
                    <div className={'bottom-right-controls-container zoom-controls'}>
                        <ViewButton
                            label='Zoom In'
                            icon='add'
                            onClick={this.zoomIn}
                            active={!(this.props.zoomLevel === (zoomLevels.length - 1))}
                        />
                        <ViewButton
                            label='Zoom Out'
                            icon='minus'
                            onClick={this.zoomOut}
                            active={!(this.props.zoomLevel === 0)}
                        />
                    </div>
                    {shouldShowTransform &&
                        <TransformerExpanded
                            model={activeTransformModel}
                            panelResizeInProgress={this.props.panelResizeInProgress}
                            leftOffset={TOOL_PALETTE_WIDTH}
                            width={this.props.width - TOOL_PALETTE_WIDTH}
                            height={this.props.height}
                        />
                    }
                </div>
                <div className='tool-palette-container' ref={this.setToolPaletteContainer} />
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
    }).isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    disabled: PropTypes.bool.isRequired,
    zoomLevel: PropTypes.number,
    setZoom: PropTypes.func.isRequired,
};

DesignView.defaultProps = {
    show: true,
    model: undefined,
    disabled: false,
    zoomLevel: 1,

};

DesignView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    isPreviewViewEnabled: PropTypes.bool.isRequired,
};

DesignView.childContextTypes = {
    fitToScreen: PropTypes.bool,
    designView: PropTypes.instanceOf(DesignView).isRequired,
    getDiagramContainer: PropTypes.instanceOf(Object).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
};

export default DesignView;
