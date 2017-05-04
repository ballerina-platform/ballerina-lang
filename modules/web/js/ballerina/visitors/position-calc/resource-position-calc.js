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

import log from 'log';
import _ from 'lodash';
import * as DesignerDefaults from './../../configs/designer-defaults';
import AST from './../../ast/module';
import ASTFactory from './../../ast/ballerina-ast-factory';
import * as PositioningUtils from './utils';
import { util } from './../sizing-utils';

class ResourceDefinitionPositionCalcVisitor {

    canVisit(node) {
        log.debug('can visit ResourceDefinitionPositionCalc');
        return true;
    }

    beginVisit(node) {
        PositioningUtils.populateInnerPanelDecoratorBBoxPosition(node);
        // Setting positions of resource parameters.
        let viewSate = node.getViewState();
        viewSate.components['parametersPrefixContainer'].x = viewSate.bBox.x + viewSate.titleWidth;
        let nextXPositionOfParameter = node.getViewState().components['parametersPrefixContainer'].x + 
            viewSate.components['parametersPrefixContainer'].w;
        if (node.getParameters().length > 0) {
            for (let i = 0; i < node.getParameters().length; i++) {
                let resourceParameter = node.getParameters()[i];
                let viewState = resourceParameter.getViewState();
                if (i !== 0) {
                    nextXPositionOfParameter = nextXPositionOfParameter + 14;
                }

                viewState.x = nextXPositionOfParameter;
                nextXPositionOfParameter += util.getTextWidth(resourceParameter.getParameterAsString()).w;
            }
        }
    }

    visit(node) {
    }

    endVisit(node) {
    }
}

export default ResourceDefinitionPositionCalcVisitor;
