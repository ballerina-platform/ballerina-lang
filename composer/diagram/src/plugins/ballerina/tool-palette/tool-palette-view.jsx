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
import _ from 'lodash';
import PropTypes, { instanceOf } from 'prop-types';
import { EVENTS as EDITOR_EVENTS } from 'core/editor/constants';
import DefaultTools from './item-provider/default-design-tools';
import ToolGroupView from './tool-group-view';
import './tool-palette.css';
import PackageScopedEnvironment from './../env/package-scoped-environment';
import { binaryOpTools, unaryOpTools, ternaryOpTools } from './item-provider/operator-tools';
import CompilationUnitNode from './../model/tree/compilation-unit-node';
import DefaultNodeFactory from '../model/default-node-factory';
import TreeUtil from './../model/tree-util';

const searchBoxHeight = 30;

class ToolsPanel extends React.Component {

    render() {
        return (
            <div className='tool-palette-panel'>
                <span className='tool-palette-panel-header-title'>{this.props.name}</span>
                <div className='tool-palette-panel-body'>
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
        const renderMode = this.props.mode;
        return (
            <div>
                {this.props.constructs &&
                    <ToolGroupView
                        group={this.props.constructs}
                        key='constructs'
                        showGridStyles
                    />}
                {this.props.currentTools && !_.isEmpty(this.props.currentTools.tools) &&
                <ToolGroupView
                    group={this.props.currentTools}
                    key='Current Package'
                    showGridStyles={false}
                />}
                <ToolsPanel name='Connectors'>
                    {this.props.connectors}
                    <a
                        tabIndex='-1'
                        className='tool-palette-add-button'
                        role='button'
                        onClick={() => this.changePane('connectors')}
                    >
                        <i className='fw fw-view' />
                        More connectors
                    </a>
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

    searchTools(tools) {
        return tools.filter(tool => tool.name.toLowerCase().includes(this.props.searchText));
    }

    render() {
        const isSearching = this.props.searchText.length > 0;

        const unaryOpToolGroup = {
            name: 'Unary Operators',
            id: 'unary-operators-tool-group',
            order: 'horizontal',
            tools: this.searchTools(unaryOpTools),
            collapsed: isSearching,
        };

        const binaryOpToolGroup = {
            name: 'Binary Operators',
            id: 'binary-operators-tool-group',
            order: 'horizontal',
            tools: this.searchTools(binaryOpTools),
            collapsed: isSearching,
        };

        const ternaryOpToolGroup = {
            name: 'Ternary Operators',
            id: 'ternary-operators-tool-group',
            order: 'horizontal',
            tools: this.searchTools(ternaryOpTools),
            collapsed: isSearching,
        };

        return (
            <div>
                {this.props.currentTools && !_.isEmpty(this.props.currentTools.tools) &&
                <ToolGroupView group={this.props.currentTools} key='Current Package' showGridStyles={false} />}
                <ToolsPanel name='Operators'>
                    <ToolGroupView group={unaryOpToolGroup} showGridStyles={false} />
                    <ToolGroupView group={binaryOpToolGroup} showGridStyles={false} />
                    <ToolGroupView group={ternaryOpToolGroup} showGridStyles={false} />
                </ToolsPanel>
                <ToolsPanel name='Libraries'>
                    {this.props.library}
                    <a
                        role='button'
                        tabIndex='-1'
                        className='tool-palette-add-button'
                        onClick={() => this.changePane('library')}
                    >
                        <i className='fw fw-view' />
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
            <div className='connector-panel'>
                <div className='tool-pane-header'>
                    Connectors
                    <a className='back' onClick={() => this.changePane('tools')}>
                        <i className='fw fw-left-arrow' />
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
            <div className='library-panel'>
                <div className='tool-pane-header'>
                    Libraries
                    <a className='back' onClick={() => this.changePane('tools')}>
                        <i className='fw fw-left-arrow' />
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
        this.clearText = this.clearText.bind(this);
        this.state = {
            text: '',
        };
    }

    handleChange(e) {
        this.setState({ text: e.target.value });
        this.props.onTextChange(e.target.value);
    }


    clearText(e) {
        this.setState({
            text: '',
        });
        this.props.onTextChange('');
    }

    render() {
        return (
            <div className='non-user-selectable wrapper'>
                <div className='search-bar'>
                    <i className='fw fw-search searchIcon' />
                    {this.state.text && <i className='fw fw-cancel clearIcon' onClick={this.clearText} />}
                    <input
                        className='search-input'
                        id='search-field'
                        placeholder='Search'
                        type='text'
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
        this.addDyamicTools = this.addDyamicTools.bind(this);

        this.state = {
            tab: 'tools',
            search: '',
        };
        this.reRender = this.reRender.bind(this);
    }

    reRender() {
        this.forceUpdate();
    }

    componentDidMount() {
        const { command: { on } } = this.context;
        on(EDITOR_EVENTS.ACTIVE_TAB_CHANGE, this.reRender);
    }

    componentWillUnmount() {
        const { command: { off } } = this.context;
        off(EDITOR_EVENTS.ACTIVE_TAB_CHANGE, this.reRender);
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
                if (item.name.toLowerCase().includes(this.searchText)) {
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

    addDyamicTools(environment, defaultTools) {
        if (environment.getPackages()) {
            let newToolItem,
                indexToBeAdded;
            for (const packageDefintion of environment.getPackages()) {
                // JMS package
                if (packageDefintion.getName() === 'ballerina.net.jms') {
                    newToolItem = {
                        id: 'jms-service',
                        name: 'JMS Service',
                        icon: 'jms',
                        title: 'JMS Service',
                        nodeFactoryMethod: DefaultNodeFactory.createJMSServiceDef,
                        description: 'JMS server connector can be used to listen to a topic/queue in a ' +
                        'JNDI-based JMS provider',
                    };
                    indexToBeAdded = defaultTools.tools.findIndex(tool => tool.id === 'constructs_seperator');
                    defaultTools.tools.splice(indexToBeAdded, 0, newToolItem);
                }
                // FS package
                else if (packageDefintion.getName() === 'ballerina.net.fs') {
                    newToolItem = {
                        id: 'fs-service',
                        name: 'FS Service',
                        icon: 'file',
                        title: 'FS Service',
                        nodeFactoryMethod: DefaultNodeFactory.createFSServiceDef,
                        description: ' FS server connector can be used to listen to a ' +
                        'directory in the local file system',
                    };
                    indexToBeAdded = defaultTools.tools.findIndex(tool => tool.id === 'constructs_seperator');
                    defaultTools.tools.splice(indexToBeAdded, 0, newToolItem);
                }
                // FTP package
                else if (packageDefintion.getName() === 'ballerina.net.ftp') {
                    newToolItem = {
                        id: 'ftp-service',
                        name: 'FTP Service',
                        icon: 'file',
                        title: 'FTP Service',
                        nodeFactoryMethod: DefaultNodeFactory.createFTPServiceDef,
                        description: 'FTP server connector can be used to listen to a remote directory',
                    };
                    indexToBeAdded = defaultTools.tools.findIndex(tool => tool.id === 'constructs_seperator');
                    defaultTools.tools.splice(indexToBeAdded, 0, newToolItem);
                }
            }
        }
        return defaultTools;
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
    package2ToolGroup(pckg, mode = 'both', transform = false, currentPackage = false) {
        const withConnectors = (mode === 'both' || mode === 'connectors');
        const withFunctions = (mode === 'both' || mode === 'functions');
        const definitions = [];

        if (withConnectors && !transform) {
            // Sort the connector package by name
            const connectorsOrdered = _.sortBy(pckg.getConnectors(), [function (connectorPackage) {
                return connectorPackage.getName();
            }]);
            _.each(connectorsOrdered, (connector) => {
                const packageName = _.last(_.split(pckg.getName(), '.')); // alias
                const connectorName = connector.getName();
                const fullPackageName = pckg.getName(); // Needed for the imports

                const args = {
                    connector,
                    packageName,
                    fullPackageName,
                };

                const endpointTool = {};
                endpointTool.nodeFactoryMethod = DefaultNodeFactory.createEndpoint;
                endpointTool.factoryArgs = args;
                endpointTool.title = connectorName + ' Endpoint';
                endpointTool.name = connectorName;
                endpointTool.id = 'Endpoint' + connectorName;
                endpointTool.icon = 'endpoint';
                definitions.push(endpointTool);

                // // Connector Actions ////
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
                    actionTool.factoryArgs = {
                        action,
                        actionConnectorName: connector.getName(),
                        packageName,
                        fullPackageName,
                    };

                    actionTool.title = action.getName();
                    actionTool.name = action.getName();
                    actionTool.icon = 'dgm-action';
                    actionTool.nodeFactoryMethod = DefaultNodeFactory
                        .createConnectorActionInvocationAssignmentStatement;
                    actionTool.id = `${connector.getName()}-${action.getName()}`;
                    actionTool.parameters = action.getParameters();
                    actionTool.returnParams = action.getReturnParams();
                    definitions.push(actionTool);
                });
            });
        }

        if (withFunctions) {
            let functionsOrdered = _.sortBy(pckg.getFunctionDefinitions(), [function (functionDef) {
                return functionDef.getName();
            }]);
            // temporary fix for duplicate functions.
            functionsOrdered = _.uniqBy(functionsOrdered, (item) => { return item.getName(); });

            _.each(functionsOrdered, (functionDef) => {
                if (TreeUtil.isMainFunction(functionDef)) {
                    // do not add main function to tool palette
                    return;
                }
                // if the function is not public do not add it to the tool palette
                if (!currentPackage && !functionDef.isPublic()) {
                    // do not private functions to tool palette
                    return;
                }

                if (transform) {
                    const packageName = _.last(_.split(pckg.getName(), '.'));
                    if (functionDef.getReturnParams().length > 0) {
                        const functionTool = {};
                        functionTool.nodeFactoryMethod =
                                               DefaultNodeFactory.createFunctionInvocationStatement;
                        functionTool.factoryArgs = {
                            functionDef,
                            packageName,
                            fullPackageName: pckg.getName(),
                        };
                        functionTool.title = functionDef.getName();
                        functionTool.name = functionDef.getName();
                        functionTool.id = functionDef.getName();
                        functionTool.icon = 'function';
                        functionTool.parameters = functionDef.getParameters();
                        functionTool.returnParams = functionDef.getReturnParams();
                        definitions.push(functionTool);
                    }
                } else {
                    const packageName = _.last(_.split(pckg.getName(), '.'));
                    const functionTool = {};
                    functionTool.nodeFactoryMethod = DefaultNodeFactory.createFunctionInvocationStatement;
                    functionTool.factoryArgs = {
                        functionDef,
                        packageName,
                        fullPackageName: pckg.getName(),
                    };
                    functionTool.title = functionDef.getName();
                    functionTool.name = functionDef.getName();
                    functionTool.id = functionDef.getName();
                    functionTool.icon = 'function';
                    functionTool.parameters = functionDef.getParameters();
                    functionTool.returnParams = functionDef.getReturnParams();
                    definitions.push(functionTool);
                }
            });
        }

        const group = {
            name: pckg.getName(),
            id: `${pckg.getName()}-tool-group`,
            order: 'vertical',
            tools: definitions,
        };

        return group;
    }

    render() {
        // check active editor before rendering tool-pallete
        const activeEditor = this.context.editor.getActiveEditor();
        if (!activeEditor || !activeEditor.file || activeEditor.file.extension !== 'bal') {
            return (<div className='tool-palette-error'>Not applicable for current tab.</div>);
        }
        const model = activeEditor.getProperty('ast');
        const environment = activeEditor.getProperty('balEnvironment');
        if (!environment || !(environment instanceof PackageScopedEnvironment)) {
            return (<div className='tool-palette-error'>Ooops! Something is wrong. Unable to load Tool Pallete.</div>);
        }
        // assigned the state to local variable.
        let state = this.state.tab;

        const searching = this.state.search.length > 0;
        const topLevelNodes = model ? model.getTopLevelNodes() : [];
        // get the current package
        const currentPackage = environment.getCurrentPackage();
        const isCurrentPackage = true;
        let currentTools = this.package2ToolGroup(currentPackage, 'both',
            this.props.isTransformActive, isCurrentPackage);
        currentTools = this.searchTools(this.state.search, _.cloneDeep(currentTools));
        if (currentTools !== undefined) {
            currentTools.collapsed = searching;
        }
        // get the constructs
        let constructs = _.cloneDeep(DefaultTools);
        if (environment.getPackages().length > 0) {
            constructs = this.addDyamicTools(environment, constructs);
        }
        // get imported packages
        const imports = topLevelNodes.filter(topLevelNode => topLevelNode.kind === 'Import');
        imports.push({ packageName: [{ value: 'ballerina' }, { value: 'builtin' }] });
        // convert imports to tool groups
        const connectors = [];
        const library = [];

        if (state === 'tools') {
            imports.forEach((item, i) => {
                // construct package name from splitted package name array.
                const pkgName = item.packageName.map(elem => elem.value).join('.');
                const pkg = environment.getPackageByName(pkgName);
                if (!_.isNil(pkg)) {
                    let group = this.package2ToolGroup(pkg, 'connectors');
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        connectors.push(<ToolGroupView
                            group={group}
                            key={`connector${i}`}
                            showGridStyles={false}
                        />);
                    }

                    group = this.package2ToolGroup(pkg, 'functions', this.props.isTransformActive);
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        library.push(
                            <ToolGroupView
                                group={group}
                                key={`library${pkgName}`}
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
            const filterOutList = [];
            // const filterOutList = imports.map(item => item.getPackageName());
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

        const scrollHeight = this.props.height - searchBoxHeight;

        constructs.collapsed = true;
        constructs = this.searchTools(this.state.search, constructs);

        return (
            <div className='tool-palette-container'>
                <ToolSearch onTextChange={this.onSearchTextChange} />
                <Scrollbars
                    style={{
                        width: this.props.width,
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
                        searchText={this.state.search}
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
    isTransformActive: PropTypes.bool.isRequired,
    height: PropTypes.number.isRequired,
    width: PropTypes.number.isRequired,
};

ToolPaletteView.contextTypes = {
    editor: PropTypes.shape({
        getActiveEditor: PropTypes.func,
    }).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        off: PropTypes.func,
    }).isRequired,
};

export default ToolPaletteView;
