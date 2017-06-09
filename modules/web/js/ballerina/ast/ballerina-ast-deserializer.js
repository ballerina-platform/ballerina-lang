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
import BallerinaASTFactory from './ballerina-ast-factory';

class BallerinaASTDeserializer {
    /**
     * deserialize to the AST model from source
     * @returns {{}|*}
     */
    static getASTModel(data) {
        var astRoot = deserializeNode(data.root);
        if (!_.isNil(data.whitespace_descriptor)) {
            astRoot.setWhiteSpaceDescriptor(data.whitespace_descriptor);
            astRoot.whiteSpace.useDefault = false;
        }
        return astRoot;
    }
}

function deserializeNode(node) {
    var astRoot = BallerinaASTFactory.createBallerinaAstRoot();

    _.each(node, function (childNode) {
        var child = BallerinaASTFactory.createFromJson(childNode);
        astRoot.addChild(child);
        child.initFromJson(childNode);
    });
    return astRoot;
}


export default BallerinaASTDeserializer;
