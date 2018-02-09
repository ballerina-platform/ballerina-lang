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
import log from 'log';
import _ from 'lodash';
import PropTypes from 'prop-types';
import { parseContent } from 'api-client/api-client';
import './import-declaration-item.css';
import TreeBuilder from './../../../../../model/tree-builder';
import ExpressionEditor from 'plugins/ballerina/expression-editor/expression-editor-utils';

export default class importDeclarationItem extends React.Component {
    constructor() {
        super();
        this.state = {
            highlighted: false,
        };
        this.handleMouseEnter = this.handleMouseEnter.bind(this);
        this.handleMouseLeave = this.handleMouseLeave.bind(this);
        this.handleDeleteClick = this.handleDeleteClick.bind(this);
        this.getImportedPackageName = this.getImportedPackageName.bind(this);
    }

    handleDeleteClick() {
        this.props.onDeleteClick(this.props.importDec);
    }

    handleMouseEnter() {
        this.setState({ highlighted: true });
    }

    handleMouseLeave() {
        this.setState({ highlighted: false });
    }

    getImportedPackageName() {
        const model = this.props.importDec;
        let importPkgName = model.parent.getPackageName(model);
        const pkgIdentifier = importPkgName.split('.');
        const pkgAliasName = pkgIdentifier[pkgIdentifier.length - 1];
        if (model.getAlias() !== undefined && model.getAlias().value !== pkgAliasName) {
            importPkgName = importPkgName + ' as ' + model.getAlias().value;
        }
        return importPkgName;
    }

    setEditedSource(value) {
        const oldNode = this;
        // If there is a semi-colon at the end of the import, remove it
        value = _.trimEnd(value, ';');
        value = `import ${value};\n`;
        parseContent(value)
            .then((jsonTree) => {
                if (jsonTree.model.topLevelNodes[0]) {
                    this.parent.replaceTopLevelNodes(oldNode,
                        TreeBuilder.build(jsonTree.model.topLevelNodes[0], this.parent, this.parent.kind));
                }
            })
            .catch(log.error);
    }
    /**
     * renders an ExpressionEditor in the selected import area which can be edited
     * @param {Object} bBox - bounding box ExpressionEditor should be rendered.
     */
    openEditor(bBox) {
        const editorOuterPadding = 10;
        const setterFunc = this.setEditedSource;
        const options = {
            propertyType: 'text',
            key: 'Import',
            model: this.props.importDec,
            getterMethod: this.getImportedPackageName,
            setterMethod: setterFunc,
        };

        const editorBBox = {
            x: (bBox.x + (editorOuterPadding / 2)),
            y: bBox.y,
            h: bBox.h,
            w: (bBox.w - editorOuterPadding),
        };

        const packageScope = this.context.enviornment;

        new ExpressionEditor(editorBBox, (s) => {}, options, packageScope)
            .render(this.context.getOverlayContainer());
    }
    render() {
        const { x, y, w, h } = this.props.bBox;
        const leftPadding = 10;

        const deleteStyle = {};

        if (!this.state.highlighted) {
            deleteStyle.display = 'none';
        }

        let className = 'package-declaration-item';

        if (this.state.highlighted) {
            className = 'package-declaration-item-hightlighted';
        }

        const importPkgName = this.getImportedPackageName(this.props.importDec);
        return (
            <g className={className} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>
                <title> {importPkgName}</title>
                <g onClick={(e) => { this.openEditor(this.props.bBox); }}>
                    <rect x={x} y={y} height={h} width={w} className='background' />
                    <text x={x + leftPadding} y={y + h / 2} rx='0' ry='0' className='import-definition-text'>
                        {importPkgName}
                    </text>
                </g>
                <rect
                    x={x + w - 30}
                    y={y}
                    height={h}
                    width={30}
                    className='delete-background'
                    onClick={this.handleDeleteClick}
                />
                <text
                    x={x + w - 18}
                    y={y + h / 2}
                    style={deleteStyle}
                    className='delete-x'
                    onClick={this.handleDeleteClick}
                >x</text>
            </g>
        );
    }
}

importDeclarationItem.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
};
