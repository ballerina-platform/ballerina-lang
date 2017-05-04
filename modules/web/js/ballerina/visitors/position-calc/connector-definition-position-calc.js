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
import ASTFactory from './../../ast/ballerina-ast-factory';
import {panel} from './../../configs/designer-defaults';

class ConnectorDefinitionPositionCalcVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
        let viewSate = node.getViewState();
        let bBox = viewSate.bBox;
        let parent = node.getParent();
        let panelChildren = parent.filterChildren(function (child) {
            return ASTFactory.isFunctionDefinition(child) ||
                ASTFactory.isServiceDefinition(child) || ASTFactory.isConnectorDefinition(child);
        });
        let heading = viewSate.components.heading;
        let body = viewSate.components.body;
        let currentConnectorIndex = _.findIndex(panelChildren, node);
        let x, y, headerX, headerY, bodyX, bodyY;
        if (currentConnectorIndex === 0) {
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = DesignerDefaults.panel.wrapper.gutter.v;
        } else if (currentConnectorIndex > 0) {
            let previousConnectorBBox = panelChildren[currentConnectorIndex - 1].getViewState().bBox;
            headerX = DesignerDefaults.panel.wrapper.gutter.h;
            headerY = previousConnectorBBox.y + previousConnectorBBox.h + DesignerDefaults.panel.wrapper.gutter.v;
        } else {
            throw 'Invalid Index for Connector Definition';
        }

        x = headerX;
        y = headerY;
        bodyX = headerX;
        bodyY = headerY + heading.h;

        bBox.x = x;
        bBox.y = y;
        heading.x = headerX;
        heading.y = headerY;
        body.x = bodyX;
        body.y = bodyY;

        // here we need to re adjest action width to match the connector width.
        let children = node.getChildren();
        // make sure you substract the panel padding to calculate the min width of a resource.
        let minWidth = node.getViewState().bBox.w - ( panel.body.padding.left + panel.body.padding.right );
        children.forEach(function(element) {
            //@todo take connectors in to account.
            let viewState = element.getViewState();
            // if the connector width is wider than action width we will readjest. 
            if(viewState.bBox.w < minWidth){
                viewState.bBox.w = minWidth;
            }
        }, this);       
    }

    visit(node) {
    }

    endVisit(node) {    
    }
}

export default ConnectorDefinitionPositionCalcVisitor;
