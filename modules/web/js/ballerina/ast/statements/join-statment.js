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
import _ from 'lodash';
import Statement from './statement';

class JoinStatement extends Statement {
    constructor(args) {
        super();
        this.type = "JoinStatement";
    }

    getWorkerDeclarations() {
        const workerDeclarations = [];
        const self = this;

        _.forEach(this.getChildren(), function (child) {
            if (self.getFactory().isWorkerDeclaration(child)) {
                workerDeclarations.push(child);
            }
        });
        return _.sortBy(workerDeclarations, [function (workerDeclaration) {
            return workerDeclaration.getWorkerName();
        }]);
    }

    setJoinType(type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('_join_type', type, options);
        }
    }

    getJoinType() {
        return this._join_type;
    }

    setParameter(type, options) {
        if (!_.isNil(type)) {
            this.setAttribute('_param', type, options);
        }
    }

    getParameter() {
        return this._param;
    }

    initFromJson(jsonNode) {
        let self = this;
        self.setJoinType(jsonNode['join_type']);
        const paramJsonNode = jsonNode['param'];
        const paramASTNode = self.getFactory().createFromJson(paramJsonNode);
        paramASTNode.initFromJson(paramJsonNode);
        self.setParameter(paramASTNode);
        _.each(jsonNode.children, function (childNode) {
            let child = self.getFactory().createFromJson(childNode);
            self.addChild(child);
            child.initFromJson(childNode);
        });
    }
}

export default JoinStatement;
