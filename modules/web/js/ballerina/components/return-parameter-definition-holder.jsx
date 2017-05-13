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

class ReturnParameterDefinitionHolder extends React.Component {

    constructor() {
        super();
        this.addReturnParameter = this.addReturnParameter.bind(this);
    }

    addReturnParameter(node) {
        this.props.model.addChild(node);
    }

    render() {
        let model = this.props.model;
        let componentData = {
            title: 'Return Types: ',
            components: {
                openingBracket: this.props.model.parent.getViewState().components.openingReturnType,
                titleText: this.props.model.parent.getViewState().components.returnTypesText,
                closingBracket: this.props.model.parent.getViewState().components.closingReturnType
            },
            openingBracketClassName: 'return-types-opening-brack-text',
            closingBracketClassName: 'return-types-closing-brack-text',
            prefixTextClassName: 'return-types-prefix-text',
        };
        let children = getComponentForNodeArray(model.getChildren());
        return (
            <TagController key={model.getID()} model={model} setter={this.addReturnParameter}
                           modelComponents={children} componentData={componentData}/>
        );
    }
}

export default ReturnParameterDefinitionHolder;
