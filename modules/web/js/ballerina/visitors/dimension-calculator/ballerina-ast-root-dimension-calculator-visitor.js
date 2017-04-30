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
import {canvas, panel} from './../../configs/designer-defaults';

class BallerinaASTRootDimensionCalculatorVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
        log.info('Begin Visit BallerinaASTRoot');
    }

    visit(node) {
        log.info('Visit BallerinaASTRoot');
    }

    endVisit(node) {
        // get the visit state to set ast root dimentions.
        let viewState = node.getViewState();
        // set the state to 0 since we re-calculate
        viewState.bBox.h = 0;
        viewState.bBox.w = 0;

        // get the children of ast root
        let children = node.getChildren();

        //now lets iterate and calculate height and width. 
        // height = sum of all the children height ( this is only for now it might change if we
        // have same level items in the future )
        // width = maximum with of the containing children. 
        children.forEach(function(element) {
            let childViewState = element.getViewState();
            viewState.bBox.h = viewState.bBox.h + childViewState.bBox.h + panel.wrapper.gutter.h;
            if(viewState.bBox.w < childViewState.bBox.w){
                viewState.bBox.w = childViewState.bBox.w;
            }
        }, this);

        //@todo check with guys if we are adding a canvous padding. 
        //then lets add canvous margins for view viewState
        //viewState.bBox.h = viewState.bBox.h + canvas.body.padding.top + canvas.body.padding.bottom;
        //viewState.bBox.w = viewState.bBox.w + canvas.body.padding.left + canvas.body.padding.right;

        // as a tmp thing I will add panel wraper gutter 
        // add bottom gutter to bBox
        //viewState.bBox.h = viewState.bBox.h + panel.wrapper.gutter.h;
        viewState.bBox.w = viewState.bBox.w + panel.wrapper.gutter.w * 2;
    }
}

export default BallerinaASTRootDimensionCalculatorVisitor;

