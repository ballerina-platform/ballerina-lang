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
import DesignView from './design-view.jsx';
import SourceView from './source-view.jsx';
import SwaggerView from './swagger-view.jsx';
import File from './../../workspace/file';
import { parseFile, getProgramPackages } from '../../api-client/api-client';
import BallerinaASTDeserializer from './../ast/ballerina-ast-deserializer';
import BallerinaASTRoot from './../ast/ballerina-ast-root';
import PackageScopedEnvironment from './../env/package-scoped-environment';
import BallerinaEnvFactory from './../env/ballerina-env-factory';
import BallerinaEnvironment from './../env/environment';
import SourceGenVisitor from './../visitors/source-gen/ballerina-ast-root-visitor';

export const DESIGN_VIEW = 'DESIGN_VIEW';
export const SOURCE_VIEW = 'SOURCE_VIEW';
export const SWAGGER_VIEW = 'SWAGGER_VIEW';

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
            sourceModified: false,
            parseFailed: true,
            model: new BallerinaASTRoot(),
            activeView: DESIGN_VIEW,
        };
        // listen for the changes to file content
        this.props.file.on('content-modified', (evt) => {
            // Change was done from source editor.
            // just keep track of it now and when user tries
            // to switch back to design view, we will try rebuild
            // the ast
            if (evt.originEvt.type === 'source-modified') {
                this.setState({
                    sourceModified: true,
                })
            } else if (evt.originEvt.type === 'tree-modified'){
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
        this.parseFile();
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
            .dispatch('open-documentation', pkgName, symbolName);
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
     * set active view
     * @param {string} newView ID of the new View
     */
    setActiveView(newView) {
        const newState = { 
            activeView: newView 
        }
        // if modifications were done from source view
        // reparse the file
        if (newView === DESIGN_VIEW && this.state.sourceModified) {
            newState.sourceModified = false;
            this.parseFile();
            this.setState(newState);
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
        this.props.file.setContent(newContent, {type: 'tree-modified', originEvt: evt});
    }

    /**
     * Parse current content of the file
     * and build AST
     * Then init the env with parsed symbols
     */
    parseFile() {
        const file = this.props.file;
        parseFile(file)
            .then((jsonTree) => {
                // get ast from json
                const ast = BallerinaASTDeserializer.getASTModel(jsonTree);
                // re-draw upon ast changes
                ast.on('tree-modified', (evt) => {
                    this.onASTModified(evt);
                });
                this.setState({
                    parseFailed: false,
                    model: ast,
                });
                this.update();
                const pkgName = ast.getPackageDefinition().getPackageName();
                // update package name of the file
                file.setPackageName(pkgName || '');
                // init bal env if not init yet
                BallerinaEnvironment.initialize()
                    .then(() => {
                        this.environment.init();
                        // fetch program packages
                        getProgramPackages(file)
                            .then((data) => {
                                const pkges = [];
                                data.packages.forEach((pkgNode) => {
                                    const pkg = BallerinaEnvFactory.createPackage();
                                    pkg.initFromJson(pkgNode);
                                });
                                this.environment.addPackages(pkges);
                                this.update();
                            })
                            .catch(error => log.error(error))
                    })
                    .catch((error) => {
                        log.error('Error while env init. ' + error);
                    });
                
            })
            .catch(error => log.error(error));
    }

    /**
     * @override
     * @memberof BallerinaFileEditor
     */
    render() {
        const showDesignView = (!this.state.parseFailed && this.state.activeView === DESIGN_VIEW)
                || !this.props.file.getContent();
        const showSourceView = !showDesignView || this.state.activeView === SOURCE_VIEW;
        const showSwaggerView = !showDesignView && this.state.activeView === SWAGGER_VIEW;
        if (showDesignView) {
            this.props.tabHeader.removeClass(sourceViewTabHeaderClass);
        } else if (showSourceView) {
            this.props.tabHeader.addClass(sourceViewTabHeaderClass);
        }
        return (
            <div id={`bal-file-editor-${this.props.file.id}`}>
                <div style={ {display: showDesignView ? 'block' : 'none'} }>
                    <DesignView model={this.state.model} />
                </div>
                <div style={ {display: showSourceView ? 'block' : 'none'} }>
                    <SourceView file={ this.props.file} commandManager={this.props.commandManager} />
                </div>
                <div style={ {display: showSwaggerView ? 'block' : 'none'} }>
                    <SwaggerView />
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
