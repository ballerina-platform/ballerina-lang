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
import TagController from './utils/tag-component';
import {getComponentForNodeArray} from './utils';

class ArgumentParameterDefinitionHolder extends React.Component {

    constructor() {
        super();
        this.addParameter = this.addParameter.bind(this);
    }

    addParameter(node) {
        this.props.model.addChild(node);
    }

    render() {
        let model = this.props.model;
        let componentData = {
            title: 'Parameters: ',
            components: {
                openingBracket: this.props.model.parent.getViewState().components.openingParameter,
                titleText: this.props.model.parent.getViewState().components.parametersText,
                closingBracket: this.props.model.parent.getViewState().components.closingParameter
            },
            prefixView: this.props.model.parent.getViewState().components.parametersPrefixContainer,
            openingBracketClassName: 'parameter-opening-brack-text',
            closingBracketClassName: 'parameter-closing-brack-text',
            prefixTextClassName: 'parameter-prefix-text'
        };
        let children = getComponentForNodeArray(model.getChildren());
        return (
            <TagController key={model.getID()} model={model} setter={this.addParameter}
                           modelComponents={children} componentData={componentData}/>
        );
    }
}

export default ArgumentParameterDefinitionHolder;
