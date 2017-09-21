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
import ASTFactory from 'ballerina/ast/ast-factory';
import OverlayPropContainer from '../diagram/views/default/components/utils/overlay-prop-container';

class OverlayComponentsRenderingVisitor {

    constructor() {
        this.resourcesForWS = [];
        this.serverConnectorProperties = [];
        this.connectorProperties = [];
    }

    canVisit() {
        return true;
    }

    visit() {
        return undefined;
    }

    beginVisit(node) {
        if (ASTFactory.isServiceDefinition(node)) {
            // Server connector prop views
            if (node.viewState.showPropertyForm) {
                const bBox = Object.assign({}, node.viewState.bBox);
                const titleHeight = 30;
                const iconSize = 14;
                const annotationBodyHeight = node.viewState.components.annotation.h;
                bBox.x = bBox.x + titleHeight + iconSize + 15;
                bBox.y = bBox.y + annotationBodyHeight + titleHeight;
                this.serverConnectorProperties.push(new OverlayPropContainer(bBox, node));
            }
            // drop down with pre defined methods for web sockets
            if (node.getProtocolPkgName() === 'ws') {
                let bBox = node.getViewState().components.transportLine;
                this.resourcesForWS.push(new OverlayPropContainer(bBox, node));
                if (node.getChildren()) {
                    node.getChildren().map((child) => {
                        const thisChildIndex = node.getIndexOfChild(child);
                        if (ASTFactory.isResourceDefinition(child) &&
                            (thisChildIndex !== node.getChildren().length - 1)) {
                            bBox = child.getViewState().bBox;
                            this.resourcesForWS.push(new OverlayPropContainer(bBox, child));
                        }
                    });
                }
            }
        } else if (ASTFactory.isConnectorDeclaration(node)) {
            // Connector prop views
            if (node.viewState.showPropertyForm) {
                const bBox = Object.assign({}, node.viewState.bBox);
                const statementContainerBBox = node.viewState.components.statementContainer
                bBox.x = statementContainerBBox.x + ((statementContainerBBox.w - 120) / 2);
                bBox.y = statementContainerBBox.y;
                this.connectorProperties.push(new OverlayPropContainer(bBox, node));
            }
        }
        return undefined;
    }

    endVisit(node) {
        return undefined;
    }

    getResourcesForWS() {
        return (this.resourcesForWS.length > 0) ? this.resourcesForWS : false;
    }
    getServerConnectorProperties() {
        return (this.serverConnectorProperties.length > 0) ? this.serverConnectorProperties : false;
    }
    getConnectorProperties() {
        return (this.connectorProperties.length > 0) ? this.connectorProperties : false;
    }
}

export default OverlayComponentsRenderingVisitor;
