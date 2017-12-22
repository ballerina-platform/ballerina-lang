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
import cn from 'classnames';
import { Scrollbars } from 'react-custom-scrollbars';
import BallerinaDiagram from 'plugins/ballerina/diagram/diagram';
import TransformExpanded from 'plugins/ballerina/diagram/views/default/components/transform/transform-expanded';
import TreeUtil from 'plugins/ballerina/model/tree-util.js';
import DragLayer from './../drag-drop/drag-layer';
import CompilationUnitNode from './../model/tree/compilation-unit-node';
import { TOOL_PALETTE_WIDTH } from './constants';

class DesignView extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isTransformActive: false,
            mode: 'default',
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
        this.props.commandProxy.on('diagram-mode-change', ({ mode }) => {
            this.setMode(mode);
        });
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
        this.props.commandProxy.dispatch('scroll-design-view', {
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

    setMode(diagramMode) {
        this.setState({ mode: diagramMode });
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

        return (
            <div className='design-view-container' style={{ display: this.props.show ? 'block' : 'none' }}>
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
                                    <BallerinaDiagram
                                        model={this.props.model}
                                        mode={this.state.mode}
                                        width={this.props.width - TOOL_PALETTE_WIDTH}
                                        height={this.props.height}
                                    />
                                }
                            </div>
                        </div>
                    </Scrollbars>
                    {shouldShowTransform &&
                        <TransformExpanded
                            model={activeTransformModel}
                            panelResizeInProgress={this.props.panelResizeInProgress}
                            leftOffset={TOOL_PALETTE_WIDTH}
                            width={this.props.width - TOOL_PALETTE_WIDTH}
                            height={this.props.height}
                        />
                    }
                </div>
                <div className='tool-palette-container' ref={this.setToolPaletteContainer} />
                <div className={cn('bottom-right-controls-container', { hide: this.context.isPreviewViewEnabled })}>
                    <div
                        className='view-source-btn btn-icon'
                        onClick={() => {
                            this.context.editor.setActiveView('SOURCE_VIEW');
                        }}
                    >
                        <div className='bottom-label-icon-wrapper'>
                            <i className='fw fw-code-view fw-inverse' />
                        </div>
                        <div
                            className='bottom-view-label'
                            onClick={() => {
                                this.context.editor.setActiveView('SOURCE_VIEW');
                            }}
                        >
                            Source View
                        </div>
                    </div>
                    <div
                        className='view-split-view-btn btn-icon'
                        onClick={() => {
                            this.props.commandProxy.dispatch('show-split-view', true);
                        }}
                    >
                        <div className='bottom-label-icon-wrapper'>
                            <i className='fw fw-code fw-inverse' />
                        </div>
                        <div
                            className='bottom-view-label'
                            onClick={() => {
                                this.props.commandProxy.dispatch('show-split-view', true);
                            }}
                        >
                            Split View
                        </div>
                    </div>
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
    }).isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
};

DesignView.defaultProps = {
    show: true,
    model: undefined,
};

DesignView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    isPreviewViewEnabled: PropTypes.bool.isRequired,
};

DesignView.childContextTypes = {
    designView: PropTypes.instanceOf(DesignView).isRequired,
    getDiagramContainer: PropTypes.instanceOf(Object).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
};

export default DesignView;
