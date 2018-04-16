/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import React from 'react';
import cn from 'classnames';
import PropTypes from 'prop-types';
import SplitPane from 'react-split-pane';
import { Loader, Dimmer } from 'semantic-ui-react';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import DebugManager from 'plugins/debugger/DebugManager/DebugManager'; // FIXME: Importing from debugger plugin
import TreeUtil from 'plugins/ballerina/model/tree-util.js';
import { parseFile } from 'api-client/api-client';
import { CONTENT_MODIFIED, UNDO_EVENT, REDO_EVENT } from 'plugins/ballerina/constants/events';
import { GO_TO_POSITION, FORMAT } from 'plugins/ballerina/constants/commands';
import File from 'core/workspace/model/file';
import { COMMANDS as LAYOUT_COMMANDS } from 'core/layout/constants';
import { DOC_VIEW_ID } from 'plugins/ballerina/constants';
import DesignView from './design-view.jsx';
import SourceView from './source-view.jsx';
import SwaggerView from './swagger-view.jsx';
import PackageScopedEnvironment from './../env/package-scoped-environment';
import BallerinaEnvFactory from './../env/ballerina-env-factory';
import BallerinaEnvironment from './../env/environment';
import { DESIGN_VIEW, SOURCE_VIEW, SWAGGER_VIEW,
        CHANGE_EVT_TYPES, CLASSES, SPLIT_VIEW } from './constants';
import FindBreakpointNodesVisitor from './../visitors/find-breakpoint-nodes-visitor';
import SyncLineNumbersVisitor from './../visitors/sync-line-numbers';
import SyncBreakpointsVisitor from './../visitors/sync-breakpoints';
import TreeUtils from './../model/tree-util';
import DefaultNodeFactory from './../model/default-node-factory';
import TreeBuilder from './../model/tree-builder';
import CompilationUnitNode from './../model/tree/compilation-unit-node';
import FragmentUtils from '../utils/fragment-utils';
import ErrorMappingVisitor from './../visitors/error-mapping-visitor';
import SyncErrorsVisitor from './../visitors/sync-errors';
import { EVENTS } from '../constants';
import ViewButton from './view-button';

/**
 * React component for BallerinaFileEditor.
 *
 * @class BallerinaFileEditor
 * @extends {React.Component}
 */
class BallerinaFileEditor extends React.Component {

    /**
     * Creates an instance of BallerinaFileEditor.
     * @param {Object} props React properties.
     * @memberof BallerinaFileEditor
     */
    constructor(props) {
        super(props);
        this.state = {
            initialParsePending: true,
            isASTInvalid: false,
            parsePending: false,
            swaggerViewTargetService: undefined,
            parseFailed: true,
            syntaxErrors: [],
            model: undefined,
            activeView: this.fetchState('activeView', SPLIT_VIEW),
            splitSize: this.fetchState('splitSize', (this.props.width / 2)),
            diagramMode: this.fetchState('diagramMode', 'action'),
            diagramFitToWidth: this.fetchState('diagramFitToWidth', true),
            lastRenderedTimestamp: undefined,
        };
        this.skipLoadingOverlay = false;

        // create debounced model update callbacks
        // we will use this to gracefull update design
        // view during source view (split mode) changes or redo/undo
        const updateUponSourceChange = _.debounce(() => {
            this.state.isASTInvalid = true;
            this.update(true);
        }, 500);
        const updateUponUndoRedo = _.debounce(() => {
            this.state.isASTInvalid = true;
            this.update(true);
        }, 100);


        // listen for the changes to file content
        this.props.file.on(CONTENT_MODIFIED, (evt) => {
            const originEvtType = evt.originEvt.type;
            if (originEvtType === CHANGE_EVT_TYPES.TREE_MODIFIED) {
                // Change was done from design view
                // do an immediate update to reflect tree changes
                this.forceUpdate();
                // since the source is changed, we need to sync
                // current AST with new position info
                this.skipLoadingOverlay = true;
                this.validateAndParseFile()
                    .then((state) => {
                        const newAST = state.model;
                        const currentAST = this.state.model;
                        // update environment object with updated current package info
                        this.updateEnvironment(this.environment, state.packageInfo);
                        this.syncASTs(currentAST, newAST);

                        // remove new AST from new state to be set
                        delete state.model;
                        this.skipLoadingOverlay = false;
                        this.setState(state);
                        this.props.editorModel.setProperty('ast', currentAST);
                    })
                    .catch(error => log.error(error));
            } else if (originEvtType === CHANGE_EVT_TYPES.SOURCE_MODIFIED) {
                updateUponSourceChange();
            } else if (originEvtType === UNDO_EVENT || originEvtType === REDO_EVENT) {
                updateUponUndoRedo();
            } else {
                // upon code format
                this.state.isASTInvalid = true;
                this.update(true);
            }
        });
        this.environment = new PackageScopedEnvironment();
        this.props.editorModel.setProperty('balEnvironment', this.environment);

        this.hideSwaggerAceEditor = false;

        // Resize the canvas
        props.commandProxy.on('resize', () => {
            this.update();
        }, this);
        // Format the source code.
        props.commandProxy.on(FORMAT, () => {
            if (this.props.isActive()) {
                let newContent = this.state.model.getSource(true);
                newContent = _.trim(newContent, '\n');
                // set the underlaying file.
                this.props.file.setContent(newContent, {
                    type: CHANGE_EVT_TYPES.CODE_FORMAT,
                });
            }
        });

        this.resetSwaggerView = this.resetSwaggerView.bind(this);
        this.handleSplitChange = this.handleSplitChange.bind(this);
        this.onModeChange = this.onModeChange.bind(this);
    }

    /**
     * @override
     * @memberof Diagram
     */
    getChildContext() {
        return {
            isTabActive: this.props.isActive,
            editor: this,
            astRoot: this.state.model,
            environment: this.environment,
            isPreviewViewEnabled: this.props.isPreviewViewEnabled,
        };
    }

    /**
     * lifecycle hook for component did mount
     */
    componentDidMount() {
        // parse the file & build the tree
        // then init the env with parsed symbols
        this.validateAndParseFile()
            .then((state) => {
                state.initialParsePending = false;
                this.setState(state);
                this.props.editorModel.setProperty('ast', state.model);
                this.props.commandProxy.dispatch(EVENTS.ACTIVE_BAL_AST_CHANGED, { ast: state.model });
            })
            .catch((error) => {
                log.error(error);
                // log the error & stop loading overlay
                this.setState({
                    initialParsePending: false,
                    parsePending: false,
                });
            });
    }

    /**
     * Decide whether to re-render or not
     */
    shouldComponentUpdate(nextProps, nextState) {
        return true;
    }

    /**
     * On ast modifications
     */
    onASTModified(evt) {
        TreeBuilder.modify(evt.origin);

        const newContent = this.state.model.getSource();
        // set breakpoints to model
        // this.reCalculateBreakpoints(this.state.model);
        // create a wrapping event object to indicate tree modification
        this.props.file.setContent(newContent, {
            type: CHANGE_EVT_TYPES.TREE_MODIFIED, originEvt: evt,
        });
        this.props.commandProxy.dispatch(EVENTS.ACTIVE_BAL_AST_CHANGED, { ast: this.state.model });
        if (evt.type === EVENTS.UPDATE_PACKAGE_DECLARATION) {
            this.props.commandProxy.dispatch(EVENTS.UPDATE_PACKAGE_DECLARATION, {
                packageName: evt.data.packageName,
                file: this.props.file,
                ast: this.state.model,
            });
        }
    }

    /**
     * @description Get package name from astRoot
     * @returns string - Package name
     */
    getPackageName(astRoot) {
        return TreeUtil.getPackageNameString(astRoot);
    }

    /**
     * Get the top most parent of the node
     * @param node - node that is to be added
     */
    getParent(node) {
        let parentNode = node.parent;
        if (!TreeUtils.isService(parentNode) && !TreeUtils.isConnector(parentNode) && !TreeUtils.isFunction(parentNode)
            && !TreeUtils.isResource(parentNode) && !TreeUtils.isAction(parentNode)) {
            parentNode = this.getParent(parentNode);
        }
        return parentNode;
    }
    /**
     * set active view
     * @param {string} newView ID of the new View
     */
    setActiveView(newView) {
        if (this.state.activeView !== newView) {
            if (newView === DESIGN_VIEW) {
                this.props.editorModel.customTitleClass = CLASSES.TAB_TITLE.DESIGN_VIEW;
            } else {
                this.props.editorModel.customTitleClass = '';
            }
        }
        // avoid additional re-render by directly updating state
        // next call update() to re-render
        // (and parse before re-render if necessary)
        // @see BallerinaFileEditor#update
        this.state.activeView = newView;
        this.update();
    }

    /**
     * Change the diagram mode.
     *
     * @param {any} data event data
     * @memberof BallerinaFileEditor
     */
    onModeChange(data) {
        this.setState({
            diagramMode: data.mode,
            diagramFitToWidth: data.fitToWidth,
        });
        this.persistState();
    }

    /**
     * @returns {File} file associated with the editor
     */
    getFile() {
        return this.props.file;
    }

    /**
     * Change the diagram fitto width.
     *
     * @param {any} newState
     * @memberof BallerinaFileEditor
     */
    handleFitToWidthChange(state) {
        this.setState({ diagramFitToWidth: state });
        this.persistState();
    }

    handleSplitChange(size) {
        this.setState({ splitSize: size });
    }
    /**
     * Sync updated information into current AST instance
     * @param {ASTNode} currentAST Currently used AST instance
     * @param {ASTNode} newAST A new AST with up-to-date position
     */
    syncASTs(currentAST, newAST) {
        const syncLineNumbersVisitor = new SyncLineNumbersVisitor(newAST);
        currentAST.sync(syncLineNumbersVisitor, newAST);
        const syncBreakpoints = new SyncBreakpointsVisitor(newAST);
        currentAST.sync(syncBreakpoints, newAST);
        const syncErrorsVisitor = new SyncErrorsVisitor();
        currentAST.sync(syncErrorsVisitor, newAST);

        const newBreakpoints = syncBreakpoints.getBreakpoints();
        this.updateBreakpoints(newBreakpoints, newAST);
    }

    /**
     * Update environment object with updated current package info
     * @param {PackageScopedEnvironment} environment Package scoped environment
     * @param {Object} currentPackageInfo Updated current package information
     */
    updateEnvironment(environment, currentPackageInfo) {
        if (currentPackageInfo) {
            const pkg = BallerinaEnvFactory.createPackage();
            pkg.initFromJson(currentPackageInfo);
            environment.setCurrentPackage(pkg);
        }
    }

    /**
     * Go to source of given AST Node
     *
     * @param {ASTNode} node AST Node
     */
    goToSource(node) {
        if (!_.isNil(node)) {
            const { position, type } = node;
            // If node has position info
            if (position) {
                const { startLine, startColumn } = position;
                this.jumpToSourcePosition(startLine - 1, startColumn - 1);
            } else {
                log.error(`Unable to find location info from ${type} node.`);
            }
        } else {
            log.error('Invalid node to find source line.');
        }
    }

    /**
     * Activates source view and move cursor to given position
     *
     * @param {number} line Line number
     * @param {number} offset Line offset
     */
    jumpToSourcePosition(line, offset) {
        const { activeView } = this.state;
        if (activeView === DESIGN_VIEW) {
            this.setActiveView(SOURCE_VIEW);
        }
        this.props.commandProxy
            .dispatch(GO_TO_POSITION, {
                file: this.props.file,
                row: line,
                column: offset,
            });
    }

    /**
     * Display the swagger view for given service def node
     *
     * @param {ServiceDefinition} serviceDef Service def node to display
     */
    showSwaggerViewForService(serviceDef) {
        // not using setState to avoid multiple re-renders
        // this.update() will finally trigger re-render
        this.state.swaggerViewTargetService = serviceDef;
        this.state.activeView = SWAGGER_VIEW;
        this.update();
    }

    /**
     * Open documentation for the given symbol
     *
     * @param {string} pkgName
     * @param {string} symbolName
     */
    openDocumentation(packageName, symbolName) {
        this.props.commandProxy.dispatch(LAYOUT_COMMANDS.SHOW_VIEW,
            {
                id: DOC_VIEW_ID,
                additionalProps: {
                    packageName, symbolName,
                },
                options: {
                    clone: true,
                    key: `${DOC_VIEW_ID}&${packageName}&${symbolName}`,
                },
            });
    }

    /**
     * Update the diagram
     * @param {boolean} skipLoadingOverlay
     */
    update(skipLoadingOverlay = false) {
        this.skipLoadingOverlay = skipLoadingOverlay;
        // We need to rebuild the AST if we are not in source-view
        // and current AST is marked as invalid
        if (this.state.isASTInvalid && this.state.activeView !== SOURCE_VIEW) {
            this.validateAndParseFile()
                .then((state) => {
                    this.skipLoadingOverlay = false;
                    this.setState(state);
                    this.forceUpdate();
                    this.props.editorModel.setProperty('ast', state.model);
                    this.props.commandProxy.dispatch(EVENTS.ACTIVE_BAL_AST_CHANGED, { ast: state.model });
                })
                .catch(error => log.error(error));
        } else {
            this.forceUpdate();
        }
    }

    /**
     * Validate & parse current content of the file
     * and build AST.
     * Then init the env with parsed symbols.
     * Finally return a promise which resolve the new state
     * of the component.
     *
     */
    validateAndParseFile() {
        this.setState({
            parsePending: true,
        });
        const file = this.props.file;
        // final state to be passed into resolve
        const newState = {
            parsePending: false,
            parseFailed: false,
            isASTInvalid: false,
            syntaxErrors: [],
        };
        // try to parse the file
        return parseFile(file)
            .then((data = {}) => {
                // keep current package information.
                newState.packageInfo = data.packageInfo;
                data.errors = data.errors || [];
                const syntaxErrors = data.errors.filter(({ category }) => {
                    return category === 'SYNTAX';
                });
                const runtimeFailures = data.errors.filter(({ category }) => {
                    return category === 'RUNTIME';
                });
                this.semanticErrors = data.errors.filter(({ category }) => {
                    return category === 'SEMANTIC';
                });
                // if syntax errors are found or model is not found
                if (!_.isEmpty(syntaxErrors)
                    || _.isNil(data.model)
                    || _.isNil(data.model.kind)
                    || (!_.isEmpty(runtimeFailures) && (_.isNil(data.model) || _.isNil(data.model.kind)))
                ) {
                    newState.parseFailed = true;
                    newState.syntaxErrors = syntaxErrors;
                    newState.isASTInvalid = true;
                    // keep current AST when in preview view - even though its not valid
                    if (!this.state.activeView === SPLIT_VIEW) {
                        newState.model = undefined;
                        // cannot be in a view which depends on AST
                        // hence forward to source view
                        newState.activeView = SOURCE_VIEW;
                    }
                }
                if (newState.parseFailed && !_.isEmpty(runtimeFailures)) {
                    this.context.alert.showError('Unexpected error occurred while parsing.'
                        + runtimeFailures[0].text);
                }
                // if no error found and no model too
                if ((_.isEmpty(syntaxErrors) && _.isEmpty(runtimeFailures))
                    && (_.isNil(data.model) || _.isNil(data.model.kind))) {
                    this.context.alert.showError('Unexpected error occurred while parsing.');
                } else if (!newState.parseFailed) {
                    const ast = TreeBuilder.build(data.model);
                    ast.setFile(file);
                    this.markBreakpointsOnAST(ast);
                    // Now we will enrich the model with Semantic errors.
                    const errorMappingVisitor = new ErrorMappingVisitor();
                    errorMappingVisitor.setErrorList(this.semanticErrors);
                    ast.accept(errorMappingVisitor);
                    // register the listener for ast modifications
                    ast.on(CHANGE_EVT_TYPES.TREE_MODIFIED, (evt) => {
                        this.onASTModified(evt);
                    });

                    newState.lastRenderedTimestamp = file.lastUpdated;
                    newState.model = ast;
                }
                return BallerinaEnvironment.initialize()
                    .then(() => {
                        this.environment.init();
                        const pkgNode = data.packageInfo;
                        if (!_.isNil(pkgNode)) {
                            const pkg = BallerinaEnvFactory.createPackage();
                            pkg.initFromJson(pkgNode);
                            this.environment.setCurrentPackage(pkg);
                        }
                        return newState;
                    });
            });
    }
    markBreakpointsOnAST(ast) {
        const fileName = `${this.props.file.name}.${this.props.file.extension}`;
        const breakpoints = DebugManager.getDebugPoints(fileName);
        const findBreakpointsVisitor = new FindBreakpointNodesVisitor(ast);
        findBreakpointsVisitor.setBreakpoints(breakpoints);
        ast.accept(findBreakpointsVisitor);
    }
    updateBreakpoints(breakpoints, ast) {
        const fileName = `${this.props.file.name}.${this.props.file.extension}`;
        const packagePath = this.getPackageName(ast);
        DebugManager.removeAllBreakpoints(fileName);
        breakpoints.forEach((lineNumber) => {
            DebugManager.addBreakPoint(lineNumber, fileName, packagePath);
        });
    }

    /**
     * Save editor state in to localstorage.
     *
     * @memberof BallerinaFileEditor
     */
    persistState() {
        const appContext = this.props.ballerinaPlugin.appContext;
        appContext.pref.put(this.props.file.id, {
            activeView: this.state.activeView,
            splitSize: this.state.splitSize,
            diagramFitToWidth: this.state.diagramFitToWidth,
            diagramMode: this.state.diagramMode,
        });
    }

    /**
     * Fetch editor active state if stored in localstorage.
     *
     * @memberof BallerinaFileEditor
     */
    fetchState(attribute, defaultVal = undefined) {
        const appContext = this.props.ballerinaPlugin.appContext;
        const data = appContext.pref.get(this.props.file.id);
        return (data && !_.isNil(data[attribute])) ? data[attribute] : defaultVal;
    }

    resetSwaggerView() {
        this.hideSwaggerAceEditor = false;
    }

    /**
     * @override
     * @memberof BallerinaFileEditor
     */
    render() {
        // persist state before render
        this.persistState();
        // Decision on which view to show, depends on several factors.
        // Even-though we have a state called activeView, we cannot simply decide on that value.
        // For example, for swagger-view to be active, we need to make sure
        // that the file is parsed properly and an AST is available. For this reason and more, we
        // use several other states called parseFailed, syntaxErrors, etc. to decide
        // which view to show.
        // ----------------------------
        // syntaxErrors  - An array of errors received from validator service.
        // parseFailed   - Indicates whether the parser was invoked successfully and an AST was built.
        // activeView    - Indicates which view user wanted to be displayed.
        // parsePending  - Indicates an ongoing validate & parse process in background

        // if we are automatically switching to source view due to syntax errors in file,
        // popup error list in source view so that the user is aware of the cause
        const popupErrorListInSourceView = this.state.activeView === DESIGN_VIEW
            && (!_.isEmpty(this.state.syntaxErrors)
                || (this.state.parseFailed && !this.state.parsePending));

        // If there are syntax errors, forward editor to source view - if split view is not active.
        // If split view is active - we will render an overly on top of design view with error list
        if (!_.isEmpty(this.state.syntaxErrors)) {
            if (this.state.activeView === DESIGN_VIEW) {
                this.state.activeView = SOURCE_VIEW;
            }
        }

        const showDesignView = this.state.initialParsePending
            || (this.state.activeView === SPLIT_VIEW)
            || ((!this.state.parseFailed)
                && _.isEmpty(this.state.syntaxErrors)
                && this.state.activeView === DESIGN_VIEW);
        const showSourceView = (this.state.parseFailed
            || !_.isEmpty(this.state.syntaxErrors)
            || this.state.activeView === SOURCE_VIEW
            || this.state.activeView === SPLIT_VIEW);
        const showSwaggerView = (!this.state.parseFailed
            && !_.isNil(this.state.swaggerViewTargetService)
            && this.state.activeView === SWAGGER_VIEW);

        const showLoadingOverlay = !this.skipLoadingOverlay && this.state.parsePending;

        const sourceWidth = (this.state.activeView === SOURCE_VIEW) ? this.props.width :
            this.state.splitSize;
        const designWidth = (this.state.activeView === DESIGN_VIEW) ? this.props.width :
            this.props.width - this.state.splitSize;

        const sourceView = (
            <SourceView
                displayErrorList={popupErrorListInSourceView}
                parseFailed={this.state.parseFailed}
                file={this.props.file}
                commandProxy={this.props.commandProxy}
                show={showSourceView}
                panelResizeInProgress={this.props.panelResizeInProgress}
                width={sourceWidth}
                height={this.props.height}
            />
        );

        const designView = (
            <div>
                <CSSTransitionGroup
                    transitionName='loading-overlay'
                    transitionEnterTimeout={300}
                    transitionLeaveTimeout={300}
                >
                    {showLoadingOverlay &&
                        <Dimmer active inverted>
                            <Loader size='large'>Loading</Loader>
                        </Dimmer>
                    }
                </CSSTransitionGroup>
                <DesignView
                    model={this.state.model}
                    show={showDesignView}
                    file={this.props.file}
                    commandProxy={this.props.commandProxy}
                    width={designWidth}
                    height={this.props.height}
                    panelResizeInProgress={this.props.panelResizeInProgress}
                    disabled={this.state.parseFailed}
                    onModeChange={this.onModeChange}
                    mode={this.state.diagramMode}
                    fitToWidth={this.state.diagramFitToWidth}
                />
            </div>
        );

        return (
            <div
                id={`bal-file-editor-${this.props.file.id}`}
                className='bal-file-editor'
            >
                {(() => {
                    switch (this.state.activeView) {
                        case SPLIT_VIEW:
                            return (
                                <div className='split-view-container'>
                                    <SplitPane
                                        split='vertical'
                                        defaultSize={this.state.splitSize}
                                        onChange={this.handleSplitChange}
                                    >
                                        {sourceView}
                                        <div>
                                            {this.state.activeView === SPLIT_VIEW
                                                && !_.isEmpty(this.state.syntaxErrors) &&
                                                <div className='syntax-error-overlay'>
                                                    <div className='error-list'>
                                                        <div className='list-heading'>
                                                            Cannot update design view due to syntax errors.
                                                        </div>
                                                    </div>
                                                </div>
                                            }
                                            {designView}
                                        </div>
                                    </SplitPane>
                                </div>
                            );
                        default:
                            return [sourceView, designView];
                    }
                })()}
                <div style={{ display: showSwaggerView ? 'block' : 'none' }}>
                    <SwaggerView
                        targetService={this.state.swaggerViewTargetService}
                        commandProxy={this.props.commandProxy}
                        hideSwaggerAceEditor={this.hideSwaggerAceEditor}
                        resetSwaggerViewFun={this.resetSwaggerView}
                        height={this.props.height}
                        width={this.props.width}
                    />
                </div>
                { !showSwaggerView &&
                    <div className={cn('bottom-right-controls-container')}>
                        <ViewButton
                            label='Source View'
                            icon='code'
                            onClick={() => { this.setActiveView(SOURCE_VIEW); }}
                            active={this.state.activeView === SOURCE_VIEW}
                        />
                        <ViewButton
                            label='Split View'
                            icon='split-view'
                            onClick={() => { this.setActiveView(SPLIT_VIEW); }}
                            active={this.state.activeView === SPLIT_VIEW}
                        />
                        <ViewButton
                            label='Design View'
                            icon='design-view'
                            onClick={() => { this.setActiveView(DESIGN_VIEW); }}
                            active={this.state.activeView === DESIGN_VIEW}
                        />
                    </div>
                }
            </div>
        );
    }
}

BallerinaFileEditor.propTypes = {
    editorModel: PropTypes.objectOf(Object).isRequired,
    file: PropTypes.instanceOf(File).isRequired,
    ballerinaPlugin: PropTypes.objectOf(Object).isRequired,
    isActive: PropTypes.func.isRequired,
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
    }).isRequired,
    isPreviewViewEnabled: PropTypes.bool,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
};

BallerinaFileEditor.defaultProps = {
    isPreviewViewEnabled: false,
};

BallerinaFileEditor.contextTypes = {
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
    }).isRequired,
};

BallerinaFileEditor.childContextTypes = {
    isTabActive: PropTypes.func.isRequired,
    astRoot: PropTypes.instanceOf(CompilationUnitNode),
    editor: PropTypes.instanceOf(BallerinaFileEditor).isRequired,
    environment: PropTypes.instanceOf(PackageScopedEnvironment).isRequired,
    isPreviewViewEnabled: PropTypes.bool.isRequired,
};

export default BallerinaFileEditor;
