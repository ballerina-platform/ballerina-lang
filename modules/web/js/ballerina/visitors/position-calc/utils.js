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

import _ from 'lodash';
import ASTFactory from './../../ast/ballerina-ast-factory';
import * as DesignerDefaults from './../../configs/designer-defaults';
import {panel} from './../../configs/designer-defaults';
import { util } from './../sizing-utils';

function getSimpleStatementPosition(node) {
    let viewState = node.getViewState();
    let bBox = viewState.bBox;
    const parent = node.getParent();
    const parentViewState = parent.getViewState();
    const parentStatementContainer = parentViewState.components.statementContainer || {};

    let parentStatements = parent.filterChildren(function (child) {
        return ASTFactory.isStatement(child) || ASTFactory.isExpression(child);
    });
    const currentIndex = _.findIndex(parentStatements, node);
    let x, y;

    /**
     * Here we center the statement based on the parent's statement container's dimensions
     * Always the statement container's width should be greater than the statements/expressions
     */
    if (parentStatementContainer.w < bBox.w) {
        throw 'Invalid statement container width found, statement width should be greater than or equal to ' +
        'statement/ statement width ';
    }
    x = parentStatementContainer.x + (parentStatementContainer.w - bBox.w)/2;
    if (currentIndex === 0) {
        y = parentStatementContainer.y;
    } else if (currentIndex > 0) {
        y = parentStatements[currentIndex - 1].getViewState().bBox.getBottom();
    } else {
        throw 'Invalid Index found for ' + node.getType();
    }

    bBox.x = x;
    bBox.y = y;
}

function getCompoundStatementChildPosition(node) {
    let viewState = node.getViewState();
    let bBox = viewState.bBox;
    const currentIndex = _.findIndex(node.getParent().getChildren(), node);
    /**
     * Current Index should be greater than 0
     */
    if (currentIndex <= 0) {
        throw 'Invalid Current Index Found for Block Statement';
    }
    const previousStatement = node.getParent().getChildren()[currentIndex - 1];
    let x, y, statementContainerX, statementContainerY;

    x = previousStatement.getViewState().bBox.x;
    y = previousStatement.getViewState().bBox.getBottom();
    statementContainerX = x;
    statementContainerY = y + DesignerDefaults.blockStatement.heading.height;

    bBox.x = x;
    bBox.y = y;
    viewState.components.statementContainer.x = statementContainerX;
    viewState.components.statementContainer.y = statementContainerY;
}



function populateInnerPanelDecoratorBBoxPosition(node){
    let parent = node.getParent();
    let viewSate = node.getViewState();
    let parentViewState = parent.getViewState();
    var parentBBox = parentViewState.bBox;
    let bBox = viewSate.bBox;
    let statementContainerBBox = viewSate.components.statementContainer;
    let headerBBox = viewSate.components.heading;
    let bodyBBox = viewSate.components.body;
    let annotation = viewSate.components.annotation;
    let resources = _.filter(parent.getChildren(), function (child) {
        return ASTFactory.isResourceDefinition(child) ||
                   ASTFactory.isConnectorAction(child);
    });
    let x, y, headerX, headerY, bodyX, bodyY;
    var currentResourceIndex = _.findIndex(resources, node);

    headerX = parentBBox.x + DesignerDefaults.panel.body.padding.left;

    if (currentResourceIndex === 0) {

        const serviceVariablesHeightGap = node.getParent().getViewState().components.variablesPane.h +
                DesignerDefaults.panel.body.padding.top;

        /**
         * If there are service level connectors, then we need to drop the first resource further down,
         * in order to maintain a gap between the connector heading and the resource heading
         */
        const parentLevelConnectors = node.getParent().filterChildren(function (child) {
            return ASTFactory.isConnectorDeclaration(child);
        });
        if (parentLevelConnectors.length > 0) {
            headerY = parentViewState.components.body.y + DesignerDefaults.panel.body.padding.top +
                    DesignerDefaults.lifeLine.head.height + DesignerDefaults.panel.wrapper.gutter.v;
        } else {
            headerY = parentViewState.components.body.y + DesignerDefaults.panel.body.padding.top;
        }

        headerY += serviceVariablesHeightGap;

    } else if (currentResourceIndex > 0) {
        let previousResourceBBox = resources[currentResourceIndex - 1].getViewState().bBox;
        headerY = previousResourceBBox.y + previousResourceBBox.h + DesignerDefaults.panel.wrapper.gutter.v;
    } else {
        throw 'Invalid Index for Resource Definition';
    }

    x = headerX;
    y = headerY;
    bodyX = headerX;
    bodyY = headerY + headerBBox.h + annotation.h;

    statementContainerBBox.x = bodyX + DesignerDefaults.innerPanel.body.padding.left;
    statementContainerBBox.y = bodyY + DesignerDefaults.innerPanel.body.padding.top +
            DesignerDefaults.lifeLine.head.height;

    bBox.x = x;
    bBox.y = y;
    headerBBox.x = headerX;
    headerBBox.y = headerY;
    bodyBBox.x = bodyX;
    bodyBBox.y = bodyY;
}

function populateOuterPanelDecoratorBBoxPosition(node){
    let viewSate = node.getViewState();
    let bBox = viewSate.bBox;
    let parent = node.getParent();
    let panelChildren = parent.filterChildren(function (child) {
        return ASTFactory.isFunctionDefinition(child) ||
                ASTFactory.isServiceDefinition(child) ||
                ASTFactory.isConnectorDefinition(child) ||
                ASTFactory.isAnnotationDefinition(child) ||
                ASTFactory.isStructDefinition(child) ||
                ASTFactory.isPackageDefinition(child);
    });
    let heading = viewSate.components.heading;
    let body = viewSate.components.body;
    let annotation = viewSate.components.annotation;
    let currentServiceIndex = _.findIndex(panelChildren, node);
    let x, y, headerX, headerY, bodyX, bodyY;
    if (currentServiceIndex === 0) {
        headerX = DesignerDefaults.panel.wrapper.gutter.h;
        headerY = DesignerDefaults.panel.wrapper.gutter.v;
    } else if (currentServiceIndex > 0) {
        let previousServiceBBox = panelChildren[currentServiceIndex - 1].getViewState().bBox;
        headerX = DesignerDefaults.panel.wrapper.gutter.h;
        headerY = previousServiceBBox.y + previousServiceBBox.h + DesignerDefaults.panel.wrapper.gutter.v;
    } else {
        throw 'Invalid Index for Service Definition';
    }

    x = headerX;
    y = headerY;
    bodyX = headerX;
    bodyY = headerY + heading.h + annotation.h;

    bBox.x = x;
    bBox.y = y;
    heading.x = headerX;
    heading.y = headerY;
    body.x = bodyX;
    body.y = bodyY;

    // here we need to re adjust resource width to match the service width.
    let resources = node.filterChildren(function (child) {
        return ASTFactory.isResourceDefinition(child) ||
                   ASTFactory.isConnectorAction(child);
    });
    // make sure you substract the panel padding to calculate the min width of a resource.
    let minWidth = node.getViewState().bBox.w - ( panel.body.padding.left + panel.body.padding.right );
    let connectorWidthTotal = 0;
    let connectors = node.filterChildren(function (child) {
        return ASTFactory.isConnectorDeclaration(child);
    });

    _.forEach(connectors, function (connector) {
        connectorWidthTotal += connector.getViewState().bBox.w + DesignerDefaults.lifeLine.gutter.h;
    });

    resources.forEach(function(element) {
        let viewState = element.getViewState();
        // if the service width is wider than resource width we will readjust.
        if(viewState.bBox.w + connectorWidthTotal < minWidth){
            viewState.bBox.w = minWidth - connectorWidthTotal;
        }
    }, this);
}

export {getSimpleStatementPosition, getCompoundStatementChildPosition, populateOuterPanelDecoratorBBoxPosition, populateInnerPanelDecoratorBBoxPosition};
