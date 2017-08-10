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

class AnnotationRenderingVisitor {

    constructor() {
        this.annotations = [];
        this.hiddenService = false;
    }

    canVisit() {
        return true;
    }

    visit() {
        // do nothing
        return undefined;
    }

    beginVisit(node) {
        const annotations = node.getChildrenOfType(ASTFactory.isAnnotationAttachment);

        if (ASTFactory.isServiceDefinition(node) || ASTFactory.isResourceDefinition(node) ||
            ASTFactory.isConnectorDefinition(node) || ASTFactory.isConnectorAction(node) ||
            ASTFactory.isAnnotationDefinition(node) || ASTFactory.isStructDefinition(node) ||
            (ASTFactory.isFunctionDefinition(node) && !node.isLambda())) {
            if (node.viewState.showAnnotationContainer && !node.getParent().getViewState().collapsed) {
                const bBox = Object.assign({}, node.viewState.bBox);
                bBox.h = node.viewState.components.annotation.h;
                this.annotations.push(new AnnotationContainer(bBox, annotations, node));
            }
            annotations.forEach((annotation) => {
                annotation.getViewState().disableEdit = false;
            });
        } else {
            annotations.forEach((annotation) => {
                annotation.getViewState().disableEdit = true;
            });
        }

        // hide annotations of resources if service is hidded
        if (ASTFactory.isServiceDefinition(node) || ASTFactory.isConnectorDefinition(node)) {
            this.hiddenService = node.viewState.collapsed;
        }

        return undefined;
    }

    endVisit(node) {
        if (ASTFactory.isServiceDefinition(node) || ASTFactory.isConnectorDefinition(node)) {
            this.hiddenService = false;
        }
        return undefined;
    }

    getAnnotations() {
        return (this.annotations.length > 0) ? this.annotations : false;
    }
}

export default AnnotationRenderingVisitor;
