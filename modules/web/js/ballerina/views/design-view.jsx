import React from 'react';
import PropTypes from 'prop-types';
import BallerinaDiagram from './../components/diagram';
import DragDropManager from '../tool-palette/drag-drop-manager';
import MessageManager from './../visitors/message-manager';
import BallerinaASTRoot from './../ast/ballerina-ast-root';

class DesignView extends React.Component {

    /**
     * @override
     * @memberof Diagram
     */
    getChildContext() {
        return {
            dragDropManager: new DragDropManager(),
            messageManager: new MessageManager(),
            overlayContainerSelector: '.html-overlay',
        };
    }

    render() {
        return (
            <div className="design-view-container">
                <div className="canvas-container">
                    <div className="canvas-top-controls-container"></div>
                    <div className="diagram root" >
                        <BallerinaDiagram model={this.props.model} />
                    </div>
                    <div className="html-overlay" ></div>
                </div>
                <div className="tool-palette-container" />
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
                        <div className="bottom-view-label">
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
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
    overlayContainerSelector: PropTypes.string.isRequired,
};

export default DesignView;
