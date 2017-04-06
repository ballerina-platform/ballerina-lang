/**
 * Copyright (c) 2016-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import AbstractSourceGenVisitor from './abstract-source-gen-visitor';

class AnnotationDefinitionVisitor extends AbstractSourceGenVisitor {
    constructor(parent) {
        super(parent);
    }

    canVisitAnnotationDefinition(annotationDefinition) {
        return true;
    }

    beginVisitAnnotationDefinition(annotationDefinition) {
        var self = this;

        var constructedSourceSegment = 'annotation ' + annotationDefinition.getAnnotationName() + ' attach ';
        var count = 0;
        _.forEach(annotationDefinition.getAttachedDefinitions(), function (definition) {
            count += 1;
            constructedSourceSegment += definition;
            if (!_.isEqual(count, annotationDefinition.getAttachedDefinitions().length)) {
                constructedSourceSegment += ',';
            }
        });
        constructedSourceSegment += ' {\n';
        this.appendSource(constructedSourceSegment);
        log.debug('Begin Visit Annotation Definition');
    }

    visitAnnotationDefinition(annotationDefinition) {
        log.debug('Visit Annotation Definition');
    }

    endVisitAnnotationDefinition(annotationDefinition) {
        this.appendSource("}\n");
        this.getParent().appendSource(this.getGeneratedSource());
        log.debug('End Visit Annotation Definition');
    }
}

export default AnnotationDefinitionVisitor;