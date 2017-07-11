import React from 'react';
import _ from 'lodash';
import $ from 'jquery';
import PropTypes from 'prop-types';
import { DESIGN_VIEW, SOURCE_VIEW } from './constants';
import SwaggerJsonVisitor from './../visitors/swagger-json-gen/service-definition-visitor';

const SwaggerEditorBundle = global.SwaggerEditorBundle;

// look & feel configurations FIXME: Make this overridable from settings
let theme = 'ace/theme/tomorrow_night';
let fontSize = '14px';

class SwaggerView extends React.Component {

    constructor(props) {
        super(props);
        this.container = undefined;
        this.swagger = undefined;
        this.swaggerEditorID = undefined;
        this.swaggerEditor = undefined;
        this.genSwaggerAndID();
    }

    /**
     * Generate the swagger for current service & the unique ID for the editor
     */
    genSwaggerAndID() {
        if (!_.isNil(this.props.targetService)) {
            const swaggerJsonVisitor = new SwaggerJsonVisitor();
            this.props.targetService.accept(swaggerJsonVisitor);
            this.swagger = swaggerJsonVisitor.getSwaggerJson();
            this.swaggerEditorID = `${this.props.targetService.id}-swagger-editor`;
        } else {
            this.swagger = undefined;
            this.swaggerEditorID = undefined;
        }
    }

    /**
     * This lifecycle hook will be triggerred
     * when ever the props object is changed
     * This is invoked prior to shouldComponentUpdate
     */
    componentWillReceiveProps(newProps) {
        this.genSwaggerAndID();
    }

    componentDidMount() {
        if (!_.isNil(this.props.targetService)) {
            $(this.container).empty();
            this.swaggerEditor = SwaggerEditorBundle({
                dom_id: `#${this.swaggerEditorID}`,
            });       
        }
    }

    render() {
        return (
            <div className="swagger-view-container">
                <div className="swaggerEditor"
                    id={this.swaggerEditorID}
                    ref={(ref) => { this.container = ref }}
                    data-editor-url="lib/swagger-editor/#/" 
                >
                </div>
                <div className="bottom-right-controls-container">
                    <div className="view-design-btn btn-icon">
                        <div className="bottom-label-icon-wrapper">
                            <i className="fw fw-design-view fw-inverse" />
                        </div>
                        <div className="bottom-view-label"
                                onClick={
                                    () => {
                                        this.context.editor.setActiveView(DESIGN_VIEW);
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
                                        this.context.editor.setActiveView(SOURCE_VIEW);
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
