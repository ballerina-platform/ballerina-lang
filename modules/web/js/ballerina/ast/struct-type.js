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
import ASTNode from './node';

class StructType extends ASTNode {
    constructor(args) {
        super('StructType');
        this._typeName = _.get(args, 'typeName', 'TypeName');
        // TODO : Inherit from BType
    }

    /**
     *
     * @param typeName
     * @param options
     */
    setTypeName(typeName, options) {
        if (!_.isNil(typeName)) {
            this.setAttribute('_typeName', typeName, options);
        } else {
            log.error('Invalid Type Name [' + typeName + '] Provided');
            throw 'Invalid Type Name [' + typeName + '] Provided';
        }
    }

    /**
     * returns the typeNme
     * @returns {*}
     */
    getTypeName() {
        return this._typeName;
    }
}

export default StructType;
