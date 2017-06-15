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
import './globals-expanded.css';
import ImageUtil from './image-util';
import PropTypes from 'prop-types';
import SuggestionsText from './suggestions-text';
import GlobalItem from './global-item';
import EditableText from './editable-text';
import BallerinaEnvironment from '../env/environment';
import { variablesPane as variablesPaneDefaults } from '../configs/designer-defaults';

export default class GlobalExpanded extends React.Component {
    constructor() {
        super();
        this.state = {
            editing: false,
            value: '',
        };
        this.handleAddGlobalClick = this.handleAddGlobalClick.bind(this);
        this.handleAddGlobalBlur = this.handleAddGlobalBlur.bind(this);
        this.onChange = this.onChange.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
    }

    handleAddGlobalClick() {
        this.setState({ editing: true });
    }

    handleAddGlobalBlur() {
        this.setState({ editing: false });
        this.props.onAddInputBlur && this.props.onAddInputBlur(this.state.value);
    }

    onChange(e) {
        this.setState({ value: e.target.value });
    }

    onKeyDown(e) {
        if (e.keyCode === 13) {
            this.props.onAddNewValue(this.state.value);
            this.setState({
                value: '',
            });
        }
    }

    render() {
        const bBox = this.props.bBox;
        const topBarHeight = variablesPaneDefaults.topBarHeight;
        const globalInputHeight = variablesPaneDefaults.inputHeight;
        const globalHeight = 30;
        const globalDeclarationWidth = 310;
        const leftPadding = 10;
        const iconSize = 20;
        const globalElements = [];

        const topBarBbox = {
            x: bBox.x,
            y: bBox.y,
        };

        let lastGlobalElementY = topBarBbox.y + topBarHeight;

        this.props.globals.forEach((globalDec) => {
            const itemBBox = {
                x: bBox.x,
                y: lastGlobalElementY,
                h: globalHeight,
                w: globalDeclarationWidth,
            };

            globalElements.push(<GlobalItem
                key={globalDec.id} bBox={itemBBox}
                globalDec={globalDec} getValue={this.props.getValue} onDeleteClick={this.props.onDeleteClick}
            />);
            lastGlobalElementY += globalHeight;
        });

        const textBoxBBox = {
            x: bBox.x + 5,
            y: lastGlobalElementY + 2,
            h: globalInputHeight - 4,
            w: globalDeclarationWidth - 10,
        };

        const options = {
            bBox: textBoxBBox,
            onChange: () => {},
            initialValue: '',
        };

        return (
            <g className="global-definitions-collection">
                <rect x={topBarBbox.x} y={topBarBbox.y} height={topBarHeight} width={globalDeclarationWidth} style={{ fill: '#ddd' }} />
                <rect x={topBarBbox.x} y={topBarBbox.y} height={topBarHeight} className="global-definition-decorator" />
                <text x={topBarBbox.x + leftPadding} y={topBarBbox.y + topBarHeight / 2} className="global-definitions-topbar-label">
                    {this.props.title}</text>
                <image
                    width={iconSize} height={iconSize} className="property-pane-action-button-delete"
                    onClick={this.props.onCollapse} xlinkHref={ImageUtil.getSVGIconString('hide')}
                    x={bBox.x + globalDeclarationWidth - iconSize - 6} y={topBarBbox.y + (topBarHeight - iconSize) / 2}
                />
                {globalElements}
                <rect x={bBox.x} y={lastGlobalElementY} height={globalInputHeight} width={globalDeclarationWidth} className="add-global-button-background" />
                <EditableText
                    x={bBox.x + 5} y={lastGlobalElementY + globalHeight / 2 + 6}
                    height={globalInputHeight - 10} width={globalDeclarationWidth - 10}
                    onBlur={this.handleAddGlobalBlur}
                    editing={this.state.editing}
                    onChange={this.onChange}
                    onKeyDown={this.onKeyDown}
                >
                    {this.state.value}
                </EditableText>
                <rect x={bBox.x} y={lastGlobalElementY} height={globalInputHeight} width={globalDeclarationWidth} className="global-definition-decorator" />
                <g onClick={this.handleAddGlobalClick}>
                    <rect
                        x={bBox.x + 7} y={lastGlobalElementY + 7} height={globalInputHeight - 14} width={globalDeclarationWidth - 14}
                        className="add-global-button"
                    />
                    <text x={bBox.x + 14} y={lastGlobalElementY + globalInputHeight / 2} className="add-global-button-text" >{this.props.addText}</text>
                </g>
            </g>
        );
    }
}
