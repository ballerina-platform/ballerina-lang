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
import _ from 'lodash';
import PropTypes from 'prop-types';
import PanelDecorator from './../decorators/panel-decorator';
import './struct-definition.css';
import Renderer from './renderer';
import * as DesignerDefaults from './../../designer-defaults';
import SuggestionsText from './suggestions-text2';
import ImageUtil from '../../../../image-util';
import EditableText from './editable-text';
import StructDefinitionItem from './struct-definition-item';
import TreeUtils from './../../../../../model/tree-util';
import Node from './../../../../../../ballerina/model/tree/node';
import TreeBuilder from './../../../../../model/tree-builder';
import FragmentUtils from './../../../../../utils/fragment-utils';

/**
 * @class StructDefinition
 * @extends {React.Component}
 */
class StructNode extends React.Component {
    /**
     * Creates an instance of StructDefinition.
     * @param {any} props
     */
    constructor(props) {
        super(props);
        this.state = {
            newType: '',
            newIdentifier: '',
            newValue: '',
            newIdentifierEditing: false,
            newValueEditing: false,
        };
    }
    /**
     * Handle struct type change in input
     * @param {string} value - Struct type
     */
    onAddStructTypeChange(value) {
        this.validateStructType(value);
        this.setState({
            newType: value,
        });
    }
    /**
     * Add new struct variable definition
     * @param {string} bType - Data type of the new struct
     * @param {string} identifier - Name of the identifier
     * @param {any} defaultValue - Default value of the new identifier
     */
    addVariableDefinitionStatement(bType, identifier, defaultValue) {
        const structSuggestions = this.context.environment.getTypes().map(name => ({ name }));
        if (!bType) {
            this.context.alert.showError('Struct field type cannot be empty');
            return;
        }
        const bTypeExists = _.findIndex(structSuggestions, (type) => {
            return type.name === bType;
        }) !== -1;
        if (!bTypeExists) {
            this.context.alert.showError(`Invalid struct field type ${bType} provided`);
            return;
        }
        if (!identifier || !identifier.length) {
            this.context.alert.showError('Struct field name cannot be empty');
            return;
        }

        const { model } = this.props;
        const identifierAlreadyExists = _.findIndex(model.getFields(), (field) => {
            return field.getName().value === identifier;
        }) !== -1;
        if (identifierAlreadyExists) {
            this.context.alert.showError(`A struct field with the name ${identifier} already exists`);
            return;
        }
        if (!this.validateDefaultValue(bType, defaultValue)) {
            return;
        }
        let statement = bType + ' ' + identifier;
        if (defaultValue) {
            statement += ' = ' + defaultValue;
        }
        statement += ';';
        const fragment = FragmentUtils.createStatementFragment(statement);
        const parsedJson = FragmentUtils.parseFragment(fragment);
        if (!parsedJson.error) {
            const structFieldNode = TreeBuilder.build(parsedJson);
            this.props.model.addFields(structFieldNode.getVariable());
        } else {
            this.context.alert.showError('Invalid content provided !');
        }
    }
    /**
     * Handle the default value if the data type is a string
     * @param {string} dataType - Data type of the new struct
     * @param {string} value - Value of the new identifier
     */
    addQuotesToString(dataType, value) {
        if (dataType === 'string' && !/".*?"$/.test(value)) {
            if (value === '') {
                return value;
            }
            return `"${value}"`;
        }
        return value;
    }
    /**
     * Create new variable definition and reset input form
     */
    createNew() {
        const { newType, newValue, newIdentifier } = this.state;
        this.addVariableDefinitionStatement(newType, newIdentifier, this.addQuotesToString(newType, newValue));
        this.setState({
            newType: '',
            newIdentifier: '',
            newValue: '',
        });
    }
    /**
     * Delete struct variable definition
     * @param {Object} node
     */
    deleteStatement(node) {
        node.remove();
    }
    /**
     * Handle click event on new struct variable's type field
     */
    handleAddTypeClick() {
        this.setState({
            canShowAddType: true,
        });
    }

    /**
     * Validate input values for struct default values
     * Only checks for the simple literals
     *
     * @param {string} type
     * @param {any} value
     * @memberof StructDefinition
     */
    validateDefaultValue(type, value) {
        if (!value) {
            return true;
        }
        if (type === 'int' && /^[-]?\d+$/.test(value)) {
            return true;
        } else if (type === 'float' && ((/\d*\.?\d+/.test(value) || parseFloat(value)))) {
            return true;
        } else if (type === 'boolean' && (/\btrue\b/.test(value) || /\bfalse\b/.test(value))) {
            return true;
        } else if (type === 'string') {
            return true;
        }
        this.context.alert.showError('Type of the default value is not compatible with the expected struct field type');
        return false;
    }
    /**
     * Hide new struct definition type dropdown
     */
    hideAddSuggestions() {
        this.setState({ canShowAddType: false });
    }

    /**
     * Open JSON import dialog box and pass callback
     */
    onClickJsonImport() {
        const onImport = (json) => {
            let success = true;
            const refExpr = TreeBuilder.build(
                FragmentUtils.parseFragment(FragmentUtils.createExpressionFragment(json)));
            if (!refExpr.error) {
                success = this.processJSONStruct(this.props.model, refExpr.variable.initialExpression);
            } else {
                success = false;
            }
            return success;
        };
        const id = 'composer.dialog.import.struct';
        const { command: { dispatch } } = this.context;
        dispatch('popup-dialog', {
            id,
            additionalProps: {
                onImport,
            },
        });
    }

    /**
     * process JSON and generate struct fields
     * @param  {object} parent      parent struct object
     * @param  {object} literalExpr JSON expression
     * @return {boolean}             status
     */
    processJSONStruct(parent, literalExpr) {
        let currentValue;
        let success = true;
        parent.setFields([], true);
        literalExpr.keyValuePairs.every((ketValPair) => {
            let currentName;
            if (TreeUtils.isLiteral(ketValPair.getKey())) {
                currentName = ketValPair.getKey().getValue().replace(/"/g, '');
            } else {
                currentName = ketValPair.getKey().getVariableName().getValue();
            }
            if (TreeUtils.isRecordLiteralExpr(ketValPair.getValue())) {
                const parsedJson = FragmentUtils.parseFragment(
                              FragmentUtils.createStatementFragment(`struct { } ${currentName};`));
                const anonStruct = TreeBuilder.build(parsedJson);
                success = this.processJSONStruct(anonStruct.getVariable().getTypeNode().anonStruct,
                            ketValPair.getValue());
                if (success) {
                    parent.addFields(anonStruct.getVariable());
                }
                return success;
            } else if (TreeUtils.isLiteral(ketValPair.getValue())) {
                currentValue = ketValPair.getValue().getValue();
                let currentType = 'string';
                if (this.isInt(currentValue)) {
                    currentType = 'int';
                } else if (this.isFloat(currentValue)) {
                    currentType = 'float';
                } else if (currentValue === 'true' || currentValue === 'false') {
                    currentType = 'boolean';
                }
                const refExpr = TreeBuilder.build(FragmentUtils.parseFragment(
                 FragmentUtils.createStatementFragment(`${currentType} ${currentName} = ${currentValue};`)));
                if (!refExpr.error) {
                    parent.addFields(refExpr.getVariable());
                } else {
                    success = false;
                }
                return success;
            } else {
                success = false;
            }
        });
        return success;
    }

    /**
     * Check given value is an Integer
     * @param  {Number} val value to check
     * @return {Boolean}  is Intger
     */
    isInt(val) {
        return !isNaN(val) && Number(val).toString().length === (Number.parseInt(Number(val), 10).toString().length);
    }

    /**
     * Check given value is an Integer
     * @param  {Number} val value to check
     * @return {Boolean}  is Intger
     */
    isFloat(val) {
        return !isNaN(val) && !this.isInt(Number(val)) && val.toString().length > 0;
    }

    /**
     * Validate identifier name
     * @param {string} identifier - identifier name
     */
    validateIdentifierName(identifier) {
        if (Node.isValidIdentifier(identifier)) {
            return true;
        }
    }
    /**
     * Validate struct type
     * @param {string} structType - struct type
     */
    validateStructType(structType) {
        if (!Node.isValidType(structType)) {
            this.context.alert.showError(`Invalid struct field type : ${structType}`);
        }
    }
    /**
     *  Render content operations
     *
     * @param {Object} { x, y, w, h } - Dimensions to render
     * @param {Number} columnSize - Width of the column
     * @returns {Object} - React node
     */
    renderContentOperations({ x, y, w, h }, columnSize) {
        const placeHolderPadding = 10;
        const submitButtonPadding = 5;
        const typeCellbox = {
            x: x + DesignerDefaults.structDefinition.panelPadding,
            y: y + DesignerDefaults.structDefinition.panelPadding,
            width: columnSize - DesignerDefaults.structDefinition.panelPadding - DesignerDefaults.structDefinition.columnPadding / 2,
            height: h - DesignerDefaults.structDefinition.panelPadding * 2,
        };

        const identifierCellBox = {
            x: x + columnSize + DesignerDefaults.structDefinition.columnPadding / 2,
            y: y + DesignerDefaults.structDefinition.panelPadding,
            width: columnSize - DesignerDefaults.structDefinition.columnPadding,
            height: h - DesignerDefaults.structDefinition.panelPadding * 2,
        };

        const defaultValueBox = {
            x: x + columnSize * 2 + DesignerDefaults.structDefinition.columnPadding / 2,
            y: y + DesignerDefaults.structDefinition.panelPadding,
            width: columnSize - DesignerDefaults.structDefinition.columnPadding / 2,
            height: h - DesignerDefaults.structDefinition.panelPadding * 2,
        };
        const { environment } = this.context;
        const structSuggestions = environment.getTypes().map(name => ({ name }));
        return (
            <g>
                <rect
                    x={x + 480}
                    y={y - 22}
                    width={120}
                    height={20}
                    className='struct-import-json-button'
                    onClick={e => this.onClickJsonImport()}
                />
                <text
                    x={x + 485}
                    y={y - 10}
                    className='struct-import-json-text'
                    onClick={e => this.onClickJsonImport()}
                > {'Import from JSON'}
                </text>
                <rect x={x} y={y} width={w} height={h} className='struct-content-operations-wrapper' fill='#3d3d3d' />
                <g onClick={e => this.handleAddTypeClick(this.state.newType, typeCellbox)} >
                    <rect {...typeCellbox} className='struct-type-dropdown-wrapper' />
                    <text
                        x={typeCellbox.x + placeHolderPadding}
                        y={y + DesignerDefaults.contentOperations.height / 2 + 5}
                    > {this.state.newType || 'Select Type'}
                    </text>
                    <SuggestionsText
                        {...typeCellbox}
                        suggestionsPool={structSuggestions}
                        show={this.state.canShowAddType}
                        onBlur={() => this.hideAddSuggestions()}
                        onEnter={() => this.hideAddSuggestions()}
                        onChange={value => this.onAddStructTypeChange(value)}
                        value={this.state.newType}
                    />
                </g>
                <g onClick={e => this.setState({ newIdentifierEditing: true })} >
                    <rect {...identifierCellBox} className='struct-input-value-wrapper' />
                    <text
                        x={identifierCellBox.x + placeHolderPadding}
                        y={y + DesignerDefaults.contentOperations.height / 2 + 5}
                        className='struct-input-text'
                    >
                        {this.state.newIdentifierEditing ? '' : (this.state.newIdentifier ? this.state.newIdentifier : 'Identifier')}
                    </text>
                </g>
                <EditableText
                    {...identifierCellBox}
                    y={y + DesignerDefaults.contentOperations.height / 2}
                    placeholder='Identifier'
                    onBlur={() => {
                        this.setState({
                            newIdentifierEditing: false,
                        });
                    }}
                    editing={this.state.newIdentifierEditing}
                    onChange={(e) => {
                        if (!e.target.value.length || this.validateIdentifierName(e.target.value)) {
                            this.setState({
                                newIdentifier: e.target.value,
                            });
                        }
                    }}
                >
                    {this.state.newIdentifierEditing ? this.state.newIdentifier : ''}
                </EditableText>
                <g onClick={e => this.setState({ newValueEditing: true })}>
                    <rect {...defaultValueBox} className='struct-input-value-wrapper' />
                    <text
                        x={defaultValueBox.x + placeHolderPadding}
                        y={y + DesignerDefaults.contentOperations.height / 2 + 5}
                        className='struct-input-text'
                    >
                        {this.state.newValueEditing ? '' : (this.state.newValue ? this.state.newValue : '+ Add Default Value')}
                    </text>
                </g>
                <EditableText
                    {...defaultValueBox}
                    y={y + DesignerDefaults.contentOperations.height / 2}
                    placeholder='+ Add Default Value'
                    onBlur={() => {
                        this.setState({
                            newValueEditing: false,
                        });
                    }}
                    editing={this.state.newValueEditing}
                    onChange={(e) => {
                        this.setState({
                            newValue: e.target.value,
                        });
                    }}
                >
                    {this.state.newValueEditing ? this.state.newValue : ''}
                </EditableText>
                <rect
                    x={x + DesignerDefaults.structDefinitionStatement.width - 30}
                    y={y + 10}
                    width={25}
                    height={25}
                    className='struct-added-value-wrapper-light'
                />
                <image
                    x={x + DesignerDefaults.structDefinitionStatement.width - 30 + submitButtonPadding}
                    style={{ cursor: 'pointer' }}
                    y={y + 10 + submitButtonPadding}
                    width={20 - submitButtonPadding}
                    height={20 - submitButtonPadding}
                    onClick={() => this.createNew()}
                    className='struct-add-icon-wrapper'
                    xlinkHref={ImageUtil.getSVGIconString('check')}
                />
            </g>
        );
    }
    /**
     * Render a text box in a given bounding box
     *
     * @param {any} textValue - Initial Value
     * @param {Object} bBox - Bounding box
     * @param {function} callback - Callback function
     */
    renderTextBox(textValue, bBox, callback) {
        Renderer.renderTextBox({
            bBox,
            display: true,
            initialValue: textValue || '',
            onChange(value) {
                callback(value);
            },
        }, this.context.getOverlayContainer());
    }
    /**
     * @inheritdoc
     */
    render() {
        const { model } = this.props;
        const { bBox, components: { body } } = model.viewState;
        const children = model.getFields() || [];
        const title = model.getName().value;

        const coDimensions = {
            x: bBox.x + DesignerDefaults.panel.body.padding.left,
            y: bBox.y + (DesignerDefaults.panel.body.padding.top * 2) + model.viewState.components.annotation.h,
            w: DesignerDefaults.contentOperations.width,
            h: DesignerDefaults.contentOperations.height,
        };
        const columnSize = (coDimensions.w - DesignerDefaults.structDefinition.submitButtonWidth) / 3;
        return (
            <PanelDecorator icon='tool-icons/struct' title={title} bBox={bBox} model={model}>
                {this.renderContentOperations(coDimensions, columnSize)}
                <g>
                    {
                        children.map((child, i) => {
                            if (TreeUtils.isVariable(child)) {
                                return (
                                    <StructDefinitionItem
                                        x={coDimensions.x}
                                        y={coDimensions.y + DesignerDefaults.contentOperations.height +
                                        DesignerDefaults.structDefinitionStatement.height * i + 10}
                                        w={coDimensions.w}
                                        h={DesignerDefaults.structDefinitionStatement.height}
                                        model={child}
                                        key={child.getName().value}
                                        validateIdentifierName={this.validateIdentifierName}
                                        validateDefaultValue={this.validateDefaultValue.bind(this)}
                                        addQuotesToString={this.addQuotesToString}
                                        index={i}
                                    />
                                );
                            }
                        })
                    }
                </g>
            </PanelDecorator>
        );
    }
}

StructNode.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
        closeEditor: PropTypes.func,
    }).isRequired,
};

export default StructNode;
