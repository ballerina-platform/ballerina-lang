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
define(['lodash', 'jquery', 'log', './ballerina-view', './service-definition-view',  './function-definition-view', './../ast/ballerina-ast-root',
        './../ast/ballerina-ast-factory', './source-view', './../visitors/source-gen/ballerina-ast-root-visitor', './../tool-palette/tool-palette'],
    function (_, $, log, BallerinaView, ServiceDefinitionView, FunctionDefinitionView, BallerinaASTRoot, BallerinaASTFactory, SourceView, SourceGenVisitor, ToolPalette) {

        /**
         * The view to represent a ballerina file editor which is an AST visitor.
         * @param {Object} args - Arguments for creating the view.
         * @param {BallerinaASTRoot} args.model - The model for ballerina file editor.
         * @param {Object} args.container - The HTML container to which the view should be added to.
         * @param {Object} [args.viewOptions={}] - Configuration values for the view.
         * @constructor
         */
        var BallerinaFileEditor = function (args) {
            BallerinaView.call(this, args);
            this._canvasList = _.get(args, 'canvasList', []);
            this._id = _.get(args, "id", "Ballerina File Editor");

            if (_.isNil(this._model) || !(this._model instanceof BallerinaASTRoot)) {
                log.error("Ballerina AST Root is undefined or is of different type." + this._model);
                throw "Ballerina AST Root is undefined or is of different type." + this._model;
            }
            this.init();
        };

        BallerinaFileEditor.prototype = Object.create(BallerinaView.prototype);
        BallerinaFileEditor.prototype.constructor = BallerinaFileEditor;

        BallerinaFileEditor.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof BallerinaASTRoot) {
                this._model = model;
            } else {
                log.error("Ballerina AST Root is undefined or is of different type." + model);
                throw "Ballerina AST Root is undefined or is of different type." + model;
            }
        };

        BallerinaFileEditor.prototype.setCanvasList = function (canvases) {
            if (!_.isNil(canvases)) {
                this._canvasList = canvases;
            } else {
                log.error("Canvas list cannot be undefined or empty." + canvases);
                throw "Canvas list cannot be undefined or empty." + canvases;
            }
        };

        BallerinaFileEditor.prototype.setId = function (id) {
            this._id = id;
        };

        BallerinaFileEditor.prototype.setViewOptions = function (viewOptions) {
            this._viewOptions = viewOptions;
        };

        BallerinaFileEditor.prototype.getModel = function () {
            return this._model;
        };

        BallerinaFileEditor.prototype.getCanvasList = function () {
            return this._canvasList;
        };

        BallerinaFileEditor.prototype.getId = function () {
            return this._id;
        };

        BallerinaFileEditor.prototype.getViewOptions = function () {
            return this._viewOptions;
        };

        BallerinaFileEditor.prototype.canVisitBallerinaASTRoot = function (ballerinaASTRoot) {
            return true;
        };
        BallerinaFileEditor.prototype.visitBallerinaASTRoot = function (ballerinaASTRoot) {

        };

        BallerinaFileEditor.prototype.canVisitServiceDefinition = function (serviceDefinition) {
            return false;
        };

        BallerinaFileEditor.prototype.canVisitFunctionDefinition = function (functionDefinition) {
            return false;
        };

        /**
         * Creates a service definition view for a service definition model and calls it's render.
         * @param serviceDefinition
         */
        BallerinaFileEditor.prototype.visitServiceDefinition = function (serviceDefinition) {
            var serviceDefinitionView = new ServiceDefinitionView({
                viewOptions: this._viewOptions,
                container: this._$canvasContainer,
                model: serviceDefinition,
                parentView: this,
                toolPalette: this.toolPalette
            });
            this.diagramRenderingContext.getViewModelMap()[serviceDefinition] = serviceDefinitionView;
            serviceDefinitionView.render(this.diagramRenderingContext);

        };

        BallerinaFileEditor.prototype.childVisitedCallback = function (child) {
            this.trigger("childViewAddedEvent", child);
        };

        /**
         * Visits FunctionDefinition
         * @param functionDefinition
         */
        BallerinaFileEditor.prototype.visitFunctionDefinition = function (functionDefinition) {
            var functionDefinitionView = new FunctionDefinitionView({
                viewOptions: this._viewOptions,
                container: this._$canvasContainer,
                model: functionDefinition,
                parentView: this,
                toolPalette: this.toolPalette
            });
            functionDefinitionView.render();
        };

        /**
         * Adds the service definitions, function definitions and connector definitions to
         * {@link BallerinaFileEditor#_canvasList} and calls {@link BallerinaFileEditor#render}.
         */
        BallerinaFileEditor.prototype.init = function () {
            var errMsg;
            if (!_.has(this._viewOptions, 'design_view.container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            // this._viewOptions.container is the root div for tab content
            var container = $(this._container).find(_.get(this._viewOptions, 'design_view.container'));
            this._$designViewContainer = container;
            var canvasContainer = $('<div></div>');
            canvasContainer.addClass(_.get(this._viewOptions, 'cssClass.canvas_container'));
            this._$designViewContainer.append(canvasContainer);
            this._$canvasContainer = canvasContainer;
            // check whether container element exists in dom
            if (!container.length > 0) {
                errMsg = 'unable to find container for file editor with selector: ' + _.get(this._viewOptions, 'design_view.container');
                log.error(errMsg);
                throw errMsg;
            }

            //Registering event listeners
            this.listenTo(this._model, 'childVisitedEvent', this.childVisitedCallback);
        };

        /**
         * Rendering the view for each canvas in {@link BallerinaFileEditor#_canvasList}.
         * @param parent - The parent container.
         * @param options - View options of the file editor.
         */
        BallerinaFileEditor.prototype.render = function (diagramRenderingContext, parent, options) {
            this.diagramRenderingContext = diagramRenderingContext;
            // render tool palette
            var toolPaletteContainer = $(this._container).find(_.get(this._viewOptions, 'design_view.tool_palette.container')).get(0);
            var toolPaletteOpts = _.clone(_.get(this._viewOptions, 'design_view.tool_palette'));
            toolPaletteOpts.container = toolPaletteContainer;
            this.toolPalette = new ToolPalette(toolPaletteOpts);
            this.toolPalette.render();

            this._model.accept(this);

            var self = this;

            // container for per-tab source view TODO improve source view to wrap this logic
            var sourceViewContainer = $(this._container).find(_.get(this._viewOptions, 'source_view.container'));
            var aceEditorContainer = $('<div></div>');
            aceEditorContainer.addClass(_.get(this._viewOptions, 'cssClass.text_editor_class'));
            sourceViewContainer.append(aceEditorContainer);
            this._sourceView = new SourceView({container: aceEditorContainer.get(0), content: "test content"});
            this._sourceView.render();

            var sourceViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_source_btn'));
            sourceViewBtn.click(function () {
                self.toolPalette.hide();
                // Visit the ast model and generate the source
                var sourceGenVisitor = new SourceGenVisitor();
                self._model.accept(sourceGenVisitor);
                self.toolPalette.hide();
                // Get the generated source and append it to the source view container's content
                self._sourceView.setContent(sourceGenVisitor.getGeneratedSource());
                sourceViewContainer.show();
                self._$designViewContainer.hide();
                designViewBtn.show();
                sourceViewBtn.hide();
            });

            var designViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_design_btn'));
            designViewBtn.click(function () {
                self.toolPalette.show();
                sourceViewContainer.hide();
                self._$designViewContainer.show();
                sourceViewBtn.show();
                designViewBtn.hide();
            });
            // activate design view by default
            designViewBtn.hide();
            sourceViewContainer.hide();
            this.initDropTarget();

            this._model.on('child-added', function(child){
                self.visit(child);
                self._model.trigger("childVisitedEvent", child);
            });
    };

    BallerinaFileEditor.prototype.initDropTarget = function() {
        var self = this,
            dropActiveClass = _.get(this._viewOptions, 'cssClass.design_view_drop');

        // on hover over canvas area
        this._$canvasContainer
            .mouseover(function(event){

            //if someone is dragging a tool from tool-palette
            if(self.toolPalette.dragDropManager.isOnDrag()){

                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self)){
                    return;
                }

                // register this as a drop target and validate possible types of nodes to drop - second arg is a call back to validate
                // tool view will use this to provide feedback on impossible drop zones
                self.toolPalette.dragDropManager.setActivatedDropTarget(self._model, function(nodeBeingDragged){
                        return self._model.canBeParentOf(nodeBeingDragged) && nodeBeingDragged.canBeAChildOf(self._model);
                });

                // indicate drop area
                self._$canvasContainer.addClass(dropActiveClass);

                // reset ui feed back on drop target change
                self.toolPalette.dragDropManager.once("drop-target-changed", function(){
                    self._$canvasContainer.removeClass(dropActiveClass);
                });
            }
            event.stopPropagation();
        }).mouseout(function(event){
            // reset ui feed back on hover out
            if(self.toolPalette.dragDropManager.isOnDrag()){
                if(_.isEqual(self.toolPalette.dragDropManager.getActivatedDropTarget(), self._model)){
                    self.toolPalette.dragDropManager.clearActivatedDropTarget();
                }
            }
            event.stopPropagation();
        })
    };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.setWidth = function (newWidth) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.setHeight = function (newHeight) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.setXPosition = function (xPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.setYPosition = function (yPosition) {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.getWidth = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.getHeight = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.getXPosition = function () {
            // TODO : Implement
        };

        /**
         * @inheritDoc
         */
        BallerinaFileEditor.prototype.getYPosition = function () {
            // TODO : Implement
        };

        return BallerinaFileEditor;
    });

