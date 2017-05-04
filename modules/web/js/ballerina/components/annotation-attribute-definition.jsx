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
import SizingUtil from './../visitors/sizing-utils';

class AnnotationAttributeDefinition extends React.Component {
    constructor(props) {
        super(props);
        this.model = this.props.model;
        this.bBox = this.props.model.viewState.bBox;
    }

    render() {
        return (
            <g className="attribute-content-operations-wrapper">
                <rect x={this.bBox.x} y={this.bBox.y} width={this.bBox.w} height={this.bBox.h}
                      className="attribute-content-operations-wrapper"/>
                <rect x={this.bBox.x + 350 + 10} y={this.bBox.y} width={30} height={30} className=""/>
                <text x={this.bBox.x + 60} y={this.bBox.y + 70} width={300}
                      height={30}>{this.model.getAttributeStatementString()}</text>
                <image x={this.bBox.x + 350 + 15} y={this.bBox.y + 55} width={20} height={20}
                       xlinkHref={ImageUtil.getSVGIconString('delete')}/>
            </g>
        );
    }
}

export default AnnotationAttributeDefinition;
