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
import ASTFactory from './../ast/ballerina-ast-factory';
import AnnotationContainer from '../components/utils/annotation-container';
import SimpleBBox from './../ast/simple-bounding-box';

class AnnotationRenderingVisitor {

    constructor(){
        this.annotations = [];
        this.hiddenService = false;
    }

    canVisit(node) {
        return true;
    }

    visit(node) {
        // do nothing
        return undefined;
    }

    beginVisit(node) {
        if(node.constructor.name == 'ServiceDefinition' ||
            node.constructor.name == 'ResourceDefinition'){
            let annotations = node.filterChildren(function (child) {
                return ASTFactory.isAnnotation(child);
            });
            if(annotations.length > 0 && !this.hiddenService){
                let bBox = Object.assign({}, node.viewState.bBox);
                bBox.h = node.viewState.components.annotation.h;
                this.annotations.push(
                    new AnnotationContainer( bBox , annotations )
                );
            }
        }
        // hide annotations of resources if service is hidded
        if(node.constructor.name == 'ServiceDefinition'){
            this.hiddenService = node.viewState.collapsed;
        }
        return undefined;
    }

    endVisit(node) {
        if(node.constructor.name == 'ServiceDefinition'){
            this.hiddenService = false;
        }        
        return undefined;
    }

    getAnnotations(){
        return (this.annotations.length > 0 )?this.annotations:false;
    }
}

export default AnnotationRenderingVisitor;