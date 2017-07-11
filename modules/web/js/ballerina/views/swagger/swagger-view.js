/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import log from 'log';
import _ from 'lodash';
import $ from 'jquery';
import EventChannel from 'event_channel';
import React from 'react';
import ReactDOM from 'react-dom';
import * as YAML from 'js-yaml';
import SwaggerParser from '../../../swagger-parser/swagger-parser';
import ConflictModal from './components/conflict-modal';
import ConflictMergeModal from './components/conflict-merge-modal';
import SwaggerJsonVisitor from './../../visitors/swagger-json-gen/service-definition-visitor';

const ace = global.ace;
const SwaggerEditorBundle = global.SwaggerEditorBundle;

/**
 * Wraps the Swagger Editor for swagger view
 * @class SwaggerView
 * @extends EventChannel
 */
class SwaggerView extends EventChannel {

    /**
     * Constructor for SwaggerView
     * @param {Object} args - Rendering args for the view
     * @param {String} args.container - Selector for div element to render ace editor
     * @param {String} args.swaggerEditorTheme - The theme of the swagger editor.
     * @param {String} args.swaggerEditorFontSize - The font size of the swagger editor.
     * @constructor
     */
    constructor(args) {
        super();
        this._options = args;
        if (!_.has(args, 'container')) {
            log.error('Container is not specified for rendering swagger view.');
        }
        this._container = _.get(args, 'container');
        this._swaggerEditorTheme = _.get(args, 'swaggerEditorTheme');
        this._swaggerEditorFontSize = _.get(args, 'swaggerEditorFontSize');
        this._manualUpdate = false;
        this._swaggerData = undefined;
        this._resourceMappings = new Map();
        this._pasteTriggered = false;
    }

    /**
     * Set the content of swagger editor.
     * @param {Object} swaggerInfos - An object array which contains service definition ASTs and their swagger
     * definitions.
     * @param {ServiceDefinition} swaggerInfos.serviceDefinitionAST The Service definition.
     * @param {Object} swaggerInfos.swagger The JSON swagger definition.
     */
    render(swaggerInfos) {
        this._swaggerData = swaggerInfos;
        // Creating the swagger editor
        const swaggerEditorElement = $(this._container).find('div.swaggerEditor');
        const swaggerElementID = `z-${this._options.swaggerEditorId}`;
        swaggerEditorElement.attr('id', swaggerElementID);
        this._swaggerEditor = SwaggerEditorBundle({
            dom_id: `#${swaggerElementID}`,
        });

        const swaggerAceEditorElement = swaggerEditorElement.find('#ace-editor');
        swaggerAceEditorElement.attr('id', `z-ace-editor-${this._options.swaggerEditorId}`);
        this._swaggerAceEditor = ace.edit(`z-ace-editor-${this._options.swaggerEditorId}`);

        this._swaggerAceEditor.$blockScrolling = Infinity;
        const editorTheme = ace.acequire(this._swaggerEditorTheme);
        this._swaggerAceEditor.setTheme(editorTheme);
        this._swaggerAceEditor.setFontSize(this._swaggerEditorFontSize);

        this.updateSpecView(this._swaggerData.swagger);

        this._swaggerAceEditor.getSession().on('change', (e) => {
            // @todo - set a dirty state in the model. need to refactor.
            this.swaggerDirty = true;

            try {
                if (this._manualUpdate === false) {
                    const serviceDefinition = this._swaggerData.serviceDefinitionAST;
                    if (_.isEqual(e.start.row, e.end.row)) {
                        this._updateResourceMappings(serviceDefinition, e);
                    }

                    // We keep updating the yaml for a service instead of merging to the service AST
                    if (_.isEqual(this._swaggerData.serviceDefinitionAST.getID(), serviceDefinition.getID())) {
                        this._swaggerData.swagger = YAML.safeLoad(this._swaggerAceEditor.getValue());
                    }

                    // Pasting a content within replacing lines will trigger onChange twice, once with 'remove' action
                    // and another with 'insert'. Hence we are updating the services only once and not on both events.
                    if (!(this._pasteTriggered && e.action === 'remove')) {
                        this._pasteTriggered = false;
                        // this.updateServices();
                    }
                }
            } catch (error) {
                log.warn(error);
            }
        });

        // Keep tracking paste events to avoid triggering onChange twice.
        this._swaggerAceEditor.on('paste', () => {
            this._pasteTriggered = true;
        });
    }

    /**
     * Merge the updated YAMLs to the service definitions.
     */
    updateServices() {
        // we do not update the dom if swagger is not edited.
        if (this.swaggerDirty) {
            const swaggerParser = new SwaggerParser(this._swaggerData.swagger, false);
            swaggerParser.mergeToService(this._swaggerData.serviceDefinitionAST);
            // setTimeout(() => {
            //     if (!this.hasSwaggerErrors()) {
            //         const swaggerParser = new SwaggerParser(this._swaggerData.swagger, false);
            //         // Keep the original service defintion as ref.
            //         const originalServiceDef = this._swaggerData.serviceDefinitionAST;
            //         // Make a copy of the service definition.
            //         const updatedServiceDef = _.cloneDeep(this._swaggerData.serviceDefinitionAST);
            //         // Removing all resourceDefs so that the resources for the new swagger def are created.
            //         updatedServiceDef.getResourceDefinitions().forEach((resourceDef) => {
            //             updatedServiceDef.removeChild(resourceDef);
            //         });

            //         // Merging will allows to have resources that maps to the new swagger json.
            //         swaggerParser.mergeToService(updatedServiceDef);

            //         const missingOriginalResourceDefs = originalServiceDef.getResourceDefinitions().filter(
            //             (originalResourceDef) => {
            //                 let isMissingResource = true;
            //                 updatedServiceDef.getResourceDefinitions().forEach((updatedResourceDef) => {
            //                     if (SwaggerView.isSameResource(originalResourceDef, updatedResourceDef)) {
            //                         isMissingResource = false;
            //                     }
            //                 });
            //                 return isMissingResource;
            //             });

            //         if (missingOriginalResourceDefs.length > 0) {
            //             this.showMergeConflictModal({
            //                 originalServiceDef,
            //                 missingOriginalResourceDefs,
            //                 swaggerParser,
            //             });
            //         } else {
            //             swaggerParser.mergeToService(originalServiceDef);
            //         }
            //     }
            //     // Wait till any errors come from the swagger editor as there is an inbuilt delay.
            // }, 2000);
        }
    }

    /**
     * Shows the merge conflict modal.
     * @param {Object} args Object needed for building the modals.
     * @param {ServiceDefinition} args.originalServiceDef The original service definition which hasnt been updated.
     * @param {ResourceDefinition[]} args.missingOriginalResourceDefs The missing resource defintions which could not be
     *  mapped.
     * @param {SwaggerParser} args.swaggerParser Swagger parser which has the new swagger json.
     * @memberof SwaggerView
     */
    showMergeConflictModal(args) {
        const {
            originalServiceDef,
            missingOriginalResourceDefs,
            swaggerParser,
        } = args;

        const modalContainerWrapperClass = 'swagger-modal-container';

        const modelContainersToDelete = document.body.getElementsByClassName(modalContainerWrapperClass);
        for (let i = 0; i < modelContainersToDelete.length; i++) {
            document.body.removeChild(modelContainersToDelete[i]);
        }

        const conflictModalPopUpWrapper = document.createElement('div');
        conflictModalPopUpWrapper.className = modalContainerWrapperClass;
        document.body.appendChild(conflictModalPopUpWrapper);

        const moreOptionsFunc = () => {
            ReactDOM.unmountComponentAtNode(conflictModalPopUpWrapper);
            args.swaggerView = this;
            const conflictMergeModal = React.createElement(ConflictMergeModal, args, null);
            ReactDOM.render(conflictMergeModal, conflictModalPopUpWrapper);
        };

        const keepResourcesFunc = () => {
            swaggerParser.mergeToService(originalServiceDef);
            const swaggerJsonVisitor = new SwaggerJsonVisitor();
            originalServiceDef.accept(swaggerJsonVisitor);
            this._swaggerData.swagger = swaggerJsonVisitor.getSwaggerJson();
            this.updateSpecView(this._swaggerData.swagger);
        };

        const conflictModal = React.createElement(ConflictModal, {
            missingOriginalResourceDefs,
            originalServiceDef,
            moreOptionsFunc,
            keepResourcesFunc,
            swaggerParser,
        }, null);

        ReactDOM.render(conflictModal, conflictModalPopUpWrapper);
    }

    /**
     * Checks whether it is the same resource using a combination of http method and path value.
     *
     * @static
     * @param {ResourceDefinition} resourceDef The resource definition.
     * @param {ResourceDefinition} resourceDefToCompare The resource definition to compare with.
     * @returns {boolean} true if same resource, else false.
     * @memberof SwaggerView
     */
    static isSameResource(resourceDef, resourceDefToCompare) {
        const matchingHttpMethod = resourceDef.getHttpMethodAnnotation() &&
                                                        resourceDef.getHttpMethodAnnotation().getIdentifier() ===
                                                        resourceDefToCompare.getHttpMethodAnnotation().getIdentifier();
        const matchingPath = resourceDef.getPathAnnotation() &&
                                            resourceDef.getPathAnnotation().getChildren()[0].getRightValue() ===
                                            resourceDefToCompare.getPathAnnotation().getChildren()[0].getRightValue();
        return matchingHttpMethod && matchingPath;
    }

    /**
     * Updates the swagger spec
     *
     * @param {Object} swagger Swagger JSON.
     * @memberof SwaggerView
     */
    updateSpecView(swagger) {
        this._swaggerEditor.specActions.updateUrl('');
        this._swaggerEditor.specActions.updateLoadingStatus('success');
        this._swaggerEditor.specActions.updateSpec(JSON.stringify(swagger));
        this._swaggerEditor.specActions.formatIntoYaml();
    }

    /**
     * Set the default node tree.
     * @param {Object} root root node.
     *
     */
    setNodeTree(root) {
        this._generatedNodeTree = root;
    }

    /**
     * Shows the swagger container.
     *
     * @memberof SwaggerView
     */
    show() {
        $(this._container).show();
    }

    /**
     * Hides the swagger container.
     *
     * @memberof SwaggerView
     */
    hide() {
        $(this._container).hide();
    }

    /**
     * Check if the swagger view shown.
     *
     * @returns {boolean} True if shown, else false.
     * @memberof SwaggerView
     */
    isVisible() {
        return $(this._container).is(':visible');
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
    _updateResourceMappings(serviceDefinition, editorEvent) {
        // If there are no errors.
        const astPath = this._swaggerEditor.fn.AST.pathForPosition(this._swaggerAceEditor.getValue(), {
            line: editorEvent.start.row,
            column: editorEvent.start.column,
        });

        if (!_.isUndefined(astPath) && _.isArray(astPath)) {
            if ((_.size(astPath) === 4 && _.isEqual(astPath[3], 'operationId')) ||
                (this._resourceMappings.has(editorEvent.start.row) &&
                    this._resourceMappings.get(editorEvent.start.row).type === 'operationId')) {
                this._updateOperationID(astPath, serviceDefinition, editorEvent);
                return true;
            } else if (_.size(astPath) === 1 && _.isEqual(astPath[0], 'paths')) {
                this._updatePath(astPath, serviceDefinition, editorEvent);
                return true;
            } else if (_.size(astPath) === 2) {
                this._updateHttpMethod(astPath, serviceDefinition, editorEvent);
                return true;
            }
            return false;
        } else if (_.isNull(astPath) && this._resourceMappings.has(editorEvent.start.row)) {
            const mapping = this._resourceMappings.get(editorEvent.start.row);
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
    _updateOperationID(astPath, serviceDefinition, editorEvent) {
        // Getting the old operation ID.
        let oldOperationID;
        if (_.isEqual(editorEvent.action, 'insert')) {
            const currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldOperationID = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            const currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldOperationID = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldOperationID = oldOperationID.trim();

        // Getting the new operation ID.
        const newOperationID = this._swaggerAceEditor.getSession().getLine(editorEvent.end.row).trim();

        // Updating resource name .
        for (const resourceDefinition of serviceDefinition.getResourceDefinitions()) {
            // Using substring we can avoid having "operationdId: " prefix in the value.
            if (resourceDefinition.getResourceName() === oldOperationID.substring(13)) {
                resourceDefinition.setResourceName(newOperationID.substring(13));
                this._resourceMappings.set(editorEvent.start.row,
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
    _updatePath(astPath, serviceDefinition, editorEvent) {
        // When resource path is modified.
        let oldResourcePath;
        if (_.isEqual(editorEvent.action, 'insert')) {
            const currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldResourcePath = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            const currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldResourcePath = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldResourcePath = oldResourcePath.trim().replace(/:\s*$/, '');

        const newResourcePath = this._swaggerAceEditor.getSession().getLine(editorEvent.end.row).trim()
                                                                                                .replace(/:\s*$/, '');
        // Getting the httpMethod value
        for (let i = this._swaggerAceEditor.getCursorPosition().row + 1; i <= this._swaggerAceEditor
            .getSession().getLength(); i++) {
            const httpMethodLine = this._swaggerAceEditor.getSession().getLine(i).trim().replace(/:\s*$/, '');
            if (!_.isEmpty(httpMethodLine)) {
                const httpMethod = httpMethodLine;

                // Get the operationId if exists.
                let operationId;
                const operationIDLineNumber = this._swaggerEditor.fn.AST.getLineNumberForPath(
                    this._swaggerAceEditor.getValue(), astPath.concat([newResourcePath, httpMethodLine,
                        'operationId']));
                if (!_.isUndefined(operationIDLineNumber)) {
                    operationId = this._swaggerAceEditor.getSession().getLine(operationIDLineNumber - 1).trim()
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
                    this._resourceMappings.set(editorEvent.start.row,
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
    _updateHttpMethod(astPath, serviceDefinition, editorEvent) {
        // When http method is updated
        let oldHttpMethod;
        if (_.isEqual(editorEvent.action, 'insert')) {
            const currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldHttpMethod = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            const currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldHttpMethod = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldHttpMethod = oldHttpMethod.trim().replace(/:\s*$/, '');

        const newHttpMethod = this._swaggerAceEditor.getSession().getLine(editorEvent.end.row).trim()
                                                                                                .replace(/:\s*$/, '');
        const resourcePath = astPath[1];

        // Get the operationId if exists.
        let operationId;
        const operationIDLineNumber = this._swaggerEditor.fn.AST.getLineNumberForPath(
            this._swaggerAceEditor.getValue(), astPath.concat([newHttpMethod, 'operationId']));
        if (!_.isUndefined(operationIDLineNumber)) {
            operationId = this._swaggerAceEditor.getSession().getLine(operationIDLineNumber - 1).trim()
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
            this._resourceMappings.set(editorEvent.start.row,
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
        return _.size(this._swaggerAceEditor.getSession().getAnnotations());
    }

    /**
     * Gets teh swagger data for this view.
     *
     * @returns {Object} Swagger data.
     * @memberof SwaggerView
     */
    getSwaggerData() {
        return this._swaggerData;
    }
}

export default SwaggerView;
