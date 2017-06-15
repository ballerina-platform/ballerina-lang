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
import Expression from './expression';

/**
 * Constructor for InstanceCreationExpression
 * @param {Object} args - Arguments to create the InstanceCreationExpression
 * @param {Object} args.typeName - Type of the instance creation.
 * @constructor
 * @augments Expression
 */
class InstanceCreationExpression extends Expression {
    constructor(args) {
        super('InstanceCreationExpression');
        this._typeName = _.get(args, 'typeName', 'newType');
    }

    setTypeName(typeName, options) {
        this.setAttribute('_typeName', typeName, options);
    }

    getTypeName() {
        return this._typeName;
    }

    /**
     * initialize InstanceCreationExpression from json object
     * @param {Object} jsonNode to initialize from
     * @param {string} [jsonNode.instance_type] - instance type
     */
    initFromJson(jsonNode) {
        this.setTypeName(jsonNode.instance_type, { doSilently: true });
        this.setExpression(this.generateExpression(), { doSilently: true });
    }

    generateExpression() {
        this._expression = 'new ' + this.getTypeName();
    }
}

export default InstanceCreationExpression;

