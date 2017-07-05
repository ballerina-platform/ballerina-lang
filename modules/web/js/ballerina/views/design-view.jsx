import React from 'react';

class DesignView extends React.Component {

    render() {
        return (
            <div className="design-view-container">
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

export default DesignView;
