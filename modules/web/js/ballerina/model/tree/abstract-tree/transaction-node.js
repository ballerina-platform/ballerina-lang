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

import Node from '../node';

class TransactionNodeAbstract extends Node {


    setFailedBody(newValue, title) {
        let oldValue = this.failedBody;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.failedBody = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'failedBody',
                newValue,
                oldValue,
            }
        });
    }

    getFailedBody() {
        return this.failedBody;
    }



    setTransactionBody(newValue, title) {
        let oldValue = this.transactionBody;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.transactionBody = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'transactionBody',
                newValue,
                oldValue,
            }
        });
    }

    getTransactionBody() {
        return this.transactionBody;
    }



    setCommittedBody(newValue, title) {
        let oldValue = this.committedBody;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.committedBody = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'committedBody',
                newValue,
                oldValue,
            }
        });
    }

    getCommittedBody() {
        return this.committedBody;
    }



    setAbortedBody(newValue, title) {
        let oldValue = this.abortedBody;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.abortedBody = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'abortedBody',
                newValue,
                oldValue,
            }
        });
    }

    getAbortedBody() {
        return this.abortedBody;
    }



    setCondition(newValue, title) {
        let oldValue = this.condition;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.condition = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'condition',
                newValue,
                oldValue,
            }
        });
    }

    getCondition() {
        return this.condition;
    }



    setWS(newValue, title) {
        let oldValue = this.wS;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.wS = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'wS',
                newValue,
                oldValue,
            }
        });
    }

    getWS() {
        return this.wS;
    }



    setKind(newValue, title) {
        let oldValue = this.kind;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.kind = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'kind',
                newValue,
                oldValue,
            }
        });
    }

    getKind() {
        return this.kind;
    }



    setPosition(newValue, title) {
        let oldValue = this.position;
        title = (_.isNil(title))? 'Modify ${child.kind}':title;
        this.position = newValue;
        this.trigger('tree-modified', {
            origin: this,
            type: 'modify-node',
            title,
            data: {
                attributeName: 'position',
                newValue,
                oldValue,
            }
        });
    }

    getPosition() {
        return this.position;
    }



}

export default TransactionNodeAbstract;
