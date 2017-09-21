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
import { panel } from '../../../../configs/designer-defaults.js';
import './properties-form.css';
import { util } from '../sizing-util';
/**
 * React component for a server connector properties handler
 *
 * @class ServerConnectorProperties
 * @extends {React.Component}
 */
class ServerConnectorProperties extends React.Component {

    constructor(props) {
        super(props);
        this.handleShowModal = this.handleShowModal.bind(this);
    }

    /**
     * Show the property window
     */
    handleShowModal() {
        this.props.model.getViewState().showPropertyForm = !this.props.model.getViewState().showPropertyForm;
        this.context.editor.update();
    }

    /**
     * Renders the view for a connector properties handler
     *
     * @returns {ReactElement} The view.
     * @memberof ServerConnectorProperties
     */
    render() {
        const bBox = this.props.bBox;
        const titleHeight = panel.heading.height;
        const iconSize = 14;
        let protocolOffset = 0;
        let protocolTextSize = 0;
        if (this.props.protocol) {
            protocolOffset = 50;
            protocolTextSize = util.getTextWidth(this.props.protocol, 0).w;
        }
        const annotationBodyHeight = this.props.annotationBodyHeight;
        this.positionX = bBox.x + titleHeight + iconSize + 15;
        this.positionY = bBox.y + annotationBodyHeight + titleHeight;
        let protocolClassName = 'protocol-rect';
        if (this.props.model.getViewState().showPropertyForm) {
            protocolClassName = 'protocol-rect-clicked';
        }
        return (
            <g id='serviceDefProps' onClick={this.handleShowModal}>
                <rect
                    x={bBox.x + titleHeight + iconSize + 15 + 3}
                    y={bBox.y + annotationBodyHeight}
                    width={protocolOffset - 3}
                    height={titleHeight}
                    className={protocolClassName}
                />
                <text
                    className="protocol-text"
                    x={bBox.x + titleHeight + iconSize + 15 + 3 + ((protocolOffset - protocolTextSize) / 2)}
                    y={bBox.y + annotationBodyHeight + 15}
                    style={{ dominantBaseline: 'central' }}
                >{this.props.protocol}</text>
            </g>
        );
    }
}

export default ServerConnectorProperties;

ServerConnectorProperties.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};
