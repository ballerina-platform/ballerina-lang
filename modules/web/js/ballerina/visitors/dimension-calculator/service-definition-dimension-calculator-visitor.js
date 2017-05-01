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
import * as DesignerDefaults from './../../configs/designer-defaults';
import SimpleBBox from './../../ast/simple-bounding-box';

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
        var viewState = node.getViewState();
        var components = {};

        components['heading'] = new SimpleBBox();
        components['heading'].h = DesignerDefaults.panel.heading.height;

        //Initial statement height include panel heading and pannel padding.
        var bodyHeight = DesignerDefaults.panel.body.padding.top + DesignerDefaults.panel.body.padding.bottom;
        // Set the width to 0 dont add the padding now since we do a comparison.
        var bodyWidth = 0;

        node.children.forEach(function(child,index) {
            bodyHeight += child.viewState.bBox.h ;
            // If there is only one child no need to add gutter
            if(index == 1){
                bodyHeight = bodyHeight + DesignerDefaults.innerPanel.wrapper.gutter.v;
            }
            if(child.viewState.bBox.w > bodyWidth){
                bodyWidth = child.viewState.bBox.w;
            }
        });


        // now add the padding for width
        bodyWidth = bodyWidth + DesignerDefaults.panel.body.padding.left + DesignerDefaults.panel.body.padding.right;

        components['body'] = new SimpleBBox();

        components['body'].h = bodyHeight;
        components['body'].w = bodyWidth;
        components['heading'].w = bodyWidth;

        viewState.bBox.h = components['heading'].h + components['body'].h;
        viewState.bBox.w = components['body'].w;

        viewState.components = components;        
    }
}

export default ServiceDefinitionDimensionCalculatorVisitor;