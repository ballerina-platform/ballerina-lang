/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import SizingUtils from '../../sizing-util';
/**
 * React component to render Http service header
 *
 * @class HttpServiceHeader
 * @extends {React.Component}
 */
class HttpServiceHeader extends React.Component {

    /**
     * Get base path of the service
     * @returns string - base path
    */
    getBasePath() {
        let annotationAttachment = this.props.model.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'configuration';
        })[0];
        annotationAttachment = annotationAttachment || {};
        annotationAttachment.attributes = annotationAttachment.attributes || [];

        const basePathAtributeNode = _.filter(annotationAttachment.attributes, (atribute) => {
            return atribute.getName().value === 'basePath';
        })[0];
        return basePathAtributeNode
                ? basePathAtributeNode.getValue().getValue().unescapedValue
                : '/' + this.props.model.name.value;
    }
    /**
     * Get Name of the service
     * @returns string - service name
    */
    getServiceName() {
        return this.props.model.getName().value;
    }

    render() {
        const { x, y, model } = this.props;
        const { viewState } = model;
        const basePathY = y + (viewState.components.heading.h / 2);
        const basePath = new SizingUtils().getTextWidth(this.getBasePath(), 80, 250);
        const serviceNameOffset = basePath.w + 80;
        return (
            <g className='http-service-header'>
                <text
                    style={{ dominantBaseline: 'central' }}
                    x={x + 80}
                    y={basePathY}
                    fill='white'
                    className='editable-text-label base-path'
                >{basePath.text}</text>

                <text
                    style={{ dominantBaseline: 'central' }}
                    x={x + serviceNameOffset}
                    y={basePathY}
                    fill='white'
                    className='editable-text-label service-name'
                >{this.getServiceName()}</text>
            </g>
        );
    }
}

HttpServiceHeader.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
};

export default HttpServiceHeader;
