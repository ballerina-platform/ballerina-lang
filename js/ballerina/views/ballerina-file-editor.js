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
define(['lodash', 'jquery', 'log', './../visitors/ast-visitor', './service-definition-view', './function-definition-view', './../ast/ballerina-ast-factory', './source-view'], function (_, $, log, ASTVisitor, ServiceDefinitionView, FunctionDefinitionView, BallerinaASTFactory, SourceView) {

    /**
     * @class BallerinaFileEditor
     * @augments ASTVisitor
     * @param args {Object}
     * @constructor
     */
    var BallerinaFileEditor = function (args) {
        this._canvasList = _.get(args, 'canvasList', []);
        this._model = _.get(args, 'model', null);
        this.id = "Ballerina File Editor";
        this._options = _.get(args, 'viewOptions', {});
        this.init();
    };

    BallerinaFileEditor.prototype = Object.create(ASTVisitor.prototype);
    BallerinaFileEditor.prototype.constructor = BallerinaFileEditor;

    BallerinaFileEditor.prototype.init = function () {
        var errMsg;
        if (!_.has(this._options, 'design.container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        // this._options.container is the root div for tab content
        var container = $(this._options.container).find(_.get(this._options, 'design.container'));
        this._$designViewContainer = container;
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = 'unable to find container for file editor with selector: ' + _.get(this._options, 'design.container');
            log.error(errMsg);
            throw errMsg;
        }
    };

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

    BallerinaFileEditor.prototype.canVisitBallerinaASTRoot = function (ballerinaASTRoot) {
        return true;
    };
    BallerinaFileEditor.prototype.visitBallerinaASTRoot = function (ballerinaASTRoot) {

    };

    BallerinaFileEditor.prototype.canVisitServiceDefinition = function (serviceDefinition) {
        return true;
    };

    /**
     * Visits ServiceDefinition
     * @param serviceDefinition
     */
    BallerinaFileEditor.prototype.visitServiceDefinition = function (serviceDefinition) {
        var serviceDefinitionView = new ServiceDefinitionView({
            options: this._options,
            container: this._$designViewContainer,
            model: serviceDefinition
        });
        serviceDefinitionView.render();
    };

    BallerinaFileEditor.prototype.canVisitFunctionDefinition = function (functionDefinition) {
        return true;
    };

    /**
     * Visits FunctionDefinition
     * @param functionDefinition
     */
    BallerinaFileEditor.prototype.visitFunctionDefinition = function (functionDefinition) {
        var functionDefinitionView = new FunctionDefinitionView({
            options: this._options,
            container: this._$designViewContainer,
            model: functionDefinition
        });
        functionDefinitionView.render();
    };

    BallerinaFileEditor.prototype.render = function (parent, options) {
        this._model.accept(this);

        var self = this;

        // container for per-tab source view TODO improve source view to wrap this logic
        var sourceViewContainer = $(this._options.container).find(_.get(this._options, 'source.container'));
        var aceEditorContainer = $('<div></div>');
        aceEditorContainer.addClass(_.get(this._options, 'cssClass.text_editor_class'));
        sourceViewContainer.append(aceEditorContainer);
        this._sourceView = new SourceView({container: aceEditorContainer.get(0), content: "test content"});
        this._sourceView.render();

        var sourceViewBtn = $(this._options.container).find(_.get(this._options, 'controls.view_source_btn'));
        sourceViewBtn.click(function () {
            self._options.toolPalette.hide();
            sourceViewContainer.show();
            self._$designViewContainer.hide();
        });

        var designViewBtn = $(this._options.container).find(_.get(this._options, 'controls.view_design_btn'));
        designViewBtn.click(function () {
            self._options.toolPalette.show();
            sourceViewContainer.hide();
            self._$designViewContainer.show();
        });
    };

    BallerinaFileEditor.prototype.getModel = function () {
        return this._model;
    };

    return BallerinaFileEditor;
});

