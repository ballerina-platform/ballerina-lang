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
import ServerConnectorPropContainer from '../diagram/views/default/components/utils/server-connector-prop-container';
import AnnotationHelper from '../env/helpers/annotation-helper';

class ServerConnectorPropertiesRenderingVisitor {

    constructor() {
        this.serverConnectorProperties = [];
    }

    canVisit() {
        return true;
    }

    visit() {
        return undefined;
    }

    beginVisit(node) {
        if (ASTFactory.isServiceDefinition(node)) {
            if (node.viewState.showPropertyForm) {
                const bBox = Object.assign({}, node.viewState.bBox);
                const titleHeight = 30;
                const iconSize = 14;
                const annotationBodyHeight = node.viewState.components.annotation.h;
                bBox.x = bBox.x + titleHeight + iconSize + 15;
                bBox.y = bBox.y + annotationBodyHeight + titleHeight;
                this.serverConnectorProperties.push(new ServerConnectorPropContainer(bBox, node));
            }
        }
        return undefined;
    }

    endVisit(node) {
        return undefined;
    }

    getServerConnectorProperties() {
        return (this.serverConnectorProperties.length > 0) ? this.serverConnectorProperties : false;
    }
}

export default ServerConnectorPropertiesRenderingVisitor;
