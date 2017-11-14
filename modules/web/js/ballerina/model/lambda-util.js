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
 *
 */

import getSourceOf from './source-gen';

export default function splitVariableDefByLambda(node) {
    const sourceFragments = getSourceOf(node, false, 0, true).split('$ function LAMBDA $');
    const lambdas = [];
    let i = 0;
    node.accept({
        beginVisit(n) {
            if (n.kind === 'Lambda' && i === 0) {
                lambdas.unshift(n.functionNode);
                i++;
            }
        },
        endVisit(n) {
            if (n.kind === 'Lambda') {
                i--;
            }
        },

    });
    return { sourceFragments, lambdas };
}
