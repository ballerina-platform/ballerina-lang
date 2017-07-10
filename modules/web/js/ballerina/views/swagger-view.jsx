import React from 'react';
import PropTypes from 'prop-types';

class SwaggerView extends React.Component {

    render() {
        return (
            <div className="swagger-view-container">
                <div className="swaggerEditor" data-editor-url="lib/swagger-editor/#/" />
                <div className="bottom-right-controls-container">
                    <div className="view-design-btn btn-icon">
                        <div className="bottom-label-icon-wrapper">
                            <i className="fw fw-design-view fw-inverse" />
                        </div>
                        <div className="bottom-view-label"
                                onClick={
                                    () => {
                                        this.context.editor.setActiveView('DESIGN_VIEW');
                                    }
                                }
                        >
                                Design View
                        </div>
                    </div>
                    <div className="view-source-btn btn-icon">
                        <div className="bottom-label-icon-wrapper">
                            <i className="fw fw-code-view fw-inverse" />
                        </div>
                        <div className="bottom-view-label"
                                onClick={
                                    () => {
                                        this.context.editor.setActiveView('SOURCE_VIEW');
                                    }
                                }
                        >
                                Source View
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

SwaggerView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default SwaggerView;
