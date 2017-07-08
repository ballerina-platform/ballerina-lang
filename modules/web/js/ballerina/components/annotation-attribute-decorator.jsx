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
import './annotation-definition.css';
import Alerts from 'alerts';
import PropTypes from 'prop-types';
import EditableText from './editable-text';
import * as DesignerDefaults from './../configs/designer-defaults';

const DEFAULT_INPUT_VALUE = '+ Add Attribute';

/**
 * Annotation Attribute Decorator
 * */
class AnnotationAttributeDecorator extends React.Component {
    constructor(props) {
        super(props);
        this.model = this.props.model;
        this.state = { inputValue: DEFAULT_INPUT_VALUE, editing: false, editValue: '' };
        this.setAnnotationAttributeFromInputBox = this.setAnnotationAttributeFromInputBox.bind(this);
    }

    onClickVariableTextBox(e) {
        this.setState({ editing: true, editValue: '' });
    }

    onInputBlur(e) {
        if (DEFAULT_INPUT_VALUE !== this.state.editValue && this.state.editValue !== '') {
            if (!this.addAnnotationAttribute()) {
                e.preventDefault();
            }
        }
        this.setState({ editing: false, editValue: '' });
    }

    onInputChange(e) {
        const variableDeclaration = e.target.value.replace(';', '');
        this.setState({ editing: true, editValue: variableDeclaration });
    }

    onKeyDown(e) {
        if (e.keyCode === 13) {
            if (DEFAULT_INPUT_VALUE !== this.state.editValue) {
                if (this.validateAttribute(this.state.editValue)) {
                    if (!this.addAnnotationAttribute()) {
                        e.preventDefault();
                    }
                }
            }
            this.setState({ editing: false, editValue: '' });
        }
    }

    /**
     * Get types of ballerina to which can be applied when declaring variables.
     * */
    getTypeDropdownValues() {
        const { environment } = this.context;
        const dropdownData = [];
        // Adding items to the type dropdown.
        const bTypes = environment.getTypes();
        _.forEach(bTypes, (bType) => {
            dropdownData.push({ id: bType, text: bType });
        });
        return dropdownData;
    }

    setAnnotationAttributeFromInputBox(input) {
        this.setState({ inputValue: input });
    }

    /**
     * Add Annotation Attribute to annotation definition.
     * */
    addAnnotationAttribute() {
        const model = this.props.model;
        try {
            const variableDeclaration = this.state.editValue.trim();
            if (DEFAULT_INPUT_VALUE !== variableDeclaration || variableDeclaration !== '') {
                const splitedExpression = variableDeclaration.split('=');
                const leftHandSideExpression = splitedExpression[0].trim();
                let rightHandSideExpression;
                if (splitedExpression.length > 1) {
                    rightHandSideExpression = splitedExpression[1].trim();
                }

                if (leftHandSideExpression.split(' ').length <= 1) {
                    const errorString = `Invalid variable declaration: ${variableDeclaration}`;
                    Alerts.error(errorString);
                    return false;
                }

                const bType = leftHandSideExpression.split(' ')[0];
                if (!this.validateType(bType)) {
                    const errorString = `Invalid type for a variable: ${bType}`;
                    Alerts.error(errorString);
                    return false;
                }

                const identifier = leftHandSideExpression.split(' ')[1];

                let defaultValue = '';
                if (rightHandSideExpression) {
                    defaultValue = rightHandSideExpression;
                }

                model.addAnnotationAttributeDefinition(bType, identifier, defaultValue);
                return true;
            }
            const errorString = 'Please Enter Variable Declaration Before Adding';
            Alerts.error(errorString);
            return false;
        } catch (e) {
            Alerts.error(e);
            return false;
        }
    }

    validateAttribute(attribute) {
        if (attribute.includes('=')) {
            const splitedExpression = attribute.split('=');
            const leftSideSplitted = splitedExpression[0].trim().split(' ');
            const rightSide = splitedExpression[1].trim();

            if (leftSideSplitted.length > 1 && rightSide) {
                return true;
            }
        } else {
            const splitedExpression = attribute.trim().split(' ');
            if (splitedExpression.length > 1) {
                return true;
            }
        }

        return false;
    }

    /**
     * Validate type.
     * */
    validateType(bType) {
        let isValid = false;
        const typeList = this.getTypeDropdownValues();
        const filteredTypeList = _.filter(typeList, type => type.id === bType);
        if (filteredTypeList.length > 0) {
            isValid = true;
        }
        return isValid;
    }

    render() {
        const bBox = this.props.bBox;
        const viewState = this.props.model.getViewState();
        const editableBox_x = bBox.x + DesignerDefaults.panel.body.padding.left;
        const editableBox_y = bBox.y + DesignerDefaults.annotationAttributeDefinition.text.padding.top
            + DesignerDefaults.panel.heading.height
            + DesignerDefaults.panel.body.padding.top
            + viewState.components.annotation.h;
        const editBoxRect_x = bBox.x + DesignerDefaults.panel.body.padding.left;
        const editBoxRect_y = bBox.y + DesignerDefaults.panel.heading.height
            + DesignerDefaults.panel.body.padding.top
            + viewState.components.annotation.h;
        return (
            <g className="attribute-content-operations-wrapper">
                <g onClick={() => this.onClickVariableTextBox()}>
                    <rect
                        x={editBoxRect_x}
                        y={editBoxRect_y}
                        width={DesignerDefaults.annotationAttributeDefinition.heading.width}
                        height={DesignerDefaults.annotationAttributeDefinition.heading.height}
                        className="annotation-input"
                    />
                    <EditableText
                        x={editableBox_x}
                        y={editableBox_y}
                        width={DesignerDefaults.annotationAttributeDefinition.text.width}
                        height={DesignerDefaults.annotationAttributeDefinition.text.height}
                        labelClass={'annotation-input-placeholder'}
                        inputClass={'annotation-input-text-box'}
                        placeholder={DEFAULT_INPUT_VALUE}
                        displayText={DEFAULT_INPUT_VALUE}
                        onKeyDown={(e) => {
                            this.onKeyDown(e);
                        }}
                        onBlur={(e) => {
                            this.onInputBlur(e);
                        }}
                        onClick={() => {
                            this.onClickVariableTextBox();
                        }}
                        editing={this.state.editing}
                        onChange={(e) => {
                            this.onInputChange(e);
                        }}
                    >
                        {this.state.editValue}
                    </EditableText>
                </g>
            </g>
        );
    }
}

AnnotationAttributeDecorator.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttributeDecorator;
