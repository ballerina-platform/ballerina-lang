/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import {statement} from './../configs/designer-defaults';
import {blockStatement} from './../configs/designer-defaults';
import BallerinaASTFactory from './../ast/ballerina-ast-factory'
import SimpleBBox from './../ast/simple-bounding-box';
import * as DesignerDefaults from './../configs/designer-defaults';
import _ from 'lodash';


class SizingUtil {
    constructor(){
        var svg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
        svg.setAttribute('style', 'border: 1px solid black');
        svg.setAttribute('width', '600');
        svg.setAttribute('height', '250');
        svg.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns:xlink", "http://www.w3.org/1999/xlink");
        this.textElement = document.createElementNS("http://www.w3.org/2000/svg", 'text');
        svg.appendChild(this.textElement);
        document.body.appendChild(svg); 
    }

    getTextWidth(text){
        this.textElement.innerHTML = _.escape(text);
        let width = statement.padding.left + this.textElement.getComputedTextLength() + statement.padding.right;
        // if the width is more then max width crop the text
        if(width <= statement.width){
            //set the width to minimam width
            width = statement.width;        
        }else if(width > statement.width && width <= statement.maxWidth){
            // do nothing
        }else {
            // We need to truncate displayText and show an ellipses at the end.
            var ellipses = '...';
            var possibleCharactersCount = 0;
            for (var i = (text.length - 1); 1 < i; i--) {
                if ((statement.padding.left + this.textElement.getSubStringLength(0, i) + statement.padding.right ) < statement.maxWidth) {
                    possibleCharactersCount = i;
                    break;
                }
            }
            // We need room for the ellipses as well, hence removing 'ellipses.length' no. of characters.
            text = text.substring(0, (possibleCharactersCount - ellipses.length)) + ellipses; // Appending ellipses.

            width = statement.maxWidth;
        }
        return {
            w: width,
            text :text
        }        
    }

    populateSimpleStatementBBox(expression, viewState){
        var textViewState = util.getTextWidth(expression);
        viewState.bBox.w = textViewState.w;
        viewState.bBox.h = statement.height + statement.gutter.v;
        viewState.expression = textViewState.text;
        return viewState;
    }

    getHighestStatementContainer(workers) {
        const sortedWorkers = _.sortBy(workers, function (worker) {
            return worker.viewState.components.statementContainer.h;
        });
        return sortedWorkers.length > 0 ? sortedWorkers[sortedWorkers.length - 1].getViewState().components.statementContainer.h : -1;
    }

    populateCompoundStatementChild(node) {
        let viewState = node.getViewState();
        let components = {};

        components['statementContainer'] = new SimpleBBox();
        var statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        var statementContainerWidth = 0;
        var statementContainerHeight = 0;

        _.forEach(statementChildren, function(child) {
            statementContainerHeight += child.viewState.bBox.h;
            if(child.viewState.bBox.w > statementContainerWidth){
                statementContainerWidth = child.viewState.bBox.w;
            }
        });

        /**
         * Add the left padding and right padding for the statement container and
         * add the additional gutter height to the statement container height, in order to keep the gap between the
         * last statement and the block statement bottom margin
         */
        statementContainerHeight += (statementContainerHeight > 0 ? statement.gutter.v :
        blockStatement.body.height - blockStatement.heading.height);

        statementContainerWidth += (statementContainerWidth > 0 ?
            (blockStatement.body.padding.left + blockStatement.body.padding.right) : blockStatement.width);

        components['statementContainer'].h = statementContainerHeight;
        components['statementContainer'].w = statementContainerWidth;

        viewState.bBox.h = statementContainerHeight + blockStatement.heading.height;
        viewState.bBox.w = statementContainerWidth;

        viewState.components = components;
    }

    populatePanelDecoratorBBox(node){
        var viewState = node.getViewState();
        var components = {};

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        components['statementContainer'] = new SimpleBBox();
        var statementChildren = node.filterChildren(BallerinaASTFactory.isStatement);
        var statementWidth = 0;
        var statementHeight = 0;

        _.forEach(statementChildren, function(child) {
            statementHeight += child.viewState.bBox.h;
            if(child.viewState.bBox.w > statementWidth){
                statementWidth = child.viewState.bBox.w;
            }
        });

        //if statement width is 0 set default
        if(statementWidth == 0){
            statementWidth = DesignerDefaults.statement.width;
        }

        /* Lets add an extra gap to the bottom of the lifeline. 
           This will prevent last statement touching bottom box of the life line.*/
        statementHeight += DesignerDefaults.lifeLine.gutter.v;

        // If the lifeline is two short we will set a minimum height.
        if(statementHeight < DesignerDefaults.lifeLine.line.height){
            statementHeight = DesignerDefaults.lifeLine.line.height;
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

        var lifeLineWidth = 0;
        _.forEach(workerChildren.concat(connectorChildren), function(child) {
            lifeLineWidth += child.viewState.bBox.w + DesignerDefaults.lifeLine.gutter.h;
            child.getViewState().bBox.h = _.max([components['statementContainer'].h, highestStatementContainerHeight]) +
                DesignerDefaults.lifeLine.head.height * 2;
            child.getViewState().components.statementContainer.h = _.max([components['statementContainer'].h,
                highestStatementContainerHeight]);
        });

        //following is to handle node collapsed for panels.
        if(node.viewState.collapsed) {
            components['body'].h = 0;
        } else {
            components['body'].h = _.max([workerLifeLineHeight, highestLifeLineHeight])
                                   + DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        }
        /**
         * If the current default worker's statement container height is less than the highest worker's statement container
         * we set the default statement container height to the highest statement container's height
         */
        components['statementContainer'].h = _.max([components['statementContainer'].h, highestStatementContainerHeight]);

        components['body'].w = components['statementContainer'].w + DesignerDefaults.panel.body.padding.right +
            DesignerDefaults.panel.body.padding.left + lifeLineWidth;

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['heading'].w + components['body'].w;

        viewState.components = components;
    }
}


export let util = new SizingUtil();