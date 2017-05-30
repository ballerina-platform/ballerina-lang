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
import BallerinaASTFactory from './../../ast/ballerina-ast-factory';
import ASTFactory from './../../ast/ballerina-ast-factory';
import {util} from './../sizing-utils';

class FunctionDefinitionDimensionCalculatorVisitor {

    canVisit(node) {
        log.info('can visit FunctionDefinitionDimensionCalc');
        return true;
    }

    beginVisit(node) {
        log.info('begin visit FunctionDefinitionDimensionCalc');
    }

    visit(node) {
        log.info('visit FunctionDefinitionDimensionCalc');
    }

    endVisit(node) {
        let viewState = node.getViewState();
        let components = {};

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        let annotationHeight = 0;

        /**
         * calculate the height of annotation view
         */
        let annotations = node.filterChildren(function (child) {
            return ASTFactory.isAnnotation(child);
        });

        _.forEach(annotations, function (annotation) {
            annotationHeight = annotationHeight + 25;
        });

        components['annotation'] = new SimpleBBox();


        if (node.viewState.annotationViewCollapsed) {
            components['annotation'].h = 25;
        } else {
            components['annotation'].h = annotationHeight;
        }

        components['statementContainer'] = new SimpleBBox();
        let statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);

        const statementContainerWidthPadding = DesignerDefaults.statementContainer.padding.left +
            DesignerDefaults.statementContainer.padding.right;

        let statementWidth = DesignerDefaults.statementContainer.width + statementContainerWidthPadding;
        let statementHeight = 0;

        _.forEach(statementChildren, function (child) {
            statementHeight += child.viewState.bBox.h;
            if ((child.viewState.bBox.w + statementContainerWidthPadding) > statementWidth) {
                statementWidth = child.viewState.bBox.w + statementContainerWidthPadding;
            }
        });

        /* Lets add an extra gap to the bottom of the lifeline.
         This will prevent last statement touching bottom box of the life line.*/
        statementHeight += DesignerDefaults.lifeLine.gutter.v;

        // If the statement container is two short we will set a minimum height.
        if (statementHeight < DesignerDefaults.statementContainer.height) {
            statementHeight = DesignerDefaults.statementContainer.height;
        }

        components['statementContainer'].h = statementHeight;
        components['statementContainer'].w = statementWidth;
        components['body'] = new SimpleBBox();

        let workerChildren = node.filterChildren(function (child) {
            return BallerinaASTFactory.isWorkerDeclaration(child);
        });

        let connectorChildren = node.filterChildren(function (child) {
            return BallerinaASTFactory.isConnectorDeclaration(child);
        });

        const highestStatementContainerHeight = util.getHighestStatementContainer(workerChildren);
        const workerLifeLineHeight = components['statementContainer'].h + DesignerDefaults.lifeLine.head.height * 2;
        const highestLifeLineHeight = highestStatementContainerHeight + DesignerDefaults.lifeLine.head.height * 2;

        let lifeLineWidth = 0;
        _.forEach(workerChildren.concat(connectorChildren), function (child) {
            lifeLineWidth += child.viewState.bBox.w + DesignerDefaults.lifeLine.gutter.h;
            child.getViewState().bBox.h = _.max([components['statementContainer'].h, highestStatementContainerHeight]) +
                DesignerDefaults.lifeLine.head.height * 2;
            child.getViewState().components.statementContainer.h = _.max([components['statementContainer'].h,
                highestStatementContainerHeight]);
        });

        //following is to handle node collapsed for panels.
        if (node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = _.max([workerLifeLineHeight, highestLifeLineHeight])
                + DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        }
        /**
         * If the current default worker's statement container height is less than the highest worker's
         * statement container. we set the default statement container height to the highest statement
         * container's height.
         */
        components['statementContainer'].h = _.max([components['statementContainer'].h,
            highestStatementContainerHeight]);

        components['body'].w = components['statementContainer'].w + DesignerDefaults.panel.body.padding.right +
            DesignerDefaults.panel.body.padding.left + lifeLineWidth;
        components['annotation'].w = components['body'].w;

        viewState.bBox.h = components['heading'].h + components['body'].h + components['annotation'].h;

        viewState.components = components;

        const textWidth = util.getTextWidth(node.getFunctionName());
        viewState.titleWidth = textWidth.w;
        viewState.trimmedTitle = textWidth.text;
        //// Creating components for parameters of the function
        // Creating component for opening bracket of the parameters view.
        viewState.components.openingParameter = {};
        viewState.components.openingParameter.w = util.getTextWidth('(', 0).w;

        // Creating component for closing bracket of the parameters view.
        viewState.components.closingParameter = {};
        viewState.components.closingParameter.w = util.getTextWidth(')', 0).w;


        //// Creating components for return types of the function
        // Creating component for the Return type text.
        viewState.components.returnTypesIcon = {};
        viewState.components.returnTypesIcon.w = util.getTextWidth('returns', 0).w;

        // Creating component for opening bracket of the return types view.
        viewState.components.openingReturnType = {};
        viewState.components.openingReturnType.w = util.getTextWidth('(', 0).w;

        // Creating component for closing bracket of the return types view.
        viewState.components.closingReturnType = {};
        viewState.components.closingReturnType.w = util.getTextWidth(')', 0).w;

        let componentWidth = components['heading'].w > components['body'].w
            ? components['heading'].w : components['body'].w;

        // Calculate function definition full width
        viewState.bBox.w = componentWidth
            + this.parameterTypeWidth(node) + this.returnTypeWidth(node)
            + viewState.titleWidth + 14
            + viewState.components.returnTypesIcon.w + viewState.components.closingReturnType.w
            + viewState.components.openingReturnType.w + viewState.components.closingParameter.w
            + viewState.components.openingParameter.w
            + (DesignerDefaults.panel.wrapper.gutter.h * 2) + 200;
    }

    /**
     * Calculate Parameters' text width for function definition.
     * @param {FunctionDefinition} node - Function Definition Node.
     * @return {number} width - return sum of widths of parameter texts.
     * */
    parameterTypeWidth(node) {
        let width = 0;
        if (node.getArguments().length > 0) {
            for (let i = 0; i < node.getArguments().length; i++) {
                width += util.getTextWidth(node.getArguments()[i].getParameterDefinitionAsString(), 0).w;
            }
        }

        return width;
    }

    /**
     * Calculate Return Parameters' text width for function definition.
     * @param {FunctionDefinition} node - Function Definition Node.
     * @return {number} width - return sum of widths of parameter texts.
     * */
    returnTypeWidth(node) {
        let width = 0;
        if (node.getReturnTypes().length > 0) {
            for (let i = 0; i < node.getReturnTypes().length; i++) {
                width += util.getTextWidth(node.getReturnTypes()[i].getParameterDefinitionAsString(), 0).w;
            }
        }
        return width;
    }
}

export default FunctionDefinitionDimensionCalculatorVisitor;
