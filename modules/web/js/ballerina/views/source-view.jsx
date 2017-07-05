import React from 'react';

class SourceView extends React.Component {

    render() {
        return (
            <div className="source-view-container">
                <div className="bottom-right-controls-container">
                    <div className="view-design-btn btn-icon">
                        <div className="bottom-label-icon-wrapper">
                            <i className="fw fw-design-view fw-inverse" />
                        </div>
                        <div className="bottom-view-label">
                            Design View
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

export default SourceView;
