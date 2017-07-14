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
import commandManager from 'command';
import React from 'react';
import PropTypes from 'prop-types';
import ASTVisitor from 'ballerina/visitors/ast-visitor';
import DesignView from './design-view.jsx';
import SourceView from './source-view.jsx';
import SwaggerView from './swagger-view.jsx';
import File from './../../workspace/file';
import { validateFile, parseFile, getProgramPackages } from '../../api-client/api-client';
import BallerinaASTDeserializer from './../ast/ballerina-ast-deserializer';
import BallerinaASTRoot from './../ast/ballerina-ast-root';
import PackageScopedEnvironment from './../env/package-scoped-environment';
import BallerinaEnvFactory from './../env/ballerina-env-factory';
import BallerinaEnvironment from './../env/environment';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';
import { DESIGN_VIEW, SOURCE_VIEW, SWAGGER_VIEW, CHANGE_EVT_TYPES } from './constants';
import { CONTENT_MODIFIED } from './../../constants/events';
import { OPEN_SYMBOL_DOCS } from './../../constants/commands';
import { getLangServerClientInstance } from './../../langserver/lang-server-client-controller';

const sourceViewTabHeaderClass = 'inverse';

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
            swaggerViewTargetService: undefined,
            parseFailed: true,
            syntaxErrors: [],
            model: new BallerinaASTRoot(),
            activeView: DESIGN_VIEW,
        };
        this.sourceModified = false;
        // listen for the changes to file content
        this.props.file.on(CONTENT_MODIFIED, (evt) => {
            // Change was done from the source view.
            // Just keep track of it for the moment and when user tries
            // to switch back to a different view, we will try to rebuild
            // the ast. See BallerinaFileEditor#setActiveView.
            // NOTE: This is to avoid unnecessary parser invocations for each change
            // event in source view
            if (evt.originEvt.type === CHANGE_EVT_TYPES.SOURCE_MODIFIED) {
                this.sourceModified = true;
            } else if (evt.originEvt.type === CHANGE_EVT_TYPES.TREE_MODIFIED) {
                // change was done from design view
                // AST is directly modified hence no need to parse again
                // we just need to update the diagram
                this.update();
            }
        });
        this.environment = new PackageScopedEnvironment();
    }

    /**
     * lifecycle hook for component did mount
     */
    componentDidMount() {
        // parse the file & build the tree
        // then init the env with parsed symbols
        this.validateAndParseFile()
            .then(state => this.setState(state))
            .catch(error => log.error(error));
    }

    /**
     * Update the diagram
     */
    update() {
        this.forceUpdate();
    }

    /**
     * Open documentation for the given symbol
     * 
     * @param {string} pkgName 
     * @param {string} symbolName 
     */
    openDocumentation(pkgName, symbolName) {
        this.props.commandManager
            .dispatch(OPEN_SYMBOL_DOCS, pkgName, symbolName);
    }

    /**
     * @override
     * @memberof Diagram
     */
    getChildContext() {
        return {
            editor: this,
            astRoot: this.state.model,
            environment: this.environment,
        };
    }

    /**
     * Display the swagger view for given service def node
     * 
     * @param {ServiceDefinition} serviceDef Service def node to display
     */
    showSwaggerViewForService(serviceDef) {
        this.setState({
            swaggerViewTargetService: serviceDef,
            activeView: SWAGGER_VIEW
        });
        this.update();
    }

    /**
     * set active view
     * @param {string} newView ID of the new View
     */
    setActiveView(newView) {
        const newState = {
            activeView: newView
        }
        // if modifications were done from source view
        // reparse the file
        if (this.sourceModified) {
            this.validateAndParseFile()
                .then((state) => {
                    this.sourceModified = false;
                    this.setState(_.merge(state, newState));
                })
                .catch(error => log.error(error));
        } else {
            this.setState(newState);
        }
    }

    /**
     * On ast modifications
     */
    onASTModified(evt) {
        const sourceGenVisitor = new SourceGenVisitor();
        this.state.model.accept(sourceGenVisitor);
        const newContent = sourceGenVisitor.getGeneratedSource();
        // create a wrapping event object to indicate tree modification
        this.props.file.setContent(newContent, {
            type: CHANGE_EVT_TYPES.TREE_MODIFIED, originEvt: evt
        });
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
        const file = this.props.file;
        return new Promise((resolve, reject) => {
            // final state to be passed into resolve
            let newState = {};
            // first validate the file for syntax errors
            validateFile(file)
                .then((errors) => {
                    // if syntax errors are found
                    if (!_.isEmpty(errors)) {
                        newState.parseFailed = true;
                        newState.syntaxErrors = errors;
                        newState.model = new BallerinaASTRoot();
                        // Cannot proceed due to syntax errors.
                        // Hence resolve now.
                        resolve(newState);
                    } else {
                        newState.syntaxErrors = [];
                    }
                    // if not, continue parsing the file & building AST
                    parseFile(file)
                        .then((jsonTree) => {
                            // get ast from json
                            const ast = BallerinaASTDeserializer.getASTModel(jsonTree);
                            // register the listener for ast modifications
                            ast.on(CHANGE_EVT_TYPES.TREE_MODIFIED, (evt) => {
                                this.onASTModified(evt);
                            });

                            newState.parseFailed = false;
                            newState.model = ast;

                            const pkgName = ast.getPackageDefinition().getPackageName();
                            // update package name of the file
                            file.setPackageName(pkgName || '');
                            // init bal env in background
                            BallerinaEnvironment.initialize()
                                .then(() => {
                                    this.environment.init();
                                    // fetch program packages
                                    getProgramPackages(file, getLangServerClientInstance())
                                        .then((data) => {
                                            // if any packages were found
                                            let packages = data.result.packages;
                                            if (!_.isNil(packages)) {
                                                const pkges = [];
                                                packages.forEach((pkgNode) => {
                                                    const pkg = BallerinaEnvFactory.createPackage();
                                                    pkg.initFromJson(pkgNode);
                                                    pkges.push(pkg);
                                                });
                                                this.environment.addPackages(pkges);
                                            }
                                            // All the things are successfull.
                                            // resolve now.
                                            resolve(newState);
                                        })
                                        .catch(reject)
                                })
                                .catch(reject);          
                        })
                        .catch(reject);
                })
                .catch(reject);
        });
    }

    /**
     * @override
     * @memberof BallerinaFileEditor
     */
    render() {
        // Desiding which view to show depends on several factors.
        // Eventhough we have a state called activeView, we cannot simply decide from that.
        // For example, for the design-view or the swagger-view to be active, we need to make sure
        // that the file is parsed properly and an AST is available. For these reasons, we 
        // use several other states such as parseFailed, syntaxErrors, etc. to decide
        // which view to show.
        // ----------------------------
        // syntaxErrors  - An array of errors received from validator service.
        // parseFailed   - Indicates whether the parser was invoked successfully and an AST was built.
        // activeView    - Indicates which view user wanted to be displayed.

        const showDesignView = (!this.state.parseFailed && this.state.activeView === DESIGN_VIEW);
        const showSourceView =  this.state.syntaxErrors.length > 0
                                    || this.state.parseFailed 
                                        || this.state.activeView === SOURCE_VIEW;
        const showSwaggerView = !this.state.parseFailed 
                                    && !_.isNil(this.state.swaggerViewTargetService)
                                        && this.state.activeView === SWAGGER_VIEW;
        // depending on the selected view - change tab header style
        // FIXME: find a better solution
        if (showDesignView) {
            this.props.tabHeader.removeClass(sourceViewTabHeaderClass);
        } else {
            this.props.tabHeader.addClass(sourceViewTabHeaderClass);
        }

        return (
            <div id={`bal-file-editor-${this.props.file.id}`}>
                <div style={{ display: showDesignView ? 'block' : 'none' }}>
                    <DesignView model={this.state.model} />
                </div>
                <div style={{ display: showSourceView ? 'block' : 'none' }}>
                    <SourceView file={this.props.file} commandManager={this.props.commandManager} />
                </div>
                <div style={{ display: showSwaggerView ? 'block' : 'none' }}>
                    <SwaggerView targetService={this.state.swaggerViewTargetService} />
                </div>
            </div>
        );
    }
}

BallerinaFileEditor.propTypes = {
    file: PropTypes.instanceOf(File).isRequired,
    tabHeader: PropTypes.instanceOf(Object).isRequired,
    commandManager: PropTypes.instanceOf(commandManager).isRequired
};

BallerinaFileEditor.childContextTypes = {
    astRoot: PropTypes.instanceOf(BallerinaASTRoot).isRequired,
    editor: PropTypes.instanceOf(BallerinaFileEditor).isRequired,
    environment: PropTypes.instanceOf(PackageScopedEnvironment).isRequired,
};

export default BallerinaFileEditor;