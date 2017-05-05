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
import Renderer from './renderer';
import {packageDefinition} from '../configs/designer-defaults';
import './package-definition.css';
import {getCanvasOverlay} from '../configs/app-context';

class PackageDefinition extends React.Component {

    constructor(props) {
        super(props);
        this.handlePackageNameInput = this.handlePackageNameInput.bind(this);
    }

    handlePackageNameInput(input) {
        this.props.model.setPackageName(input);
    }

    handlePackageNameClick(e) {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const headerPadding = packageDefinition.header.padding;

        const textBoxBBox = {
            x: bBox.x + headerPadding.left + 75,
            y: bBox.y + 15
        };

        const options = {
            bBox: textBoxBBox,
            onChange: this.handlePackageNameInput,
            initialValue: model.getPackageName()
        }

        this.context.renderer.renderTextBox(options);
    }

    render() {
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        const packageName = model._packageName || "";
        const headerHeight = packageDefinition.header.height;
        const headerPadding = packageDefinition.header.padding;

        return (
            <g>
                <rect x={ bBox.x } y={ bBox.y } width={ bBox.w } height={ headerHeight } rx="0" ry="0" className="package-definition-header"/>
                <text x={ bBox.x + headerPadding.left } y={ bBox.y + headerHeight/2 } className="package-definition-text">Package</text>
                <text x={ bBox.x + headerPadding.left + 75 } y={ bBox.y + headerHeight/2 }
                      className="package-definition-package-name"
                      onClick={e => {this.handlePackageNameClick(e)}}
                >
                      {packageName}
                </text>
            </g>
        );
    }
}

PackageDefinition.contextTypes = {
    renderer: PropTypes.instanceOf(Renderer).isRequired,
};

export default PackageDefinition;
