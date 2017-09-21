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
import ConnectorPropContainer from '../diagram/views/default/components/utils/connector-prop-container';

class ConnectorPropertiesRenderingVisitor {

    constructor() {
        this.connectorProperties = [];
    }

    canVisit() {
        return true;
    }

    visit() {
        return undefined;
    }

    beginVisit(node) {
        if (ASTFactory.isConnectorDeclaration(node)) {
            if (node.viewState.showPropertyForm) {
                const bBox = Object.assign({}, node.viewState.bBox);
                const statementContainerBBox = node.viewState.components.statementContainer
                bBox.x = statementContainerBBox.x + ((statementContainerBBox.w - 120) / 2);
                bBox.y = statementContainerBBox.y;
                this.connectorProperties.push(new ConnectorPropContainer(bBox, node));
            }
        }
        return undefined;
    }

    endVisit(node) {
        return undefined;
    }

    getConnectorProperties() {
        return (this.connectorProperties.length > 0) ? this.connectorProperties : false;
    }
}

export default ConnectorPropertiesRenderingVisitor;
