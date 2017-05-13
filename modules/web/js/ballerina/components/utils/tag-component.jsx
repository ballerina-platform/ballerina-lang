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
import EditableText from './../editable-text';
import Alerts from 'alerts';

const DEFAULT_INPUT_VALUE = "Enter Value";

class TagController extends React.Component {

    constructor() {
        super();
        this.state = {editing: false, editValue: DEFAULT_INPUT_VALUE};
    }

    onTitleClick() {
        this.setState({editing: true, editValue: DEFAULT_INPUT_VALUE});
    }

    onTitleInputBlur(e) {
        console.log(this.props.model);
        let model = this.props.model;
        let setter = this.props.setter;
        if (DEFAULT_INPUT_VALUE !== this.state.editValue) {
            let splitedExpression = this.state.editValue.split(" ");
            let parameterDef = model.getFactory().createParameterDefinition();
            let bType = splitedExpression[0];
            if (this.validateType(bType)) {
                parameterDef.setTypeName(bType);
            } else {
                let errorString = "Incorrect Variable Type: " + bType;
                Alerts.error(errorString);
                e.preventDefault();
                return false;
            }

            if (splitedExpression[1]) {
                parameterDef.setName(splitedExpression[1]);
            } else {
                let errorString = "Invalid Variable Name.";
                Alerts.error(errorString);
                e.preventDefault();
                return false;
            }

            // model.getArgumentParameterDefinitionHolder().addChild(parameterDef);
            setter.addChild(parameterDef);
        }
        this.setState({editing: false, editValue: DEFAULT_INPUT_VALUE})
    }

    onTitleInputChange(e) {
        this.setState({editing: true, editValue: e.target.value.trim()});
        let variableDeclaration = e.target.value.replace("=", "") || e.target.value.replace(";", "");
        if (this.isValidateInput(variableDeclaration)) {
            this.setState({editing: true, editValue: variableDeclaration});
        }
    }

    /**
     * Get types of ballerina to which can be applied when declaring variables.
     * */
    getTypeDropdownValues() {
        let dropdownData = [];
        // Adding items to the type dropdown.
        // TODO: Add types to diagram context
        let bTypes = ["int", "string", "message"];
        _.forEach(bTypes, function (bType) {
            dropdownData.push({id: bType, text: bType});
        });

        let structTypes = [];
        _.forEach(structTypes, function (sType) {
            dropdownData.push({id: sType.getAnnotationName(), text: sType.getAnnotationName()});
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

    isValidateInput(input) {
        let splitedExpression = input.split(" ");
        return splitedExpression.length > 1;
    }

    render() {
        let modelComponents = this.props.modelComponents;
        let componentData = this.props.componentData;
        return (
            <g key={componentData.title}>
                <text x={componentData.components.openingBracket.x} y={componentData.components.openingBracket.y + 3}
                      className={componentData.openingBracketClassName}>(
                </text>
                <text x={componentData.components.titleText.x} y={componentData.components.titleText.y + 3}
                      className={componentData.prefixTextClassName}>{componentData.title}</text>
                {modelComponents}
                <text x={componentData.components.closingBracket.x + 110}
                      y={componentData.components.closingBracket.y + 3}
                      className={componentData.closingBracketClassName}>)
                </text>

                <g>
                    {this.props.tags}
                </g>
                <g>
                    <rect x={componentData.components.closingBracket.x + 10}
                          y={componentData.components.closingBracket.y + 3} width={90} height={18}
                          className="attribute-content-operations-wrapper"/>
                    <EditableText x={componentData.components.closingBracket.x + 10}
                                  y={componentData.components.closingBracket.y + 17}
                                  className="tag-component-editable-text-box"
                                  placeHolder={this.state.editValue}
                                  onBlur={e => {
                                      this.onTitleInputBlur(e)
                                  }}
                                  onClick={() => {
                                      this.onTitleClick()
                                  }}
                                  editing={this.state.editing}
                                  onChange={e => {
                                      this.onTitleInputChange(e)
                                  }}>
                    </EditableText>
                </g>
            </g>
        );
    }
}

export default TagController;