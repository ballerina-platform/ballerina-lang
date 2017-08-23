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
import React from 'react';
import ImageUtil from './image-util';

class AnnotationDefinitionAttachment extends React.Component {

    constructor() {
        super();
        this.state = { value: '' };
    }

    render() {
        this.state = { value: this.props.attachmentValue };
        const viewState = this.props.viewState;
        return (<g>

            <rect
                x={viewState.bBox.x + 7}
                y={viewState.bBox.y + 5}
                width={viewState.w + 5}
                height={viewState.h + 2}
                rx="0"
                ry="0"
                className="parameter-wrapper"
            />
            <text
                x={viewState.bBox.x + 10}
                y={viewState.bBox.y + 6}
                className="parameter-text"
            >
                {this.state.value}
            </text>
            <g onClick={() => this.props.onDelete(this.state.value)}>
                <rect
                    x={viewState.components.deleteIcon.x}
                    y={viewState.components.deleteIcon.y + 5}
                    width={viewState.components.deleteIcon.w + 2}
                    height={viewState.components.deleteIcon.h + 2}
                    rx="0"
                    ry="0"
                    className="parameter-delete-icon-wrapper"
                />
                <image
                    x={viewState.components.deleteIcon.x + 5}
                    y={viewState.components.deleteIcon.y + 10}
                    width={10}
                    height={10}
                    className="parameter-delete-icon"
                    xlinkHref={ImageUtil.getSVGIconString('cancel')}
                    onClick={() => this.props.onDelete(this.state.value)}
                >
                    <title>Remove</title>
                </image>
                <rect
                    x={viewState.components.deleteIcon.x + viewState.components.deleteIcon.w + 4}
                    y={viewState.components.deleteIcon.y + 4}
                    width={1}
                    height={viewState.h - 2}
                    rx="0"
                    ry="0"
                    className="parameter-space"
                />
            </g>

        </g>);
    }
}

export default AnnotationDefinitionAttachment;
