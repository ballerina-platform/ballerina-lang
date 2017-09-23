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
import './import-declaration-item.css';
import * as DesignerDefaults from './../../../../configs/designer-defaults';
import { util } from './../../../../visitors/sizing-utils';

export default class importDeclarationItem extends React.Component {
    constructor() {
        super();
        this.state = {
            highlighted: false,
        };
        this.handleMouseEnter = this.handleMouseEnter.bind(this);
        this.handleMouseLeave = this.handleMouseLeave.bind(this);
        this.handleDeleteClick = this.handleDeleteClick.bind(this);
    }

    handleDeleteClick() {
        this.props.onDeleteClick(this.props.importDec.package);
    }

    handleMouseEnter() {
        this.setState({ highlighted: true });
    }

    handleMouseLeave() {
        this.setState({ highlighted: false });
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

        let importPkgName = this.props.importDec.package;
        /*if (this.props.importDec.getAsName() !== undefined) {
            importPkgName = importPkgName + ' as ' + this.props.importDec.getAsName();
        }*/
        return (
            <g className={className} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>
                <title> {importPkgName}</title>
                <rect x={x} y={y} height={h} width={w} className="background" />
                <text x={x + leftPadding} y={y + h / 2} rx="0" ry="0" className="import-definition-text">

                    {util.getTextWidth(importPkgName, 0, DesignerDefaults.globalDeclarationWidth).text}
                </text>
                <rect x={x + w - 30} y={y} height={h} width={30} className="delete-background" onClick={this.handleDeleteClick} />
                <text x={x + w - 18} y={y + h / 2} style={deleteStyle} className="delete-x" onClick={this.handleDeleteClick}>x</text>
            </g>
        );
    }
}
