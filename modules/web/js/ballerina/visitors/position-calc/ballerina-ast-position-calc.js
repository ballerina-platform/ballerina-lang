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
import {panel} from './../../configs/designer-defaults';

class BallerinaASTRootPositionCalcVisitor {

    canVisit(node) {
        return true;
    }

    beginVisit(node) {
        // here we need to re adjust panel width to match the screen.
        let children = node.getChildren();
        let minWidth = node.getViewState().container.width - ( panel.wrapper.gutter.h * 2 );
        children.forEach(function(element) {
            let viewState = element.getViewState();
            if(viewState.bBox.w < minWidth){
                viewState.bBox.w = minWidth;
            }
        }, this);

        //lets adjest the canvous width and height
        if(node.getViewState().container.width > node.viewState.bBox.w){
            node.viewState.bBox.w = node.getViewState().container.width;
        }
        if(node.getViewState().container.height > node.viewState.bBox.h){
            node.viewState.bBox.h = node.getViewState().container.height;
        }        
    }

    visit(node) {
        log.debug('Visit BallerinaASTRoot');
    }

    endVisit(node) {
        log.debug('End Visit BallerinaASTRoot');
    }
}

export default BallerinaASTRootPositionCalcVisitor;

