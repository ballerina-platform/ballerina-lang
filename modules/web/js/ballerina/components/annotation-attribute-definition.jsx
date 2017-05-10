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
import ImageUtil from './image-util';
import _ from 'lodash';
import Renderer from './renderer';
import PropTypes from 'prop-types';
import Alerts from 'alerts';
import './annotation-definition.css';

class AnnotationAttributeDefinition extends React.Component {
    constructor(props) {
        super(props);
        this.model = this.props.model;
        this.bBox = this.props.model.viewState.bBox;
        this.state = {inputValue: this.props.model.getAttributeStatementString()};
        this.setAnnotationAttributeDefinition = this.setAnnotationAttributeDefinition.bind(this);
    }

    /**
     * Delete specified attribute.
     * */
    deleteAttribute() {
        this.model.remove();
    }

    /**q
     * Render Edit mode for attribute definition.
     * */
    renderEditAttribute() {
        let model = this.props.model;
        let bBox = model.viewState.bBox;

        let textBoxBBox = {
            x: bBox.x,
            y: bBox.y,
            w: bBox.w,
            h: bBox.h
        };

        const options = {
            bBox: textBoxBBox,
            onChange: this.setAnnotationAttributeDefinition,
            initialValue: model.getAttributeStatementString()
        };

        this.context.renderer.renderTextBox(options);
    }

    /**
     * Set attribute annotation definition.
     * @param {string} input value from the text box.
     * */
    setAnnotationAttributeDefinition(input) {
        let model = this.props.model;
        try {
            let variableDeclaration = input.trim();
            if (variableDeclaration && variableDeclaration !== "") {
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
                model.setAttributeName(identifier);

                let defaultValue = "";
                if (rightHandSideExpression) {
                    defaultValue = rightHandSideExpression;
                }

                model.setAttributeType(bType);
                model.setAttributeValue(defaultValue);

                this.setState({
                    inputValue: this.props.model.getAttributeStatementString()
                });
            } else {
                let errorString = "Annotation Attribute Cannot be Empty";
                Alerts.error(errorString);
                return false;
            }
        } catch (e) {
            Alerts.error(e);
        }
    }

    render() {
        return (
            <g className="attribute-content-operations-wrapper">
                <g onClick={() => this.renderEditAttribute()}>
                    <rect x={this.bBox.x} y={this.bBox.y} width={this.bBox.w} height={this.bBox.h}
                          className="attribute-content-operations-wrapper"/>
                    <text x={this.bBox.x + 10} y={this.bBox.y + 20} width={this.bBox.w} height={this.bBox.h}>
                        {this.props.model.viewState.textLength.text}
                        <title>{this.props.model.getAttributeStatementString()}</title>
                    </text>
                </g>
                <g onClick={() => this.deleteAttribute()}>
                    <rect x={this.bBox.x + this.bBox.w + 10} y={this.bBox.y} width={30} height={30} className=""/>
                    <image x={this.bBox.x + this.bBox.w + 15} y={this.bBox.y + 5} width={20} height={20}
                           className="delete-button-icon" xlinkHref={ImageUtil.getSVGIconString('delete-red')}>
                        <title>Remove</title>
                    </image>
                </g>
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
}

AnnotationAttributeDefinition.contextTypes = {
    renderer: PropTypes.instanceOf(Renderer).isRequired,
};

export default AnnotationAttributeDefinition;
