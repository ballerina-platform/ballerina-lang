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
import React from 'react';
import PropTypes from 'prop-types';
import { packageDefinition } from '../../../../configs/designer-defaults';
import './package-definition.css';
import ImportDeclaration from './import-declaration';
import ImportDeclarationExpanded from './import-declaration-expanded';
import GlobalDefinitions from './global-definitions';
import GlobalExpanded from './globals-expanded';
import ImageUtil from './image-util';
import EditableText from './editable-text';
import PackageDeclarationModel from '../../../../model/tree/abstract-tree/package-declaration-node';
import { parseContent } from './../../../../../api-client/api-client';

/**
 * Class representing the package definition and other top level views.
 * This class is responsible for views regarding imports and global variables and constants
 * @extends React.Component
 */
class PackageDefinition extends React.Component {

    /**
     * Called by the global expanded view to get the value displayed for each global constant or variable node
     * @param {Object} globalDef - the global constant or variable node
     * @return {string} the text displayed in the expanded view for the given node
     */
    static getDisplayValue(globalDef) {
        if (globalDef.parent.getGlobalVariableDefinitions().includes(globalDef)) {
            return globalDef.getTypeNode().typeKind.toLowerCase() + ' ' + globalDef.getSource();
        }

        if (globalDef.parent.getConstantDefinitions().includes(globalDef)) {
            return 'const ' + globalDef.getTypeNode().typeKind.toLowerCase() + ' ' + globalDef.getSource();
        }

        return 'invalid value';
    }

    /**
     * Creates PackageDefinition component instance.
     * @param {Object} props - React props.
     */
    constructor(props) {
        super(props);
        this.state = {
            packageDefExpanded: false,
            packageDefValue: this.props.model.package,
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
        if (!this.state.packageDefValue || this.state.packageDefValue.trim().length === 0) {
            this.setState({
                packageDefExpanded: false,
                packageNameEditing: false,
            });
        }
        this.props.model.setPackageName(this.state.packageDefValue);
        this.setState({ packageNameEditing: false });
    }

    /**
     * Called when a value is typed or pasted for package name
     * @param {Object} event - the input change event
     */
    onPackageInputChange(event) {
        this.setState({ packageDefValue: event.target.value });
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
     * Called when a new global constant or variable is entered
     * @param {string} value - statement for adding the new global
     */
    handleAddGlobal(value) {
        if (!value) {
            return;
        }

        value += ';\n';
        parseContent(value)
            .then((jsonTree) => {
                // 0 th object of jsonTree is a packageDeclaration. Next should be global var or const.
                if (!jsonTree.root[1]) {
                    return;
                }

                this.props.model.parent.addGlobal(jsonTree.root[1]);
            })
            .catch(log.error);
    }

    /**
     * Called when a new import declaration is added
     * @param {string} value - name of the package newly imported
     */
    handleAddImport(value) {
        if (!value) {
            return;
        }
        value = 'import ' + value + ';\n';
        parseContent(value)
            .then((jsonTree) => {
                if (jsonTree.root[1]) {
                    this.props.model.parent.addImportFromJson(jsonTree.root[1]);
                }
            })
            .catch(log.error);
    }

    /**
     * Called when a global constant or variable is deleted
     * @param {object} deletedGlobal - global constant or variable node deleted
     */
    handleDeleteGlobal(deletedGlobal) {
        this.props.model.parent.removeChild(deletedGlobal);
    }

    /**
     * Called when a new import declaration is deleted
     * @param {string} value - name of the package deleted
     */
    handleDeleteImport(value) {
        this.props.model.parent.deleteImport(value);
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
        this.setState({ packageDefExpanded: true });
        if (!this.state.packageDefValue) {
            this.setState({ packageNameEditing: true });
        }
    }

    /**
     * Returns the React element for the views of the top level nodes
     * @override
     */
    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const headerHeight = packageDefinition.header.height;
        const importsExpanded = this.props.model.viewState.importsExpanded;
        const globalsExpanded = this.props.model.viewState.globalsExpanded;
        const packageDefTextWidth = 275;
        const yGutterSize = 10;
        const xGutterSize = 15;
        const iconSize = 20;
        const importDecViewState = this.props.model.viewState.components.importDeclaration;
        const importsExpandedViewState = this.props.model.viewState.components.importsExpanded;

        const importsBbox = {
            x: bBox.x + headerHeight + xGutterSize,
            y: bBox.y,
        };

        const globalsBbox = {
            x: importsBbox.x + xGutterSize + 115,
            y: bBox.y,
        };

        const expandedImportsBbox = {
            x: bBox.x,
            y: bBox.y + headerHeight,
        };

        const expandedGlobalsBbox = {
            x: bBox.x,
            y: bBox.y + headerHeight + yGutterSize,
        };

        const astRoot = this.props.model.parent;
        const imports = astRoot.getImports();
        const globals = astRoot.getGlobalVariableDefinitions().concat(astRoot.getConstantDefinitions());

        const packageSuggestions = this.context.environment.getPackages()
            .filter(p => !imports.map(i => (i.getPackageName())).includes(p.getName()))
            .map(p => ({ name: p.getName() })).sort();

        const packageDefExpanded = this.state.packageDefExpanded || !!this.state.packageDefValue;

        if (packageDefExpanded) {
            importsBbox.x += packageDefTextWidth;
            globalsBbox.x += packageDefTextWidth;
        }

        if (importsExpanded) {
            const {
                topBarHeight,
                importDeclarationHeight,
                importInputHeight,
            } = importsExpandedViewState;

            expandedGlobalsBbox.y += (imports.length * importDeclarationHeight) + topBarHeight +
                importInputHeight + yGutterSize;
            globalsBbox.x -= (importDecViewState.badgeWidth + xGutterSize);
        }

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
                    {
                        packageDefExpanded && (
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
                                {this.state.packageDefValue || ''}
                            </EditableText>
                        </g>
                        )
                    }(
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
                            model={this.props.model.parent}
                            onCollapse={this.handleGlobalsBadgeClick}
                            title={'Globals'}
                            addText={'+ Add Global'}
                            onAddNewValue={this.handleAddGlobal}
                            newValuePlaceholder={''}
                            onDeleteClick={this.handleDeleteGlobal}
                            getValue={PackageDefinition.getDisplayValue}
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

PackageDefinition.propTypes = {
    model: PropTypes.instanceOf(PackageDeclarationModel).isRequired,
};

PackageDefinition.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default PackageDefinition;
