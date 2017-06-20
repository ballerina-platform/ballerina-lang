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
                    <ToolGroupView group={this.props.constructs} key="constructs" showGridStyles />}
                {this.props.currentTools && !_.isEmpty(this.props.currentTools.tools) &&
                <ToolGroupView group={this.props.currentTools} key="Current Package" showGridStyles={false} />}
                <ToolsPanel name="Connectors">
                    {this.props.connectors}
                    <a
                        tabIndex="-1"
                        className="tool-palette-add-button"
                        role="button"
                        onClick={() => this.changePane('connectors')}
                    >
                        <i className="fw fw-add fw-helper fw-helper-circle-outline icon" />
                        More
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
                        <i className="fw fw-add fw-helper fw-helper-circle-outline icon" />
                        More
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
                        <i className="fw fw-cancel fw-helper fw-helper-circle-outline icon" />
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
                        <i className="fw fw-cancel fw-helper fw-helper-circle-outline icon" />
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

    constructor(props) {
        super(props);
        this.provider = props.provider;
        // bind the event handlers to this
        this.onSearchTextChange = this.onSearchTextChange.bind(this);
        this.changePane = this.changePane.bind(this);

        this.state = {
            tab: 'tools',
            search: '',
        };

        this.editor = props.editor;
        this.setModel(this.editor.getModel());
        this.editor.on('update-diagram', () => {
            // only update model if it's new
            if (this.getModel().id !== this.editor.getModel().id) {
                this.setModel(this.editor.getModel());
            }
            this.forceUpdate();
        });
        this.editor.on('update-tool-patette', () => {
            this.forceUpdate();
        });
    }

    getChildContext() {
        return { dragDropManager: this.props.dragDropManager };
    }

    onSearchTextChange(value) {
        this.setState({ search: value });
    }

    setModel(model) {
        this.model = model;
        this.model.on('tree-modified', () => {
            this.forceUpdate();
        });
    }

    getModel() {
        return this.model;
    }

    changePane(type) {
        this.setState({ tab: type, search: '' });
    }

    searchTools(text, data) {
        if (text === undefined || text === '') {
            return data;
        } else if (data.tools instanceof Array) {
            data.tools = data.tools.filter(function (item) {
                if (item.get('title').toLowerCase().includes(this.searchText)) {
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

    render() {
        const searching = this.state.search.length > 0;
        // get the model
        const model = this.props.editor.getModel();
        // get the environment
        const environment = this.props.editor.getEnvironment();
        // get the current package
        const currentPackage = this.props.editor.generateCurrentPackage();
        let currentTools = this.provider.getCombinedToolGroup(currentPackage);
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

        if (this.state.tab === 'tools') {
            imports.forEach((item) => {
                const pkg = environment.getPackageByName(item.getPackageName());
                if (!_.isNil(pkg)) {
                    let group = this.provider.getConnectorToolGroup(pkg);
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        connectors.push(<ToolGroupView
                            group={group}
                            key={`connector${item.getPackageName()}`}
                            showGridStyles={false}
                        />);
                    }

                    group = this.provider.getLibraryToolGroup(pkg);
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        library.push(
                            <ToolGroupView group={group} key={`library${item.getPackageName()}`} showGridStyles={false} />);
                    }
                }
            });
        } else {
            const filterOutList = imports.map(item => item.getPackageName());
            filterOutList.push('Current Package');

            const packages = environment.getFilteredPackages(filterOutList);
            packages.forEach((pkg) => {
                let group;
                if (this.state.tab === 'connectors') {
                    group = this.provider.getConnectorToolGroup(pkg);
                    group = this.searchTools(this.state.search, _.cloneDeep(group));
                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        connectors.push(
                            <ToolGroupView group={group} key={`connector${pkg.getName()}`} showGridStyles={false} />);
                    }
                } else {
                    group = this.provider.getLibraryToolGroup(pkg);
                    group = this.searchTools(this.state.search, _.cloneDeep(group));

                    if (group !== undefined && !_.isEmpty(group.tools)) {
                        group.collapsed = searching;
                        library.push(
                            <ToolGroupView group={group} key={`library${pkg.getName()}`} showGridStyles={false} />);
                    }
                }
            });
        }

        // calculate the height of the tool.
        // this is a hack need to find a better approch.
        // let scrollHeight = window.innerHeight - 176;
        const scrollHeight = $(this.props.container).height() - 50;

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
                    {this.state.tab === 'tools' && <ToolsPane
                        constructs={constructs}
                        currentTools={currentTools}
                        connectors={connectors}
                        library={library}
                        changePane={this.changePane}
                    />}
                    {this.state.tab === 'connectors' &&
                    <ConnectorPane connectors={connectors} changePane={this.changePane} />}
                    {this.state.tab === 'library' &&
                    <LibraryPane library={library} changePane={this.changePane} />}
                </Scrollbars>
            </div>
        );
    }
}

ToolPaletteView.childContextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
};

export default ToolPaletteView;
