/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
define(['require', 'lodash', 'log'], function (require, _, log) {

    var ballerinaFileEditor = function (canvasList, astRoot) {
        this.canvasList = canvasList || [];
        this._astRoot = astRoot;
        this.id = "Ballerina File Editor";

    }

    ballerinaFileEditor.prototype.getId = function () {
        return this.id;
    };

    ballerinaFileEditor.prototype.setId = function (id) {
        this.id = id;
    }

    ballerinaFileEditor.prototype.addCanvas = function (canvas) {
        if (!_.isNil(canvas)) {
            this.canvasList.push(canvas);
        }
        else {
            log.error("Unable to add empty canvas" + canvas);
        }


    };
    ballerinaFileEditor.prototype.init = function (astRoot, options) {
        this._astRoot = astRoot;
        var errMsg;
        var editorParent = this;
        if (!_.has(options, 'container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        var container = $(_.get(options, 'container'));
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = 'unable to find container for file editor with selector: ' + _.get(options, 'container');
            log.error(errMsg);
            throw errMsg;
        }
        if (!_.isNil(astRoot)) {

            if (_.has(astRoot, 'serviceDefinitions')) {

                var serviceDefs = $(_.get(astRoot, 'serviceDefinitions'));
                _.each(serviceDefs, function (serviceModel) {

                    //TODO: Add serviceModel id and css props
                    var serviceContainer = $('<div><svg class="diagram"></svg></div>');
                    serviceContainer.attr('id', serviceModel.id);
                    serviceContainer.attr('name','service');
                    editorParent.addCanvas(serviceContainer);
                });
            }
            if (_.has(astRoot, 'functionDefinitions')) {
                _.each(astRoot.functionDefinitions, function (functionModel) {
                    var functionContainer = $('<div></div>');
                   // var functionView = new FunctionDefinitionView(functionModel, functionContainer);
                    editorParent.addCanvas(functionContainer);
                });
            }
            if (_.has(astRoot, 'connectorDefinitions')) {
                _.each(astRoot.connectorDefinitions, function (connectorModel) {
                    var connectorContainer = $('<div>Connector View Container</div>');
                    connectorContainer.attr('id', connectorModel.id);
                    connectorContainer.attr('name','connector');
                    editorParent.addCanvas(connectorContainer);
                });
            }
            //TODO: rest of definitions when implemented
        }
        else {
            log.error("Provided astRoot is undefined" + astRoot);
        }

        this.render(container);

    };

    ballerinaFileEditor.prototype.render = function (parent) {
        if (!_.isNil(this.canvasList)) {
            _.each(this.canvasList, function (canvas) {
                //draw a collapse accordion
                var outerDiv = $('<div></div>');
                // TODO: For the moment disabling the adding classes in order to show the containers
                 outerDiv.addClass('panel panel-default');
                var panelHeading = $('<div></div>');
                panelHeading.addClass('panel-heading');
                //TODO: UPDATE ID
                panelHeading.attr('id', canvas[0].id + 3).attr('role', 'tab');
                var panelTitle = $('<h4></h4>');
                panelTitle.addClass('panel-title');
                var titleLink = $('<a>'+ canvas[0].getAttribute('name') + '</a>');
                titleLink.addClass("collapsed");
                //TODO: update href,aria-controls
                titleLink.attr('role', 'button').attr('data-toggle', 'collapse').attr('data-parent', "#accordion").attr('href', '#' + canvas[0].id).attr('aria-expanded', 'false').attr('aria-controls', canvas[0].id);
                panelTitle.append(titleLink);
                panelHeading.append(panelTitle);

                var bodyDiv = $('<div></div>');
                // TODO: For the moment disabling the adding classes in order to show the containers
                 bodyDiv.addClass('panel-collapse collapse');
                //TODO: UPDATE ID
                bodyDiv.attr('id', canvas[0].id).attr('aria-labelledby', canvas[0].id + 3).attr('role', 'tabpanel');
                canvas.addClass('panel-body');
                bodyDiv.append(canvas);

                outerDiv.append(panelHeading);
                outerDiv.append(bodyDiv);

                // append to parent
                parent.append(outerDiv);
            });
        }
    };

    return ballerinaFileEditor;
});

