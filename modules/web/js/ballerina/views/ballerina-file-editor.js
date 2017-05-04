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
import _ from 'lodash';
import $ from 'jquery';
import log from 'log';
import BallerinaView from './ballerina-view';
import BallerinaASTRoot from './../ast/ballerina-ast-root';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import SourceView from './source-view';
import SwaggerView from './swagger-view';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';
import SymbolTableGenVisitor from './../visitors/symbol-table/ballerina-ast-root-visitor';
import ToolPalette from './../tool-palette/tool-palette';
import UndoManager from './../undo-manager/undo-manager';
import Backend from './backend';
import BallerinaASTDeserializer from './../ast/ballerina-ast-deserializer';
import PackageScopedEnvironment from './../env/package-scoped-environment';
import BallerinaEnvironment from './../env/environment';
import ConstantsDefinitionsPaneView from './constant-definitions-pane-view';
import ToolPaletteItemProvider from './../item-provider/tool-palette-item-provider';
import alerts from 'alerts';
import 'typeahead.js';
import FindBreakpointsVisitor from './../visitors/find-breakpoints-visitor';
import DimensionCalculatorVisitor from './../visitors/dimension-calculator-visitor';
import PositionCalculatorVisitor from './../visitors/position-calculator-visitor';
import DebugManager from './../../debugger/debug-manager';
import React from 'react';
import ReactDOM from 'react-dom';
import BallerinaDiagram from './../components/diagram';

/**
 * The view to represent a ballerina file editor which is an AST visitor.
 * @class BallerinaFileEditor
 * @extends BallerinaView
 */
class BallerinaFileEditor extends BallerinaView {
    /**
     * @param {Object} args - Arguments for creating the view.
     * @param {BallerinaASTRoot} args.model - The model for ballerina file editor.
     * @param {Object} args.container - The HTML container to which the view should be added to.
     * @param {Object} [args.viewOptions={}] - Configuration values for the view.
     * @constructor
     */
    constructor(args) {
        super(args);
        this._parseFailed = _.get(args, 'parseFailed');
        this._debugger = _.get(args, 'debugger');
        this._file = _.get(args, 'file');
        this._id = _.get(args, 'id', 'Ballerina Composer');

        if (!this._parseFailed && (_.isNil(this._model) || !(this._model instanceof BallerinaASTRoot))) {
            log.error('Ballerina AST Root is undefined or is of different type.' + this._model);
            throw 'Ballerina AST Root is undefined or is of different type.' + this._model;
        }

        if (!_.has(args, 'backendEndpointsOptions')){
            log.error('Backend endpoints options not defined.');
           // not throwing an exception for now since we need to work without a backend.
        }
        this.parserBackend = new Backend({url: _.get(args, 'backendEndpointsOptions.parser.endpoint', {})});
        this.validatorBackend = new Backend({url: _.get(args, 'backendEndpointsOptions.validator.endpoint', {})});
        this._isInSourceView = false;
        this._isvInSwaggerView = false;
        this._constantDefinitionsPane = undefined;
        this.deserializer = BallerinaASTDeserializer;
        this._currentBreakpoints = [];
        this.init();
    }

    getContent() {
        if (this.isInSourceView()) {
            return this._sourceView.getContent();
        } else if (this.isInSwaggerView()) {
            return this._swaggerView.getContent();
        } else {
            var generatedSource = this.generateSource();
            this._sourceView.setContent(generatedSource);
            this._sourceView.format(true);
            return this._sourceView.getContent();
        }
    }

    isInSourceView() {
        return _.isEqual(this._activeView, 'source');
    }

    isInSwaggerView() {
        return _.isEqual(this._activeView, 'swagger');
    }

    isInDesignView() {
        return _.isEqual(this._activeView, 'design');
    }

    setActiveView(activeView) {
        this._activeView = activeView;
        switch (activeView){
        case 'source': this.trigger('source-view-activated'); break;
        case 'design': this.trigger('design-view-activated'); break;
        case 'swagger': this.trigger('swagger-view-activated'); break;
        }
    }

    setModel(model) {
        if(this._parseFailed){
            return;
        }
        if ((!_.isUndefined(model) && !_.isNil(model) && model instanceof BallerinaASTRoot)) {
            this._model = model;
            var self = this;
            // make undo-manager capture all tree modifications after initial rendering
            this._model.on('tree-modified', function(event){
                if(this.getUndoManager().hasUndo()){
                    // clear undo stack from source view
                    if(this.getUndoManager().getOperationFactory()
                            .isSourceModifiedOperation(this.getUndoManager().undoStackTop())){
                        this.getUndoManager().reset();
                    }
                }
                if(this.getUndoManager().hasRedo()){
                    // clear redo stack from source view
                    if(this.getUndoManager().getOperationFactory()
                            .isSourceModifiedOperation(this.getUndoManager().redoStackTop())){
                        this.getUndoManager().reset();
                    }
                }
                // If we have added a new action/function invocation statement the particular import has to be added
                // to the imports view
                if (_.isEqual(event.title, 'add import')) {
                    var childModel = event.data.child;
                    self.visit(childModel);
                }
                _.set(event, 'editor', this);
                _.set(event, 'skipInSourceView', true);
                this.getUndoManager().onUndoableOperation(event);
                this.trigger('content-modified');
            }, this);
        } else {
            log.error('Ballerina AST Root is undefined or is of different type.' + model);
            throw 'Ballerina AST Root is undefined or is of different type.' + model;
        }
    }

    setId(id) {
        this._id = id;
    }

    setViewOptions(viewOptions) {
        this._viewOptions = viewOptions;
    }

    getModel() {
        return this._model;
    }

    getId() {
        return this._id;
    }

    getViewOptions() {
        return this._viewOptions;
    }

    /**
     *
     */
    init() {
        var viewOptions = this._viewOptions;
        var errMsg;
        if (!_.has(viewOptions, 'design_view.container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        // this._viewOptions.container is the root div for tab content
        var container = $(this._container).find(_.get(viewOptions, 'design_view.container'));
        this._$designViewContainer = container;
        var canvasContainer = $('<div></div>');
        canvasContainer.addClass(_.get(viewOptions, 'cssClass.canvas_container'));
        var canvasTopControlsContainer = $('<div></div>')
            .addClass(_.get(viewOptions, 'cssClass.canvas_top_controls_container'))
            .append($('<div></div>').addClass(_.get(viewOptions, 'cssClass.canvas_top_control_package_define')))
            .append($('<div></div>').addClass(_.get(viewOptions, 'cssClass.canvas_top_control_packages_import')))
            .append($('<div></div>').addClass(_.get(viewOptions, 'cssClass.canvas_top_control_constants_define')));
        canvasContainer.append(canvasTopControlsContainer);

        this._$designViewContainer.append(canvasContainer);
        this._$canvasContainer = canvasContainer;
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = 'unable to find container for file composer with selector: ' + _.get(viewOptions, 'design_view.container');
            log.error(errMsg);
            throw errMsg;
        }

        var toolPaletteItemProvider = new ToolPaletteItemProvider();
        var toolPaletteContainer = $(this._container)
                                    .find(_.get(viewOptions, 'design_view.tool_palette.container'))
                                    .get(0);
        var toolPaletteOpts = _.clone(_.get(viewOptions, 'design_view.tool_palette'));
        toolPaletteOpts.itemProvider = toolPaletteItemProvider;
        toolPaletteOpts.container = toolPaletteContainer;
        toolPaletteOpts.ballerinaFileEditor = this;
        this.toolPalette = new ToolPalette(toolPaletteOpts);

        this._createImportDeclarationPane(canvasContainer);

        // init undo manager
        this._undoManager = new UndoManager();

        this._environment =  new PackageScopedEnvironment();
        this._package = this._environment.getCurrentPackage();
    }

    /**
     * Rendering the view for each canvas in {@link BallerinaFileEditor#_canvasList}.
     * @param diagramRenderingContext
     */
    render(diagramRenderingContext) {
        var self = this;
        this.diagramRenderingContext = diagramRenderingContext;
        //TODO remove this for adding filecontext to the map
        this.diagramRenderingContext.ballerinaFileEditor = this;
        this.diagramRenderingContext.packagedScopedEnvironemnt = this._environment;

        if(!this._parseFailed){
            // adding current package to the tool palette with functions, connectors, actions etc. of the current package
            this.addCurrentPackageToToolPalette();
        }

        var importDeclarations = [];
        if(!this._parseFailed) {
            //pass the container width and height to root view state.
            let viewState = this._model.getViewState();
            viewState.container = {
                width : this._$canvasContainer.width(),
                height : this._$canvasContainer.height()
            };
            //create Rect component for diagram
            let root = React.createElement(BallerinaDiagram, {
                model: this._model,
                dragDropManager: this.toolPalette.dragDropManager
            }, null);
            ReactDOM.render(
              root,
              this._$canvasContainer[0]
            );
        }

        // render tool palette
        this.toolPalette.render();

        // add current imported packages to tool pallet
        _.forEach(importDeclarations, function (importDeclaration) {
            var pckg = BallerinaEnvironment.searchPackage(importDeclaration.getPackageName());
            self.toolPalette.getItemProvider().addImportToolGroup(pckg[0]);
        });

        // container for per-tab source view TODO improve source view to wrap this logic
        var sourceViewContainer = $(this._container).find(_.get(this._viewOptions, 'source_view.container'));
        var aceEditorContainer = $('<div></div>');
        aceEditorContainer.addClass(_.get(this._viewOptions, 'cssClass.text_editor_class'));
        sourceViewContainer.append(aceEditorContainer);
        var sourceViewOpts = _.clone(_.get(this._viewOptions, 'source_view'));
        _.set(sourceViewOpts, 'container', aceEditorContainer.get(0));
        _.set(sourceViewOpts, 'content', '');
        _.set(sourceViewOpts, 'debugger', this._debugger);
        _.set(sourceViewOpts, 'storage', this._file._storage);
        this._sourceView = new SourceView(sourceViewOpts);

        this._sourceView.on('modified', function (changeEvent) {
            if(self.getUndoManager().hasUndo()){
                // clear undo stack from design view
                if(!self.getUndoManager().getOperationFactory()
                        .isSourceModifiedOperation(self.getUndoManager().undoStackTop())){
                    self.getUndoManager().reset();
                }
            }

            if(self.getUndoManager().hasRedo()){
                // clear redo stack from design view
                if(!self.getUndoManager().getOperationFactory()
                        .isSourceModifiedOperation(self.getUndoManager().redoStackTop())){
                    self.getUndoManager().reset();
                }
            }
            _.set(changeEvent, 'editor', self);
            self.getUndoManager().onUndoableOperation(changeEvent);
            self.trigger('content-modified');
        });

        this._sourceView.on('breakpoints-updated', () => {
            this.publishBreakPoints();
        });

        this.on('breakpoints-updated', () => {
            this.publishBreakPoints();
        });

        this._sourceView.on('dispatch-command', function (id) {
            self.trigger('dispatch-command', id);
        });

        this._debugger.on('resume-execution', _.bind(this._clearExistingDebugHit, this));
        this._debugger.on('session-completed', _.bind(this._clearExistingDebugHit, this));

        this._sourceView.render();

        var lastRenderedTimestamp = this._file.getLastPersisted();

        // container for per-tab swagger view TODO improve swagger view to wrap this logic
        var swaggerViewContainer = $(this._container).find(_.get(this._viewOptions, 'swagger_view.container'));
        var swaggerViewOpts = _.clone(_.get(this._viewOptions, 'swagger_view'));
        _.set(swaggerViewOpts, 'container', swaggerViewContainer);
        _.set(swaggerViewOpts, 'content', '');
        _.set(swaggerViewOpts, 'backend', new Backend({url : _.get(this._backendEndpointsOptions, 'swagger.endpoint')}));
        this._swaggerView = new SwaggerView(swaggerViewOpts);
        this._swaggerView.render();

        var sourceViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_source_btn'));
        sourceViewBtn.click(() => {
            lastRenderedTimestamp = this._file.getLastPersisted();
            this.toolPalette.hide();
            // If the file has changed we will add the generated source to source view
            // If not we will display the content as it is in the file.
            const generatedSource = this.generateSource();
            if(this._file.isDirty()){
                this._sourceView.setContent(generatedSource);
                this._sourceView.format(true);
            } else {
                this._sourceView.setContent(this._file.getContent());
            }

            sourceViewContainer.show();
            swaggerViewContainer.hide();
            this._$designViewContainer.hide();
            designViewBtn.show();
            swaggerViewBtn.show();
            sourceViewBtn.hide();
            this.setActiveView('source');
        });

        var swaggerViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_swagger_btn'));
        swaggerViewBtn.click(function () {
            try {
                var isSourceChanged = !self._sourceView.isClean(),
                    savedWhileInSourceView = lastRenderedTimestamp < self._file.getLastPersisted();
                if (isSourceChanged || savedWhileInSourceView || self._parseFailed) {
                    var source = self._sourceView.getContent();
                    if(!_.isEmpty(source.trim())){
                        var validateResponse = self.validatorBackend.parse(source.trim());
                        //TODO : error messages from backend come as error or errors. Make this consistent.
                        if (validateResponse.errors && !_.isEmpty(validateResponse.errors)) {
                            // syntax errors found
                            // no need to show error as annotations are already displayed for each line
                            alerts.error('Cannot switch to Swagger view due to syntax errors');
                            return;
                        } else if (validateResponse.error && !_.isEmpty(validateResponse.message)) {
                            // end point error
                            alerts.error('Cannot switch to Swagger view due to syntax errors : ' + validateResponse.message);
                            return;
                        }
                    }
                    self._parseFailed = false;
                    //if no errors display the design.
                    var response = self.parserBackend.parse(source);
                    if (response.error && !_.isEmpty(response.message)) {
                        alerts.error('Cannot switch to Swagger view due to syntax errors : ' + response.message);
                        return;
                    }
                    var root = self.deserializer.getASTModel(response);
                    self.setModel(root);
                    self._sourceView.markClean();
                    self.addCurrentPackageToToolPalette();
                }

                var treeModel = self.generateNodeTree();
                if (_.isUndefined(treeModel)) {
                    alerts.error('Cannot switch to Swagger due to parser error');
                    return;
                }

                var serviceDef = _.find(treeModel.getChildren(), function (child) {
                    return BallerinaASTFactory.isServiceDefinition(child);
                });

                if (_.isUndefined(serviceDef)) {
                    alerts.warn('Provide at least one service to generate Swagger definition');
                    return;
                }

                var generatedSource = self.generateSource();

                // Get the generated swagger and append it to the swagger view container's content
                self._swaggerView.setContent(generatedSource);
                self._swaggerView.setNodeTree(treeModel);//setting fallback node tree

                swaggerViewContainer.show();
                sourceViewContainer.hide();
                self._$designViewContainer.hide();
                designViewBtn.show();
                sourceViewBtn.show();
                swaggerViewBtn.hide();
                self.toolPalette.hide();
                self.setActiveView('swagger');
                alerts.warn('This version only supports one service representation on Swagger');
            } catch (err) {
                alerts.error(err.message);
            }
        });

        var designViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_design_btn'));
        designViewBtn.click(function () {
            // re-parse if there are modifications to source
            var isSourceChanged = !self._sourceView.isClean(),
                savedWhileInSourceView = lastRenderedTimestamp < self._file.getLastPersisted();
            var isSwaggerChanged = self.isInSwaggerView();
            if (isSourceChanged || savedWhileInSourceView || self._parseFailed) {
                var source = self._sourceView.getContent();
                if(!_.isEmpty(source.trim())){
                    var validateResponse = self.validatorBackend.parse(source.trim());
                    //TODO : error messages from backend come as error or errors. Make this consistent.
                    if (validateResponse.errors && !_.isEmpty(validateResponse.errors)) {
                        // syntax errors found
                        // no need to show error as annotations are already displayed for each line
                        alerts.error('Cannot switch to Design view due to syntax errors');
                        return;
                    } else if (validateResponse.error && !_.isEmpty(validateResponse.message)) {
                        // end point error
                        alerts.error('Cannot switch to Design view due to syntax errors : ' + validateResponse.message);
                        return;
                    }
                }
                self._parseFailed = false;
                //if no errors display the design.
                var response = self.parserBackend.parse(source);
                if (response.error && !_.isEmpty(response.message)) {
                    alerts.error('Cannot switch to design view due to syntax errors : ' + response.message);
                    return;
                }
                var root = self.deserializer.getASTModel(response);
                self.setModel(root);
                // reset source editor delta stack
                self._sourceView.markClean();
            } else if (isSwaggerChanged) {
                self.setModel(self._swaggerView.getContent());
                // reset source editor delta stack
            }
            //canvas should be visible before you can call reDraw. drawing dependednt on attr:offsetWidth
            self.toolPalette.show();
            sourceViewContainer.hide();
            swaggerViewContainer.hide();
            self._$designViewContainer.show();
            sourceViewBtn.show();
            swaggerViewBtn.show();
            designViewBtn.hide();
            self.setActiveView('design');
            if(isSourceChanged || isSwaggerChanged || savedWhileInSourceView){
                self._environment.resetCurrentPackage();
                self.rerenderCurrentPackageTool();
            }
            $('.outer-box').mCustomScrollbar('scrollTo', 'left');
        });

        if(this._parseFailed){
            this._swaggerView.hide();
            this._$designViewContainer.hide();
            this._sourceView.show();
            self._sourceView.setContent(self._file.getContent());
            self.setActiveView('source');
        }else{
            designViewBtn.hide();
            sourceViewContainer.hide();
            swaggerViewContainer.hide();
            self.setActiveView('design');
        }

    }

    /**
     * Returns a package object with functions, connectors,
     * actions etc. of the current package
     * @returns {Object}
     */
    generateCurrentPackage() {
        var symbolTableGenVisitor = new SymbolTableGenVisitor(this._environment.getCurrentPackage(), this._model);
        this._model.accept(symbolTableGenVisitor);
        return symbolTableGenVisitor.getPackage();
    }

    /**
     * adding current package to the tool palette with functions, connectors,
     * actions etc. of the current package
     */
    addCurrentPackageToToolPalette() {
        this.toolPalette.getItemProvider().addImport(this.generateCurrentPackage(), 0);
    }

    rerenderCurrentPackageTool() {
        var currentPackage = this.generateCurrentPackage();
        var provider = this.toolPalette.getItemProvider();

        // Remove current package tool group from pallete and re add it.
        provider.removeImportToolGroup(currentPackage.getName());
        var options = {
            addToTop: true,
            collapsed: false
        };
        provider.addImportToolGroup(currentPackage, options);
    }

    /**
     * generate Ballerina source for this editor page
     * @returns {*}
     */
    generateSource() {
        // Visit the ast model and generate the source
        var sourceGenVisitor = new SourceGenVisitor();
        this._model.accept(sourceGenVisitor);
        return sourceGenVisitor.getGeneratedSource();
    }

    /**
     * Generates Ballerina node tree for design view
     * @returns {BallerinaASTRoot} generated node tree
     */
    generateNodeTree() {
        var root;
        var source = this._sourceView.getContent();
        if (!_.isEmpty(source.trim())) {
            var response = this.parserBackend.parse(source);
           //if there are errors display the error.
           //@todo: proper error handling need to get the service specs
            if (response.error !== undefined && response.error) {
                alerts.error('cannot switch to design view due to parse errors');
                return;
            } else if (!_.isUndefined(response.errorMessage)) {
               //TODO : error messages from backend come as message or errorMessage. Make this consistent.
                alerts.error('Unable to parse the source: ' + response.errorMessage);
                return;
            }
            this._parseFailed = false;
           //if no errors display the design.
           //@todo
            root = this.deserializer.getASTModel(response);
        } else {
            //if source is empty get the current model. i.e. when in design view. TODO : refactor this behaviour
            root = this.getModel();
        }
        return root;
    }

    /**
     * Creating the package view of a ballerina-file-editor.
     * @param canvasContainer - The canvas container.
     * @private
     */
    _createImportDeclarationPane(canvasContainer) {
        var self = this;

        var _paneAppendElement = $(canvasContainer).find('.package-imports-wrapper');
        // Creating import button.
        this._importDeclarationButton = $('<div class=\'imports-btn\' data-toggle=\'tooltip\' title=\'Imports\' ' +
            'data-placement=\'bottom\'></div>')
            .appendTo(_paneAppendElement);

        $('<span class=\'btn-icon\'> Imports </span>').appendTo(this._importDeclarationButton).tooltip();

        this._importDeclarationMainWrapper = $('<div class=\'imports-pane\'/>').appendTo(_paneAppendElement);

        var importDeclarationWrapper = $('<div class=\'imports-wrapper\'/>').appendTo(this._importDeclarationMainWrapper);

        var collapserWrapper = $('<div class=\'import-pane-collapser-wrapper btn-icon\' data-placement=\'bottom\' ' +
            ' title=\'Open Import Pane\' data-toggle=\'tooltip\'/>')
            .data('collapsed', 'true')
            .appendTo(importDeclarationWrapper);

        $('<i class=\'fw fw-right\'></i>').appendTo(collapserWrapper);

        var importDeclarationControlsWrapper = $('<div />').appendTo(importDeclarationWrapper);

        var importDeclarationActionWrapper = $('<div class=\'imports-action-wrapper\'/>').appendTo(importDeclarationControlsWrapper);

        // Creating add imports editor button.
        var addImportButton = $('<div class=\'action-icon-wrapper import-add-icon-wrapper\' title=\'Add Import\'' +
            'data-toggle=\'tooltip\' data-placement=\'bottom\'/>')
            .appendTo(importDeclarationActionWrapper);
        $('<i class=\'fw fw-add\'></i>').appendTo(addImportButton);

        var importsAddPane = $('<div class=\'action-import-wrapper-heading import-add-action-wrapper\'/>')
            .appendTo(importDeclarationActionWrapper);

        var importValueText = $('<input id=\'import-package-text\' placeholder=\'Enter Package Name\'/>').appendTo(importsAddPane);

        // Creating cancelling add new import button.
        var importAddCancelButtonPane = $('<div class=\'action-icon-wrapper import-add-cancel-action-wrapper\' ' +
            'data-placement=\'bottom\' title=\'Cancel\' data-toggle=\'tooltip\'/>')
            .appendTo(importsAddPane);
        $('<span class=\'fw-stack fw-lg\'><i class=\'fw fw-square fw-stack-2x\'></i>' +
            '<i class=\'fw fw-cancel fw-stack-1x fw-inverse\'></i></span>').appendTo(importAddCancelButtonPane);
        // Creating add new import button.
        var importAddCompleteButtonPane = $('<div class=\'action-icon-wrapper ' +
            'import-add-complete-action-wrapper\' title=\'Import\' data-placement=\'bottom\' data-toggle=\'tooltip\'/>')
            .appendTo(importsAddPane);
        $('<span class=\'fw-stack fw-lg\'><i class=\'fw fw-square fw-stack-2x\'></i>' +
            '<i class=\'fw fw-check fw-stack-1x fw-inverse\'></i></span>').appendTo(importAddCompleteButtonPane);


        // Add new constant activate button.
        $(addImportButton).click(function () {
            $(importsAddPane).show();
            $(this).hide();
            $(importValueText).focus();
            self._importDeclarationButton.css('opacity', '1');
        });

        // Cancel adding a new constant.
        $(importAddCancelButtonPane).click(function () {
            $(importsAddPane).hide();
            $(addImportButton).show();
        });

        var importsDeclarationContentWrapper = $("<span class='imports-content-wrapper'/>")
            .appendTo(importDeclarationControlsWrapper);

        var substringMatcher = function(strs) {
            return function findMatches(q, cb) {
                // an array that will be populated with substring matches
                var matches = [];

                // regex used to determine if a string contains the substring `q`
                var substrRegex = new RegExp(q, 'i');

                // iterate through the pool of strings and for any string that
                // contains the substring `q`, add it to the `matches` array
                $.each(strs, function(i, str) {
                    if (substrRegex.test(str)) {
                        matches.push(str);
                    }
                });

                cb(matches);
            };
        };

        //add import suggestions
        var packages = BallerinaEnvironment.getPackages();
        var packageNames = _.map(packages, function(p){return p._name;});

        importValueText.typeahead({
            hint: false,
            highlight: true,
            minLength: 1
        },
            {
                name: 'packages',
                source: substringMatcher(packageNames)
            }
        );

        // Click event for adding an import.
        importAddCompleteButtonPane.click(function () {
            // TODO : Validate new import package name.
            var importTestValue = importValueText.val().trim();
            if (!_.isEmpty(importTestValue)) {
                var currentASTRoot = self.getModel();
                log.debug('Adding new import');

                // Creating new import.
                var newImportDeclaration = BallerinaASTFactory.createImportDeclaration();
                newImportDeclaration.setPackageName(importValueText.val());

                try {
                    newImportDeclaration.setParent(currentASTRoot);
                    currentASTRoot.addImport(newImportDeclaration);

                    // add import to the tool pallet
                    var newPackage = BallerinaEnvironment.searchPackage(newImportDeclaration.getPackageName())[0];
                    // Only add to tool palette if the user input exactly matches an existing package.
                    if(!_.isUndefined(newPackage) && (newPackage.getName() === importTestValue)) {
                        self.toolPalette.getItemProvider().addImportToolGroup(newPackage);
                    }

                    //Clear the import value box
                    importValueText.typeahead('val','');
                    collapserWrapper.empty();
                    collapserWrapper.data('collapsed', 'false');
                    $('<i class=\'fw fw-left\'></i>').appendTo(collapserWrapper);
                    importDeclarationWrapper.show();
                    self._importDeclarationMainWrapper.css('width', '92%');

                } catch (error) {
                    alerts.error(error);
                }
            }
        });

        // Add new import upon enter key.
        $(importValueText).on('change paste keydown', function (e) {
            if (e.which === 13) {
                importAddCompleteButtonPane.click();
            }
        });

        // The click event for hiding and showing constants.
        collapserWrapper.click(function () {
            $(this).empty();
            if ($(this).data('collapsed') === 'false') {
                $(this).data('collapsed', 'true').attr('data-original-title', 'Open Import Pane').tooltip('hide');
                $('<i class=\'fw fw-right\'></i>').appendTo(this);
                importDeclarationWrapper.find('.imports-content-wrapper').hide();
                importDeclarationActionWrapper.hide();
                self._importDeclarationMainWrapper.css('width', '0%');
            } else {
                $(this).data('collapsed', 'false').attr('data-original-title', 'Close Import Pane').tooltip('hide');
                $('<i class=\'fw fw-left\'></i>').appendTo(this);
                importDeclarationActionWrapper.show();
                importDeclarationWrapper.find('.imports-content-wrapper').show();
                self._importDeclarationMainWrapper.css('width', '92%');
            }
        });
    }

    getUndoManager() {
        return this._undoManager;
    }

    getSourceView() {
        return this._sourceView;
    }

    highlightExecutionPoint() {
        this._sourceView._editor.selection.moveCursorToPosition({row: 1, column: 0});
        this._sourceView._editor.selection.selectLine();
    }

    debugHit(position) {
        this._sourceView.debugHit(position);
        this._debugHitDesignView(position);
    }

    _debugHitDesignView(position) {
        var self = this;
        var modelMap = this.diagramRenderingContext.getViewModelMap();
        // hide previous debug hit
        if(this._currentDebugHit) {
            this._currentDebugHit.clearDebugHit();
        }
        _.each(modelMap, function(aView) {
            if(!_.isNil(aView.getModel)) {
                var lineNumber = aView.getModel().getLineNumber();
                if(lineNumber === position.lineNumber && !_.isNil(aView.showDebugHit)) {
                    aView.showDebugHit();
                    self._currentDebugHit = aView;
                }
            }
        });
    }

    _clearExistingDebugHit() {
        if(this._currentDebugHit) {
            this._currentDebugHit.clearDebugHit();
        }
        this._sourceView.clearExistingDebugHit();
    }

     /**
      * highlights breakpoints in designview
     */
    _showDesignViewBreakpoints(breakpoints = []) {
        const findBreakpointsVisitor = new FindBreakpointsVisitor(this._model);
        findBreakpointsVisitor.setBreakpoints(breakpoints);

        var generatedSource = this.getContent();
        var model = this.getModelFromSource(generatedSource);
        const modelMap = this.diagramRenderingContext.getViewModelMap();
        model.accept(findBreakpointsVisitor);
        const breakpointNodes = findBreakpointsVisitor.getBreakpointNodes();

        // hide previous breakpoints first
        this.hideCurrentBreakpoints();

        breakpointNodes.forEach( breakpointNode => {
            const nodeView = modelMap[breakpointNode.id];
            if(!nodeView) {
                return;
            }
            nodeView.showDebugIndicator = nodeView.showDebugIndicator || function() {};
            nodeView.showDebugIndicator();
            const pathVector = [];
            this.getPathToNode(breakpointNode, pathVector);
            this._currentBreakpoints.push(JSON.stringify(pathVector));
        });
    }

    _showSourceViewBreakPoints() {
        const pathVectors = this.getCurrentBreakpoints() || [];
        const breakpoints = [];
        pathVectors.forEach(pathVectorStr => {
            const pathVector = JSON.parse(pathVectorStr);
            const node = this.getNodeByVector(this._model, pathVector);
            const lineNumber = node.getLineNumber();
            breakpoints.push(lineNumber);
        });
        this._sourceView.setBreakpoints(breakpoints);
    }

    hideCurrentBreakpoints() {
        this._currentBreakpoints = this._currentBreakpoints || [];
        this._currentBreakpoints.forEach( pathVectorStr => {
            const pathVector = JSON.parse(pathVectorStr);
            const node = this.getNodeByVector(this._model, pathVector);
            const modelMap = this.diagramRenderingContext.getViewModelMap();
            const aView = modelMap[node.id] || {};
            aView.hideDebugIndicator = aView.hideDebugIndicator || function() {};
            aView.hideDebugIndicator();
        });
        this._currentBreakpoints = [];
    }

    getCurrentBreakpoints() {
        return this._currentBreakpoints;
    }

    addBreakPoint(viewNode) {
        const pathVector = [];
        this.getPathToNode(viewNode.getModel(), pathVector);
        return this._currentBreakpoints.push(JSON.stringify(pathVector));
    }

    removeBreakPoint(viewNodeToDel) {
        viewNodeToDel._model.isBreakPoint = false;
        this._currentBreakpoints = this._currentBreakpoints.filter( pathVectorStr => {
            const pathVector = JSON.parse(pathVectorStr);
            const node = this.getNodeByVector(this._model, pathVector);
            return  viewNodeToDel.getModel().id !==  node.id;
        });
    }

    getModelFromSource(source) {
        var response = this.parserBackend.parse(source);
        if (response.error && !_.isEmpty(response.message)) {
            return response.message;
        }
        return this.deserializer.getASTModel(response);
    }

    getPathToNode(node, pathVector) {
        var nodeParent = node.getParent();
        if (!_.isNil(nodeParent)) {
            var nodeIndex = _.findIndex(nodeParent.getChildren(), node);
            pathVector.push(nodeIndex);
            this.getPathToNode(nodeParent, pathVector);
        }
    }

    getNodeByVector(root, pathVector) {
        var returnNode = root;
        var reverseVector = _.reverse(pathVector);

        _.forEach(reverseVector, function (index) {
            returnNode = returnNode.getChildren()[index];
        });
        return returnNode;
    }

    publishBreakPoints() {
        const fileName = this._file.getName();
        let breakpoints = [];
        if(this.isInSourceView()) {
            breakpoints = this._sourceView.getBreakpoints();
        } else if(this.isInDesignView()) {
            breakpoints = this.getBreakpoints();
        }
        DebugManager.removeAllBreakpoints(fileName);
        breakpoints.forEach( lineNumber => {
            DebugManager.addBreakPoint(lineNumber, fileName);
        });
        return breakpoints;
    }

    getBreakpoints() {
        const breakpoints = [];

        this._currentBreakpoints.forEach( pathVectorStr => {
            const pathVector = JSON.parse(pathVectorStr);
            const node = this.getNodeByVector(this._model, pathVector) || {};
            node.getLineNumber = node.getLineNumber || function() {};
            breakpoints.push(node.getLineNumber());
        });

        return breakpoints;
    }
}



export default BallerinaFileEditor;
