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

class AbstractTransactionNode extends StatementNode {


    setTransactionBody(newValue, silent, title) {
        const oldValue = this.transactionBody;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.transactionBody = newValue;

        this.transactionBody.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'transactionBody',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getTransactionBody() {
        return this.transactionBody;
    }



    setOnRetryBody(newValue, silent, title) {
        const oldValue = this.onRetryBody;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.onRetryBody = newValue;

        this.onRetryBody.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'onRetryBody',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getOnRetryBody() {
        return this.onRetryBody;
    }



    setRetryCount(newValue, silent, title) {
        const oldValue = this.retryCount;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.retryCount = newValue;

        this.retryCount.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'retryCount',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getRetryCount() {
        return this.retryCount;
    }



    setOnCommitFunction(newValue, silent, title) {
        const oldValue = this.onCommitFunction;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.onCommitFunction = newValue;

        this.onCommitFunction.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'onCommitFunction',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getOnCommitFunction() {
        return this.onCommitFunction;
    }



    setOnAbortFunction(newValue, silent, title) {
        const oldValue = this.onAbortFunction;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.onAbortFunction = newValue;

        this.onAbortFunction.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'onAbortFunction',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getOnAbortFunction() {
        return this.onAbortFunction;
    }



}

export default AbstractTransactionNode;
