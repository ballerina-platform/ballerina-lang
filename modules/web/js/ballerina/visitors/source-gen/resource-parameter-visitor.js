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
import log from 'log';
import EventChannel from 'event_channel';
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';
import AST from '../../ast/module';

class ResourceParameterVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitResourceParameter(resourceParameter) {
        return true;
    }

    beginVisitResourceParameter(resourceParameter) {
        this.appendSource('(');
        log.debug('Begin Visit Resource Parameter');
    }

    visitResourceParameter(resourceParameter) {
        log.debug('Visit Resource Parameter');
    }

    endVisitResourceParameter(resourceParameter) {
        this.appendSource(' ' + resourceParameter.getIdentifier() + ')');
        this.getParent().appendSource(this.getIndentation() + this.getGeneratedSource());
        log.debug('End Visit Resource Parameter');
    }
}

export default ResourceParameterVisitor;
