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
import alerts from 'alerts';
import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import CSSTransitionGroup from 'react-transition-group/CSSTransitionGroup';
import DebugManager from './../../debugger/debug-manager';
import DesignView from './design-view.jsx';
import SourceView from './source-view.jsx';
import SwaggerView from './swagger-view.jsx';
import File from './../../../src/core/workspace/model/file';
import { validateFile, parseFile, getProgramPackages } from '../../api-client/api-client';
import PackageScopedEnvironment from './../env/package-scoped-environment';
import BallerinaEnvFactory from './../env/ballerina-env-factory';
import BallerinaEnvironment from './../env/environment';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';
import { DESIGN_VIEW, SOURCE_VIEW, SWAGGER_VIEW, CHANGE_EVT_TYPES, CLASSES } from './constants';
import { CONTENT_MODIFIED } from './../../constants/events';
import { OPEN_SYMBOL_DOCS, GO_TO_POSITION } from './../../constants/commands';
import FindBreakpointNodesVisitor from './../visitors/find-breakpoint-nodes-visitor';
import FindBreakpointLinesVisitor from './../visitors/find-breakpoint-lines-visitor';
import FindLineNumbersVisiter from './../visitors/find-line-numbers';
import UpdateLineNumbersVisiter from './../visitors/update-line-numbers';
import TreeBuilder from './../model/tree-builder';
import CompilationUnitNode from './../model/tree/compilation-unit-node';


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
        const astRoot = new CompilationUnitNode();
        // TODOX astRoot.setFile(this.props.file);
        this.state = {
            initialParsePending: true,
            isASTInvalid: false,
            validatePending: false,
            parsePending: false,
            swaggerViewTargetService: undefined,
            parseFailed: true,
            syntaxErrors: [],
            model: astRoot,
            activeView: DESIGN_VIEW,
            lastRenderedTimestamp: undefined,
        };
        this.skipLoadingOverlay = false;
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
                        this.syncASTs(currentAST, newAST);
                        // remove new AST from new state to be set
                        delete state.model;
                        this.skipLoadingOverlay = false;
                        if (!(state.parseFailed && this.props.isPreviewViewEnabled)) {
                            this.setState(state);
                        }
                    })
                    .catch(error => log.error(error));
            } else {
                // Source was changed due to a source editor
                // change or undo/redo
                this.state.isASTInvalid = true;
                this.update(true);
            }
        });
        this.environment = new PackageScopedEnvironment();

        this.hideSwaggerAceEditor = false;
        // Show the swagger view when 'try it' is invoked.
        props.commandProxy.on('show-try-it-view', () => {
            // Creating try it service for first service definition.
            this.hideSwaggerAceEditor = true;
            this.showSwaggerViewForService(this.state.model.getServiceDefinitions()[0]);
        }, this);

        // Show the swagger view when 'try it' is invoked.
        props.commandProxy.on('hide-try-it-view', () => {
            if (this.state.activeView === SWAGGER_VIEW) {
                this.setState({
                    activeView: DESIGN_VIEW,
                });
                this.resetSwaggerView();
            }
        }, this);
        // Resize the canvas
        props.commandProxy.on('resize', () => {
            this.update();
        }, this);

        this.resetSwaggerView = this.resetSwaggerView.bind(this);
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
                if (!(state.parseFailed && this.props.isPreviewViewEnabled)) {
                    this.setState(state);
                }
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
     * lifecycle hook for component will receive props
     */
    componentWillReceiveProps(newProps) {
        // editor tab was not active previously and now becoming active
        if (!this.props.isActive && newProps.isActive) {
            // we need to re-render
            this.update();
        }
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
        const sourceGenVisitor = new SourceGenVisitor();
        this.state.model.accept(sourceGenVisitor);
        const newContent = sourceGenVisitor.getGeneratedSource();
        // set breakpoints to model
        this.reCalculateBreakpoints(this.state.model);
        // create a wrapping event object to indicate tree modification
        this.props.file.setContent(newContent, {
            type: CHANGE_EVT_TYPES.TREE_MODIFIED, originEvt: evt,
        });
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
     * @returns {File} file associated with the editor
     */
    getFile() {
        return this.props.file;
    }

    resetSwaggerView() {
        this.hideSwaggerAceEditor = false;
    }

    /**
     * Sync updated information into current AST instance
     * @param {ASTNode} currentAST Currently used AST instance
     * @param {ASTNode} newAST A new AST with up-to-date position
     */
    syncASTs(currentAST, newAST) {
        const findLineNumbersVisiter = new FindLineNumbersVisiter(newAST);
        newAST.accept(findLineNumbersVisiter);
        const lineNumbers = findLineNumbersVisiter.getLineNumbers();

        const updateLineNumbersVisiter = new UpdateLineNumbersVisiter(currentAST);
        updateLineNumbersVisiter.setLineNumbers(lineNumbers);
        currentAST.accept(updateLineNumbersVisiter);
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
                const { startLine, startOffset } = position;
                this.jumpToSourcePosition(startLine - 1, startOffset);
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
        if (!this.props.isPreviewViewEnabled) {
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
    openDocumentation(pkgName, symbolName) {
        this.props.commandProxy
            .dispatch(OPEN_SYMBOL_DOCS, pkgName, symbolName);
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
                    if (!(state.parseFailed && this.props.isPreviewViewEnabled)) {
                        this.setState(state);
                        this.forceUpdate();
                    }
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
            validatePending: true,
            parsePending: true,
        });
        const file = this.props.file;
        return new Promise((resolve, reject) => {
            // final state to be passed into resolve
            const newState = {
                validatePending: false,
                parsePending: false,
            };
            // first validate the file for syntax errors
            validateFile(file)
                .then((errors) => {
                    // if syntax errors are found
                    if (!_.isEmpty(errors)) {
                        newState.parseFailed = true;
                        newState.syntaxErrors = errors;
                        newState.validatePending = false;
                        const astRoot = new CompilationUnitNode();
                        // TODOX astRoot.setFile(this.props.file);
                        newState.model = astRoot;
                        // Cannot proceed due to syntax errors.
                        // Hence resolve now.
                        resolve(newState);
                        return;
                    } else {
                        // we need to fire a update for this state as soon as we
                        // receive the validate response to prevent additional
                        // wait time to some user actions.
                        // For example: User had a syntax error (and current state represents it) and corrected it in
                        // source editor. Then he wanted to move design view. If we do not do the update here,
                        // (meaning if we set newState.syntaxErrors = [] without using setState)
                        // displaying design view will be delayed until the rest of the logic in this function
                        // is finished. Instead, we do the update here, so it will switch to design view as soon
                        // as it knows the syntax is valid. However, the loading overlay will displayed until
                        // the AST is ready so that render can continue.
                        this.setState({
                            validatePending: false,
                            syntaxErrors: [],
                        });
                    }
                    // if not, continue parsing the file & building AST
                    parseFile(file)
                        .then((jsonTree) => {
                            /* TODOX
                            // something went wrong with the parser
                            if (_.isNil(jsonTree.root)) {
                                log.error('Error while parsing the file: ' + file.name
                                    + ' Error:' + jsonTree.errorMessage || jsonTree);
                                // cannot be in a view which depends on AST
                                // hence forward to source view
                                newState.activeView = SOURCE_VIEW;
                                newState.parseFailed = true;
                                newState.isASTInvalid = true;
                                const astRoot = new CompilationUnitNode();
                                // TODOX astRoot.setFile(this.props.file);
                                newState.model = astRoot;
                                resolve(newState);
                                alerts.error('Seems to be there is a bug in back-end parser.'
                                        + 'Please report an issue attaching current source.');
                                return;
                            }
                            */
                            // get ast from json
                            const ast = TreeBuilder.build(jsonTree /* , this.props.file*/);
                            console.log(ast);
                            // TODOX this.markBreakpointsOnAST(ast);
                            // register the listener for ast modifications
                            ast.on(CHANGE_EVT_TYPES.TREE_MODIFIED, (evt) => {
                                this.onASTModified(evt);
                            });

                            newState.lastRenderedTimestamp = file.lastUpdated;
                            newState.parseFailed = false;
                            newState.isASTInvalid = false;
                            newState.model = ast;

                            // TODOX const pkgName = ast.getPackageDefinition().getPackageName();
                            // update package name of the file
                            // TODOX file.packageName = pkgName || '.';
                            // init bal env in background
                            BallerinaEnvironment.initialize()
                                .then(() => {
                                    this.environment.init();

                                    // Resolve now and let rest happen in background
                                    resolve(newState);

                                    // fetch program packages
                                    getProgramPackages(file)
                                        .then((data) => {
                                            // if any packages were found
                                            const packages = data.result.packages;
                                            if (!_.isNil(packages)) {
                                                const pkges = [];
                                                packages.forEach((pkgNode) => {
                                                    const pkg = BallerinaEnvFactory.createPackage();
                                                    pkg.initFromJson(pkgNode);
                                                    pkges.push(pkg);
                                                });
                                                this.environment.addPackages(pkges);
                                            }
                                            this.update();
                                        })
                                        .catch(error => log.error(error));
                                })
                                .catch(reject);
                        })
                        .catch(reject);
                })
                .catch(reject);
        });
    }
    reCalculateBreakpoints(newAst) {
        const findBreakpointsVisiter = new FindBreakpointLinesVisitor(newAst);
        newAst.accept(findBreakpointsVisiter);
        const breakpoints = findBreakpointsVisiter.getBreakpoints();
        const fileName = this.props.file.name;
        const packagePath = newAst.getPackageDefinition().getPackageName() || '.';
        DebugManager.removeAllBreakpoints(fileName);
        breakpoints.forEach((lineNumber) => {
            DebugManager.addBreakPoint(lineNumber, fileName, packagePath);
        });
    }
    markBreakpointsOnAST(ast) {
        const fileName = this.props.file.name;
        const breakpoints = DebugManager.getDebugPoints(fileName);
        const findBreakpointsVisitor = new FindBreakpointNodesVisitor(ast);
        findBreakpointsVisitor.setBreakpoints(breakpoints);
        ast.accept(findBreakpointsVisitor);
    }

    /**
     * @override
     * @memberof BallerinaFileEditor
     */
    render() {
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

        // If there are syntax errors, forward editor to source view & update state
        // to make that decision reflect in state. This is to prevent automatic
        // redirection to design view once the syntax errors are fixed in source view.
        if (!this.props.isPreviewViewEnabled && !this.state.validatePending && !_.isEmpty(this.state.syntaxErrors)
                && this.state.activeView !== SOURCE_VIEW) {
            this.state.activeView = SOURCE_VIEW;
        }

        const showDesignView = this.state.initialParsePending
            || ((!this.state.parseFailed)
                                            && _.isEmpty(this.state.syntaxErrors)
                                                && this.state.activeView === DESIGN_VIEW);
        const showSourceView = this.state.parseFailed
            || !_.isEmpty(this.state.syntaxErrors)
            || this.state.activeView === SOURCE_VIEW;
        const showSwaggerView = (!this.state.parseFailed
                                    && !_.isNil(this.state.swaggerViewTargetService)
                                        && this.state.activeView === SWAGGER_VIEW);
        const showLoadingOverlay = !this.skipLoadingOverlay && this.state.parsePending;

        return (
            <div
                id={`bal-file-editor-${this.props.file.id}`}
                className='bal-file-editor'
            >
                <CSSTransitionGroup
                    transitionName="loading-overlay"
                    transitionEnterTimeout={300}
                    transitionLeaveTimeout={300}
                >

                    {showLoadingOverlay &&
                        <div className='bal-file-editor-loading-container'>
                            <div id="parse-pending-loader">
                                Loading
                            </div>
                        </div>
                    }
                </CSSTransitionGroup>
                <DesignView
                    model={this.state.model}
                    show={showDesignView}
                    file={this.props.file}
                    commandProxy={this.props.commandProxy}
                />
                <SourceView
                    displayErrorList={popupErrorListInSourceView}
                    parseFailed={this.state.parseFailed}
                    file={this.props.file}
                    commandProxy={this.props.commandProxy}
                    show={showSourceView}
                />
                <div style={{ display: showSwaggerView ? 'block' : 'none' }}>
                    <SwaggerView
                        targetService={this.state.swaggerViewTargetService}
                        commandProxy={this.props.commandProxy}
                        hideSwaggerAceEditor={this.hideSwaggerAceEditor}
                        resetSwaggerViewFun={this.resetSwaggerView}
                    />
                </div>
            </div>
        );
    }
}

BallerinaFileEditor.propTypes = {
    editorModel: PropTypes.objectOf(Object).isRequired,
    file: PropTypes.instanceOf(File).isRequired,
    isActive: PropTypes.bool.isRequired,
    commandProxy: PropTypes.shape({
        on: PropTypes.func.isRequired,
        dispatch: PropTypes.func.isRequired,
    }).isRequired,
    isPreviewViewEnabled: PropTypes.bool,
};

BallerinaFileEditor.defaultProps = {
    isPreviewViewEnabled: false,
};

BallerinaFileEditor.childContextTypes = {
    isTabActive: PropTypes.bool.isRequired,
    astRoot: PropTypes.instanceOf(CompilationUnitNode).isRequired,
    editor: PropTypes.instanceOf(BallerinaFileEditor).isRequired,
    environment: PropTypes.instanceOf(PackageScopedEnvironment).isRequired,
    isPreviewViewEnabled: PropTypes.bool.isRequired,
};

export default BallerinaFileEditor;
