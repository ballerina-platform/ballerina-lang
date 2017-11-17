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
import PropTypes from 'prop-types';
import _ from 'lodash';
import { packageDefinition } from '../../designer-defaults';
import './package-definition.css';
import ImportDeclaration from './import-node';
import ImportDeclarationExpanded from './import-declaration-expanded';
import GlobalDefinitions from './global-definitions';
import GlobalExpanded from './globals-expanded';
import ImageUtil from '../../../../image-util';
import EditableText from './editable-text';
import CompilationUnitNode from '../../../../../model/tree/abstract-tree/compilation-unit-node';
import TreeBuilder from './../../../../../model/tree-builder';
import FragmentUtils from './../../../../../../ballerina/utils/fragment-utils';

/**
 * Class representing the package definition and other top level views.
 * This class is responsible for views regarding imports and global variables and constants
 * @extends React.Component
 */
class TopLevelNodes extends React.Component {

    /**
     * Called by the global expanded view to get the value displayed for each global constant or variable node
     * @param {Object} globalDef - the global constant or variable node
     * @return {string} the text displayed in the expanded view for the given node
     */
    static getDisplayValue(globalDef) {
        if (globalDef.parent.filterTopLevelNodes({ kind: 'Variable' })
                .concat(globalDef.parent.filterTopLevelNodes({ kind: 'Xmlns' })).includes(globalDef)) {
            return globalDef.getSource();
        }
        return 'invalid value';
    }

    /**
     * Creates PackageDefinition component instance.
     * @param {Object} props - React props.
     */
    constructor(props) {
        super(props);
        this.packageDefValue = this.getPackageName(this.props.model);
        this.state = {
            packageNameEditing: false,
        };

        this.handleImportsBadgeClick = this.handleImportsBadgeClick.bind(this);
        this.handleGlobalsBadgeClick = this.handleGlobalsBadgeClick.bind(this);
        this.handleAddImport = this.handleAddImport.bind(this);
        this.handleDeleteImport = this.handleDeleteImport.bind(this);
        this.handleAddGlobal = this.handleAddGlobal.bind(this);
        this.handleDeleteGlobal = this.handleDeleteGlobal.bind(this);
        this.handlePackageIconClick = this.handlePackageIconClick.bind(this);
        this.onPackageInputKeyDown = this.onPackageInputKeyDown.bind(this);
        this.getPackageName = this.getPackageName.bind(this);
    }

    componentWillReceiveProps() {
        const model = this.props.model;
        this.packageDefValue = this.getPackageName(model);
    }

    /**
     * Called when the package name is clicked when the package definition view is expanded
     */
    onPackageClick() {
        this.setState({ packageNameEditing: true });
    }

    /**
     * Called focus is removed from the input field for package name
     */
    onPackageInputBlur() {
        if (!this.packageDefValue || this.packageDefValue.trim().length === 0) {
            this.setState({
                packageNameEditing: false,
            });
            this.props.model.viewState.packageDefExpanded = false;
        }
        if (this.packageDefValue) {
            // If there is a semi-colon at the end of the package name, remove it
            this.packageDefValue = _.trimEnd(this.packageDefValue, ';');
            const pkgName = `package ${this.packageDefValue};`;
            const fragment = FragmentUtils.createTopLevelNodeFragment(pkgName);
            const parsedJson = FragmentUtils.parseFragment(fragment);
            // If there's no packageDeclaration node, then create one
            if (this.props.model.filterTopLevelNodes({ kind: 'PackageDeclaration' }).length === 0) {
                this.props.model.addTopLevelNodes(TreeBuilder.build(parsedJson), 0);
            } else {
                // If a packageDeclaratioNode already exists then remove that node, and add a new one
                const pkgDeclarationNode = this.props.model.filterTopLevelNodes({ kind: 'PackageDeclaration' })[0];
                this.props.model.removeTopLevelNodes(pkgDeclarationNode, true);
                this.props.model.addTopLevelNodes(TreeBuilder.build(parsedJson), 0);
            }
        } else if (this.props.model.filterTopLevelNodes({ kind: 'PackageDeclaration' }).length > 0) {
            this.props.model.removeTopLevelNodes(this.props.model.filterTopLevelNodes(
                    { kind: 'PackageDeclaration' })[0]);
        }

        this.setState({ packageNameEditing: false });
        this.props.model.trigger('tree-modified', {
            origin: this.props.model,
            type: 'Package name changed',
            title: 'Package name changed',
            data: {
                node: this.props.model,
            },
        });
    }

    /**
     * Called when a value is typed or pasted for package name
     * @param {Object} event - the input change event
     */
    onPackageInputChange(event) {
        this.packageDefValue = event.target.value;
        this.setState({ packageNameEditing: true });
    }

    /**
     * Called for the key down event when a package name is typed
     * @param {Object} event - the keydown event object
     */
    onPackageInputKeyDown(event) {
        if (event.keyCode === 13) {
            this.onPackageInputBlur(event);
        }
    }

    /**
     * Get the full package name
     * @param model: CompilationUnit node
     * @returns if exists: the full pkg name else: undefined
     */
    getPackageName(model) {
        // model is the compilation level node
        if (model.filterTopLevelNodes({ kind: 'PackageDeclaration' }).length > 0) {
            const pkgDecNodes = model.filterTopLevelNodes({ kind: 'PackageDeclaration' });
            return _.isEmpty(pkgDecNodes) ? undefined : model.getPackageName(pkgDecNodes[0]);
        }
        return undefined;
    }

    /**
     * Called when a new global constant or variable is entered
     * @param {string} value - statement for adding the new global
     */
    handleAddGlobal(value) {
        if (!value) {
            return;
        }
        value = `${value}\n`;
        const fragment = FragmentUtils.createTopLevelNodeFragment(value);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const globalNode = TreeBuilder.build(parsedJson);
        globalNode.clearWS();
        this.props.model.addGlobal(globalNode);
    }

    /**
     * Called when a new import declaration is added
     * @param {string} value - name of the package newly imported
     */
    handleAddImport(value) {
        if (!value) {
            return;
        }
        value = `import ${value};\n`;
        const fragment = FragmentUtils.createTopLevelNodeFragment(value);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        const importNode = TreeBuilder.build(parsedJson);
        importNode.clearWS();
        this.props.model.addImport(importNode);
    }

    /**
     * Called when a global constant or variable is deleted
     * @param {object} deletedGlobal - global constant or variable node deleted
     */
    handleDeleteGlobal(deletedGlobal) {
        this.props.model.removeTopLevelNodes(deletedGlobal);
    }

    /**
     * Called when a new import declaration is deleted
     * @param {string} value - name of the package deleted
     */
    handleDeleteImport(importDecl) {
        this.props.model.removeTopLevelNodes(importDecl);
    }

    /**
     * Called when badge showing summary of global constants and variables is clicked
     */
    handleGlobalsBadgeClick() {
        this.props.model.viewState.globalsExpanded = !this.props.model.viewState.globalsExpanded;
        this.context.editor.update();
    }

    /**
     * Called when badge showing summary of imports is clicked
     */
    handleImportsBadgeClick() {
        this.props.model.viewState.importsExpanded = !this.props.model.viewState.importsExpanded;
        this.context.editor.update();
    }

    /**
     * Called when the package definition icon is clicked
     */
    handlePackageIconClick() {
        if (!this.packageDefValue) {
            this.setState({ packageNameEditing: true });
        }
        this.props.model.viewState.packageDefExpanded = true;
        this.props.model.trigger('tree-modified', {
            origin: this.props.model,
            type: 'Package name changed',
            title: 'Package name changed',
            data: {
                node: this.props.model,
            },
        });
    }

    /**
     * Returns the React element for the views of the top level nodes
     * @override
     */
    render() {
        const model = this.props.model;
        // TODO fix for the model not being parsed properly
        if (model.viewState.components.topLevelNodes === undefined) {
            return (null);
        }
        const bBox = model.viewState.components.topLevelNodes;
        const headerHeight = packageDefinition.header.height;

        // TODO fix for the model not being parsed properly
        if (this.props.model.viewState.components.importDeclaration === undefined ||
            this.props.model.viewState.components.importsExpanded === undefined) {
            return (null);
        }
        const importsExpanded = this.props.model.viewState.importsExpanded;
        const globalsExpanded = this.props.model.viewState.globalsExpanded;
        const packageDefTextWidth = 275;
        const yGutterSize = 10;
        const xGutterSize = 15;
        const iconSize = 20;
        const importDecViewState = this.props.model.viewState.components.importDeclaration;
        const importsExpandedViewState = this.props.model.viewState.components.importsExpanded;

        const importsBbox = this.props.model.viewState.components.importsBbox;
        const globalsBbox = this.props.model.viewState.components.globalsBbox;
        const expandedImportsBbox = this.props.model.viewState.components.importsExpandedBbox;
        const expandedGlobalsBbox = this.props.model.viewState.components.globalsExpandedBbox;
        const astRoot = this.props.model;
        const imports = astRoot.filterTopLevelNodes({ kind: 'Import' });
        const globals = astRoot.filterTopLevelNodes({ kind: 'Variable' })
            .concat(astRoot.filterTopLevelNodes({ kind: 'Xmlns' }));

        const packageSuggestions = this.context.environment.getPackages()
            .filter(p => !imports.map(i => (i.getPackageName())).includes(p.getName()))
            .map(p => ({ name: p.getName() })).sort();
        _.remove(packageSuggestions, p => p.name === 'Current Package');
        if (this.packageDefValue) {
            model.viewState.packageDefExpanded = true;
        }
        const packageDefExpanded = model.viewState.packageDefExpanded
            || (this.packageDefValue && (this.packageDefValue !== ''));
        return (
            <g>
                <g className="package-definition-head">
                    <rect
                        x={bBox.x}
                        y={bBox.y}
                        width={headerHeight}
                        height={headerHeight}
                        onClick={this.handlePackageIconClick}
                        rx={headerHeight / 2}
                        ry={headerHeight / 2}
                        className="package-definition-header"
                    />
                    { packageDefExpanded &&
                    <g>
                        <rect
                            x={bBox.x}
                            y={bBox.y}
                            width={packageDefTextWidth + headerHeight}
                            height={headerHeight}
                            onClick={() => {
                                this.onPackageClick();
                            }}
                            className="package-definition-header"
                        />
                        <EditableText
                            x={bBox.x + headerHeight}
                            y={bBox.y + (headerHeight / 2)}
                            width={packageDefTextWidth - 5}
                            placeholder="Package Name (eg: org.ballerinalang)"
                            onBlur={() => {
                                this.onPackageInputBlur();
                            }}
                            onKeyDown={this.onPackageInputKeyDown}
                            onClick={() => {
                                this.onPackageClick();
                            }}
                            editing={this.state.packageNameEditing}
                            onChange={(e) => {
                                this.onPackageInputChange(e);
                            }}
                        >
                            {this.packageDefValue || ''}
                        </EditableText>
                    </g> }
                    <image
                        width={iconSize}
                        height={iconSize}
                        xlinkHref={ImageUtil.getSVGIconString('package')}
                        onClick={this.handlePackageIconClick}
                        x={bBox.x + ((headerHeight - iconSize) / 2)}
                        y={bBox.y + ((headerHeight - iconSize) / 2)}
                    >

                        <title>View Package Name</title> </image>
                </g>
                {
                    importsExpanded ?
                        <ImportDeclarationExpanded
                            bBox={expandedImportsBbox}
                            imports={imports}
                            packageSuggestions={packageSuggestions}
                            onCollapse={this.handleImportsBadgeClick}
                            onAddImport={this.handleAddImport}
                            onDeleteImport={this.handleDeleteImport}
                        /> :
                        <ImportDeclaration
                            bBox={importsBbox}
                            noOfImports={imports.length}
                            onClick={this.handleImportsBadgeClick}
                            viewState={importDecViewState}
                        />
                }
                {
                    globalsExpanded ?
                        <GlobalExpanded
                            bBox={expandedGlobalsBbox}
                            globals={globals}
                            model={this.props.model}
                            onCollapse={this.handleGlobalsBadgeClick}
                            title={'Globals'}
                            addText={'+ Add Global (i.e. const int i = 0)'}
                            onAddNewValue={this.handleAddGlobal}
                            newValuePlaceholder={''}
                            onDeleteClick={this.handleDeleteGlobal}
                            getValue={TopLevelNodes.getDisplayValue}
                        /> :
                        <GlobalDefinitions
                            bBox={globalsBbox}
                            numberOfItems={globals.length}
                            title={'Globals'}
                            onExpand={this.handleGlobalsBadgeClick}
                        />
                }
            </g>
        );
    }
}

TopLevelNodes.propTypes = {
    model: PropTypes.instanceOf(CompilationUnitNode).isRequired,
};

TopLevelNodes.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default TopLevelNodes;
