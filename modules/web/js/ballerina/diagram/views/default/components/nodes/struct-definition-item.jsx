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
import * as DesignerDefaults from '../../designer-defaults';
import ImageUtil from '../../../../image-util';
import EditableText from './editable-text';
import NodeFactory from './../../../../../model/node-factory';

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
    constructor(props) {
        super();
        this.state = {
            newIdentifier: props.model.getName().value,
            newDefaultValue: (props.model.getInitialExpression() ? props.model.getInitialExpression().value : undefined),
            newIdentifierEditing: false,
            newDefaultValueEditing: false,
        };
    }
    /**
     * Delete struct variable definition
     * @param {Object} node
     */
    deleteStatement(model) {
        model.parent.removeFields(model);
    }
    getTypeKind(model) {
        const typeNode = model.getTypeNode();
        switch (typeNode.kind) {
            case 'ArrayType':
                const { dimensions = 1 } = typeNode;
                let val = this.getTypeFromNode(typeNode.elementType);
                for (let i = 0; i < dimensions; i++) {
                    val = `${val}[]`;
                }
                return val;
            default:
                return this.getTypeFromNode(typeNode);
        }
    }
    getTypeFromNode(typeNode) {
        switch (typeNode.kind) {
            case 'ValueTypeNode':
                return typeNode.typeKind;
            case 'UserDefinedType':
                if (typeNode.anonStruct) {
                    return typeNode.anonStruct.getSource().replace(typeNode.anonStruct.name.getValue(), '').trim();
                } else {
                    return typeNode.typeName.value;
                }
            default:
                return typeNode.typeKind;
        }
    }
    render() {
        const { model } = this.props;

        const { x, y, w, h, validateIdentifierName, validateDefaultValue } = this.props;
        const columnSize = (w - closeButtonWidth) / 3;

        const type = this.getTypeKind(model);

        const identifier = model.getName().value;
        const value = (model.getInitialExpression() ? model.getInitialExpression().value : undefined);
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

        let zebraShadeType = '-light';
        if (this.props.index % 2 === 1) {
            zebraShadeType = '-dark';
        }

        return (
            <g key={identifier} className='struct-definition-statement'>

                <g className='struct-variable-definition-type' >
                    <rect {...typeCellbox} className={'struct-added-value-wrapper' + zebraShadeType} />
                    <text
                        x={DesignerDefaults.structDefinition.panelPadding + x}
                        y={y + (DesignerDefaults.structDefinitionStatement.height / 2) + 3}
                        className='struct-variable-definition-type-text'
                    > {type} </text>
                </g>
                <g
                    className='struct-variable-definition-identifier'
                    onClick={() => this.setState({ newIdentifierEditing: true })}
                >
                    <rect {...identifierCellBox} className={'struct-added-value-wrapper' + zebraShadeType} />
                    <text
                        x={x + DesignerDefaults.structDefinition.panelPadding + columnSize}
                        y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                        className='struct-variable-definition-identifier-text'
                    >
                        {this.state.newIdentifierEditing ? '' : identifier}
                    </text>
                </g>
                <EditableText
                    {...identifierCellBox}
                    y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                    placeholder='Identifier'
                    onBlur={() => {
                        this.setState({
                            newIdentifierEditing: false,
                        });
                        // Identifier has not changed
                        if (this.state.newIdentifier === identifier) {
                            return;
                        }
                        const identifierAlreadyExists = _.findIndex(model.parent.getFields(), (field) => {
                            return field.getName().value === this.state.newIdentifier;
                        }) !== -1;
                        if (identifierAlreadyExists) {
                            const errorString = `A variable with identifier ${this.state.newIdentifier} already exists.`;
                            this.context.alert.showError(errorString);
                            throw errorString;
                        }
                        model.getName().setValue(this.state.newIdentifier || identifier);
                    }}
                    editing={this.state.newIdentifierEditing}
                    onChange={(e) => {
                        if (validateIdentifierName(e.target.value)) {
                            this.setState({
                                newIdentifier: e.target.value,
                            });
                        }
                    }}
                >
                    {this.state.newIdentifierEditing ? this.state.newIdentifier : ''}
                </EditableText>
                <g
                    className='struct-variable-definition-value'
                    onClick={() => this.setState({ newDefaultValueEditing: true })}
                >
                    <rect {...defaultValueBox} className={'struct-added-value-wrapper' + zebraShadeType} />
                    <text
                        x={x + DesignerDefaults.structDefinition.panelPadding + columnSize * 2}
                        y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                        className='struct-variable-definition-value-text'
                    > {value} </text>
                </g>
                <EditableText
                    {...defaultValueBox}
                    y={y + DesignerDefaults.structDefinitionStatement.height / 2 + 3}
                    placeholder='Default Value'
                    onBlur={() => {
                        this.setState({
                            newDefaultValueEditing: false,
                        });
                        if (validateDefaultValue(type, this.state.newDefaultValue)) {
                            if (!this.state.newDefaultValue) {
                                const valueArrayId = model.getInitialExpression() && model.getInitialExpression().id;
                                if (valueArrayId) {
                                    model.getInitialExpression().setValue('');
                                }
                            } else {
                                const newDefaultValue = this.props.addQuotesToString(type, this.state.newDefaultValue);
                                if (model.getInitialExpression()) {
                                    model.getInitialExpression().setValue(newDefaultValue);
                                } else {
                                    model.setInitialExpression(NodeFactory.createLiteral({ value: newDefaultValue }));
                                }
                            }
                        }
                    }}
                    editing={this.state.newDefaultValueEditing}
                    onChange={(e) => {
                        this.setState({
                            newDefaultValue: e.target.value,
                        });
                    }}
                >
                    {this.state.newDefaultValueEditing ? this.state.newDefaultValue : ''}
                </EditableText>
                <rect
                    x={x + DesignerDefaults.structDefinitionStatement.width - DesignerDefaults.structDefinitionStatement.deleteButtonOffset}
                    y={y}
                    onClick={() => this.deleteStatement(model)}
                    width='30'
                    height='30'
                    className={'struct-delete-icon-wrapper' + zebraShadeType}
                />
                <image
                    x={x + DesignerDefaults.structDefinitionStatement.width - DesignerDefaults.structDefinitionStatement.deleteButtonOffset + 9}
                    y={y + 9}
                    onClick={() => this.deleteStatement(model)}
                    width='12'
                    height='12'
                    className='parameter-delete-icon'
                    xlinkHref={ImageUtil.getSVGIconString('cancel')}
                />
            </g>
        );
    }
}

StructDefinitionItem.propTypes = {
    model: PropTypes.object,
};

StructDefinitionItem.contextTypes = {
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
    }).isRequired,
};

export default StructDefinitionItem;
