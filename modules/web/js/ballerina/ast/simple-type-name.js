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

class SimpleTypeName extends ASTNode {
    constructor(args) {
        super('SimpleTypeName');
        this._typeName = _.get(args, 'typeName', '');
        this._packageName = _.get(args, 'packageName', '');
        this._fullPackageName = _.get(args, 'fullPackageName', '');
        this.whiteSpace.defaultDescriptor.regions = {
            0: ' ',
            1: '',
            2: '',
            3: '',
        };
    }

    setTypeName(typename, options) {
        if (!_.isNil(typename)) {
            this.setAttribute('_typeName', typename, options);
        }
    }

    getTypeName() {
        return this._typeName;
    }

    setPackageName(packageName, options) {
        if (!_.isNil(packageName)) {
            this.setAttribute('_packageName', packageName, options);
        }
    }

    setFullPackageName(packageName, options) {
        if (!_.isNil(packageName)) {
            this.setAttribute('_fullPackageName', packageName, options);
        }
    }

    getPackageName() {
        return this._packageName;
    }

    getFullPackageName() {
        return this._fullPackageName;
    }

    /**
     * initialize from json
     * @param jsonNode
     */
    initFromJson(jsonNode) {
        this.setTypeName(jsonNode.type_name, { doSilently: true });
        this.setPackageName(jsonNode.package_name, { doSilently: true });
        this.setFullPackageName(jsonNode.full_package_name, { doSilently: true });
    }

    toString() {
        let typeNameString = '';
        typeNameString += ((!_.isEmpty(this.getPackageName())) ? this.getPackageName()
                  + this.getWSRegion(1) + ':' + this.getWSRegion(2) : '');
        typeNameString += this.getTypeName() + this.getWSRegion(3);
        return typeNameString;
    }
}

export default SimpleTypeName;
