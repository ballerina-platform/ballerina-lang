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
import { Log as log } from '@ballerina-lang/composer-core';
import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import TagController from './../decorators/tag-component';
import FragmentUtils from './../../../../../utils/fragment-utils';
import TreeBuilder from './../../../../../model/tree-builder';
/**
 * Component class for ReturnParameterDefinitionHolder.
 * */
class ReturnParameterDefinitionHolder extends React.Component {

    /**
     * constructor for return parameter definition holder.
     * */
    constructor() {
        super();
        this.addReturnParameter = this.addReturnParameter.bind(this);
    }

    /**
     * Setter to add return parameters.
     * @param {string} input - input from tag-controller.
     * @return {boolean} true||false
     * */
    addReturnParameter(input) {
        if (input !== '') {
            input = input.replace(';', '');
            const fragment = FragmentUtils.createReturnParameterFragment(input);
            const parsedJson = FragmentUtils.parseFragment(fragment);

            if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
                if (_.isEqual(parsedJson.kind, 'Variable')) {
                    const returnParam = TreeBuilder.build(parsedJson);
                    let index = this.props.model.getReturnParameters().length - 1;

                    // If there are no arguments index is -1. Then the argument is added in the first position
                    if (index === -1) {
                        index = 0;
                    }
                    this.props.model.addReturnParameters(returnParam, index + 1);
                } else {
                    log.error('Error while parsing parameter. Error response' + JSON.stringify(parsedJson));
                }
            } else {
                log.error('Error while parsing parameter. Error response' + JSON.stringify(parsedJson));
            }
        }
    }

    /**
     * Validate input from controller and apply condition to tell whether to change the state.
     * @param {string} input
     * @return {boolean} true - change the state, false - don't change the state
     * */
    validateInput(input) {
        const splitedExpression = input.split(' ');
        return splitedExpression.length > 0;
    }

    render() {
        const parent = this.props.model;
        const componentData = {
            title: 'Return Types: ',
            components: {
                openingBracket: parent.viewState.components.returnParameterHolder.openingReturnType,
                typesIcon: parent.viewState.components.returnParameterHolder.returnTypesIcon,
                closingBracket: parent.viewState.components.returnParameterHolder.closingReturnType,
            },
            openingBracketClassName: 'return-types-opening-brack-text',
            closingBracketClassName: 'return-types-closing-brack-text',
            prefixTextClassName: 'return-types-prefix-text',
            defaultText: '+ Add Returns',
        };
        const children = this.props.children;
        this.editorOptions = {
            propertyType: 'text',
            key: 'ParameterDefinition',
            model: parent,
            getterMethod: () => {
                return '';
            },
            setterMethod: this.addReturnParameter,
            fontSize: 12,
            isCustomHeight: true,
        };
        return (
            <TagController
                key={parent.getID()}
                model={parent}
                setter={this.addReturnParameter}
                validateInput={this.validateInput}
                modelComponents={children}
                componentData={componentData}
                editorOptions={this.editorOptions}
                groupClass='return-parameter-group'
            />
        );
    }
}

ReturnParameterDefinitionHolder.propsTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

ReturnParameterDefinitionHolder.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default ReturnParameterDefinitionHolder;
