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
import * as DesignerDefaults from './../../configs/designer-defaults';
import { util } from './../sizing-utils';

class ReturnParameterDefinitionHolderDimensionCalculatorVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
    }

    visit(node) {
    }

    endVisit(node) {
        // Creating component for text.
        for (let i = 0; i < node.getChildren().length; i++) {
            const parameterDefinition = node.getChildren()[i];
            const viewState = parameterDefinition.getViewState();

            viewState.w = util.getTextWidth(parameterDefinition.getParameterDefinitionAsString(), 0).w;
            viewState.h = DesignerDefaults.panelHeading.heading.height - 7;

            // Creating component for delete icon.
            viewState.components.deleteIcon = {};
            viewState.components.deleteIcon.w = DesignerDefaults.panelHeading.heading.height - 7;
            viewState.components.deleteIcon.h = DesignerDefaults.panelHeading.heading.height - 7;
        }
    }
}

export default ReturnParameterDefinitionHolderDimensionCalculatorVisitor;
