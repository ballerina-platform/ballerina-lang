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
import Alerts from 'alerts';
import * as DesignerDefaults from './../configs/designer-defaults';
import ImageUtil from './image-util';
import EditableText from './editable-text';

const closeButtonWidth = 40;
/**
 *
 *
 * @class StructDefinitionItem
 * @extends {React.Component}
 */
class StructDefinitionItem extends React.Component {
    /**
     * Creates an instance of StructDefinitionItem.
     * @memberof StructDefinitionItem
     */
    constructor() {
        super();
        this.state = {
            newType: '',
            newIdentifier: '',
            newDefaultValue: '',
            newIdentifierEditing: false,
            newDefaultValueEditing: false,
        };
    }
    /**
     * Delete struct variable definition
     * @param {Object} node
     */
    deleteStatement(model) {
        model.remove();
    }
    render() {
        const { model } = this.props;

        const {x, y, w, h, validateIdentifierName, validateDefaultValue} = this.props;
        const columnSize = (w - closeButtonWidth) / 3;

        const typePkgName = model.getBTypePkgName();
        let type = model.getVariableType();
        if (typePkgName !== undefined) {
            type = typePkgName + ':' + type;
        }
        const identifier = model.getIdentifier();
        const value = model.getValue();

        const typeCellbox = {
            x,
            y,
            width: columnSize,
            height: DesignerDefaults.structDefinitionStatement.height,
        };

        const identifierCellBox = {
            x: x + columnSize,
            y,
            width: columnSize,
            height: DesignerDefaults.structDefinitionStatement.height,
        };

        const defaultValueBox = {
            x: x + (columnSize * 2),
            y,
            width: columnSize + closeButtonWidth,
            height: DesignerDefaults.structDefinitionStatement.height,
        };

        return (
            <g key={model.getIdentifier()} className="struct-definition-statement">

                <g className="struct-variable-definition-type" >
                    <rect {...typeCellbox} className="struct-added-value-wrapper" />
                    <text
                        x={DesignerDefaults.structDefinition.panelPadding + x}
                        y={y + (DesignerDefaults.structDefinitionStatement.height / 2) + 3}
                        className="struct-variable-definition-type-text"
                    > {type} </text>
                </g>
                <g
                    className="struct-variable-definition-identifier"
                    onClick={() => this.setState({ newIdentifierEditing: true })}
                >
                    <rect {...identifierCellBox} className="struct-added-value-wrapper" />
                    <text
                        x={x + DesignerDefaults.structDefinition.panelPadding + columnSize}
                        y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                        className="struct-variable-definition-identifier-text"
                    >
                        {this.state.newIdentifierEditing ? '' : identifier}
                    </text>
                </g>
                <EditableText
                    {...identifierCellBox}
                    y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                    placeholder="Identifier"
                    onBlur={() => {
                        this.setState({
                            newIdentifierEditing: false,
                        });
                        const identifierAlreadyExists = _.findIndex(model.getParent().getVariableDefinitionStatements(), (variableDefinitionStatement) => {
                            return variableDefinitionStatement.getIdentifier() === identifier;
                        }) !== -1;
                        if (identifierAlreadyExists) {
                            const errorString = `A variable with identifier ${identifier} already exists.`;
                            Alerts.error(errorString);
                            throw errorString;
                        }
                        model.setIdentifier(this.state.newIdentifier || identifier);
                    }}
                    editing={this.state.newIdentifierEditing}
                    onChange={ (e) => {
                        if (validateIdentifierName(e.target.value)) {
                            this.setState({
                                newIdentifier: e.target.value,
                            });
                        }
                    }}
                >
                    {this.state.newIdentifierEditing ? (this.state.newIdentifier ? this.state.newIdentifier : identifier) : ''}
                </EditableText>
                <g
                    className="struct-variable-definition-value"
                    onClick={() => this.setState({ newDefaultValueEditing: true })}
                >
                    <rect {...defaultValueBox} className="struct-added-value-wrapper" />
                    <text
                        x={x + DesignerDefaults.structDefinition.panelPadding + columnSize * 2}
                        y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                        className="struct-variable-definition-value-text"
                    > {value} </text>
                </g>
                <EditableText
                    {...defaultValueBox}
                    y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                    placeholder="Default Value"
                    onBlur={() => {
                        this.setState({
                            newDefaultValueEditing: false,
                        });
                        model.setValue(this.state.newDefaultValue || value);
                    }}
                    editing={this.state.newDefaultValueEditing}
                    onChange={ (e) => {
                        validateDefaultValue(type, e.target.value);
                        this.setState({
                            newDefaultValue: e.target.value,
                        });
                    }}
                >
                    {this.state.newDefaultValueEditing ? (this.state.newDefaultValue ? this.state.newDefaultValue : value) : ''}
                </EditableText>
                <rect
                    x={x + DesignerDefaults.structDefinitionStatement.width - DesignerDefaults.structDefinitionStatement.deleteButtonOffset}
                    y={y}
                    onClick={() => this.deleteStatement(model)}
                    width="30"
                    height="30"
                    className="struct-delete-icon-wrapper"
                />
                <image
                    x={x + DesignerDefaults.structDefinitionStatement.width - DesignerDefaults.structDefinitionStatement.deleteButtonOffset + 9}
                    y={y + 9}
                    onClick={() => this.deleteStatement(model)}
                    width="12"
                    height="12"
                    className="parameter-delete-icon"
                    xlinkHref={ImageUtil.getSVGIconString('cancel')}
                />
            </g>
        );
    }
}

StructDefinitionItem.propTypes = {
    model: PropTypes.object,
};

export default StructDefinitionItem;
