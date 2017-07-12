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
        this.swaggerAce = undefined;
    }

    /**
     * Generate the swagger spec for current service & the unique ID for the editor
     */
    genSwaggerAndID() {
        if (!_.isNil(this.props.targetService)) {
            const swaggerJsonVisitor = new SwaggerJsonVisitor();
            this.props.targetService.accept(swaggerJsonVisitor);
            this.swagger = swaggerJsonVisitor.getSwaggerJson();
            this.swaggerEditorID = `z-${this.props.targetService.id}-swagger-editor`;
        } else {
            this.swagger = undefined;
            this.swaggerEditorID = undefined;
        }
    }

    /**
     * When this component is initially rendered, it won't have
     * a targetService. However, when a user clicks swagger-view 
     * icon of a service, we update the state of parent component 
     * and it will eventually provide a new set of props to this.
     * 
     * Since we have disabled react rerender for this component
     * {@see SwaggerView#shouldComponentUpdate},
     * we have to grab those new props and update the component
     * manually.
     * 
     */
    componentWillReceiveProps(newProps) {
        if (!_.isNil(newProps.targetService)) {
            this.props = newProps;
            this.genSwaggerAndID();
            this.renderSwaggerEditor();
        }
    }

    renderSwaggerEditor() {
        if (!_.isNil(this.props.targetService)) {
            const $container = $(this.container);
            $container.empty();
            $container.attr('id', this.swaggerEditorID);
            this.swaggerEditor = SwaggerEditorBundle({
                dom_id: `#${this.swaggerEditorID}`,
            });
            const $swaggerAceContainer = $container.find('#ace-editor');
            const swaggerAceContainerID = `z-ace-editor-${this.swaggerEditorID}`;
            $swaggerAceContainer.attr('id', swaggerAceContainerID);
            const swaggerAce = ace.edit(swaggerAceContainerID);

            swaggerAce.$blockScrolling = Infinity;
            swaggerAce.setTheme(ace.acequire(theme));
            swaggerAce.setFontSize(fontSize);
            this.swaggerAce = swaggerAce;
            this.updateSwaggerEditorWithCurrentSwaggerSpec();
        }
    }

    updateSwaggerEditorWithCurrentSwaggerSpec() {
        this.swaggerEditor.specActions.updateUrl('');
        this.swaggerEditor.specActions.updateLoadingStatus('success');
        this.swaggerEditor.specActions.updateSpec(JSON.stringify(this.swagger));
        this.swaggerEditor.specActions.formatIntoYaml();
    }

    shouldComponentUpdate() {
        // prevent this component being re-rendered by react
        return false;
    }

    /**
     * Put the required foundation to render swagger editor
     */
    render() {
        return (
            <div className="swagger-view-container">
                <div className="swaggerEditor"
                    // keep the ref to this element as the container ref
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
