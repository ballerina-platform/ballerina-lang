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
import './hidden-statement.css';
import ImageUtil from './../../../image-util';

/**
 * Add an mark to hidden elements.
 */
class HiddenStatement extends React.Component {

    /**
     *
     * @param {object} props - Init props.
     * Initialize the statement decorator.
     */
    constructor(props) {
        super();
        this.designer = _.get(props, 'designer');
        this.mode = _.get(props, 'mode');
    }

    /**
     * Override the rendering logic.
     * @returns {XML} rendered component.
     */
    render() {
        const viewState = this.props.model.viewState;
        const bBox = viewState.bBox;

        const h = 10;
        const y = bBox.y + ((bBox.h - h) / 2);
        return (
            <g>
                <g className='hidden-statement'>
                    <rect 
                        x={bBox.x - (bBox.w / 2) + 2} 
                        y={y} 
                        width={bBox.w - 5} 
                        height={h} 
                        rx='5' 
                        ry='5' 
                        />
                    {/* <text x={bBox.x - (bBox.w / 2) + 3} y={y + 6} className='hidden-code' fontSize='35' >
                        ...
                    </text> */}
                    <text
                        x={bBox.x - (bBox.w / 2) + 10}
                        y={y + 10} 
                        className='hidden-code'
                        fontFamily='font-ballerina'
                    >{ImageUtil.getCodePoint('ellipsis')}</text>
                </g>
            </g>);
    }

}

HiddenStatement.propTypes = {
    model: PropTypes.instanceOf(Object).isRequired,
};

export default HiddenStatement;
