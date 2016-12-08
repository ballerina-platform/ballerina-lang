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
define(['lodash', 'jquery', 'log', './../visitors/ast-visitor', './service-definition-view', 'ballerina/ast/ballerina-ast-factory'], function (_, $, log, ASTVisitor, ServiceDefinitionView, BallerinaASTFactory) {

    /**
     * @class BallerinaFileEditor
     * @augments ASTVisitor
     * @param args {Object}
     * @constructor
     */
    var BallerinaFileEditor = function (args) {
        this._canvasList = _.get(args, 'canvasList', []);
        this._model =  _.get(args, 'model', null);
        this.id = "Ballerina File Editor";
        this._options =  _.get(args, 'viewOptions', {});
        this.init();
    };

    BallerinaFileEditor.prototype = Object.create(ASTVisitor.prototype);
    BallerinaFileEditor.prototype.constructor = BallerinaFileEditor;

    BallerinaFileEditor.prototype.getId = function () {
        return this.id;
    };

    BallerinaFileEditor.prototype.setId = function (id) {
        this.id = id;
    };

    BallerinaFileEditor.prototype.addCanvas = function (canvas) {
        if (!_.isNil(canvas)) {
            this._canvasList.push(canvas);
        }
        else {
            log.error("Unable to add empty canvas" + canvas);
        }
    };

    BallerinaFileEditor.prototype.canVisitBallerinaASTRoot = function(ballerinaASTRoot){
        return true;
    };
    BallerinaFileEditor.prototype.visitBallerinaASTRoot = function(ballerinaASTRoot){

    };

    BallerinaFileEditor.prototype.canVisitServiceDefinition = function(serviceDefinition){
        return true;
    };
    BallerinaFileEditor.prototype.visitServiceDefinition = function(serviceDefinition){
        log.info("Visiting service definition");
        var divId = serviceDefinition.id;
        var currentContainer = $('#'+ divId);
        var serviceDefinitionView = new ServiceDefinitionView({model: serviceDefinition, container:currentContainer});
        serviceDefinitionView.render();
    };

    BallerinaFileEditor.prototype.init = function() {
        var errMsg;
        var editorParent = this;
        var options = this._options.file_editor;
        if (!_.has(this._options, 'container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        var container = $(_.get(this._options, 'container'));
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = 'unable to find container for file editor with selector: ' + _.get(this._options, 'container');
            log.error(errMsg);
            throw errMsg;
        }
        if (!_.isNil(this._model)) {
            if(_.has(this._model, 'children')){
                var children = $(_.get(this._model, 'children'));
                _.each(children, function (child) {
                    var ballerinaASTFactory = new BallerinaASTFactory();
                    if(ballerinaASTFactory.isServiceDefinition(child)){
                        var serviceContainer =  $('<div><svg class="service-container"></svg></div>');
                        serviceContainer.attr('id', child.id);
                        serviceContainer.attr('name','service');
                        serviceContainer.addClass(_.get(options, 'cssClass.outer_box'));
                        editorParent.addCanvas(serviceContainer);
                    } else if(ballerinaASTFactory.isFunctionDefinition(child)){
                        var functionContainer = $('<div>Function View Container</div>');
                        functionContainer.attr('id', child.id);
                        functionContainer.attr('name','function');
                        editorParent.addCanvas(functionContainer);
                    } else if(ballerinaASTFactory.isConnectorDefinition(child)){
                        var connectorContainer = $('<div>Connector View Container</div>');
                        connectorContainer.attr('id', child.id);
                        connectorContainer.attr('name','connector');
                        editorParent.addCanvas(connectorContainer);
                    }
                });
            }

            //TODO: rest of definitions when implemented
        }
        else {
            log.error("Provided astRoot is undefined");
        }
        this.render(container,options);
    };

    BallerinaFileEditor.prototype.render = function (parent,options) {
        if (!_.isNil(this._canvasList)) {
            _.each(this._canvasList, function (canvas) {
                //draw a collapse accordion
                var outerDiv = $('<div></div>');
                outerDiv.addClass(_.get(options, 'cssClass.outer_div'));
                var panelHeading = $('<div></div>');
                panelHeading.attr('id', canvas[0].id + 3).attr('role', 'tab');
                var panelTitle = $('<h4></h4>');
                panelTitle.addClass(_.get(options, 'cssClass.panel_title'));
                var panelIcon = $('<i></i>');
                panelIcon.addClass(_.get(options, 'cssClass.panel_icon'));
                if(canvas[0].getAttribute('name') == "service") {
                    panelIcon.addClass(_.get(options, 'cssClass.service_icon'));
                } else if (canvas[0].getAttribute('name') == "connector") {
                    panelIcon.addClass(_.get(options, 'cssClass.connector_icon'));
                } else if(canvas[0].getAttribute('name') == "function") {
                    panelIcon.addClass(_.get(options, 'cssClass.function_icon'));
                }
                panelTitle.append(panelIcon);
                var titleLink = $('<a>'+ canvas[0].getAttribute('name') + '</a>');
                titleLink.addClass(_.get(options, 'cssClass.title_link'));
                //TODO: update href,aria-controls
                titleLink.attr('role', 'button').attr('data-toggle', 'collapse').attr('data-parent', "#accordion").attr('href', '#' + canvas[0].id).attr('aria-expanded', 'false').attr('aria-controls', canvas[0].id);
                panelTitle.append(titleLink);

                var panelRightIcon = $('<i></i>');
                panelRightIcon.addClass(_.get(options, 'cssClass.panel_right_icon'));
                panelRightIcon.attr('role', 'button').attr('data-toggle', 'collapse').attr('data-parent', "#accordion").attr('href', '#' + canvas[0].id).attr('aria-expanded', 'false').attr('aria-controls', canvas[0].id);
                panelTitle.append(panelRightIcon);

                panelHeading.append(panelTitle);

                titleLink.click(function () {
                    $(this).parent().find('i.right-icon-clickable').toggleClass('fw-down fw-up');
                });

                panelRightIcon.click(function () {
                    $(this).toggleClass('fw-down fw-up');
                });

                var bodyDiv = $('<div></div>');
                bodyDiv.addClass(_.get(options, 'cssClass.body_div'));
                bodyDiv.attr('id', canvas[0].id).attr('aria-labelledby', canvas[0].id + 3).attr('role', 'tabpanel');
                bodyDiv.addClass(_.get(options, 'cssClass.canvas'));
                bodyDiv.append(canvas);

                outerDiv.append(panelHeading);
                outerDiv.append(bodyDiv);

                // append to parent
                parent.append(outerDiv);
            });
        }
    };

    BallerinaFileEditor.prototype.getModel = function () {
        return this._model;
    };

    return BallerinaFileEditor;
});

