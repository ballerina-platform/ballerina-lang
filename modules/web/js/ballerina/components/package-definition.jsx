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
import { packageDefinition } from '../configs/designer-defaults';
import './package-definition.css';
import ImportDeclaration from './import-declaration';
import ImportDeclarationExpanded from './import-declaration-expanded';
import GlobalDefinitions from './global-definitions';
import GlobalExpanded from './globals-expanded';
import BallerinaASTFactory from '../ast/ballerina-ast-factory';
import ImageUtil from './image-util';
import EditableText from './editable-text';
import PackageDefinitionModel from '../ast/package-definition';
import { parseContent } from './../../api-client/api-client';

class PackageDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            packageDefExpanded: false,
            packageDefValue: this.props.model.getPackageName(),
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

    onPackageClick() {
        this.setState({ packageNameEditing: true });
    }

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

    onPackageInputChange(e) {
        this.setState({ packageDefValue: e.target.value });
    }

    handleImportsBadgeClick() {
        this.props.model.viewState.importsExpanded = !this.props.model.viewState.importsExpanded;
        this.context.editor.update();
    }

    handleGlobalsBadgeClick() {
        this.props.model.viewState.globalsExpanded = !this.props.model.viewState.globalsExpanded;
        this.context.editor.update();
    }


    onPackageInputChange(e) {
        this.setState({ packageDefValue: e.target.value });
    }

    handleAddGlobal(value) {
        if(!value){
            return;
        }

        value += ';\n';
        parseContent(value)
            .then((jsonTree) => {
                // 0 th object of jsonTree is a packageDeclaration. Next should be global var or const.
                if(!jsonTree.root[1]){
                    return;
                }

                this.props.model.parent.addGlobal(jsonTree.root[1]);

            })
            .catch(log.error);
    }

    handleAddImport(value) {
        const newImportDeclaration = BallerinaASTFactory.createImportDeclaration();
        newImportDeclaration.setPackageName(value);
        newImportDeclaration.setParent(this.props.model.parent, {doSilently: true});
        this.props.model.parent.addImport(newImportDeclaration);
    }

    handleDeleteGlobal(deletedGlobal) {
        this.props.model.parent.removeChild(deletedGlobal);
    }

    handleDeleteImport(value) {
        this.props.model.parent.deleteImport(value);
    }

    handlePackageIconClick() {
        this.setState({ packageDefExpanded: true });
        if (!this.state.packageDefValue) {
            this.setState({ packageNameEditing: true });
        }
    }

    onPackageInputKeyDown(e) {
        if (e.keyCode === 13) {
            this.onPackageInputBlur(e);
        }
    }

    static getDisplayValue(globalDef) {
        if(BallerinaASTFactory.isGlobalVariableDefinition(globalDef)){
            return globalDef.getGlobalVariableDefinitionAsString();
        }

        if(BallerinaASTFactory.isConstantDefinition(globalDef)){
            return globalDef.getConstantDefinitionAsString();
        }
    }

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
            x: importsBbox.x + importDecViewState.badgeWidth + xGutterSize,
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
        const imports = astRoot.children.filter(c => c.constructor.name === 'ImportDeclaration');
        const globals = astRoot.children.filter(
            c => c.constructor.name === 'ConstantDefinition' || c.constructor.name === 'GlobalVariableDefinition');

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
                    />
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
                            onAddNewValue={this.handleAddGlobal.bind(this)}
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
    model: PropTypes.instanceOf(PackageDefinitionModel).isRequired,
};

PackageDefinition.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired
};

export default PackageDefinition;
