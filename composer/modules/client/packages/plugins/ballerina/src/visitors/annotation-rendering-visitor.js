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
import TreeUtil from '../model/tree-util';
import AnnotationContainerMockNode from '../diagram/views/default/components/decorators/annotation-container';

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
        const isTopLevelNode = (TreeUtil.isService(node) || TreeUtil.isConnector(node) ||
                                TreeUtil.isAnnotation(node) || TreeUtil.isStruct(node) ||
                                (TreeUtil.isFunction(node) && !node.lambda)) && TreeUtil.isCompilationUnit(node.parent);
        const isSecondLevelNode = (TreeUtil.isResource(node) && TreeUtil.isService(node.parent)) ||
                                    (TreeUtil.isAction(node) && TreeUtil.isConnector(node.parent));
        if (isTopLevelNode || isSecondLevelNode) {
            const annotations = node.getAnnotationAttachments();
            if (node.viewState.showAnnotationContainer && !node.parent.viewState.collapsed) {
                const bBox = Object.assign({}, node.viewState.bBox);
                bBox.h = node.viewState.components.annotation.h;
                this.annotations.push(new AnnotationContainerMockNode(bBox, annotations, node));
            }
            annotations.forEach((annotation) => {
                annotation.viewState.disableEdit = false;
            });
        }

        // hide annotations of resources if service is hidded
        if (TreeUtil.isService(node) || TreeUtil.isConnector(node)) {
            this.hiddenService = node.viewState.collapsed;
        }

        return undefined;
    }

    endVisit(node) {
        if (TreeUtil.isService(node) || TreeUtil.isConnector(node)) {
            this.hiddenService = false;
        }
        return undefined;
    }

    getAnnotations() {
        return (this.annotations.length > 0) ? this.annotations : false;
    }
}

export default AnnotationRenderingVisitor;
