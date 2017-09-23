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
import PropTypes from 'prop-types';
import './global-definitions.css';
import ImageUtil from './image-util';
import { util as SizingUtils } from '../../../../visitors/sizing-utils';
import { variablesPane as variablesPaneDefaults } from '../../../../configs/designer-defaults';

const GlobalDefinitions = ({ bBox, title, numberOfItems, onExpand }) => {
    const headerHeight = variablesPaneDefaults.headerHeight;
    const leftRightPadding = variablesPaneDefaults.leftRightPadding;
    const iconSize = variablesPaneDefaults.iconSize;
    const noOfGlobalsBGHeight = variablesPaneDefaults.noOfGlobalsBGHeight;
    const globalsNoFontSize = variablesPaneDefaults.globalsNoFontSize;
    const iconLeftPadding = variablesPaneDefaults.iconLeftPadding;
    const noOfGlobalsLeftPadding = variablesPaneDefaults.noOfGlobalsLeftPadding;
    const noOfGlobalsTextPadding = variablesPaneDefaults.noOfGlobalsTextPadding;
    const globalDefDecorationWidth = variablesPaneDefaults.globalDefDecorationWidth;
    const globalsLabelWidth = SizingUtils.getOnlyTextWidth(title);

    const noOfGlobalsTextWidth = SizingUtils.getOnlyTextWidth(numberOfItems, { fontSize: globalsNoFontSize });
    const noOfGlobalsBGWidth = Math.max(noOfGlobalsTextWidth + noOfGlobalsTextPadding, noOfGlobalsBGHeight);

    const badgeWidth = leftRightPadding + globalsLabelWidth + noOfGlobalsLeftPadding + noOfGlobalsTextWidth +
        iconLeftPadding + iconSize + leftRightPadding;

    const labelBbox = {
        x: bBox.x + leftRightPadding,
        y: bBox.y + (headerHeight / 2),
    };

    const numberBbox = {
        x: labelBbox.x + globalsLabelWidth + noOfGlobalsLeftPadding,
        y: labelBbox.y,
    };

    const iconBbox = {
        x: numberBbox.x + noOfGlobalsTextWidth + iconLeftPadding,
        y: numberBbox.y - (iconSize / 2),
    };

    return (
        <g className="package-definition-head" onClick={onExpand}>
            <rect
                x={bBox.x}
                y={bBox.y}
                width={badgeWidth}
                height={headerHeight}
                rx="0"
                ry="0"
                className="package-definition-header"
            />
            <rect
                x={bBox.x}
                y={bBox.y}
                height={headerHeight}
                width={globalDefDecorationWidth}
                className="global-definition-decorator"
            />
            <text x={labelBbox.x} y={labelBbox.y} rx="0" ry="0">
                {title}
            </text>
            <rect
                x={numberBbox.x - ((noOfGlobalsBGWidth - noOfGlobalsTextWidth) / 2)}
                y={numberBbox.y - (noOfGlobalsBGHeight / 2)}
                width={noOfGlobalsBGWidth}
                height={noOfGlobalsBGHeight}
                rx={noOfGlobalsBGHeight / 2}
                ry={noOfGlobalsBGHeight / 2}
                className="global-badge"
            />
            <text
                x={numberBbox.x}
                y={numberBbox.y}
                rx="0"
                ry="0"
                style={{ fontSize: globalsNoFontSize }}
                className="global-badge-text"
            >
                {numberOfItems}
            </text>
            <image
                x={iconBbox.x}
                y={iconBbox.y}
                width={iconSize}
                height={iconSize}
                className="property-pane-action-button-delete"
                xlinkHref={ImageUtil.getSVGIconString('view')}
            >
                <title>View Global Variables</title> </image>
        </g>
    );
};

GlobalDefinitions.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }).isRequired,
    title: PropTypes.string.isRequired,
    numberOfItems: PropTypes.number.isRequired,
    onExpand: PropTypes.func.isRequired,
};

export default GlobalDefinitions;
