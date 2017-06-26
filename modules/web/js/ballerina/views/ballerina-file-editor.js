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
import ASTVisitor from './../visitors/ast-visitor';
import React from 'react';
import ReactDOM from 'react-dom';
import alerts from 'alerts';
import EventChannel from 'event_channel';
import BallerinaASTRoot from './../ast/ballerina-ast-root';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';
import SourceView from './source-view';
import SwaggerView from './swagger/swagger-view';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';
import SwaggerJsonVisitor from './../visitors/swagger-json-gen/service-definition-visitor';
import SymbolTableGenVisitor from './../visitors/symbol-table/ballerina-ast-root-visitor';
import ToolPaletteView from './../tool-palette/tool-palette-view';
import Backend from './backend';
import BallerinaASTDeserializer from './../ast/ballerina-ast-deserializer';
import PackageScopedEnvironment from './../env/package-scoped-environment';
import Package from './../env/package';
import FindBreakpointNodesVisitor from './../visitors/find-breakpoint-nodes-visitor';
import FindBreakpointLinesVisitor from './../visitors/find-breakpoint-lines-visitor';
import DebugManager from './../../debugger/debug-manager';
import BallerinaDiagram from './../components/diagram';
import MessageManager from './../visitors/message-manager';
import Renderer from '../components/renderer';
import StructOperationsRenderer from '../components/struct-operations-renderer';
import FindDebugHitVisitor from './../visitors/find-debug-hit-visitor';
import DragDropManager from '../tool-palette/drag-drop-manager';

/**
 * The view to represent a ballerina file editor which is an AST visitor.
 * @class BallerinaFileEditor
 * @extends EventChannel
 */
class BallerinaFileEditor extends EventChannel {
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
        this._programPackages = _.get(args, 'programPackages');
        this._viewOptions = _.get(args, 'viewOptions', {});
        this._container = _.get(args, 'container');
        this._backendEndpointsOptions = _.get(args, 'backendEndpointsOptions', {});
        this.toolPalette = _.get(args, 'toolPalette');
        this.messageManager = _.get(args, 'messageManager');
        this.diagramRenderingContext = _.get(args, 'diagramRenderContext');

        this.setModel(_.get(args, 'model'));

        if (!this._parseFailed && (_.isNil(this._model) || !(this._model instanceof BallerinaASTRoot))) {
            log.error(`Ballerina AST Root is undefined or is of different type.${this._model}`);
            throw new Error(`Ballerina AST Root is undefined or is of different type.${this._model}`);
        }

        if (!_.has(args, 'backendEndpointsOptions')) {
            log.error('Backend endpoints options not defined.');
            // not throwing an exception for now since we need to work without a backend.
        }
        this.parserBackend = new Backend({ url: _.get(args, 'backendEndpointsOptions.parser.endpoint', {}) });
        this.validatorBackend = new Backend({ url: _.get(args, 'backendEndpointsOptions.validator.endpoint', {}) });
        this._isInSourceView = false;
        this._isInSwaggerView = false;
        this.deserializer = BallerinaASTDeserializer;
        this.init();
    }

    getContent() {
        if (this.isInSourceView()) {
            return this._sourceView.getContent();
        }
        return this.generateSource();
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
        switch (activeView) {
        case 'source': this.trigger('source-view-activated'); break;
        case 'design': this.trigger('design-view-activated'); break;
        case 'swagger': this.trigger('swagger-view-activated'); break;
        default:
            throw new Error(`Unknown view selected ${activeView}`);
        }
    }

    setModel(model) {
        if (_.isUndefined(model)) {
            // set an empty ast root if the model is undefined
            model = BallerinaASTFactory.createBallerinaAstRoot();
        }
        this._model = model;
        this._model.on('tree-modified', (event) => {
            this.trigger('content-modified', event);
        });

        this._model.on('import-new-package', (packageString) => {
            this.trigger('update-tool-patette');
        });
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
        const viewOptions = this._viewOptions;
        let errMsg;
        if (!_.has(viewOptions, 'design_view.container')) {
            errMsg = 'unable to find configuration for container';
            log.error(errMsg);
            throw errMsg;
        }
        // this._viewOptions.container is the root div for tab content
        const container = $(this._container).find(_.get(viewOptions, 'design_view.container'));
        this._$designViewContainer = container;
        const canvasContainer = $('<div></div>');
        canvasContainer.addClass(_.get(viewOptions, 'cssClass.canvas_container'));
        const canvasTopControlsContainer = $('<div></div>')
            .addClass(_.get(viewOptions, 'cssClass.canvas_top_controls_container'));
        canvasContainer.append(canvasTopControlsContainer);

        this._$designViewContainer.append(canvasContainer);
        this._$canvasContainer = canvasContainer;
        // check whether container element exists in dom
        if (!container.length > 0) {
            errMsg = 'unable to find container for file composer with selector: ' +
                _.get(viewOptions, 'design_view.container');
            log.error(errMsg);
            throw errMsg;
        }

        this._environment = new PackageScopedEnvironment();
        this._environment.addPackages(this._programPackages);
        this._package = this._environment.getCurrentPackage();

        this.dragDropManager = new DragDropManager();

        this.toolPaletteContainer = $(this._container)
            .find(_.get(viewOptions, 'design_view.tool_palette.container'))
            .get(0);

        this.messageManager = new MessageManager({ fileEditor: this });
    }

    /**
     * Rendering the view for each canvas in {@link BallerinaFileEditor#_canvasList}.
     * @param diagramRenderingContext
     */
    render(diagramRenderingContext) {
        this.diagramRenderingContext = diagramRenderingContext;
        // TODO remove this for adding filecontext to the map
        this.diagramRenderingContext.ballerinaFileEditor = this;
        this.diagramRenderingContext.packagedScopedEnvironemnt = this._environment;

        // attach a wrapper to the react diagram so we can attach expression editor to the container.
        const diagramRoot = $('<div class="diagram root" ></div>');
        const overlay = $('<div class="html-overlay" ></div>');
        const renderer = new Renderer(overlay[0]);
        const structOperationsOverlay = $('<div class="struct-operations-html-overlay" ></div>');
        const structOperationsRenderer = new StructOperationsRenderer(structOperationsOverlay[0]);
        this._$canvasContainer.append(diagramRoot);
        this._$canvasContainer.append(overlay);
        this._$canvasContainer.append(structOperationsOverlay);
        // create Rect component for diagram
        const root = React.createElement(BallerinaDiagram, {
            editor: this,
            dragDropManager: this.dragDropManager,
            messageManager: this.messageManager,
            container: this._$canvasContainer,
            renderingContext: this.diagramRenderingContext,
            renderer,
            overlay: overlay[0],
            structOperationsRenderer,
        }, null);
        ReactDOM.render(root, diagramRoot[0]);

        // render tool palette
        // this.toolPalette.render();
        const toolPalette = React.createElement(ToolPaletteView, {
            editor: this,
            dragDropManager: this.dragDropManager,
            container: this.toolPaletteContainer,
        }, null);
        ReactDOM.render(toolPalette, this.toolPaletteContainer);

        // container for per-tab source view TODO improve source view to wrap this logic
        this._sourceViewContainer = $(this._container).find(_.get(this._viewOptions, 'source_view.container'));
        const aceEditorContainer = $('<div></div>');
        aceEditorContainer.addClass(_.get(this._viewOptions, 'cssClass.text_editor_class'));
        this._sourceViewContainer.append(aceEditorContainer);
        const sourceViewOpts = _.clone(_.get(this._viewOptions, 'source_view'));
        _.set(sourceViewOpts, 'container', aceEditorContainer.get(0));
        _.set(sourceViewOpts, 'content', '');
        _.set(sourceViewOpts, 'debugger', this._debugger);
        _.set(sourceViewOpts, 'storage', this._file._storage);
        _.set(sourceViewOpts, 'fileEditor', this);
        _.set(sourceViewOpts, 'langserverClientController',
            this.diagramRenderingContext._application.langseverClientController);
        this._sourceView = new SourceView(sourceViewOpts);

        this._sourceView.on('modified', (changeEvent) => {
            this.trigger('content-modified', changeEvent);
        });

        this._sourceView.on('breakpoints-updated', () => {
            this.publishBreakpoints();
        });

        this.on('breakpoints-updated', () => {
            this.publishBreakpoints();
        });

        this._sourceView.on('dispatch-command', (id) => {
            this.trigger('dispatch-command', id);
        });

        this._debugger.on('resume-execution', _.bind(this._clearExistingDebugHit, this));
        this._debugger.on('session-completed', _.bind(this._clearExistingDebugHit, this));

        this._sourceView.render();
        this._sourceView.setContent(this._file.getContent() || '');

        let lastRenderedTimestamp = this._file.getLastPersisted();
        this._transformPopUp = '#transformOverlay';

        // container for per-tab swagger view TODO improve swagger view to wrap this logic
        this._swaggerViewContainer = $(this._container).find(_.get(this._viewOptions, 'swagger_view.container'));
        const swaggerViewOpts = _.clone(_.get(this._viewOptions, 'swagger_view'));
        _.set(swaggerViewOpts, 'container', this._swaggerViewContainer);
        _.set(swaggerViewOpts, 'content', '');
        _.set(swaggerViewOpts, 'backend', new Backend({ url: _.get(this._backendEndpointsOptions, 'swagger.endpoint') }));
        _.set(swaggerViewOpts, 'swaggerEditorId', `${this.getModel().getID()}-swagger-editor`);
        _.set(swaggerViewOpts, 'swaggerEditorTheme', this._file._storage.get('pref:sourceViewTheme') ||
            _.get(this.getViewOptions().source_view, 'theme'));
        _.set(swaggerViewOpts, 'swaggerEditorFontSize', this._file._storage.get('pref:sourceViewFontSize') ||
            _.get(this.getViewOptions().source_view, 'font_size'));
        this._swaggerView = new SwaggerView(swaggerViewOpts);

        this._sourceViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_source_btn'));
        this._sourceViewBtn.click(() => {
            if (this.isInSwaggerView()) {
                if (this._swaggerView.hasSwaggerErrors()) {
                    alerts.error('Cannot switch to Source view due to syntax errors.');
                    log.error('Cannot switch to Source view due to syntax errors.');
                    return false;
                }
            }

            lastRenderedTimestamp = this._file.getLastPersisted();

            this._sourceViewContainer.show();
            this._sourceView._editor.resize(true);
            this._swaggerViewContainer.hide();
            this._$designViewContainer.hide();
            this._designViewBtn.show();
            this._sourceViewBtn.hide();
            $(this._transformPopUp).remove();
            this.setActiveView('source');

            return true;
        });

        this._designViewBtn = $(this._container).find(_.get(this._viewOptions, 'controls.view_design_btn'));
        this._designViewBtn.click(() => {
            if (this.isInSwaggerView() && this._swaggerView.hasSwaggerErrors()) {
                alerts.error('Cannot switch to Design view due to syntax errors.');
                log.error('Cannot switch to Design view due to syntax errors.');
                return false;
            }

            // re-parse if there are modifications to source
            const isSourceChanged = !this._sourceView.isClean();

            const savedWhileInSourceView = lastRenderedTimestamp < this._file.getLastPersisted();
            const isSwaggerChanged = this.isInSwaggerView();
            if (isSourceChanged || savedWhileInSourceView || this._parseFailed) {
                const source = this._sourceView.getContent();
                if (!_.isEmpty(source.trim())) {
                    const validateResponse = this.validatorBackend.parse({ content: source.trim() });
                    // TODO : error messages from backend come as error or errors. Make this consistent.
                    if (validateResponse.errors && !_.isEmpty(validateResponse.errors)) {
                        // syntax errors found
                        // no need to show error as annotations are already displayed for each line
                        alerts.error('Cannot switch to Design view due to syntax errors');
                        return false;
                    } else if (validateResponse.error && !_.isEmpty(validateResponse.message)) {
                        // end point error
                        alerts.error(`Cannot switch to Design view due to syntax errors : ${validateResponse.message}`);
                        return false;
                    }
                }
                this._parseFailed = false;
                // if no errors display the design.
                const response = this.parserBackend.parse({
                    name: this._file.getName(),
                    path: this._file.getPath(),
                    package: this._astRoot,
                    content: source,
                });
                if (response.error && !_.isEmpty(response.message)) {
                    alerts.error(`Cannot switch to design view due to syntax errors : ${response.message}`);
                    return false;
                }
                this.setModel(this.deserializer.getASTModel(response));
                // reset source editor delta stack
                this._sourceView.markClean();
            } else if (isSwaggerChanged) {
                if (this._swaggerView.hasSwaggerErrors()) {
                    alerts.error('Cannot switch to Design view due to syntax errors.');
                    log.error('Cannot switch to Design view due to syntax errors.');
                    return false;
                }
            }
            // canvas should be visible before you can call reDraw. drawing dependednt on attr:offsetWidth
            // this.toolPalette.show();
            this._sourceViewContainer.hide();
            this._swaggerViewContainer.hide();
            this._$designViewContainer.show();
            this._sourceViewBtn.show();
            this._designViewBtn.hide();
            this.setActiveView('design');
            this.trigger('update-diagram');
            if (isSourceChanged || isSwaggerChanged || savedWhileInSourceView) {
                // trigger contet drawing
            }

            return true;
        });

        if (this._parseFailed) {
            this._swaggerView.hide();
            this._$designViewContainer.hide();
            this._sourceView.show();
            this._sourceView.setContent(this._file.getContent());
            this.setActiveView('source');
        } else {
            this._designViewBtn.hide();
            this._sourceViewContainer.hide();
            this._swaggerViewContainer.hide();
            this.setActiveView('design');
        }
    }

    showSwaggerView() {
        try {
            const isSourceChanged = !this._sourceView.isClean();
            const lastRenderedTimestamp = this._file.getLastPersisted();
            const savedWhileInSourceView = lastRenderedTimestamp < this._file.getLastPersisted();
            if (isSourceChanged || savedWhileInSourceView || this._parseFailed) {
                const source = this._sourceView.getContent();
                if (!_.isEmpty(source.trim())) {
                    const validateResponse = this.validatorBackend.parse({ content: source.trim() });
                    // TODO : error messages from backend come as error or errors. Make this consistent.
                    if (validateResponse.errors && !_.isEmpty(validateResponse.errors)) {
                        // syntax errors found
                        // no need to show error as annotations are already displayed for each line
                        alerts.error('Cannot switch to Swagger view due to syntax errors');
                        return;
                    } else if (validateResponse.error && !_.isEmpty(validateResponse.message)) {
                        // end point error
                        alerts.error(`Cannot switch to Swagger view due to syntax errors : ${validateResponse.message}`);
                        return;
                    }
                }
                this._parseFailed = false;
                // if no errors display the design.
                const response = this.parserBackend.parse({ content: source });
                if (response.error && !_.isEmpty(response.message)) {
                    alerts.error(`Cannot switch to Swagger view due to syntax errors : ${response.message}`);
                    return;
                }
                const root = this.deserializer.getASTModel(response);
                this.setModel(root);
                this._sourceView.markClean();
            }

            const treeModel = this.generateNodeTree();
            if (_.isUndefined(treeModel)) {
                alerts.error('Cannot switch to Swagger due to parser error');
                return;
            }

            const serviceDef = _.find(treeModel.getChildren(), (child) => {
                return BallerinaASTFactory.isServiceDefinition(child);
            });

            if (serviceDef.getResourceDefinitions().length === 0) {
                alerts.warn('Provide at least one service with one resource to generate Swagger definition');
                return;
            }

            // Get the generated swagger and append it to the swagger view container's content
            this._swaggerView.render(this.generateSwaggerSources());// setting fallback node tree

            this._swaggerViewContainer.show();
            this._sourceViewContainer.hide();
            this._$designViewContainer.hide();
            this._designViewBtn.show();
            this._sourceViewBtn.show();
            $(this._transformPopUp).remove();
            this.setActiveView('swagger');
        } catch (err) {
            log.error(err);
            alerts.error(err.message);
        }
    }

    /**
     * Returns a package object with functions, connectors,
     * actions etc. of the current package
     * @returns {Object}
     */
    generateCurrentPackage() {
        // get the latest symbols from this file.
        let currentPackage = new Package();
        currentPackage.setName('Current Package');
        const symbolTableGenVisitor = new SymbolTableGenVisitor(currentPackage, this._model);
        this._model.accept(symbolTableGenVisitor);
        currentPackage = symbolTableGenVisitor.getPackage();

        // check if a similar package exists.
        const packages = this._environment.getPackages();
        const currentPackageArray = _.filter(packages, pkg => !_.isEmpty(this._model.children) && (pkg.getName() ===
            this._model.children[0].getPackageName()));
        // Check whether the program contains a package name or it is in the dafault package
        if (!_.isEmpty(currentPackageArray)) {
            // Update Current package object after the package resolving
            const currentPackageInEvn = _.clone(currentPackageArray[0]);
            // todo merge the package with this.
            currentPackage = _.merge(currentPackageInEvn, currentPackage);
        }
        // update the package scoped environment with current package
        this._environment.setCurrentPackage(currentPackage);


        return currentPackage;
    }

    /**
     * generate Ballerina source for this editor page
     * @returns {*}
     */
    generateSource() {
        // Visit the ast model and generate the source
        const sourceGenVisitor = new SourceGenVisitor();
        this._model.accept(sourceGenVisitor);
        return sourceGenVisitor.getGeneratedSource();
    }

    generateSwaggerSources() {
        // Visit the ast model and generate the source
        const swaggerJsonVisitor = new SwaggerJsonVisitor();
        this.getModel().getServiceDefinitions()[0].accept(swaggerJsonVisitor);
        return {
            serviceDefinitionAST: this.getModel().getServiceDefinitions()[0],
            swagger: swaggerJsonVisitor.getSwaggerJson(),
        };
    }

    /**
     * Generates Ballerina node tree for design view
     * @returns {BallerinaASTRoot} generated node tree
     */
    generateNodeTree() {
        let root;
        const source = this._sourceView.getContent();
        if (!_.isEmpty(source.trim())) {
            const response = this.parserBackend.parse({ content: source });
            // if there are errors display the error.
            // @todo: proper error handling need to get the service specs
            if (response.error !== undefined && response.error) {
                alerts.error('cannot switch to design view due to parse errors');
                return undefined;
            } else if (!_.isUndefined(response.errorMessage)) {
                // TODO : error messages from backend come as message or errorMessage. Make this consistent.
                alerts.error(`Unable to parse the source: ${response.errorMessage}`);
                return undefined;
            }
            this._parseFailed = false;
            // if no errors display the design.
            // @todo
            root = this.deserializer.getASTModel(response);
        } else {
            // if source is empty get the current model. i.e. when in design view. TODO : refactor this behaviour
            root = this.getModel();
        }
        return root;
    }

    getSourceView() {
        return this._sourceView;
    }
    /**
     * Call designview and source view to indicate a debug hit
     * @param {DebugPoint} position
     * @memberof BallerinaFileEditor
     */
    debugHit(position) {
        this._sourceView.debugHit(position);
        this._debugHitDesignView(position);
    }
    /**
     * Indicate a debug hit in design view
     * @param {DebugPoint} position
     * @memberof BallerinaFileEditor
     */
    _debugHitDesignView(position) {
        const findDebugHitVisitor = new FindDebugHitVisitor(this._model);
        findDebugHitVisitor.setPosition(position);
        this._model.accept(findDebugHitVisitor);
        if (findDebugHitVisitor.hasToRerender) {
            this.reRender();
        }
    }
    /**
     * Clears existing debughit in design view and source view
     * @memberof BallerinaFileEditor
     */
    _clearExistingDebugHit() {
        this._currentDebugHit = {};
        const findDebugHitVisitor = new FindDebugHitVisitor(this._model);
        findDebugHitVisitor.setPosition({});
        this._model.accept(findDebugHitVisitor);
        if (findDebugHitVisitor.hasToRerender) {
            this.reRender();
        }
        this._sourceView.clearExistingDebugHit();
    }

    /**
     * find nodes which has debugpoints in design view
    */
    showDesignViewBreakpoints(breakpoints = []) {
        const findBreakpointsVisitor = new FindBreakpointNodesVisitor(this._model);
        findBreakpointsVisitor.setBreakpoints(breakpoints);
        this._model.accept(findBreakpointsVisitor);
    }

    /**
     * find line numbers which has debugpoints in source view
    */
    _showSourceViewBreakPoints() {
        const breakpoints = this.getBreakpoints();
        this._sourceView.setBreakpoints(breakpoints);
    }

    getModelFromSource(source) {
        const response = this.parserBackend.parse({ content: source });
        if (response.error && !_.isEmpty(response.message)) {
            return response.message;
        }
        return this.deserializer.getASTModel(response);
    }

    /**
     * Returns a Array of breakpoints
     * @returns [int]
    */
    getBreakpoints() {
        // handle if failed to build model
        if (!this._model) {
            return [];
        }

        const findBreakpointsVisitor = new FindBreakpointLinesVisitor(this._model);
        this._model.accept(findBreakpointsVisitor);
        const breakpoints = findBreakpointsVisitor.getBreakpoints();
        return breakpoints;
    }

    getFile() {
        return this._file;
    }

    /**
     * This function will rerender the diagram and tool palette.
     * Will be used to re adjest diagram when browser window resized.
     */
    reRender() {
        this.trigger('update-diagram');
        this._sourceView.resize();
    }
    /**
     * Returns file name with package name
     * @returns string - File name with package name
     * @memberof BallerinaFieleEditor
     */
    getFileNameWithPackage() {
        const packageInfo = this._model.getChildrenOfType(this._model.getFactory().isPackageDefinition)[0];
        const fileName = this.getFile().getName();
        if (packageInfo && packageInfo.getPackageName()) {
            const packageName = packageInfo.getPackageName();
            return `${packageName.replace(/\./g, '/')}/${fileName}`;
        }
        return fileName;
    }

    /**
     * Remove existing breakpoints and  publish breakpoints to DebugManager
    */
    publishBreakpoints() {
        const fileName = this.getFileNameWithPackage();
        let breakpoints = [];
        if (this.isInSourceView()) {
            breakpoints = this._sourceView.getBreakpoints();
        } else if (this.isInDesignView()) {
            breakpoints = this.getBreakpoints();
        }
        DebugManager.removeAllBreakpoints(fileName);
        breakpoints.forEach((lineNumber) => {
            DebugManager.addBreakPoint(lineNumber, fileName);
        });
    }

    getEnvironment() {
        return this._environment || new PackageScopedEnvironment();
    }

    activateSourceView() {
        this._sourceViewBtn.click();
    }

}


export default BallerinaFileEditor;
