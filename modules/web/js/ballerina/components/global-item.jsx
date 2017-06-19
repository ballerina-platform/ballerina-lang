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
import './global-item.css';
import { variablesPane as variablesPaneDefaults } from '../configs/designer-defaults';

export default class GlobalDefinitionItem extends React.Component {
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
        this.props.onDeleteClick(this.props.globalDec);
    }

    handleMouseEnter() {
        this.setState({ highlighted: true });
    }

    handleMouseLeave() {
        this.setState({ highlighted: false });
    }

    render() {
        const { x, y, w, h } = this.props.bBox;
        const leftPadding = variablesPaneDefaults.globalItemLeftPadding;

        const deleteStyle = {};

        if (!this.state.highlighted) {
            deleteStyle.display = 'none';
        }

        let className = 'global-definition-item';

        if (this.state.highlighted) {
            className = 'global-definition-item-hightlighted';
        }

        return (
            <g className={className} onMouseEnter={this.handleMouseEnter} onMouseLeave={this.handleMouseLeave}>
                <rect x={x} y={y} height={h} width={w} className="background" />
                <text x={x + leftPadding} y={y + h / 2} rx="0" ry="0" className="global-definition-text">
                    {this.props.getValue(this.props.globalDec)}
                </text>
                <rect x={x} y={y} height={h} width={w} className="global-definition-decorator" />
                <rect x={x + w - 30} y={y} height={h} width={30} className="delete-background" onClick={this.handleDeleteClick} />
                <text x={x + w - 18} y={y + h / 2} style={deleteStyle} className="delete-x" onClick={this.handleDeleteClick}>x</text>
            </g>
        );
    }
}
