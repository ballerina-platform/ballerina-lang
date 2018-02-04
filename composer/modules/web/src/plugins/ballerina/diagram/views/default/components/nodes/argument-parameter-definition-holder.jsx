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
import FragmentUtils from './../../../../../utils/fragment-utils';
import TagController from './../decorators/tag-component';
import TreeBuilder from './../../../../../model/tree-builder';
/**
 * Component class for ArgumentParameterDefinitionHolder.
 *
 * @class ArgumentParameterDefinitionHolder
 * @extends {React.Component}
 */
class ArgumentParameterDefinitionHolder extends React.Component {

    /**
     * Creates an instance of ArgumentParameterDefinitionHolder.
     *
     * @memberof ArgumentParameterDefinitionHolder
     */
    constructor() {
        super();
        this.addArgumentParameter = this.addArgumentParameter.bind(this);
    }

    /**
     * Setter to add argument parameters.
     * @param {string} input - input from tag-controller.
     * @return {boolean} true|false
     * */
    addArgumentParameter(input) {
        if (input !== '') {
            input = input.replace(/;$/, '');
            const fragment = FragmentUtils.createArgumentParameterFragment(input);
            const parsedJson = FragmentUtils.parseFragment(fragment);

            if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
                if (_.isEqual(parsedJson.kind, 'Variable')) {
                    const argumentParam = TreeBuilder.build(parsedJson);
                    let index = this.props.model.getParameters().length - 1;

                    // If there are no arguments index is -1. Then the argument is added in the first position
                    if (index === -1) {
                        index = 0;
                    }
                    this.props.model.addParameters(argumentParam, index + 1);
                } else {
                    this.context.alert.showError('Invalid content provided');
                }
            } else {
                this.context.alert.showError('Invalid content provided');
            }
        }
    }

    /**
     * Validate input from controller and apply condition to tell whether to change the state.
     * @param {string} input Input value.
     * @return {boolean} true - change the state, false - don't change the state
     * */
    validateInput(input) {
        const splitedExpression = input.split(' ');
        return splitedExpression.length > 1;
    }

    /**
     * Renders the view for argument parameter definition holder.
     *
     * @returns {ReactElement} The view.
     *
     * @memberof ArgumentParameterDefinitionHolder
     */
    render() {
        const parent = this.props.model;
        const componentData = {
            title: 'Parameters: ',
            components: {
                openingBracket: parent.viewState.components.argParameterHolder.openingParameter,
                closingBracket: parent.viewState.components.argParameterHolder.closingParameter,
            },
            prefixView: parent.viewState.components.parametersPrefixContainer,
            openingBracketClassName: 'parameter-bracket-text',
            closingBracketClassName: 'parameter-bracket-text',
            prefixTextClassName: 'parameter-prefix-text',
            defaultText: '+ Add Param',
        };
        const children = this.props.children;
        this.editorOptions = {
            propertyType: 'text',
            key: 'ParameterDefinition',
            model: parent,
            getterMethod: () => {
                return '';
            },
            setterMethod: this.addArgumentParameter,
            fontSize: 12,
            isCustomHeight: true,
        };
        return (
            <TagController
                key={parent.getID()}
                model={parent}
                setter={this.addArgumentParameter}
                validateInput={this.validateInput}
                modelComponents={children}
                componentData={componentData}
                editorOptions={this.editorOptions}
                groupClass='argument-parameter-group'
            />
        );
    }
}

ArgumentParameterDefinitionHolder.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    alert: PropTypes.shape({
        showInfo: PropTypes.func,
        showSuccess: PropTypes.func,
        showWarning: PropTypes.func,
        showError: PropTypes.func,
        closeEditor: PropTypes.func,
    }).isRequired,
};

export default ArgumentParameterDefinitionHolder;
