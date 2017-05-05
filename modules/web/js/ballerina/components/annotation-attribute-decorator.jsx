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
import './annotation-attribute-decorator.css';
import ImageUtil from './image-util';
import Alerts from 'alerts';
import Renderer from './renderer';
import PropTypes from 'prop-types';

/**
 * Annotation Attribute Decorator
 * */
class AnnotationAttributeDecorator extends React.Component {

    constructor(props) {
        super(props);
        this.model = this.props.model;
        this.state = {inputValue: "Enter Variable"};
        this.setAnnotationAttributeFromInputBox = this.setAnnotationAttributeFromInputBox.bind(this);
    }

    onClickVariableTextBox() {
        let model = this.props.model;
        let bBox = model.viewState.bBox;

        let textBoxBBox = {
            x: bBox.x + 50,
            y: bBox.y + 50,
            w: 300,
            h: 30
        };

        const options = {
            bBox: textBoxBBox,
            onChange: this.setAnnotationAttributeFromInputBox,
            initialValue: ""
        }

        this.context.renderer.renderTextBox(options);
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
            let variableDeclaration = this.state.inputValue.trim();
            let splitedExpression = variableDeclaration.split("=");
            let leftHandSideExpression = splitedExpression[0].trim();
            let rightHandSideExpression;
            if (splitedExpression.length > 1) {
                rightHandSideExpression = splitedExpression[1].trim();
            }

            if (leftHandSideExpression.split(" ").length <= 1) {
                let errorString = "Invalid variable declaration: " + variableDeclaration;
                Alerts.error(errorString);
                e.stopPropagation();
                return false;
            }

            let bType = leftHandSideExpression.split(" ")[0];
            if (!this.validateType(bType)) {
                let errorString = "Invalid type for a variable: " + bType;
                Alerts.error(errorString);
                e.stopPropagation();
                return false;
            }

            let identifier = leftHandSideExpression.split(" ")[1];

            let defaultValue = "";
            if (rightHandSideExpression) {
                defaultValue = rightHandSideExpression;
            }

            model.addAnnotationAttributeDefinition(bType, identifier, defaultValue);
            this.setState({inputValue: "Enter Variable"});
        } catch (e) {
            Alerts.error(e);
        }
    }

    render() {
        let bBox = this.props.bBox;
        return (
            <g className="attribute-content-operations-wrapper">
                <rect x={bBox.x + 50} y={bBox.y + 50} width={300} height={30}
                      className="attribute-content-operations-wrapper"/>
                <rect x={bBox.x + 350 + 10} y={bBox.y + 50} width={30} height={30} className=""
                      onClick={() => this.addAnnotationAttribute()}/>

                <text x={bBox.x + 60} y={bBox.y + 70} width={300} height={30}
                      onClick={() => this.onClickVariableTextBox()}>
                    {this.state.inputValue}
                </text>
                <image x={bBox.x + 350 + 15} y={bBox.y + 55} width={20} height={20}
                       xlinkHref={ImageUtil.getSVGIconString('add')}/>
            </g>
        );
    }

    /**
     * Get types of ballerina to which can be applied when declaring variables.
     * */
    getTypeDropdownValues() {
        let dropdownData = [];
        // Adding items to the type dropdown.
        // TODO: Add types to diagram context
        let bTypes = ["int", "string"];
        _.forEach(bTypes, function (bType) {
            dropdownData.push({id: bType, text: bType});
        });

        var structTypes = [];
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
}

AnnotationAttributeDecorator.contextTypes = {
    renderer: PropTypes.instanceOf(Renderer).isRequired,
};

export default AnnotationAttributeDecorator;
