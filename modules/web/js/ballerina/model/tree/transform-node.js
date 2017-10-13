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

import AbstractTransformNode from './abstract-tree/transform-node';
import TransformUtils, { VarPrefix } from '../../utils/transform-utils';

class TransformNode extends AbstractTransformNode {

    addInput(input, silent, title) {
        const oldValue = this.inputs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.inputs.push(input);
        const newValue = this.inputs;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'inputs',
                    newValue,
                    oldValue,
                },
            });
        }
    }

    addOutput(output, silent, title) {
        const oldValue = this.outputs;
        title = (_.isNil(title)) ? `Modify ${this.kind}` : title;
        this.outputs.push(output);
        const newValue = this.outputs;

        if (!silent) {
            this.trigger('tree-modified', {
                origin: this,
                type: 'modify-node',
                title,
                data: {
                    attributeName: 'outputs',
                    newValue,
                    oldValue,
                },
            });
        }
    }
}

export default TransformNode;
