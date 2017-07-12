import React from 'react';
import _ from 'lodash';
import $ from 'jquery';
import * as YAML from 'js-yaml';
import PropTypes from 'prop-types';
import { DESIGN_VIEW, SOURCE_VIEW } from './constants';
import SwaggerJsonVisitor from './../visitors/swagger-json-gen/service-definition-visitor';
import SwaggerParser from './../../swagger-parser/swagger-parser';
import ConflictModal from './swagger/components/conflict-modal';
import ConflictMergeModal from './swagger/components/conflict-merge-modal';

const ace = global.ace;
const SwaggerEditorBundle = global.SwaggerEditorBundle;
const AceUndoManager = ace.acequire('ace/undomanager').UndoManager;

// override default undo manager of swagger editor
class CustomUndoManager extends AceUndoManager {
    constructor(onExecute) {
        super();
        this.onExecute = onExecute;
    }
    execute(args) {
        super.execute(args);
        this.onExecute(args);
    }
}

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
        this.resourceMappings = new Map();
        this.onEditorChange = this.onEditorChange.bind(this);
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

            swaggerAce.getSession().setUndoManager(new CustomUndoManager(this.onEditorChange));
            swaggerAce.$blockScrolling = Infinity;
            swaggerAce.setTheme(ace.acequire(theme));
            swaggerAce.setFontSize(fontSize);
            this.swaggerAce = swaggerAce;
            this.syncSpec();
        }
    }
    
    syncSpec() {
        this.swaggerEditor.specActions.updateUrl('');
        this.swaggerEditor.specActions.updateLoadingStatus('success');
        this.swaggerEditor.specActions.updateSpec(JSON.stringify(this.swagger));
        this.swaggerEditor.specActions.formatIntoYaml();
    }

     /**
     * Merge the updated YAMLs to the service definition
     */
    updateService() {
        // we do not update the dom if swagger is not edited.
        if (!this.swaggerAce.getSession().getUndoManager().isClean()) {
            const swaggerParser = new SwaggerParser(this.swagger, false);
            swaggerParser.mergeToService(this.props.targetService);
        }
    }

    onEditorChange(evt) {
        // keep swagger spec up-to-date
        this.swagger = YAML.safeLoad(this.swaggerAce.getValue());

        // IMPORTANT - Check the delta and if this is a single line change,
        // update Service node on demand 
        // Note: Porting the logic in deprecrated swagger-view module here
        if (_.has(evt, 'action') && _.isEqual(evt.action, 'aceupdate')) {
            // Note: Accessing the last delta from deltas in undoable operation
            const lastDelta = _.last(_.get(_.first(_.first(_.get(evt, 'args'))), 'deltas'));
            if (_.isEqual(lastDelta.start.row, lastDelta.end.row)) {
                this.updateResourceMappings(this.props.targetService, lastDelta);
            }
        }
    }

    shouldComponentUpdate() {
        // prevent this component being re-rendered by react
        return false;
    }

    /**
     * Render the required foundation to init swagger editor
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
                                        if (!this.hasSwaggerErrors()) {
                                            this.updateService();
                                            this.context.editor.setActiveView(DESIGN_VIEW);
                                        }
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
                                        if (!this.hasSwaggerErrors()) {
                                            this.updateService();
                                            this.context.editor.setActiveView(SOURCE_VIEW);
                                        }
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

    /**
     * Updates mappings of a resource with ace editor event
     *
     * @param {ServiceDefinition} serviceDefinition The service definition which has the resource definitions.
     * @param {Object} editorEvent The ace onChange event.
     * @return {boolean} True if value is updated, else false.
     *
     * @memberof SwaggerView
     */
    updateResourceMappings(serviceDefinition, editorEvent) {
        // If there are no errors.
        const astPath = this.swaggerEditor.fn.AST.pathForPosition(this.swaggerAce.getValue(), {
            line: editorEvent.start.row,
            column: editorEvent.start.column,
        });

        if (!_.isUndefined(astPath) && _.isArray(astPath)) {
            if ((_.size(astPath) === 4 && _.isEqual(astPath[3], 'operationId')) ||
                (this.resourceMappings.has(editorEvent.start.row) &&
                    this.resourceMappings.get(editorEvent.start.row).type === 'operationId')) {
                this.updateOperationID(astPath, serviceDefinition, editorEvent);
                return true;
            } else if (_.size(astPath) === 1 && _.isEqual(astPath[0], 'paths')) {
                this.updatePath(astPath, serviceDefinition, editorEvent);
                return true;
            } else if (_.size(astPath) === 2) {
                this.updateHttpMethod(astPath, serviceDefinition, editorEvent);
                return true;
            }
            return false;
        } else if (_.isNull(astPath) && this.resourceMappings.has(editorEvent.start.row)) {
            const mapping = this.resourceMappings.get(editorEvent.start.row);
            if (_.isEqual(mapping.type, 'path') && !_.isUndefined(mapping.ast)) {
                mapping.ast.getChildren()[0].setRightValue('""', { doSilently: true });
                return true;
            } else if (_.isEqual(mapping.type, 'method') && !_.isUndefined(mapping.ast)) {
                mapping.ast.setIdentifier('', { doSilently: true });
                return true;
            }
            return false;
        }

        return false;
    }

    /**
     * Updates the resource name that maps to a corresponding resource using the editors change event.
     *
     * @param {string[]} astPath The ast path
     * @param {ServiceDefinition} serviceDefinition The service definition which has the resource definitions
     * @param {Object} editorEvent The onchange event object of the ace editor
     *
     * @memberof SwaggerView
     */
    updateOperationID(astPath, serviceDefinition, editorEvent) {
        // Getting the old operation ID.
        let oldOperationID;
        if (_.isEqual(editorEvent.action, 'insert')) {
            const currentValue = this.swaggerAce.getSession().getLine(editorEvent.start.row);
            oldOperationID = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            const currentValue = this.swaggerAce.getSession().getLine(editorEvent.start.row);
            oldOperationID = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldOperationID = oldOperationID.trim();

        // Getting the new operation ID.
        const newOperationID = this.swaggerAce.getSession().getLine(editorEvent.end.row).trim();

        // Updating resource name .
        for (const resourceDefinition of serviceDefinition.getResourceDefinitions()) {
            // Using substring we can avoid having "operationdId: " prefix in the value.
            if (resourceDefinition.getResourceName() === oldOperationID.substring(13)) {
                resourceDefinition.setResourceName(newOperationID.substring(13));
                this.resourceMappings.set(editorEvent.start.row,
                    {
                        type: 'operationId',
                        ast: undefined,
                    });
                break;
            }
        }
    }

    /**
     * Updates the path value that maps to a corresponding resource using the editors change event.
     *
     * @param {string[]} astPath The ast path
     * @param {ServiceDefinition} serviceDefinition The service definition which has the resource definitions
     * @param {Object} editorEvent The onchange event object of the ace editor
     *
     * @memberof SwaggerView
     */
    updatePath(astPath, serviceDefinition, editorEvent) {
        // When resource path is modified.
        let oldResourcePath;
        if (_.isEqual(editorEvent.action, 'insert')) {
            const currentValue = this.swaggerAce.getSession().getLine(editorEvent.start.row);
            oldResourcePath = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            const currentValue = this.swaggerAce.getSession().getLine(editorEvent.start.row);
            oldResourcePath = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldResourcePath = oldResourcePath.trim().replace(/:\s*$/, '');

        const newResourcePath = this.swaggerAce.getSession().getLine(editorEvent.end.row).trim()
                                                                                                .replace(/:\s*$/, '');
        // Getting the httpMethod value
        for (let i = this.swaggerAce.getCursorPosition().row + 1; i <= this.swaggerAce
            .getSession().getLength(); i++) {
            const httpMethodLine = this.swaggerAce.getSession().getLine(i).trim().replace(/:\s*$/, '');
            if (!_.isEmpty(httpMethodLine)) {
                const httpMethod = httpMethodLine;

                // Get the operationId if exists.
                let operationId;
                const operationIDLineNumber = this.swaggerEditor.fn.AST.getLineNumberForPath(
                    this.swaggerAce.getValue(), astPath.concat([newResourcePath, httpMethodLine,
                        'operationId']));
                if (!_.isUndefined(operationIDLineNumber)) {
                    operationId = this.swaggerAce.getSession().getLine(operationIDLineNumber - 1).trim()
                                                                                        .replace(/operationId:\s/, '');
                }

                // Finding the resource to update
                let resourceDefinitionToUpdate;
                for (const resourceDefinition of serviceDefinition.getResourceDefinitions()) {
                    if (!_.isUndefined(operationId) && resourceDefinition.getResourceName() === operationId) {
                        // Getting the resource to update using the operation ID
                        resourceDefinitionToUpdate = resourceDefinition;
                        break;
                    } else {
                        // Getting the resource to update using the path and http method.
                        const currentResourcePathAnnotation = resourceDefinition.getPathAnnotation();
                        const currentResourcePath = currentResourcePathAnnotation.getChildren()[0].getRightValue()
                            .replace(/"/g, '');

                        const currentHttpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();

                        if (_.isEqual(currentResourcePath, oldResourcePath) &&
                            _.isEqual(httpMethod, currentHttpMethodAnnotation.getIdentifier().toLowerCase())) {
                            resourceDefinitionToUpdate = resourceDefinition;
                            break;
                        }
                    }
                }

                // Updating the path of the resource.
                if (resourceDefinitionToUpdate) {
                    resourceDefinitionToUpdate.getPathAnnotation().getChildren()[0].setRightValue(
                        JSON.stringify(newResourcePath), { doSilently: true });
                    this.resourceMappings.set(editorEvent.start.row,
                        {
                            type: 'path',
                            ast: resourceDefinitionToUpdate.getPathAnnotation(),
                        });
                }
                break;
            }
        }
    }

    /**
     * Updates the http method value that maps to a corresponding resource using the editors change event.
     *
     * @param {string[]} astPath The ast path
     * @param {ServiceDefinition} serviceDefinition The service definition which has the resource definitions
     * @param {Object} editorEvent The onchange event object of the ace editor
     *
     * @memberof SwaggerView
     */
    updateHttpMethod(astPath, serviceDefinition, editorEvent) {
        // When http method is updated
        let oldHttpMethod;
        if (_.isEqual(editorEvent.action, 'insert')) {
            const currentValue = this.swaggerAce.getSession().getLine(editorEvent.start.row);
            oldHttpMethod = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            const currentValue = this.swaggerAce.getSession().getLine(editorEvent.start.row);
            oldHttpMethod = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldHttpMethod = oldHttpMethod.trim().replace(/:\s*$/, '');

        const newHttpMethod = this.swaggerAce.getSession().getLine(editorEvent.end.row).trim()
                                                                                                .replace(/:\s*$/, '');
        const resourcePath = astPath[1];

        // Get the operationId if exists.
        let operationId;
        const operationIDLineNumber = this.swaggerEditor.fn.AST.getLineNumberForPath(
            this.swaggerAce.getValue(), astPath.concat([newHttpMethod, 'operationId']));
        if (!_.isUndefined(operationIDLineNumber)) {
            operationId = this.swaggerAce.getSession().getLine(operationIDLineNumber - 1).trim()
                                                                                        .replace(/operationId:\s/, '');
        }

        // Finding the resource to update
        let resourceDefinitionToUpdate;
        for (const resourceDefinition of serviceDefinition.getResourceDefinitions()) {
            if (!_.isUndefined(operationId) && resourceDefinition.getResourceName() === operationId) {
                // Getting the resource to update using the operation ID
                resourceDefinitionToUpdate = resourceDefinition;
                break;
            } else {
                // Getting the resource to update using the path and http method.
                const currentResourcePathAnnotation = resourceDefinition.getPathAnnotation();
                const currentHttpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();

                if (!_.isUndefined(currentResourcePathAnnotation) &&
                    !_.isUndefined(currentHttpMethodAnnotation) &&
                    _.isEqual(currentResourcePathAnnotation.getChildren()[0].getRightValue().toLowerCase()
                                                                    .replace(/"/g, ''), resourcePath.toLowerCase()) &&
                    _.isEqual(currentHttpMethodAnnotation.getIdentifier().toLowerCase().replace(/"/g, ''),
                                                                                        oldHttpMethod.toLowerCase())) {
                    resourceDefinitionToUpdate = resourceDefinition;
                    break;
                }
            }
        }

        // Updating the http method of the resource.
        if (resourceDefinitionToUpdate) {
            resourceDefinitionToUpdate.getHttpMethodAnnotation().setIdentifier(newHttpMethod.toUpperCase(),
                                                                                                { doSilently: true });
            this.resourceMappings.set(editorEvent.start.row,
                {
                    type: 'method',
                    ast: resourceDefinitionToUpdate.getHttpMethodAnnotation(),
                });
        }
    }

    /**
     * Returns the number of errors in the editor.
     *
     * @returns {boolean} True if errors exists, else false.
     * @memberof SwaggerView
     */
    hasSwaggerErrors() {
        return _.size(this.swaggerAce.getSession().getAnnotations());
    }
}

SwaggerView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default SwaggerView;
