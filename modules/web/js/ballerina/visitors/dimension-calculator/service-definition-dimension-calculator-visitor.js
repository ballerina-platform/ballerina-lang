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
import SimpleBBox from './../../ast/simple-bounding-box';
import ASTFactory from './../../ast/ballerina-ast-factory';

class ServiceDefinitionDimensionCalculatorVisitor {

    canVisit(node) {
        log.debug('can visit ServiceDefinitionDimensionCalc');
        return true;
    }

    beginVisit(node) {

        log.debug('begin visit ServiceDefinitionDimensionCalc');
    }

    visit(node) {
        log.debug('visit ServiceDefinitionDimensionCalc');
    }

    endVisit(node) {
        let viewState = node.getViewState();
        let components = {};
        let totalResourceHeight = 0;
        let connectorStatementContainerHeight = 0;
        let resources = node.filterChildren(function (child) {
            return ASTFactory.isResourceDefinition(child);
        });
        let connectors = node.filterChildren(function (child) {
            return ASTFactory.isConnectorDeclaration(child)
        });
        let maxResourceWidth = 0;
        //Initial statement height include panel heading and panel padding.
        let bodyHeight = DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        // Set the width initial value to the padding left and right
        var bodyWidth = DesignerDefaults.panel.body.padding.left + DesignerDefaults.panel.body.padding.right;

        /**
         * If there are service level connectors, their height depends on the heights of the resources
         */
        _.forEach(resources, function (resource) {
            totalResourceHeight += resource.getViewState().bBox.h;
            if (maxResourceWidth < resource.getViewState().bBox.w) {
                maxResourceWidth = resource.getViewState().bBox.w;
            }
        });

        /**
         * Set the max resource width to the resources
         */
        _.forEach(resources, function (resource) {
            resource.getViewState().bBox.w = maxResourceWidth;
            resource.getViewState().components.body.w = maxResourceWidth;
            resource.getViewState().components.heading.w = maxResourceWidth;
        });

        // Add the max resource width to the body width
        bodyWidth += maxResourceWidth;

        /**
         * Set the connector statement container height and the connectors' height accordingly only if there are service
         * level connectors
         */
        if (connectors.length > 0) {
            if (totalResourceHeight <= 0) {
                // There are no resources in the service
                connectorStatementContainerHeight = DesignerDefaults.statementContainer.height;
            } else {
                // Here we add additional gutter height to balance the gaps from top and bottom
                connectorStatementContainerHeight = totalResourceHeight +
                    DesignerDefaults.panel.wrapper.gutter.v * (resources.length + 1);
            }
            /**
             * Adjust the height of the connectors and adjust the service's body width with the connector widths
             */
            _.forEach(connectors, function (connector) {
                connector.getViewState().bBox.h = connectorStatementContainerHeight +
                    DesignerDefaults.lifeLine.head.height * 2;
                connector.getViewState().components.statementContainer.h = connectorStatementContainerHeight;
                bodyWidth += (connector.getViewState().components.statementContainer.w + DesignerDefaults.lifeLine.gutter.h);
            });

            bodyHeight = connectorStatementContainerHeight + DesignerDefaults.lifeLine.head.height * 2 +
                DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        } else if (totalResourceHeight > 0) {
            bodyHeight = totalResourceHeight + DesignerDefaults.panel.body.padding.top +
                DesignerDefaults.panel.body.padding.bottom + DesignerDefaults.panel.wrapper.gutter.v * (resources.length - 1);
        } else {
            // There are no connectors as well as resources, since we set the default height
            bodyHeight = DesignerDefaults.innerPanel.body.height;
        }

        components['heading'] = new SimpleBBox();
        components['body'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;
        if(node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = bodyHeight;
        }
        components['body'].w = bodyWidth;
        components['heading'].w = bodyWidth;

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['body'].w;

        viewState.components = components;
    }
}

export default ServiceDefinitionDimensionCalculatorVisitor;
