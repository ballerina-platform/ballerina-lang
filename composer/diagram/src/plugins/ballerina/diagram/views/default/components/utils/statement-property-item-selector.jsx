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
import SimpleBBox from 'plugins/ballerina/model/view/simple-bounding-box';
import ImageUtil from '../../../../image-util';

class StatementPropertyItemSelector extends React.Component {

    constructor(props) {
        super(props);
        this.handleShowDropDown = this.handleShowDropDown.bind(this);
    }

    handleShowDropDown() {
        const node = this.props.model;
        const itemsMetaArr = this.props.itemsMeta;
        const dropDownBBox = new SimpleBBox();
        dropDownBBox.x = node.viewState.components['statement-box'].x
            + node.viewState.components['statement-box'].w + 2;
        dropDownBBox.y = node.viewState.components['statement-box'].y + node.viewState.components['statement-box'].h;
        node.viewState.overlayContainer = {
            kind: 'AbstractDropdown',
            props: {
                key: node.getID(),
                model: node,
                dropDownBBox,
                itemsMeta: itemsMetaArr,
                editor: this.context.editor,
                environment: this.context.environment,
            },
        };
        node.viewState.showOverlayContainer = true;
        this.context.editor.update();
    }

    /**
     * Renders the view for property item selector
     *
     * @returns {ReactElement} The view.
     */
    render() {
        const bBox = this.props.bBox;
        return (
            <g
                id='serviceDefProps'
                onClick={this.handleShowDropDown}
                className={'statement-item-selector statement-item-selector-' + this.props.show}
            >
                <circle cx={bBox.x} cy={bBox.y} r='10' strokeWidth='0' fill='#3498db' />
                <image
                    x={bBox.x - 6}
                    y={bBox.y - 6}
                    width={12}
                    height={12}
                    xlinkHref={ImageUtil.getSVGIconString('rightarrow-white')}
                />
            </g>
        );
    }
}

StatementPropertyItemSelector.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
    bBox: PropTypes.instanceOf(Object).isRequired,
    show: PropTypes.string.isRequired,
};

StatementPropertyItemSelector.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default StatementPropertyItemSelector;
