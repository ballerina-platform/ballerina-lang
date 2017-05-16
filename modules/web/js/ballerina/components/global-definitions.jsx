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
import './global-definitions.css'
import ImageUtil from './image-util';

export default class GlobalDefinitions extends React.Component {

    render() {
        const bBox = this.props.bBox;
        const headerHeight = 35;
        const leftPadding = 10;
        const iconSize = 20;
        const iconLeftPadding = 12;
        const globalsLabelWidth = 47.94;

        const badgeWidth = leftPadding + globalsLabelWidth + iconLeftPadding + iconSize + leftPadding;

        const labelBbox = {
            x: bBox.x + leftPadding,
            y: bBox.y + headerHeight / 2,
        };

        const iconBbox = {
            x: labelBbox.x + globalsLabelWidth + iconLeftPadding,
            y: labelBbox.y - iconSize/2,
        };

        return (
            <g className="package-definition-head" onClick={ e => {this.props.onClick(e);} }>
                <rect x={ bBox.x } y={ bBox.y } width={badgeWidth} height={ headerHeight } rx="0" ry="0" className="package-definition-header"/>
                <text x={ labelBbox.x } y={ labelBbox.y } rx="0" ry="0">
                    Globals
                </text>
                <image width={ iconSize } height={ iconSize } className="property-pane-action-button-delete"
                       xlinkHref={ ImageUtil.getSVGIconString('view') } x={ iconBbox.x } y={ iconBbox.y } />
            </g>
        );
    }
}
