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
import './import-declaration.css'

export default class importDeclaration extends React.Component {
    render() {
        const bBox = this.props.bBox;
        const numberBadgeHeight = 24;
        const headerHeight = 35;
        const leftPadding = 15;
        let text = { };
        text.x = bBox.x + leftPadding;
        text.y = bBox.y + headerHeight / 2;
        let number = {};
        number.x = text.x + 65;
        number.y = text.y;
        return (
            <g className="package-definition-head" onClick={ e => {this.props.onClick(e);} }>
                <rect x={ bBox.x } y={ bBox.y } width={110} height={ headerHeight } rx="0" ry="0" className="package-definition-header"/>

                <text x={ text.x } y={ text.y } rx="0" ry="0">
                    Imports
                </text>
                <rect x={ number.x - 9 } y={ number.y - headerHeight/2 + 5 } width={25} height={25} rx="12.5" ry="12.5" className="import-badge"/>
                <text x={ number.x } y={ number.y } rx="0" ry="0">
                    {this.props.imports.length}
                </text>
            </g>
        );
    }
}
