import React from 'react';
import PropTypes from 'prop-types';
import BallerinaDiagram from './../components/diagram';
import DragDropManager from '../tool-palette/drag-drop-manager';
import MessageManager from './../visitors/message-manager';
import BallerinaASTRoot from './../ast/ballerina-ast-root';
import ToolPaletteView from './../tool-palette/tool-palette-view';

class DesignView extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            isTransformActive: false
        }
        this.overlayContainer = undefined;
        this.diagramContainer = undefined;
        this.toolPaletteContainer = undefined;
        this.setOverlayContainer = this.setOverlayContainer.bind(this);
        this.getOverlayContainer = this.getOverlayContainer.bind(this);
        this.setDiagramContainer = this.setDiagramContainer.bind(this);
        this.getDiagramContainer = this.getDiagramContainer.bind(this);
        this.setToolPaletteContainer = this.setToolPaletteContainer.bind(this);
        this.getToolPaletteContainer = this.getToolPaletteContainer.bind(this);
    }

    setDiagramContainer(ref) {
        this.diagramContainer = ref;
    }

    setTransformActive(isTransformActive) {
        this.setState({
            isTransformActive,
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

    /**
     * @override
     * @memberof Diagram
     */
    getChildContext() {
        return {
            designView: this,
            dragDropManager: new DragDropManager(),
            messageManager: new MessageManager(),
            getOverlayContainer: this.getOverlayContainer,
            getDiagramContainer: this.getDiagramContainer,
        };
    }

    render() {
        return (
            <div className="design-view-container">
                <div className="canvas-container">
                    <div className="canvas-top-controls-container"></div>
                    <div className="html-overlay" ref={this.setOverlayContainer} />
                    <div className="diagram root" ref={this.setDiagramContainer} >
                        <BallerinaDiagram model={this.props.model} />
                    </div>
                </div>
                <div className="tool-palette-container" ref={this.setToolPaletteContainer}>
                    <ToolPaletteView getContainer={this.getToolPaletteContainer} 
                        isTransformActive={this.state.isTransformActive} />
                </div>
                <div className="top-right-controls-container">
                    <div className={`top-right-controls-container-editor-pane 
                            main-action-wrapper import-packages-pane`}
                    >
                        <div className="action-content-wrapper">
                            <div className="action-content-wrapper-heading import-wrapper-heading">
                                <span>Import :</span>
                                <input id="import-package-text" type="text" />
                                <div className="action-icon-wrapper">
                                    <span className="fw-stack fw-lg">
                                        <i className="fw fw-square fw-stack-2x" />
                                        <i className="fw fw-add fw-stack-1x fw-inverse" />
                                    </span>
                                </div>
                            </div>
                            <div className="action-content-wrapper-body">
                                <div className="imports-wrapper">
                                    <span className="font-bold">Current Imports </span>
                                    <hr />
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
                <div className="bottom-right-controls-container">
                    <div className="view-source-btn btn-icon">
                        <div className="bottom-label-icon-wrapper">
                            <i className="fw fw-code-view fw-inverse" />
                        </div>
                        <div className="bottom-view-label" 
                                onClick={ () => {
                                    this.context.editor.setActiveView('SOURCE_VIEW');
                                } }>
                            Source View
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

DesignView.propTypes = {
    model: PropTypes.instanceOf(BallerinaASTRoot).isRequired,
};

DesignView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

DesignView.childContextTypes = {
    designView: PropTypes.instanceOf(DesignView).isRequired,
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
    getDiagramContainer: PropTypes.instanceOf(Object).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
};

export default DesignView;
