import React from 'react';
import PropTypes from 'prop-types';

class SourceView extends React.Component {

    constructor (props) {
        super (props);
    }

    shouldComponentUpdate () {
        // keep this component untouched from react
        return false;
    }

    render() {
        return (
            <div className="source-view-container">
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
                                }>
                            Design View
                        </div>
                    </div>
                </div>
            </div>
        );
    }
}

SourceView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default SourceView;
