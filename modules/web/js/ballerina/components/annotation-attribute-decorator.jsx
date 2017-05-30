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

const DEFAULT_INPUT_VALUE = "+ Add Attribute";

/**
 * Annotation Attribute Decorator
 * */
class AnnotationAttributeDecorator extends React.Component {
    constructor(props) {
        super(props);
        this.model = this.props.model;
        this.state = {inputValue: DEFAULT_INPUT_VALUE, editing: false, editValue: ""};
        this.setAnnotationAttributeFromInputBox = this.setAnnotationAttributeFromInputBox.bind(this);
    }

    onClickVariableTextBox(e) {
        this.setState({editing: true, editValue: ""});
    }

    onInputBlur(e) {
        if (DEFAULT_INPUT_VALUE !== this.state.editValue && this.state.editValue !== "") {
            if (!this.addAnnotationAttribute()) {
                e.preventDefault();
            }
        }
        this.setState({editing: false, editValue: ""});
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
            this.setState({editing: false, editValue: ""});
        }
    }

    onInputChange(e) {
        let variableDeclaration = e.target.value.replace(";", "");
        this.setState({editing: true, editValue: variableDeclaration});
    }

    validateAttribute(attribute) {
        if (attribute.includes("=")) {
            let splitedExpression = attribute.split("=");
            let leftSideSplitted = splitedExpression[0].trim().split(" ");
            let rightSide = splitedExpression[1].trim();

            if (leftSideSplitted.length > 1 && rightSide) {
                return true;
            }
        } else {
            let splitedExpression = attribute.trim().split(" ");
            if (splitedExpression.length > 1) {
                return true;
            }
        }

        return false;
    }

    setAnnotationAttributeFromInputBox(input) {
        this.setState({inputValue: input});
    }

    /**
     * Add Annotation Attribute to annotation definition.
     * */
    addAnnotationAttribute() {
        let model = this.props.model;
        try {
            let variableDeclaration = this.state.editValue.trim();
            if (DEFAULT_INPUT_VALUE !== variableDeclaration || variableDeclaration !== "") {
                let splitedExpression = variableDeclaration.split("=");
                let leftHandSideExpression = splitedExpression[0].trim();
                let rightHandSideExpression;
                if (splitedExpression.length > 1) {
                    rightHandSideExpression = splitedExpression[1].trim();
                }

                if (leftHandSideExpression.split(" ").length <= 1) {
                    let errorString = "Invalid variable declaration: " + variableDeclaration;
                    Alerts.error(errorString);
                    return false;
                }

                let bType = leftHandSideExpression.split(" ")[0];
                if (!this.validateType(bType)) {
                    let errorString = "Invalid type for a variable: " + bType;
                    Alerts.error(errorString);
                    return false;
                }

                let identifier = leftHandSideExpression.split(" ")[1];

                let defaultValue = "";
                if (rightHandSideExpression) {
                    defaultValue = rightHandSideExpression;
                }

                model.addAnnotationAttributeDefinition(bType, identifier, defaultValue);
                return true;
            } else {
                let errorString = "Please Enter Variable Declaration Before Adding";
                Alerts.error(errorString);
                return false;
            }
        } catch (e) {
            Alerts.error(e);
            return false;
        }
    }

    render() {
        let bBox = this.props.bBox;
        let editableBox_x = bBox.x + DesignerDefaults.panel.body.padding.left;
        let editableBox_y = bBox.y + DesignerDefaults.annotationAttributeDefinition.text.padding.top
            + DesignerDefaults.panel.heading.height
            + DesignerDefaults.panel.body.padding.top;
        let editBoxRect_x = bBox.x + DesignerDefaults.panel.body.padding.left;
        let editBoxRect_y = bBox.y + DesignerDefaults.panel.heading.height
            + DesignerDefaults.panel.body.padding.top;
        return (
            <g className="attribute-content-operations-wrapper">
                <g onClick={() => this.onClickVariableTextBox()}>
                    <rect x={editBoxRect_x}
                          y={editBoxRect_y}
                          width={DesignerDefaults.annotationAttributeDefinition.heading.width}
                          height={DesignerDefaults.annotationAttributeDefinition.heading.height}
                          className="annotation-input"/>
                    <EditableText x={editableBox_x}
                                  y={editableBox_y}
                                  width={DesignerDefaults.annotationAttributeDefinition.text.width}
                                  height={DesignerDefaults.annotationAttributeDefinition.text.height}
                                  labelClass={"annotation-input-placeholder"}
                                  inputClass={"annotation-input-text-box"}
                                  placeholder={DEFAULT_INPUT_VALUE}
                                  displayText={DEFAULT_INPUT_VALUE}
                                  onKeyDown={e => {
                                      this.onKeyDown(e);
                                  }}
                                  onBlur={e => {
                                      this.onInputBlur(e);
                                  }}
                                  onClick={() => {
                                      this.onClickVariableTextBox();
                                  }}
                                  editing={this.state.editing}
                                  onChange={e => {
                                      this.onInputChange(e);
                                  }}>
                        {this.state.editValue}
                    </EditableText>
                </g>
            </g>
        );
    }

    /**
     * Get types of ballerina to which can be applied when declaring variables.
     * */
    getTypeDropdownValues() {
        const {renderingContext} = this.context;
        let dropdownData = [];
        // Adding items to the type dropdown.
        let bTypes = renderingContext.environment.getTypes();
        _.forEach(bTypes, function (bType) {
            dropdownData.push({id: bType, text: bType});
        });
        return dropdownData;
    }

    /**
     * Validate type.
     * */
    validateType(bType) {
        let isValid = false;
        let typeList = this.getTypeDropdownValues();
        let filteredTypeList = _.filter(typeList, function (type) {
            return type.id === bType;
        });
        if (filteredTypeList.length > 0) {
            isValid = true;
        }
        return isValid;
    }
}

AnnotationAttributeDecorator.contextTypes = {
    renderingContext: PropTypes.instanceOf(Object).isRequired
};

export default AnnotationAttributeDecorator;
