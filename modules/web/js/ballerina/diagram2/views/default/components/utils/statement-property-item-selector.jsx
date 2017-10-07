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
import _ from 'lodash';
import SimpleBBox from './../../../../../ast/simple-bounding-box';

class StatementPropertyItemSelector extends React.Component {

    constructor(props) {
        super(props);
        this.itemsMeta = this.props.itemsMeta;
        this.handleShowDropDown = this.handleShowDropDown.bind(this);
    }

    handleShowDropDown() {
        const node = this.props.model;
        const dropDownBBox = new SimpleBBox();
        dropDownBBox.x = node.viewState.components['statement-box'].x
            + node.viewState.components['statement-box'].w + 2;
        dropDownBBox.y = node.viewState.components['statement-box'].y + node.viewState.components['statement-box'].h;
        // const bBox = Object.assign({}, node.viewState.bBox);
        // const titleHeight = 30;
        // const iconSize = 14;
        // const annotationBodyHeight = node.viewState.components.annotation.h;
        // bBox.x = bBox.x + titleHeight + iconSize;
        // bBox.y = bBox.y + annotationBodyHeight + titleHeight;
        node.viewState.overlayContainer = {
            kind: 'AbstractDropdown',
            props: {
                key: node.getID(),
                model: node,
                dropDownBBox,
                itemsMeta: this.itemsMeta,
                editor: this.context.editor,
                environment: this.context.environment,
            },
        };
        this.context.editor.update();
    }

    /**
     * Renders the view for property item selector
     *
     * @returns {ReactElement} The view.
     */
    render() {
        const bBox = this.props.bBox;
        const dropDownItems = this.props.items;
        return (
            <g id='serviceDefProps' onClick={this.handleShowDropDown}>
                <circle cx={bBox.x} cy={bBox.y} r="7" stroke="black" strokeWidth="3" fill="red" />
            </g>
        );
    }
}

StatementPropertyItemSelector.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default StatementPropertyItemSelector;
