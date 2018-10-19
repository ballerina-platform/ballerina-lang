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

/**
 * Visitor util class for rendering the overlay components
 */
class OverlayComponentsRenderingUtil {

    showServerConnectorPropertyWindow(node) {
        const bBox = Object.assign({}, node.viewState.bBox);
        const titleHeight = 30;
        const iconSize = 14;
        const annotationBodyHeight = node.viewState.components.annotation.h;
        bBox.x = bBox.x + titleHeight + iconSize;
        bBox.y = bBox.y + annotationBodyHeight + titleHeight;
        const overlayComponents = {
            kind: 'ServerConnectorPropertiesForm',
            props: {
                key: node.getID(),
                model: node,
                bBox,
            },
        };
        node.viewState.overlayContainer = overlayComponents;
        return node;
    }

    showConnectorPropertyWindow(node) {
        const bBox = Object.assign({}, node.viewState.bBox);
        const statementContainerBBox = node.viewState.bBox;
        bBox.x = statementContainerBBox.x + ((statementContainerBBox.w - 120) / 2);
        bBox.y = statementContainerBBox.y + 30;
        const overlayComponents = {
            kind: 'ConnectorPropertiesForm',
            props: {
                key: node.getID(),
                model: node,
                bBox,
            },
        };
        node.viewState.overlayContainer = overlayComponents;
        return node;
    }

    showStructsInPackageForBinding(node, x, y, structs) {
        const bBox = Object.assign({}, node.viewState.bBox);
        bBox.x = x - 5;
        bBox.y = y + 23;
        const overlayComponents = {
            kind: 'StructBindingDropDown',
            props: {
                key: node.getID(),
                model: node,
                bBox,
                structList: structs,

            },
        };
        node.viewState.overlayContainer = overlayComponents;
        return node;
    }
}

export default new OverlayComponentsRenderingUtil();
