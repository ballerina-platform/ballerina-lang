/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import TreeUtil from './../../../model/tree-util';
import SimpleBBox from './../../../model/view/simple-bounding-box';

class EndpointAggregatorUtil {
    /**
     * Aggregate the all visible endpoints for the function node.
     *
     * @param {object} node
     *
     */
    aggregateFunctionNode(node) {
        this.aggregateAllVisibleEndpoints(node);
    }

    /**
     * Aggregate the all visible endpoints for the resource node.
     *
     * @param {object} node
     *
     */
    aggregateResourceNode(node) {
        this.aggregateAllVisibleEndpoints(node);
    }

    aggregateAllVisibleEndpoints(node) {
        if (!node.body) {
            return;
        }

        const visibleOuterEndpoints = TreeUtil.getAllEndpoints(node.parent);
        const invocationStmts = [];

        if (node.workers && node.workers.length > 0) {
            // aggregate worker invocation statements
            node.workers.forEach((workerNode) => {
                workerNode.body.accept({
                    beginVisit: (statement) => {
                        if (TreeUtil.isInvocation(statement) && statement.actionInvocation) {
                            if (!TreeUtil.statementIsClientResponder(statement)) {
                                invocationStmts.push(statement);
                            }
                        }
                    },
                    endVisit: (statement) => {},
                });
            });
        } else {
            node.body.accept({
                beginVisit: (statement) => {
                    if (TreeUtil.isInvocation(statement) && statement.actionInvocation) {
                        if (!TreeUtil.statementIsClientResponder(statement)) {
                            invocationStmts.push(statement);
                        }
                    }
                },
                endVisit: (statement) => {},
            });
        }
        node.endpointNodes = _.filter(node.endpointNodes, (endpoint) => {
            return endpoint.id;
        });
        _.forEach(visibleOuterEndpoints, (ep) => {
            const invocationIndex = _.findIndex(invocationStmts, (invocation) => {
                return invocation.expression.variableName.value === ep.name.value;
            });
            if (invocationIndex >= 0) {
                const epClone = {
                    name: ep.name,
                    viewState: {
                        alias: undefined,
                        bBox: new SimpleBBox(),
                    },
                    kind: ep.kind,
                    endPointType: ep.endPointType,
                    parent: ep.parent,
                    skipSourceGen: true,
                };
                node.endpointNodes.push(epClone);
            }
        });
    }
}

export default EndpointAggregatorUtil;
