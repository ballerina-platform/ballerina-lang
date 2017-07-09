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
import React from 'react';
import { Scrollbars } from 'react-custom-scrollbars';
import $ from 'jquery';
import _ from 'lodash';
import PropTypes from 'prop-types';
import DragDropManager from '../tool-palette/drag-drop-manager';
import InitialTools from '../item-provider/initial-definitions';
import ToolGroupView from './tool-group-view';
import './tool-palette.css';
import ToolGroup from './../tool-palette/tool-group';
import DefaultBallerinaASTFactory from '../ast/default-ballerina-ast-factory';
import BallerinaASTRoot from '../ast/ballerina-ast-root';
import PackageScopedEnvironment from './../env/package-scoped-environment';


class ToolsPanel extends React.Component {

    render() {
        return (
            <div className="tool-palette-panel">
                <span className="tool-palette-panel-header-title">{this.props.name}</span>
                <div className="tool-palette-panel-body">
                    {this.props.children}
                </div>
            </div>
        );
    }
}

class ToolsPane extends React.Component {

    constructor(props) {
        super(props);
        this.changePane = this.changePane.bind(this);
    }

    changePane(type) {
        this.props.changePane(type);
    }

    render() {
        return (
            <div>
                {this.props.constructs &&
                    <ToolGroupView group={this.props.constructs}
                                   key="constructs"
                                   showGridStyles
                    />}
                {this.props.currentTools && !_.isEmpty(this.props.currentTools.tools) &&
                <ToolGroupView
                    group={this.props.currentTools}
                    key="Current Package"
                    showGridStyles={false}
                />}
                <ToolsPanel name="Connectors">
                    {this.props.connectors}
                    <a
                        tabIndex="-1"
                        className="tool-palette-add-button"
                        role="button"
                        onClick={() => this.changePane('connectors')}
                    >
                        <i className="fw fw-view icon" />
                         More connectors
              </a>
                </ToolsPanel>
                <ToolsPanel name="Libraries">
                    {this.props.library}
                    <a
                        role="button"
                        tabIndex="-1"
                        className="tool-palette-add-button"
                        onClick={() => this.changePane('library')}
                    >
                        <i className="fw fw-view icon" />
                         More libraries
                    </a>
                    <br />
                </ToolsPanel>
            </div>
        );
    }
}

class TransformPane extends React.Component {

    constructor(props) {
        super(props);
        this.changePane = this.changePane.bind(this);
    }

    changePane(type) {
        this.props.changePane(type);
    }

    render() {
        return (
            <div>
                {this.props.currentTools && !_.isEmpty(this.props.currentTools.tools) &&
                <ToolGroupView group={this.props.currentTools} key="Current Package" showGridStyles={false} />}
                <ToolsPanel name="Libraries">
                    {this.props.library}
                    <a
                        role="button"
                        tabIndex="-1"
                        className="tool-palette-add-button"
                        onClick={() => this.changePane('library')}
                    >
                        <i className="fw fw-view icon" />
                        More libraries
                    </a>
                    <br />
                </ToolsPanel>
            </div>
        );
    }
}

class ConnectorPane extends React.Component {

    constructor(props) {
        super(props);
        this.changePane = this.changePane.bind(this);
    }

    changePane(type) {
        this.props.changePane(type);
    }

    render() {
        return (
            <div className="connector-panel">
                <div className="tool-pane-header">
                    Connectors
                    <a className="back" onClick={() => this.changePane('tools')}>
                        <i className="fw fw-left-arrow icon" />
                    </a>
                </div>
                {this.props.connectors}
            </div>
        );
    }
}

class LibraryPane extends React.Component {

    constructor(props) {
        super(props);
        this.changePane = this.changePane.bind(this);
    }

    changePane(type) {
        this.props.changePane(type);
    }

    render() {
        return (
            <div className="library-panel">
                <div className="tool-pane-header">
                    Libraries
                    <a className="back" onClick={() => this.changePane('tools')}>
                        <i className="fw fw-left-arrow icon" />
                    </a>
                </div>
                {this.props.library}
            </div>
        );
    }
}

class ToolSearch extends React.Component {

    constructor(props) {
        super(props);
        this.handleChange = this.handleChange.bind(this);
        this.state = {
            text: '',
        };
    }

    handleChange(e) {
        this.setState({ text: e.target.value });
        this.props.onTextChange(e.target.value);
    }

    render() {
        return (
            <div className="non-user-selectable">
                <div className="search-bar">
                    <i className="fw fw-search searchIcon" />
                    <input
                        className="search-input"
                        id="search-field"
                        placeholder="Search"
                        type="text"
                        onChange={this.handleChange}
                        value={this.state.text}
                    />
                </div>
            </div>
        );
    }
}

class ToolPaletteView extends React.Component {

    /**
     * Creates an instance of ToolPaletteView.
     * @param {any} props react props.
     * 
     * @memberof ToolPaletteView
     */
    constructor(props) {
        super(props);
        // bind the event handlers to this
        this.onSearchTextChange = this.onSearchTextChange.bind(this);
        this.changePane = this.changePane.bind(this);

        this.state = {
            tab: 'tools',
            search: '',
        };
    }

    onSearchTextChange(value) {
        this.setState({ search: value });
    }


    changePane(type) {
        this.setState({ tab: type, search: '' });
    }

    /**
     * Filter out functions and connectors in a tool group.
     *
     * @param {tring} text search text.
     * @param {ToolGroup} data tool group.
     * @returns {ToolGroup} filtered out tool group.
     *
     * @memberof ToolPaletteView
     */
    searchTools(text, data) {
        if (text === undefined || text === '') {
            return data;
        } else if (data.tools instanceof Array) {
            data.tools = data.tools.filter(function (item) {
                if (item.get('name').toLowerCase().includes(this.searchText)) {
                    return true;
                }
                return false;
            }, { searchText: text.toLowerCase() });
            if (data.tools.length === 0) {
                return undefined;
            }
        } else {
            return undefined;
        }
        return data;
    }

    /**
     * Convert package to tool group.
     *
     * @param {any} pckg package to be converted.
     * @param {string} [mode='both' | 'connectors' | 'functions' ] include connectors or functions.
     * @param {boolean} [transform=false]  
     * @returns {ToolGroup}
     *
     * @memberof ToolPaletteView
     */
    package2ToolGroup(pckg, mode = 'both', transform = false) {
        const withConnectors = (mode === 'both' || mode === 'connectors');
        const withFunctions = (mode === 'both' || mode === 'functions');
        const definitions = [];

        if (withConnectors && !transform) {
            // Sort the connector package by name
            const connectorsOrdered = _.sortBy(pckg.getConnectors(), [function (connectorPackage) {
                return connectorPackage.getName();
            }]);
            _.each(connectorsOrdered, (connector) => {
                const packageName = _.last(_.split(pckg.getName(), '.'));
                const args = {
                    pkgName: packageName,
                    connectorName: connector.getName(),
                    fullPackageName: pckg.getName(),
                };
                const connTool = {};
                connTool.nodeFactoryMethod = DefaultBallerinaASTFactory.createConnectorDeclaration;
                connTool.meta = args;
                // TODO : use a generic icon
                connTool.title = connector.getName();
                connTool.name = connector.getName();
                connTool.id = connector.getName();
                connTool.cssClass = 'icon fw fw-connector';
                definitions.push(connTool);

                const actionsOrdered = _.sortBy(connector.getActions(), [function (action) {
                    return action.getName();
                }]);
                _.each(actionsOrdered, (action, index, collection) => {
                    /* We need to add a special class to actions to indent them in tool palette. */
                    const actionTool = {};
                    actionTool.classNames = 'tool-connector-action';
                    if ((index + 1) === collection.length) {
                        action.classNames = 'tool-connector-action tool-connector-last-action';
                    }
                    actionTool.meta = {
                        action: action.getName(),
                        actionConnectorName: connector.getName(),
                        actionPackageName: packageName,
                        fullPackageName: pckg.getName(),
                        actionDefinition: action,
                    };

                    actionTool.title = action.getName();
                    actionTool.name = action.getName();
                    actionTool.cssClass = 'icon fw fw-dgm-action';
                    actionTool.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedActionInvocationAssignmentStatement;
                    if (action.getReturnParams().length > 0) {
                        actionTool.nodeFactoryMethod = DefaultBallerinaASTFactory
                                                    .createAggregatedActionInvocationAssignmentStatement;
                    }

                    actionTool.id = `${connector.getName()}-${action.getName()}`;
                    actionTool._parameters = action.getParameters();
                    actionTool._returnParams = action.getReturnParams();
                    definitions.push(actionTool);
                });
            });
        }

        if (withFunctions) {
            const functionsOrdered = _.sortBy(pckg.getFunctionDefinitions(), [function (functionDef) {
                return functionDef.getName();
            }]);

            _.each(functionsOrdered, (functionDef) => {
                if (functionDef.getName() === 'main') {
                    // do not add main function to tool palette
                    return;
                }

                if (transform) {
                    const packageName = _.last(_.split(pckg.getName(), '.'));
                    if (functionDef.getReturnParams().length > 0) {
                        const functionTool = {};
                        functionTool.nodeFactoryMethod =
                                               DefaultBallerinaASTFactory.createAssignmentFunctionInvocationStatement;
                        functionTool.meta = {
                            functionDef,
                            packageName,
                            fullPackageName: pckg.getName(),
                        };
                        functionTool.title = functionDef.getName();
                        functionTool.name = functionDef.getName();
                        functionTool.id = functionDef.getName();
                        functionTool.cssClass = 'icon fw fw-function';
                        functionTool._parameters = functionDef.getParameters();
                        functionTool._returnParams = functionDef.getReturnParams();
                        definitions.push(functionTool);
                    }

                } else {
                    const packageName = _.last(_.split(pckg.getName(), '.'));
                    const functionTool = {};
                    functionTool.nodeFactoryMethod = DefaultBallerinaASTFactory.createAggregatedFunctionInvocationStatement;
                    functionTool.meta = {
                        functionDef,
                        packageName,
                        fullPackageName: pckg.getName(),
                    };
                    functionTool.title = functionDef.getName();
                    functionTool.name = functionDef.getName();
                    functionTool.id = functionDef.getName();
                    functionTool.cssClass = 'icon fw fw-function';
                    functionTool._parameters = functionDef.getParameters();
                    functionTool._returnParams = functionDef.getReturnParams();
                    definitions.push(functionTool);
                }
            });
        }

        const group = new ToolGroup({
            toolGroupName: pckg.getName(),
            toolGroupID: `${pckg.getName()}-tool-group`,
            toolOrder: 'vertical',
            toolDefinitions: definitions,
        });

        return group;
    }

    render() {
        // assigned the state to local variable.
        let state = this.state.tab;

        const searching = this.state.search.length > 0;
        // get the model
        const model = this.context.astRoot;
        // get the environment
        const environment = this.context.environment;
        // get the current package
        const currentPackage = environment.createCurrentPackageFromAST(model);
        let currentTools = this.package2ToolGroup(currentPackage,'both',this.props.isTransformActive);
        currentTools = this.searchTools(this.state.search, _.cloneDeep(currentTools));
        if (currentTools !== undefined) {
            currentTools.collapsed = searching;
        }
        // get the constructs
        let constructs = _.cloneDeep(InitialTools[0]);
        // get imported packages
        const imports = model.getImportDeclarations();
        // convert imports to tool groups
        const connectors = [];
        const library = [];

        if (state === 'tools') {
            imports.forEach((item) => {
                const pkg = environment.getPackageByName(item.getPackageName());
                if (!_.isNil(pkg)) {
                    let group = this.package2ToolGroup(pkg, 'connectors');
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        connectors.push(<ToolGroupView
                            group={group}
                            key={`connector${item.getPackageName()}`}
                            showGridStyles={false}
                        />);
                    }

                    group = this.package2ToolGroup(pkg, 'functions',this.props.isTransformActive);
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        library.push(
                            <ToolGroupView
                                group={group}
                                key={`library${item.getPackageName()}`}
                                showGridStyles={false}
                            />);
                    }
                }
            });
            // if the tab state is tool we will see if the transform is opened.
            if (this.props.isTransformActive) {
                state = 'transform';
            }
        } else {
            const filterOutList = imports.map(item => item.getPackageName());
            filterOutList.push('Current Package');

            const packages = environment.getFilteredPackages(filterOutList);
            packages.forEach((pkg) => {
                let group;
                if (state === 'connectors') {
                    group = this.package2ToolGroup(pkg, 'connectors');
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        connectors.push(
                            <ToolGroupView
                                group={group}
                                key={`connector${pkg.getName()}`}
                                showGridStyles={false}
                            />);
                    }
                } else {
                    group = this.package2ToolGroup(pkg, 'functions', this.props.isTransformActive);
                    group = this.searchTools(this.state.search, _.cloneDeep(group));

                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        library.push(
                            <ToolGroupView
                                group={group}
                                key={`library${pkg.getName()}`}
                                showGridStyles={false}
                            />);
                    }
                }
            });
        }

        // calculate the height of the tool.
        // this is a hack need to find a better approch.
        // let scrollHeight = window.innerHeight - 176;
        const scrollHeight = $(this.props.getContainer()).height() - 50;

        constructs.collapsed = true;
        constructs = this.searchTools(this.state.search, constructs);

        return (
            <div className="tool-palette-container">
                <ToolSearch onTextChange={this.onSearchTextChange} />
                <Scrollbars
                    style={{
                        width: 243,
                        height: scrollHeight,
                    }}
                    autoHide // Hide delay in ms
                    autoHideTimeout={1000}
                >
                    {state === 'tools' && <ToolsPane
                        constructs={constructs}
                        currentTools={currentTools}
                        connectors={connectors}
                        library={library}
                        changePane={this.changePane}
                    />}
                    {state === 'transform' && <TransformPane
                        constructs={constructs}
                        currentTools={currentTools}
                        connectors={connectors}
                        library={library}
                        changePane={this.changePane}
                    />}
                    {state === 'connectors' &&
                    <ConnectorPane connectors={connectors} changePane={this.changePane} />}
                    {state === 'library' &&
                    <LibraryPane library={library} changePane={this.changePane} />}
                </Scrollbars>
            </div>
        );
    }
}

ToolPaletteView.propTypes = {
    isTransformActive: PropTypes.bool.isRequired
};

ToolPaletteView.contextTypes = {
    astRoot: PropTypes.instanceOf(BallerinaASTRoot).isRequired,
    environment: PropTypes.instanceOf(PackageScopedEnvironment).isRequired,
};

export default ToolPaletteView;
