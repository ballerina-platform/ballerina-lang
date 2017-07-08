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
import ExpressionEditor from '../../expression-editor/expression-editor-utils';
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

    handleAddGlobalBlur() {
        this.setState({ editing: false });
        this.props.onAddInputBlur && this.props.onAddInputBlur(this.state.value);
    }

    handleAddGlobalClick() {
        this.setState({ editing: true });
    }

    /**
     * renders an ExpressionEditor in the add new variable area.
     * @param {Object} bBox - bounding box ExpressionEditor should be rendered.
     */
    openEditor(bBox) {
        const options = {
            propertyType: 'text',
            key: 'If condition',
            model: this.props.model,
            getterMethod: () => this.props.newValuePlaceholder,
            setterMethod: this.props.onAddNewValue,
        };

        const packageScope = this.context.enviornment;

        new ExpressionEditor(bBox, s => {} /*no-op*/, options, packageScope)
            .render(this.context.getOverlayContainer());
    }

    render() {
        const bBox = this.props.bBox;
        const topBarHeight = variablesPaneDefaults.topBarHeight;
        const globalInputHeight = variablesPaneDefaults.inputHeight;
        const iconSize = variablesPaneDefaults.iconSize;
        const globalHeight = variablesPaneDefaults.globalItemHeight;
        const globalDeclarationWidth = variablesPaneDefaults.globalDeclarationWidth;
        const leftPadding = 10;
        const globalElements = [];
        const editorOuterPadding = 10;

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
            x: bBox.x + editorOuterPadding/2,
            y: lastGlobalElementY + editorOuterPadding/2,
            h: globalInputHeight - editorOuterPadding,
            w: globalDeclarationWidth - editorOuterPadding,
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
                <rect x={bBox.x} y={lastGlobalElementY} height={globalInputHeight} width={globalDeclarationWidth} className="add-global-button-background"
                      />
                <rect x={bBox.x} y={lastGlobalElementY} height={globalInputHeight} width={globalDeclarationWidth} className="global-definition-decorator" />
                <g onClick={e => {this.openEditor(textBoxBBox)}}>
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

GlobalExpanded.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    enviornment: PropTypes.instanceOf(Object).isRequired,
};
