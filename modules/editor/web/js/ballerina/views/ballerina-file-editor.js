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
        './../ast/ballerina-ast-factory', './../ast/package-definition', './source-view',
        './../visitors/source-gen/ballerina-ast-root-visitor', './../tool-palette/tool-palette',
        './../undo-manager/undo-manager','./backend', './../ast/ballerina-ast-deserializer', './connector-definition-view'],
    function (_, $, log, BallerinaView, ServiceDefinitionView, FunctionDefinitionView, BallerinaASTRoot, BallerinaASTFactory,
              PackageDefinition, SourceView, SourceGenVisitor, ToolPalette, UndoManager, Backend, BallerinaASTDeserializer,
              ConnectorDefinitionView) {

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


            if (!_.has(args, 'viewOptions.backend')){
                log.error("Backend is not defined.");
                // not throwing an exception for now since we need to work without a backend.
            }
            this.backend = new Backend(_.get(args, 'viewOptions.backend', {}));
            this._isInSourceView = false;
            this.deserializer = BallerinaASTDeserializer;
            this.init();
        };

        BallerinaFileEditor.prototype = Object.create(BallerinaView.prototype);
        BallerinaFileEditor.prototype.constructor = BallerinaFileEditor;

        BallerinaFileEditor.prototype.getContent = function(){
            if (this.isInSourceView()) {
                return this._sourceView.getContent();
            } else {
                return this.generateSource();
            }
        };

        BallerinaFileEditor.prototype.isInSourceView = function(){
                    return this._isInSourceView;
        };

        BallerinaFileEditor.prototype.setInSourceView = function(isInSourceView){
            this._isInSourceView = isInSourceView;
            if(isInSourceView){
                this.trigger('source-view-activated');
                this.trigger('design-view-deactivated');
            } else {
                this.trigger('design-view-activated');
                this.trigger('source-view-deactivated');
            }
        };

        BallerinaFileEditor.prototype.setModel = function (model) {
            if (!_.isNil(model) && model instanceof BallerinaASTRoot) {
                this._model = model;
                //Registering event listeners
                this._model.on('child-removed', this.childViewRemovedCallback, this);
                this._model.on('child-added', function(child){
                     this.visit(child);
                }, this);
                // make undo-manager capture all tree modifications after initial rendering
                this._model.on('tree-modified', function(event){
                    this.getUndoManager().onUndoableOperation(event);
                    this.trigger("content-modified");
                }, this);
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
            this.diagramRenderingContext.getViewModelMap()[serviceDefinition.id] = serviceDefinitionView;
            serviceDefinitionView.render(this.diagramRenderingContext);

        };

        /**
         * Creates a connector definition view for a connector definition model and calls it's render.
         * @param connectorDefinition
         */
        BallerinaFileEditor.prototype.visitConnectorDefinition = function (connectorDefinition) {
            var connectorDefinitionView = new ConnectorDefinitionView({
                viewOptions: this._viewOptions,
                container: this._$canvasContainer,
                model: connectorDefinition,
                parentView: this,
                toolPalette: this.toolPalette
            });
            this.diagramRenderingContext.getViewModelMap()[connectorDefinition.id] = connectorDefinitionView;
            connectorDefinitionView.render(this.diagramRenderingContext);
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
            this.diagramRenderingContext.getViewModelMap()[functionDefinition.id] = functionDefinitionView;
            functionDefinitionView.render(this.diagramRenderingContext);
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

            var toolPaletteContainer = $(this._container).find(_.get(this._viewOptions, 'design_view.tool_palette.container')).get(0);
            var toolPaletteOpts = _.clone(_.get(this._viewOptions, 'design_view.tool_palette'));
            toolPaletteOpts.container = toolPaletteContainer;
            toolPaletteOpts.ballerinaFileEditor = this;
            this.toolPalette = new ToolPalette(toolPaletteOpts);

            this._createPackagePropertyPane(canvasContainer);

            // init undo manager
            this._undoManager = new UndoManager();
        };

        BallerinaFileEditor.prototype.importPackage = function(packageName){
            if (packageName != undefined && packageName != "") {
                log.debug("Adding new import");
                var backend = new Backend({ url : "" });
                var package = backend.searchPackage(packageName,[]);
                if(package == undefined){
                    log.error("Unable to find the package.");
                    return;
                }
                // Creating new import.
                var newImportDeclaration = BallerinaASTFactory.createImportDeclaration();
                newImportDeclaration.setPackageName(packageName);
                this._model.addImport(newImportDeclaration);
                //this.toolPalette.addImport(package);
            }
        }

        /**
         * Rendering the view for each canvas in {@link BallerinaFileEditor#_canvasList}.
         * @param parent - The parent container.
         * @param options - View options of the file editor.
         */
        BallerinaFileEditor.prototype.render = function (diagramRenderingContext, parent, options) {
            this.diagramRenderingContext = diagramRenderingContext;
            //TODO remove this for adding filecontext to the map
            this.diagramRenderingContext.ballerinaFileEditor = this;
            // render tool palette
            this.toolPalette.render();

            this._model.accept(this);

            var self = this;

            // container for per-tab source view TODO improve source view to wrap this logic
            var sourceViewContainer = $(this._container).find(_.get(this._viewOptions, 'source_view.container'));
            var aceEditorContainer = $('<div></div>');
            aceEditorContainer.addClass(_.get(this._viewOptions, 'cssClass.text_editor_class'));
            sourceViewContainer.append(aceEditorContainer);
            var sourceViewOpts = _.clone(_.get(this._viewOptions, 'source_view'));
            _.set(sourceViewOpts, 'container', aceEditorContainer.get(0));
            _.set(sourceViewOpts, 'content', "");
            this._sourceView = new SourceView(sourceViewOpts);
            this._sourceView.render();

            var sourceViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_source_btn'));
            sourceViewBtn.click(function () {
                self.toolPalette.hide();
                var generatedSource = self.generateSource();

                self.toolPalette.hide();
                // Get the generated source and append it to the source view container's content
                self._sourceView.setContent(generatedSource);
                sourceViewContainer.show();
                self._$designViewContainer.hide();
                designViewBtn.show();
                sourceViewBtn.hide();
                self.setInSourceView(true);
            });

            var designViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_design_btn'));
            designViewBtn.click(function () {
                // re-parse if there are modifications to source
                var isSourceChanged = !self._sourceView._editor.getSession().getUndoManager().isClean();
                if (isSourceChanged) {
                    var source = self._sourceView.getContent();
                    var response = self.backend.parse(source);
                    //if there are errors display the error.
                    //@todo: proper error handling need to get the service specs
                    if (response.error != undefined && response.error) {
                        $(_.get(self._viewOptions, 'dialog_boxes.parser_error')).modal();
                        return;
                    }
                    //if no errors display the design.
                    //@todo
                    var root = self.deserializer.getASTModel(response);
                    self.setModel(root);
                    // reset source editor delta stack
                    self._sourceView._editor.getSession().getUndoManager().markClean();
                }
                //canvas should be visible before you can call reDraw. drawing dependednt on attr:offsetWidth
                self.toolPalette.show();
                sourceViewContainer.hide();
                self._$designViewContainer.show();
                sourceViewBtn.show();
                designViewBtn.hide();
                self.setInSourceView(false);
                if(isSourceChanged){
                    // reset undo manager for the design view
                    self.getUndoManager().reset();
                    self.reDraw();
                }
            });
            // activate design view by default
            designViewBtn.hide();
            sourceViewContainer.hide();
            this.initDropTarget();
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
                self.toolPalette.dragDropManager.setActivatedDropTarget(self._model);

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
         * generate Ballerina source for this editor page
         * @returns {*}
         */
        BallerinaFileEditor.prototype.generateSource = function () {
            // Visit the ast model and generate the source
            var sourceGenVisitor = new SourceGenVisitor();
            this._model.accept(sourceGenVisitor);
            return sourceGenVisitor.getGeneratedSource();
        };

        BallerinaFileEditor.prototype.childViewRemovedCallback = function (child) {
            log.debug("[Eventing] Child element view removed. ");
            //TODO: remove canvas container for each delete click
            $(this._$canvasContainer)[0].remove();

            var self = this;
            self.reDraw({
                model: self._model,
                container: self._container,
                viewOptions: self._viewOptions
            });
        };

        /**
         * Creating the package view of a ballerina-file-editor.
         * @param canvasContainer - The canvas container.
         * @private
         */
        BallerinaFileEditor.prototype._createPackagePropertyPane = function (canvasContainer) {
            var currentASTRoot = this._model;

            var topRightControlsContainer = $(canvasContainer).siblings(".top-right-controls-container");
            var propertyPane = topRightControlsContainer.children(".top-right-controls-container-editor-pane");

            var packageButton = $(propertyPane).parent().find(".package-btn");

            // Package button click event.
            $(packageButton).click(function (event) {
                // If property pane is already shown, cancel this event.
                if (!(propertyPane.css("display") == "none")) {
                    return;
                }

                // Stopping propagation for the package button.
                event.stopPropagation();

                // Darkening the package button.
                packageButton.css("opacity", 1);

                // Showing the property pane.
                propertyPane.show();

                // Cancelling all event propagation when clicked on the property pane.
                $(propertyPane).click(function (event) {
                    log.debug("Property pane clicked");
                    event.stopPropagation();
                });

                var importPackageTextBox = propertyPane.find(".action-content-wrapper-heading input[type=text]");
                var addImportButton = $(propertyPane).find(".action-icon-wrapper");

                // Click event for adding an import.
                $(addImportButton).click(function () {
                    // TODO : Validate new import package name.
                    if (importPackageTextBox.val() != "") {
                        log.debug("Adding new import");

                        // Creating new import.
                        var newImportDeclaration = BallerinaASTFactory.createImportDeclaration();
                        newImportDeclaration.setPackageName(importPackageTextBox.val());

                        currentASTRoot.addImport(newImportDeclaration);

                        //Clear the import value box
                        importPackageTextBox.val("");

                        // Updating current imports view.
                        addImportsToView(currentASTRoot, propertyPane.find(".imports-wrapper"));
                    }
                });

                // Add new import upon enter key.
                $(importPackageTextBox).on("change paste keydown", function (e) {
                    if (e.which == 13) {
                        addImportButton.click();
                    }
                });

                $(importPackageTextBox).focus();

                // Getting package name text box.
                var packageTextBox = propertyPane.find(".package-name-wrapper input[type=text]");

                // Setting package name to text box.
                packageTextBox.val((!_.isUndefined(currentASTRoot.getPackageDefinition())) ?
                    currentASTRoot.getPackageDefinition().getPackageName() : "");

                // Updating model along with text change on package text box.
                // @todo: need to bring the for loop out of the event.
                packageTextBox.on("change", function () {
                    log.debug("Saving package name : " + $(packageTextBox).val());

                    //TODO - this for loop needs to be replaced to get the package definition
                    var childrenArray = currentASTRoot.getChildren();
                    for (var child in childrenArray) {
                        if (BallerinaASTFactory.isPackageDefinition(childrenArray[child])) {
                            childrenArray[child].setPackageName($(this).val());
                            break;
                        }
                    }
                });

                var importsWrapper = propertyPane.find(".imports-wrapper");

                // Adding current imports to view.
                addImportsToView(currentASTRoot, importsWrapper);

                // When clicked outside of the property pane.
                $(window).click(function () {
                    log.debug("Window Click");
                    propertyPane.hide();

                    // Unbinding all events.
                    $(this).unbind("click");
                    $(packageTextBox).unbind("change");
                    $(packageTextBox).unbind("keyup");
                    $(packageTextBox).unbind("input");
                    $(addImportButton).unbind("click");
                    $(propertyPane).unbind("click");

                    // Resetting import text box.
                    $(importPackageTextBox).val("");

                    // Resetting the opacity of the package button.
                    packageButton.removeAttr("style");
                });

                /**
                 * Removes the existing imports in the "Current imports" section and recreating them.
                 * @param {BallerinaASTRoot} model - The ballerina ast root model.
                 * @param importsWrapper - The html <div> tag to which an import view to be appended to.
                 */
                function addImportsToView(model, importsWrapper) {
                    // Removing existing imports.
                    $(importsWrapper).children("div").each(function () {
                        $(this).remove();
                    });

                    // Creating the imports according to the model.
                    _.forEach(model.getImportDeclarations(), function (importDeclaration) {
                        // Adding the imports.
                        var importWrapper = $("<div/>").appendTo(importsWrapper);
                        var importPackageNameSpan = $("<span>" + importDeclaration.getPackageName() + "</span>").appendTo(importWrapper);
                        var importDelete = $("<i class='fw fw-cancel'></i>").appendTo(importWrapper);

                        // Creating import delete event.
                        $(importDelete).click({
                            model: model,
                            wrapper: importWrapper,
                            packageName: importDeclaration.getPackageName()
                        }, function (event) {
                            log.debug("Delete import clicked :" + event.data.packageName);
                            $(event.data.wrapper).remove();
                            event.data.model.deleteImport(event.data.packageName);
                        });
                    });
                }
            });
        };

        BallerinaFileEditor.prototype.reDraw = function (args) {
            if (!_.has(this._viewOptions, 'design_view.container')) {
                errMsg = 'unable to find configuration for container';
                log.error(errMsg);
                throw errMsg;
            }
            // this._viewOptions.container is the root div for tab content
            var container = $(this._container).find(_.get(this._viewOptions, 'design_view.container'));
            //remove the old canves before creating a new one.
            var canvas = container.find('div.canvas-container');
            canvas.remove();

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
            this._model.accept(this);
            this.initDropTarget();
            this.trigger('redraw');
        };

        BallerinaFileEditor.prototype.getUndoManager = function(){
            return this._undoManager;
        };

        return BallerinaFileEditor;
    });

