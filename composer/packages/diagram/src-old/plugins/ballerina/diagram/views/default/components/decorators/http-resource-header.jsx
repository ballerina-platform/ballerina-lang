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
import './http-resource-header.scss';

/**
 * React component to render Http resource header
 *
 * @class HttpResourceHeader
 * @extends {React.Component}
 */
class HttpResourceHeader extends React.Component {
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
        return '/' + this.props.model.name.value;
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
        let offset = dimensions.bBox.w;
        if (offset < 50) {
            offset = 50;
        }
        const basePathY = y + (viewState.components.heading.h / 2);
        const path = `${this.getResourcePath()}`;
        const pathTextSize = new SizingUtils().getTextWidth(path, 80, 250);
        const resourceNameOffset = offset + pathTextSize.w + 30;

        const resourceName = this.getResourceName();
        const resourceNameSize = new SizingUtils().getTextWidth(resourceName, 40, 170);

        const methods = this.getMethods().reverse();
        return (
            <g className='http-resource-header'>
                {methods.length === 0 &&
                (<g className='http-methods'>
                    <rect
                        x={x - 15}
                        y={y + 5}
                        width={20}
                        height={20}
                        fill='#e2e2e2'
                        rx='3'
                        ry='3'
                    />
                    <text
                        style={{ dominantBaseline: 'central' }}
                        x={x - 8}
                        y={basePathY}
                        className='editable-text-label'
                    >
                        {'*'}
                    </text>
                </g>)

                }
                {methods.length > 0 && methods.map((method) => {
                    if (typeof method === 'string') {
                        return (
                            <g className='http-methods'>
                                <rect
                                    x={x + dimensions[method].offset - 15}
                                    y={y + 5}
                                    width={dimensions[method].w}
                                    height={20}
                                    fill='#e2e2e2'
                                    rx='3'
                                    ry='3'
                                    className={method.toLowerCase()}
                                />
                                <text
                                    style={{ dominantBaseline: 'central' }}
                                    x={x + dimensions[method].offset - 10}
                                    y={basePathY}
                                    className={`editable-text-label ${method.toLowerCase()}`}
                                >
                                    {method}
                                </text>
                            </g>
                        );
                    }
                    return null;
                })}
                <text
                    style={{ dominantBaseline: 'central' }}
                    x={x + offset - 50}
                    y={basePathY}
                    fill='white'
                    className='editable-text-label resource-path'
                >{pathTextSize.text}</text>

                <text
                    style={{ dominantBaseline: 'central' }}
                    x={x + resourceNameOffset + 50}
                    y={basePathY}
                    fill='white'
                    className='editable-text-label resource-name'
                >{resourceNameSize.text}</text>
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
