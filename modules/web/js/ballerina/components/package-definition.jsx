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
import ImageUtil from './image-util';
import EditableText from './editable-text';

class PackageDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            packageDefExpanded: false,
            packageDefValue: this.props.model.getPackageName(),
            packageNameEditing: false
        }
        this.handleImportsHeaderClick = this.handleImportsHeaderClick.bind(this);
        this.handleAddImport = this.handleAddImport.bind(this);
        this.handleDeleteImport = this.handleDeleteImport.bind(this);
        this.handlePackageIconClick = this.handlePackageIconClick.bind(this);
    }

    handleImportsHeaderClick() {
        this.props.model.setAttribute('viewState.expanded', !this.props.model.viewState.expanded);
    }

    handlePackageIconClick() {
        this.setState({packageDefExpanded: true});
        if(!this.state.packageDefValue){
            this.setState({packageNameEditing: true});
        }
    }

    handleAddImport(value) {
        const newImportDeclaration = BallerinaASTFactory.createImportDeclaration();
        newImportDeclaration.setPackageName(value);
        newImportDeclaration.setParent(this.props.model.parent);
        this.props.model.parent.addImport(newImportDeclaration);
    }

    handleDeleteImport(value) {
        this.props.model.parent.deleteImport(value);
    }

    onPackageClick() {
        this.setState({packageNameEditing: true});
    }

    onPackageInputBlur() {
        if(!this.state.packageDefValue || this.state.packageDefValue.trim().length === 0){
            this.setState({
                packageDefExpanded: false,
                packageNameEditing: false
            })
            // TODO: unset package name
            return;
        }
        this.props.model.setPackageName(this.state.packageDefValue);
        this.setState({packageNameEditing: false});
    }

    onPackageInputChange(e) {
        this.setState({packageDefValue: e.target.value});
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const packageName = model.getPackageName();
        const headerHeight = packageDefinition.header.height;
        const headerPadding = packageDefinition.header.padding;
        const expanded = this.props.model.viewState.expanded;
        const packageDefTextWidth = 275;
        const iconSize = 20;

        const importsBbox = {
            x: bBox.x + headerHeight + 15,
            y: bBox.y
        }

        const expandedImportsBbox = {
            x: bBox.x,
            y: bBox.y + headerHeight
        }

        const packageDefExpanded = this.state.packageDefExpanded || !!this.state.packageDefValue
        if(packageDefExpanded) {
            importsBbox.x += packageDefTextWidth
        }

        const astRoot = this.props.model.parent;
        const imports = astRoot.children.filter(c => {return c.constructor.name === 'ImportDeclaration'});

        return (
            <g>
                <rect x={ bBox.x } y={ bBox.y } width={headerHeight} height={headerHeight} onClick={this.handlePackageIconClick}
                      rx={headerHeight/2} ry={headerHeight/2} className="package-definition-header"/>
                {
                    packageDefExpanded && (
                        <g>
                            <rect x={ bBox.x } y={ bBox.y } width={packageDefTextWidth + headerHeight} height={headerHeight} onClick={() => {this.onPackageClick()}}
                              className="package-definition-header"/>
                          <EditableText x={bBox.x + headerHeight} y={bBox.y + headerHeight / 2 } width={packageDefTextWidth - 5}
                                     onBlur={() => {this.onPackageInputBlur()}} onClick={() => {this.onPackageClick()}}
                                     editing={this.state.packageNameEditing} onChange={e => {this.onPackageInputChange(e)}}>
                                     {this.state.packageDefValue || ""}
                            </EditableText>
                        </g>
                    )
               }
               <image width={ iconSize } height={ iconSize } xlinkHref={ ImageUtil.getSVGIconString('package') }
                      onClick={this.handlePackageIconClick} x={bBox.x + (headerHeight-iconSize)/2 }
                      y={bBox.y + (headerHeight-iconSize)/2}/>
               { expanded ? <ImportDeclarationExpanded
                            bBox={expandedImportsBbox} imports={imports} onCollapse={this.handleImportsHeaderClick}
                            onAddImport={this.handleAddImport} onDeleteImport={this.handleDeleteImport}/> :
                         <ImportDeclaration bBox={importsBbox} imports={imports} onClick={this.handleImportsHeaderClick} /> }
            </g>
        );
    }
}

export default PackageDefinition;
