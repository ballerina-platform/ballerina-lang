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
import * as YAML from 'js-yaml';
import SwaggerParser from '../../swagger-parser/swagger-parser';
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
        this._currentEditingServiceDefinition = undefined;
        this._manualUpdate = false;
        this._swaggerData = undefined;
        this._resourceMappings = new Map();
    }

    /**
     * Set the content of swagger editor.
     * @param {Object[]} swaggerInfos - An object array which contains service definition ASTs and their swagger
     * definitions.
     * @param {ServiceDefinition} swaggerInfos[].serviceDefinitionAST The Service definition.
     * @param {Object} swaggerInfos[].swagger The JSON swagger definition.
     */
    render(swaggerInfos) {
        this._swaggerData = swaggerInfos;
        let self = this;
        // Creating the swagger editor
        let swaggerEditorElement = $(this._container).find('div.swaggerEditor');
        let swaggerElementID = 'z-' + this._options.swaggerEditorId;
        swaggerEditorElement.attr('id', swaggerElementID);
        this._swaggerEditor = SwaggerEditorBundle({
            dom_id: '#' + swaggerElementID
        });

        let swaggerAceEditorElement = swaggerEditorElement.find('#ace-editor');
        swaggerAceEditorElement.attr('id', 'z-ace-editor-' + this._options.swaggerEditorId);
        this._swaggerAceEditor = ace.edit('z-ace-editor-' + this._options.swaggerEditorId);

        this._swaggerAceEditor.$blockScrolling = Infinity;
        let editorTheme = ace.acequire(this._swaggerEditorTheme);
        this._swaggerAceEditor.setTheme(editorTheme);
        this._swaggerAceEditor.setFontSize(this._swaggerEditorFontSize);

        // Remove dropdown wrapper if already exists.
        $(this._container).find('div.swagger-service-selector-wrapper').remove();

        // Creating the service selecting wrapper.
        let swaggerServiceSelectorWrapper = $('<div/>', {
            class: 'swagger-service-selector-wrapper',
        }).prependTo(this._container);

        $('<span/>', {
            text: 'Select Service to Edit: ',
            class: 'swagger-service-selector-span'
        }).appendTo(swaggerServiceSelectorWrapper);

        let swaggerServiceSelectorDropdownWrapper = $('<div/>', {
            class: 'type-drop-wrapper'
        }).appendTo(swaggerServiceSelectorWrapper);

        let servicesDropDown = $($('<select/>').appendTo(swaggerServiceSelectorDropdownWrapper));
        servicesDropDown = servicesDropDown.select2({
            data: this.getServicesForDropdown(this._swaggerData)
        });

        // Hiding top bar if there is only one service.
        if (_.isEqual(_.size(this._swaggerData), 1)) {
            swaggerServiceSelectorWrapper.hide();
        }

        // Event when an item is selected from the dropdown.
        servicesDropDown.on('select2:select', (e) => {
            let serviceDefinitionID = e.params.data.id;
            _.forEach(this._swaggerData, swaggerInfo => {
                if (_.isEqual(swaggerInfo.serviceDefinitionAST.getID(), serviceDefinitionID)) {
                    this._manualUpdate = true;
                    this._swaggerEditor.specActions.updateUrl('');
                    this._swaggerEditor.specActions.updateLoadingStatus('success');
                    this._swaggerEditor.specActions.updateSpec(JSON.stringify(swaggerInfo.swagger));
                    this._swaggerEditor.specActions.formatIntoYaml();

                    this._currentEditingServiceDefinition = swaggerInfo.serviceDefinitionAST;
                    this._manualUpdate = false;
                }
            });
        });

        // Setting the default selected swagger
        this._swaggerEditor.specActions.updateUrl('');
        this._swaggerEditor.specActions.updateLoadingStatus('success');
        this._swaggerEditor.specActions.updateSpec(JSON.stringify(this._swaggerData[0].swagger));
        this._swaggerEditor.specActions.formatIntoYaml();

        this._currentEditingServiceDefinition = this._swaggerData[0].serviceDefinitionAST;

        this._swaggerAceEditor.getSession().on('change', (e) => {
            //@todo - set a dirty state in the model. need to refactor.
            this.swaggerDirty = true;

            try {
                if (this._manualUpdate === false) {
                    //log.info(e);
                    let serviceDefinition = this._currentEditingServiceDefinition;
                    if (_.isEqual(e.start.row, e.end.row)) {
                        this._updateResourceMappings(serviceDefinition, e);
                    }

                    // We keep updating the yaml for a service instead of merging to the service AST
                    _.forEach(this._swaggerData, swaggerDataEntry => {
                        if (_.isEqual(swaggerDataEntry.serviceDefinitionAST.getID(), serviceDefinition.getID())) {
                            swaggerDataEntry.swagger = YAML.safeLoad(self._swaggerAceEditor.getValue());
                            swaggerDataEntry.hasModified = true;
                        }
                    });
                }
            } catch (error) {
                log.warn(error);
            }
        });
    }

    /**
     * Merge the updated YAMLs to the service definitions.
     */
    updateServices() {
        //we do not update the dom if swagger is not edited.
        if (this.swaggerDirty) {
            _.forEach(this._swaggerData, swaggerDataEntry => {
                if (swaggerDataEntry.hasModified) {
                    let swaggerParser = new SwaggerParser(swaggerDataEntry.swagger, false);
                    swaggerParser.mergeToService(swaggerDataEntry.serviceDefinitionAST);
                }
            });
        }
    }

    getServicesForDropdown(swaggerInfos) {
        let dataArray = [];
        _.forEach(swaggerInfos, swaggerInfo => {
            dataArray.push({
                id: swaggerInfo.serviceDefinitionAST.getID(),
                text: swaggerInfo.serviceDefinitionAST.getServiceName()
            });
        });
        return dataArray;
    }
    /**
     * Set the default node tree.
     * @param {Object} root root node.
     *
     */
    setNodeTree(root) {
        this._generatedNodeTree = root;
    }

    show() {
        $(this._container).show();
    }

    hide() {
        $(this._container).hide();
    }

    isVisible() {
        return $(this._container).is(':visible');
    }

    /**
     * Updates mappings of a resource with ace editor event
     * 
     * @param {ServiceDefinition} serviceDefinition The service definition which has the resource definitions.
     * @param {Object} editorEvent The ace onChange event.
     * 
     * @memberof SwaggerView
     */
    _updateResourceMappings(serviceDefinition, editorEvent) {
        // If there are no errors.
        let astPath = this._swaggerEditor.fn.AST.pathForPosition(this._swaggerAceEditor.getValue(), {
            line: editorEvent.start.row,
            column: editorEvent.start.column
        });

        if (!_.isUndefined(astPath) && _.isArray(astPath)) {
            if ((_.size(astPath) === 4 && _.isEqual(astPath[3], 'operationId')) ||
                (this._resourceMappings.has(editorEvent.start.row) &&
                    this._resourceMappings.get(editorEvent.start.row).type === 'operationId')) {
                this._updateOperationID(astPath, serviceDefinition, editorEvent);
            } else if (_.size(astPath) === 1 && _.isEqual(astPath[0], 'paths')) {
                this._updatePath(astPath, serviceDefinition, editorEvent);
            } else if (_.size(astPath) === 2) {
                this._updateHttpMethod(astPath, serviceDefinition, editorEvent);
            }
        } else if (_.isNull(astPath) && this._resourceMappings.has(editorEvent.start.row)) {
            let mapping = this._resourceMappings.get(editorEvent.start.row);
            if (_.isEqual(mapping.type, 'path') && !_.isUndefined(mapping.ast)) {
                mapping.ast.getChildren()[0].setRightValue('\"\"', { doSilently: true });
            } else if (_.isEqual(mapping.type, 'method') && !_.isUndefined(mapping.ast)) {
                mapping.ast.setIdentifier('', { doSilently: true });
            }
        }
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
            let currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldOperationID = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            let currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldOperationID = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldOperationID = oldOperationID.trim();

        // Getting the new operation ID.
        let newOperationID = this._swaggerAceEditor.getSession().getLine(editorEvent.end.row).trim();

        // Updating resource name .
        for (let resourceDefinition of serviceDefinition.getResourceDefinitions()) {
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
            let currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldResourcePath = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            let currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldResourcePath = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldResourcePath = oldResourcePath.trim().replace(/:\s*$/, '');

        let newResourcePath = this._swaggerAceEditor.getSession().getLine(editorEvent.end.row).trim().replace(/:\s*$/, '');
        // Getting the httpMethod value
        for (let i = this._swaggerAceEditor.getCursorPosition().row + 1; i <= this._swaggerAceEditor
            .getSession().getLength(); i++) {
            let httpMethodLine = this._swaggerAceEditor.getSession().getLine(i).trim().replace(/:\s*$/, '');
            if (!_.isEmpty(httpMethodLine)) {
                let httpMethod = httpMethodLine;

                // Get the operationId if exists.
                let operationId;
                let operationIDLineNumber = this._swaggerEditor.fn.AST.getLineNumberForPath(
                    this._swaggerAceEditor.getValue(), astPath.concat([newResourcePath, httpMethodLine, 'operationId']));
                if (!_.isUndefined(operationIDLineNumber)) {
                    operationId = this._swaggerAceEditor.getSession().getLine(operationIDLineNumber - 1).trim().replace(/operationId:\s/, '');
                }

                // Finding the resource to update
                let resourceDefinitionToUpdate;
                for (let resourceDefinition of serviceDefinition.getResourceDefinitions()) {
                    if (!_.isUndefined(operationId) && resourceDefinition.getResourceName() === operationId) {
                        // Getting the resource to update using the operation ID
                        resourceDefinitionToUpdate = resourceDefinition;
                        break;
                    } else {
                        // Getting the resource to update using the path and http method.
                        let currentResourcePathAnnotation = resourceDefinition.getPathAnnotation();
                        let currentResourcePath = currentResourcePathAnnotation.getChildren()[0].getRightValue()
                            .replace(/"/g, '');

                        let currentHttpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();

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
            let currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldHttpMethod = currentValue.substring(0, editorEvent.start.column) +
                currentValue.substring(editorEvent.end.column, currentValue.length);
        } else if (_.isEqual(editorEvent.action, 'remove')) {
            let currentValue = this._swaggerAceEditor.getSession().getLine(editorEvent.start.row);
            oldHttpMethod = currentValue.slice(0, editorEvent.start.column) + editorEvent.lines[0] +
                currentValue.slice(editorEvent.start.column);
        }

        oldHttpMethod = oldHttpMethod.trim().replace(/:\s*$/, '');

        let newHttpMethod = this._swaggerAceEditor.getSession().getLine(editorEvent.end.row).trim().replace(/:\s*$/, '');
        let resourcePath = astPath[1];

        // Get the operationId if exists.
        let operationId;
        let operationIDLineNumber = this._swaggerEditor.fn.AST.getLineNumberForPath(
            this._swaggerAceEditor.getValue(), astPath.concat([newHttpMethod, 'operationId']));
        if (!_.isUndefined(operationIDLineNumber)) {
            operationId = this._swaggerAceEditor.getSession().getLine(operationIDLineNumber - 1).trim().replace(/operationId:\s/, '');
        }

        // Finding the resource to update
        let resourceDefinitionToUpdate;
        for (let resourceDefinition of serviceDefinition.getResourceDefinitions()) {
            if (!_.isUndefined(operationId) && resourceDefinition.getResourceName() === operationId) {
                // Getting the resource to update using the operation ID
                resourceDefinitionToUpdate = resourceDefinition;
                break;
            } else {
                // Getting the resource to update using the path and http method.
                let currentResourcePathAnnotation = resourceDefinition.getPathAnnotation();
                let currentHttpMethodAnnotation = resourceDefinition.getHttpMethodAnnotation();

                if (!_.isUndefined(currentResourcePathAnnotation) &&
                    !_.isUndefined(currentHttpMethodAnnotation) &&
                    _.isEqual(currentResourcePathAnnotation.getChildren()[0].getRightValue().toLowerCase().replace(/"/g, ''), resourcePath.toLowerCase()) &&
                    _.isEqual(currentHttpMethodAnnotation.getIdentifier().toLowerCase().replace(/"/g, ''), oldHttpMethod.toLowerCase())) {
                    resourceDefinitionToUpdate = resourceDefinition;
                    break;
                }
            }
        }

        // Updating the http method of the resource.
        if (resourceDefinitionToUpdate) {
            resourceDefinitionToUpdate.getHttpMethodAnnotation().setIdentifier(newHttpMethod.toUpperCase(), { doSilently: true });
            this._resourceMappings.set(editorEvent.start.row,
                {
                    type: 'method',
                    ast: resourceDefinitionToUpdate.getHttpMethodAnnotation()
                });
        }
    }

    /**
     * Returns the number of errors in the editor.
     */
    hasSwaggerErrors() {
        return _.size(this._swaggerAceEditor.getSession().getAnnotations());
    }
}

export default SwaggerView;
