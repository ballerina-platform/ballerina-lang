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
 * This will visit the tree and clear any intermediate state populated by the
 * diagram rendering process. Currently this will handle following intermediate
 * states.
 *
 *  1) Arrow conflict resolver set an offset for each statement node which will be used
 *     in subsiquent dimention and position calculation to resolve any arrow conflicts.
 *
 * @class Clean
 */
class Clean {

    beginVisit(node) {
        node.viewState.offSet = 0;
        node.viewState.hidden = false;
        node.viewState.alias = undefined;
        node.viewState.dimensionsSynced = false;
        // Component for worker invocation
        node.viewState.components['invocation-arrow'] = undefined;
        // Component relevant to the action invocation
        node.viewState.components.invocation = undefined;
        node.viewState.errors = {};
        node.viewState.errorsForImports = {};
        node.viewState.errorsForGlobals = {};
    }

    endVisit() {
        return undefined;
    }
}

export default Clean;
