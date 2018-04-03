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
import Node from '../node';

class AbstractPatternClauseNode extends Node {


    setPatternStreamingNode(newValue, silent, title) {
        const oldValue = this.patternStreamingNode;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.patternStreamingNode = newValue;

        this.patternStreamingNode.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'patternStreamingNode',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getPatternStreamingNode() {
        return this.patternStreamingNode;
    }



    setWithinClause(newValue, silent, title) {
        const oldValue = this.withinClause;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.withinClause = newValue;

        this.withinClause.parent = this;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'withinClause',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    getWithinClause() {
        return this.withinClause;
    }




    isForAllEvents() {
        return this.forAllEvents;
    }

    setForAllEvents(newValue, silent, title) {
        const oldValue = this.forAllEvents;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.forAllEvents = newValue;
        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'forAllEvents',
                    newValue,
                    oldValue,
                },
            });
        }
    }

}

export default AbstractPatternClauseNode;
