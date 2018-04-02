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
import './import-declaration.css';
import ImageUtil from '../../../../image-util';

const ImportDeclaration = ({ bBox, viewState, noOfImports, onClick }) => {
    const {
        headerHeight = 35,
        leftPadding = 10,
        iconSize = 20,
        importNoFontSize = 13,
        noOfImportsLeftPadding = 12,
        iconLeftPadding = 12,
        noOfImportsBGHeight = 18,
        noOfImportsBGWidth = 18,
        importLabelWidth = 48,
        noOfImportsTextWidth = 100,
        badgeWidth = 150,
        importDecDecoratorWidth = 3,
    } = viewState;

    const labelBbox = {
        x: bBox.x + leftPadding,
        y: bBox.y + (headerHeight / 2),
    };

    const numberBbox = {
        x: labelBbox.x + importLabelWidth + noOfImportsLeftPadding,
        y: labelBbox.y,
    };

    const iconBbox = {
        x: numberBbox.x + noOfImportsTextWidth + iconLeftPadding,
        y: numberBbox.y - (iconSize / 2),
    };

    return (
        <g className='package-definition-head' onClick={onClick}>
            <rect
                x={bBox.x}
                y={bBox.y}
                width={badgeWidth}
                height={headerHeight}
                rx='0'
                ry='0'
                className='package-definition-header'
            />
            <rect
                x={bBox.x}
                y={bBox.y}
                height={headerHeight}
                width={importDecDecoratorWidth}
                className='import-definition-decorator'
            />
            <text x={labelBbox.x} y={labelBbox.y} rx='0' ry='0'>
                Imports
            </text>
            <rect
                x={numberBbox.x - ((noOfImportsBGWidth - noOfImportsTextWidth) / 2)}
                y={numberBbox.y - (noOfImportsBGHeight / 2)}
                width={noOfImportsBGWidth}
                height={noOfImportsBGHeight}
                rx={noOfImportsBGHeight / 2}
                ry={noOfImportsBGHeight / 2}
                className='import-badge'
            />
            <text
                x={numberBbox.x}
                y={numberBbox.y}
                rx='0'
                ry='0'
                style={{ fontSize: importNoFontSize }}
                className='import-badge-text'
            >
                {noOfImports}
            </text>
            <text
                width={iconSize}
                height={iconSize}
                fontFamily='font-ballerina'
                fontSize={iconSize}
                className='property-pane-action-button-delete'
                xlinkHref={ImageUtil.getCodePoint('view')}
                x={iconBbox.x}
                y={iconBbox.y}
            >
                <title>View Imported Packages</title> </text>
        </g>
    );
};

ImportDeclaration.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
    }).isRequired,
    viewState: PropTypes.shape({
        headerHeight: PropTypes.number,
        leftPadding: PropTypes.number,
        iconSize: PropTypes.number,
        importNoFontSize: PropTypes.number,
        noOfImportsLeftPadding: PropTypes.number,
        iconLeftPadding: PropTypes.number,
        noOfImportsBGHeight: PropTypes.number,
        importLabelWidth: PropTypes.number,
        noOfImportsTextWidth: PropTypes.number,
        noOfImportsBGWidth: PropTypes.number,
        badgeWidth: PropTypes.number,
    }).isRequired,
    noOfImports: PropTypes.number.isRequired,
    onClick: PropTypes.func.isRequired,
};

export default ImportDeclaration;
