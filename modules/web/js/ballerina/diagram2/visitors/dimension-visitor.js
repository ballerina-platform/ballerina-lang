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

import log from 'log';
import _ from 'lodash';
import { getDimentionVisitor, getDesigner } from '../diagram-util.js';

class DimensionVisitor {

    /**
     * Constructor for DimensionVisitor
     * @param {object} options - DimensionVisitor options
     */
    constructor(options) {
        this.mode = 'default';
        this.setDesigner(_.get(options, 'designer'));
    }

    setMode(mode) {
        this.mode = mode;
    }

    /**
     * Set the designer
     * @param {object} designer - designer related default values
     */
    setDesigner(designer) {
        if (_.isNil(designer)) {
            this.designer = getDesigner();
        } else {
            this.designer = designer;
        }
    }

    /**
     * Get the designer
     * @returns {object} designer - designer related default values
     */
    getDesigner() {
        return this.designer;
    }

    beginVisit(node) {
        if (getDimentionVisitor(`${node.getKind()}DimensionVisitor`, this.mode)) {
            const nodeVisitor = new (getDimentionVisitor(`${node.getKind()}DimensionVisitor`,
                this.mode))({ designer: this.getDesigner() });
            return nodeVisitor.beginVisit(node);
        }
        return undefined;
    }

    endVisit(node) {
        if (getDimentionVisitor(`${node.getKind()}DimensionVisitor`, this.mode)) {
            const nodeVisitor = new (getDimentionVisitor(`${node.getKind()}DimensionVisitor`,
                this.mode))({ designer: this.getDesigner() });
            return nodeVisitor.endVisit(node);
        }
        // log.warn(`Unable to find Dimension Calculator for : ${node.getKind()}`);
        return undefined;
    }
}

export default DimensionVisitor;
