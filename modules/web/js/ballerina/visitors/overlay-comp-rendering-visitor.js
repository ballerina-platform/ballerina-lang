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
import _ from 'lodash';

class OverlayComponentsRenderingVisitor {

    constructor() {
        this.overlayComponents = [];
    }

    canVisit() {
        return true;
    }

    visit() {
        return undefined;
    }

    beginVisit(node) {
        if (node.viewState.showOverlayContainer && !_.isEmpty(node.viewState.overlayContainer)) {
            this.overlayComponents.push(node.viewState.overlayContainer);
        }
        return undefined;
    }

    endVisit(node) {
        return undefined;
    }

    getOverlayComponents() {
        return (this.overlayComponents.length > 0) ? this.overlayComponents : false;
    }
}

export default OverlayComponentsRenderingVisitor;
