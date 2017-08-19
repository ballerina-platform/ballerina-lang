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
import PropTypes from 'prop-types';
import Alerts from 'alerts';
import './annotation-definition.css';
import { util } from './../visitors/sizing-utils';
import ExpressionEditor from '../../expression-editor/expression-editor-utils';
import SimpleBBox from "../ast/simple-bounding-box";

class AnnotationAttributeDefinition extends React.Component {
    /**
     * calculate the component BBox.
     * @param {object} props - props.
     * @return {SimpleBBox} bBox.
     * */
    static calculateComponentBBox(props) {
        const bBox = props.model.viewState.bBox;
        return new SimpleBBox(bBox.x,
            bBox.y + 1, bBox.w, bBox.h - 1);
    }

    constructor(props) {
        super(props);
        this.model = this.props.model;
        this.bBox = this.props.model.viewState.bBox;
        this.state = {
            inputValue: this.props.model.getAttributeStatementString().replace(';', ''),
            editing: false,
            editValue: this.props.model.getAttributeStatementString().replace(';', ''),
            inputBox: AnnotationAttributeDefinition.calculateComponentBBox(props),
        };
        this.setAnnotationAttributeDefinition = this.setAnnotationAttributeDefinition.bind(this);
        this.getStatementString = this.getStatementString.bind(this);
    }

    componentWillReceiveProps(props) {
        this.setState({
            inputBox: AnnotationAttributeDefinition.calculateComponentBBox(props),
        });
    }

    getStatementString() {
        return this.props.model.getAttributeStatementString().replace(';', '');
    }

    /** q
     * Render Edit mode for attribute definition.
     * */
    onClickVariableTextBox() {
        this.editorOptions = {
            propertyType: 'text',
            key: 'VariableDefinition',
            model: this.props.model,
            getterMethod: this.getStatementString,
            setterMethod: this.setAnnotationAttributeDefinition,
            fontSize: 12,
            isCustomHeight: true,
        };
        const options = this.editorOptions;
        const packageScope = this.context.environment;
        if (options) {
            new ExpressionEditor(this.state.inputBox,
                text => this.onUpdate(text), options, packageScope).render(this.context.getOverlayContainer());
        }
        this.setState({editing: true, editValue: this.props.model.getAttributeStatementString().replace(';', '')});
    }

    onUpdate(text) {
        // TODO: Implement the on update.
    }

    onInputBlur(e) {
        this.setState({editing: false, editValue: this.props.model.getAttributeStatementString().replace(';', '')});
    }

    onInputChange(e) {
        const variableDeclaration = e.target.value.replace(';', '');
        if (variableDeclaration !== '') {
            if (this.validateAttribute(variableDeclaration)) {
                if (!this.setAnnotationAttributeDefinition(variableDeclaration)) {
                    e.preventDefault();
                }
            }
        }
        this.setState({ editing: true, editValue: variableDeclaration });
    }

    onKeyDown(e) {
        if (e.keyCode === 13) {
            if (this.state.editValue !== '') {
                if (this.validateAttribute(this.state.editValue)) {
                    if (!this.setAnnotationAttributeDefinition(this.state.editValue)) {
                        e.preventDefault();
                    }
                }
            }
            this.setState({
                editing: false, editValue: this.props.model.getAttributeStatementString().replace(';', '')
            });
        }
    }

    /**
     * Get types of ballerina to which can be applied when declaring variables.
     * */
    getTypeDropdownValues() {
        const {environment} = this.context;
        const dropdownData = [];
        // Adding items to the type dropdown.
        const bTypes = environment.getTypes();
        _.forEach(bTypes, (bType) => {
            dropdownData.push({ id: bType, text: bType });
        });

        return dropdownData;
    }

    /**
     * Set attribute annotation definition.
     * @param {string} input value from the text box.
     * */
    setAnnotationAttributeDefinition(input) {
        const model = this.props.model;
        if (input !== '') {
            try {
                const variableDeclaration = input.trim();
                if (variableDeclaration && variableDeclaration !== '') {
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
                    model.setAttributeName(identifier);

                    let defaultValue = '';
                    if (rightHandSideExpression) {
                        defaultValue = rightHandSideExpression;
                    }

                    model.setAttributeType(bType);
                    model.setAttributeValue(defaultValue);

                    this.setState({
                        inputValue: this.props.model.getAttributeStatementString().replace(';', ''),
                    });
                } else {
                    const errorString = 'Annotation Attribute Cannot be Empty';
                    Alerts.error(errorString);
                    return false;
                }
            } catch (e) {
                Alerts.error(e);
            }
        }
    }

    /**
     * Delete specified attribute.
     * */
    deleteAttribute() {
        this.model.remove();
    }

    validateAttribute(attribute) {
        if (attribute.includes('=')) {
            const splitedExpression = attribute.split('=');
            const leftSideSplitted = splitedExpression[0].split(' ');
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
        return (
            <g className="attribute-content-operations-wrapper">
                <g onClick={() => this.onClickVariableTextBox()}>
                    <rect
                        x={this.bBox.x} y={this.bBox.y} width={this.bBox.w + 30} height={this.bBox.h}
                        className="annotation-attribute-wrapper "
                    />
                    <text
                        x={this.bBox.x + 4}
                        y={this.bBox.y + 18}
                        className={"annotation-attribute-wrapper-text"}
                    >
                        {util.getTextWidth(this.props.model.getAttributeStatementString().replace(';', '')).text}
                    </text>
                </g>
                <g onClick={() => this.deleteAttribute()}>
                    <rect
                        x={this.bBox.x + this.bBox.w} y={this.bBox.y} width={30} height={30}
                        className="annotation-delete-wrapper"
                    />
                    <image
                        x={this.bBox.x + this.bBox.w + 5} y={this.bBox.y + 7} width={20} height={20}
                        className="delete-button-icon" xlinkHref={ImageUtil.getSVGIconString('cancel')}
                    >
                        <title>Remove</title>
                    </image>
                </g>
            </g>
        );
    }
}

AnnotationAttributeDefinition.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default AnnotationAttributeDefinition;
