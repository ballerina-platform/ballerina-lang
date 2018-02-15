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
 * React component to render Http resource header
 *
 * @class HttpResourceHeader
 * @extends {React.Component}
 */
class HttpResourceHeader extends React.Component {

    /**
     * Get base path of the service
     * @returns string - base path
    */
    getBasePath() {
        const annotationAttachment = this.props.model.parent.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'configuration';
        });

        const basePathAtributeNode = _.filter(annotationAttachment[0].attributes, (atribute) => {
            return atribute.getName().value === 'basePath';
        })[0];
        let basePath = basePathAtributeNode.getValue().getValue().unescapedValue;
        if (basePath.charAt(0) === '/') {
            basePath = basePath.substr(1);
        }
        return basePath;
    }
    /**
     * Get base path of the resource
     * @returns string - base path
    */
    getResourcePath() {
        let annotationAttachment = this.props.model.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'resourceConfig';
        })[0];
        annotationAttachment = annotationAttachment || {};
        annotationAttachment.attributes = annotationAttachment.attributes || [];
        const pathAtributeNode = _.filter(annotationAttachment.attributes, (atribute) => {
            return atribute.getName().value === 'path';
        })[0];
        if (pathAtributeNode && pathAtributeNode.getValue() && pathAtributeNode.getValue()) {
            return pathAtributeNode.getValue().getValue().unescapedValue;
        }
        return '';
    }
    /**
     * Get Name of the resource
     * @returns string - resource name
    */
    getServiceName() {
        return this.props.model.getName().value;
    }
    /**
     * Get Name of the resource
     * @returns string - resource name
    */
    getResourceName() {
        return this.props.model.getName().value;
    }

    getParametersString() {
        const parameters = this.props.model.parameters || [];
        const parametersString = parameters.map((item) => {
            return item.getSource();
        }).join(', ');
        return parametersString;
    }

    getMethods() {
        let annotationAttachment = this.props.model.filterAnnotationAttachments((attachment) => {
            return attachment.getAnnotationName().value === 'resourceConfig';
        })[0];
        annotationAttachment = annotationAttachment || {};
        annotationAttachment.attributes = annotationAttachment.attributes || [];
        const pathAtributeNode = _.filter(annotationAttachment.attributes, (atribute) => {
            return atribute.getName().value === 'methods';
        })[0];

        if (pathAtributeNode && pathAtributeNode.getValue() && pathAtributeNode.getValue().getValueArray()) {
            return pathAtributeNode.getValue().getValueArray().map((method) => {
                return method.getValue().unescapedValue;
            });
        }
        return [];
    }

    render() {
        const { x, y, model } = this.props;
        const { viewState } = model;
        const dimensions = viewState.components.httpMethods;
        const offset = 70 + dimensions.bBox.w;
        const basePathY = y + (viewState.components.heading.h / 2);
        const title = `${this.getBasePath()}${this.getResourcePath()}`;
        const resourceNameOffset = offset + new SizingUtils().getTextWidth(title, 150, 170).w;

        const resourceName = this.getResourceName();
        const parameterOffset = resourceNameOffset + new SizingUtils().getTextWidth(resourceName, 150, 170).w;

        const parameters = this.getParametersString();
        const methods = this.getMethods();
        return (
            <g>
                {methods.map((method) => {
                    return (
                        <g>
                            <rect
                                x={x + dimensions[method].offset}
                                y={y + 5}
                                width={dimensions[method].w}
                                height={20}
                                fill='#e2e2e2'
                                rx='3'
                                ry='3'
                            />
                            <text
                                style={{ dominantBaseline: 'central' }}
                                x={x + dimensions[method].offset + 5}
                                y={basePathY}
                                className='editable-text-label'
                            >
                                {method}
                            </text>
                        </g>
                    );
                })}
                <text
                    style={{ dominantBaseline: 'central' }}
                    x={x + offset}
                    y={basePathY}
                    fill='white'
                    className='editable-text-label'
                >{title}</text>

                <text
                    style={{ dominantBaseline: 'central' }}
                    x={x + resourceNameOffset}
                    y={basePathY}
                    fill='white'
                    className='editable-text-label'
                >{resourceName}</text>

                <text
                    style={{ dominantBaseline: 'central' }}
                    x={x + parameterOffset}
                    y={basePathY}
                    fill='white'
                    className='editable-text-label'
                >{parameters}</text>
            </g>
        );
    }
}

HttpResourceHeader.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
};

export default HttpResourceHeader;
