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
import './import-declaration-expanded.css'
import ImageUtil from './image-util';
import PropTypes from 'prop-types';
import Renderer from './renderer';

export default class importDeclarationExpanded extends React.Component {
    render() {
        const bBox = this.props.bBox;
        const importDeclarationHeight = 30;
        const importInputHeight = 40;
        const importDeclarationWidth = 300;
        const leftPadding = 10;
        const topGutter = 10;
        const iconSize = 20;
        const importElements = [];
        let lastImportElementY = bBox.y + topGutter;

        this.props.imports.forEach((importDec, count) => {
            importElements.push(
                <g className='package-declaration-item'>
                    <rect x={ bBox.x } y={ lastImportElementY } height={importDeclarationHeight} width={importDeclarationWidth} />
                    <text x={ bBox.x + leftPadding } y={ lastImportElementY + importDeclarationHeight/2 } rx="0" ry="0">
                        {importDec._packageName}
                    </text>
                </g>
            );
            lastImportElementY += importDeclarationHeight;
        });

        const textBoxBBox = {
            x: bBox.x + 5,
            y: lastImportElementY + 2,
            h: importInputHeight - 4,
            w: importDeclarationWidth - 10
        };

        const options = {
            bBox: textBoxBBox,
            onChange: () => {},
            initialValue: '',
        }

        this.context.renderer.renderTextBox(options);

        return (
            <g className="package-definitions-collection">
                {importElements}
                <rect x={ bBox.x } y={ lastImportElementY } height={importInputHeight} width={importDeclarationWidth} />
                <image width={ iconSize } height={ iconSize } className="property-pane-action-button-delete"
                    onClick={this.props.onCollapse} xlinkHref={ ImageUtil.getSVGIconString('hide') }
                    x={bBox.x + importDeclarationWidth - iconSize - 6 } y={bBox.y + topGutter + 3}/>
            </g>
        );
    }
}

importDeclarationExpanded.contextTypes = {
    renderer: PropTypes.instanceOf(Renderer).isRequired,
};
