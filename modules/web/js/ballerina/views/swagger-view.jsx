import React from 'react';

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
                        <div className="bottom-view-label">Design View</div>
                    </div>
                    <div className="view-source-btn btn-icon">
                        <div className="bottom-label-icon-wrapper">
                            <i className="fw fw-code-view fw-inverse" />
                        </div>
                        <div className="bottom-view-label">Source View</div>
                    </div>
                </div>
            </div>
        );
    }
}

export default SwaggerView;
