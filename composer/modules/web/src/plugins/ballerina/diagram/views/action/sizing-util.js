/**
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import DefaultSizingUtil from '../default/sizing-util';
import SimpleBBox from './../../../model/view/simple-bounding-box';
import TreeUtil from './../../../model/tree-util';

class SizingUtil extends DefaultSizingUtil {

    sizeBlockNode(node) {
        // we will iterate the statements and hide nodes.
        const statements = node.statements;
        // loop the statements
        let hiddenElement = false;
        statements.forEach((element) => {
            // filter out statements to show.
            if (this.isVisibleInActionView(element)) {
                hiddenElement = false;
            // following check is to convert the first hidden element to a placeholder.
            } else if (hiddenElement) {
                // hide the rest of the statements.
                this.hideElement(element);
            } else {
                this.convertToHiddenPlaceHolder(element);
                hiddenElement = true;
            }
        }, this);
        // size the body.
        super.sizeBlockNode(node);
    }

    isVisibleInActionView(node) {
        // Action invocation statements.
        if (
            TreeUtil.statementIsInvocation(node) ||
            (TreeUtil.statementIsAwaitResponse(node) && TreeUtil.findCompatibleStart(node)) ||
            TreeUtil.isIf(node) ||
            TreeUtil.isWhile(node) ||
            TreeUtil.isEndpointTypeVariableDef(node) ||
            TreeUtil.statementIsClientResponder(node) ||
            TreeUtil.isTry(node) ||
            TreeUtil.isForeach(node) ||
            TreeUtil.isTransaction(node) ||
            TreeUtil.isWorkerReceive(node) ||
            TreeUtil.isWorkerSend(node) ||
            TreeUtil.isForkJoin(node) ||
            TreeUtil.isMatch(node)
        ) {
            return true;
        }
        // Block statements
        // aditional nodes
        return false;
    }

    convertToHiddenPlaceHolder(node) {
        node.viewState.alias = 'HiddenStatement';
        node.viewState.bBox = new SimpleBBox();
        node.viewState.bBox.w = 30;
        node.viewState.bBox.h = 30;
    }

    hideElement(node) {
        node.viewState.hidden = true;
        node.viewState.bBox = new SimpleBBox();
    }
}

export default SizingUtil;
