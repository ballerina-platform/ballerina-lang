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
import StatementNode from '../statement-node';

class AbstractStreamingQueryNode extends StatementNode {


    setStreamingInput(newValue, silent, title) {
        const oldValue = this.streamingInput;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.streamingInput = newValue;

        this.streamingInput.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'streamingInput',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getStreamingInput() {
        return this.streamingInput;
    }



    setJoiningInput(newValue, silent, title) {
        const oldValue = this.joiningInput;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.joiningInput = newValue;

        this.joiningInput.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'joiningInput',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getJoiningInput() {
        return this.joiningInput;
    }



    setPatternClause(newValue, silent, title) {
        const oldValue = this.patternClause;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.patternClause = newValue;

        this.patternClause.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'patternClause',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPatternClause() {
        return this.patternClause;
    }



    setSelectClause(newValue, silent, title) {
        const oldValue = this.selectClause;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.selectClause = newValue;

        this.selectClause.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'selectClause',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getSelectClause() {
        return this.selectClause;
    }



    setOrderbyClause(newValue, silent, title) {
        const oldValue = this.orderbyClause;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.orderbyClause = newValue;

        this.orderbyClause.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'orderbyClause',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getOrderbyClause() {
        return this.orderbyClause;
    }



    setStreamingAction(newValue, silent, title) {
        const oldValue = this.streamingAction;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.streamingAction = newValue;

        this.streamingAction.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'streamingAction',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getStreamingAction() {
        return this.streamingAction;
    }



    setOutputRateLimitNode(newValue, silent, title) {
        const oldValue = this.outputRateLimitNode;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.outputRateLimitNode = newValue;

        this.outputRateLimitNode.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'outputRateLimitNode',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getOutputRateLimitNode() {
        return this.outputRateLimitNode;
    }



}

export default AbstractStreamingQueryNode;
