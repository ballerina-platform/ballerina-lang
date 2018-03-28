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

class EndpointAggregatorVisitor {

    setAggregatorUtil(aggregatorUtil) {
        this.util = aggregatorUtil;
    }

    beginVisit(node) {
        if (_.isFunction(this.util[`aggregate${node.getKind()}Node`])) {
            this.util[`aggregate${node.getKind()}Node`](node);
        }
        return undefined;
    }

    endVisit() {
        // do nothing.
        return undefined;
    }
}

export default EndpointAggregatorVisitor;
