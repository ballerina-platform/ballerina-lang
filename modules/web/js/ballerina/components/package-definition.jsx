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
import Renderer from './renderer';
import {packageDefinition} from '../configs/designer-defaults';
import './package-definition.css';
import {getCanvasOverlay} from '../configs/app-context';
import ImportDeclaration from './import-declaration';
import ImportDeclarationExpanded from './import-declaration-expanded';
import BallerinaASTFactory from '../ast/ballerina-ast-factory';

class PackageDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.handlePackageNameInput = this.handlePackageNameInput.bind(this);
        this.handleImportsHeaderClick = this.handleImportsHeaderClick.bind(this);
        this.handleImportsMouseEnter = this.handleImportsMouseEnter.bind(this);
        this.handleImportsMouseLeave = this.handleImportsMouseLeave.bind(this);
        this.handleAddImport = this.handleAddImport.bind(this);
    }

    handlePackageNameInput(input) {
        this.props.model.setPackageName(input);
    }

    handleImportsHeaderClick() {
        this.props.model.setAttribute('viewState.expanded', !this.props.model.viewState.expanded);
    }

    handleImportsMouseEnter(e) {
        this.props.model.setAttribute('viewState.addingImport', true);
    }

    handleImportsMouseLeave(e) {
        console.log('ml');
        this.props.model.setAttribute('viewState.addingImport', false);
    }

    handleAddImport(value) {
        const newImportDeclaration = BallerinaASTFactory.createImportDeclaration();
        newImportDeclaration.setPackageName(value);
        newImportDeclaration.setParent(this.props.model.parent);
        this.props.model.parent.addImport(newImportDeclaration);
    }

    handlePackageNameClick(e) {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const headerPadding = packageDefinition.header.padding;
        const headerHeight = packageDefinition.header.height;
        const packageDefTextWidth = packageDefinition.textWidth;
        const packageDefLabelWidth = packageDefinition.labelWidth;
        const textBoxPadding = 3;
        const textBoxHeight = 25;

        const textBoxBBox = {
            x: bBox.x + headerPadding.left + packageDefLabelWidth - textBoxPadding - 2,
            y: bBox.y + headerHeight/2 - textBoxHeight/2,
            h: textBoxHeight,
            w: packageDefTextWidth
        };

        const options = {
            bBox: textBoxBBox,
            onChange: this.handlePackageNameInput,
            initialValue: model.getPackageName(),
            styles: {
                paddingLeft: textBoxPadding
            }
        }

        this.context.renderer.renderTextBox(options);
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const packageName = model._packageName || 'Undefined';
        const headerHeight = packageDefinition.header.height;
        const headerPadding = packageDefinition.header.padding;
        const expanded = this.props.model.viewState.expanded;
        const packageDefTextWidth = packageDefinition.textWidth;
        const packageDefLabelWidth = packageDefinition.labelWidth;

        const importsBbox = {
            x: bBox.x + headerPadding.left + packageDefTextWidth + packageDefLabelWidth + 15,
            y: bBox.y
        }

        const expandedImportsBbox = {
            x: bBox.x + headerPadding.left,
            y: bBox.y + headerHeight
        }

        const astRoot = this.props.model.parent;
        const imports = astRoot.children.filter(c => {return c.constructor.name === 'ImportDeclaration'});

        return (
            <g>
                <rect x={ bBox.x } y={ bBox.y } width={310} height={ headerHeight } rx="0" ry="0" className="package-definition-header"/>
                <text
                    x={ bBox.x + headerPadding.left }
                    y={ bBox.y + headerHeight/2 }
                    className="package-definition-label"
                >
                    {'package'}
                </text>
                <text
                    x={ bBox.x + headerPadding.left + packageDefLabelWidth}
                    y={ bBox.y + headerHeight/2 }
                    className={ "package-definition-text  pkg-name-" + packageName }
                    onClick={e => {this.handlePackageNameClick(e)}}
                >
                    {packageName}
                </text>
                { expanded ? <ImportDeclarationExpanded
                                bBox={expandedImportsBbox} imports={imports} onCollapse={this.handleImportsHeaderClick}
                                onMouseEnter={this.handleImportsMouseEnter} onMouseLeave={this.handleImportsMouseLeave}
                                showSuggestions={this.props.model.viewState.addingImport}
                                onAddImport={this.handleAddImport} /> :
                             <ImportDeclaration bBox={importsBbox} imports={imports} onClick={this.handleImportsHeaderClick} /> }
            </g>
        );
    }
}

PackageDefinition.contextTypes = {
    renderer: PropTypes.instanceOf(Renderer).isRequired,
};

export default PackageDefinition;
