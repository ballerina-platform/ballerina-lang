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
import { statement } from './../configs/designer-defaults';
import { lifeLine } from './../configs/designer-defaults';
import ArrowDecorator from './arrow-decorator';

const text_offset = 50;

class StatementArrowConnection extends React.Component {

    render() {
        const { start, end } = this.props;

        const arrowStart = { x: 0, y: 0 };
        const arrowEnd = { x: 0, y: 0 };

        if (start.bBox.x < end.bBox.x) {
			// start statement is on the right

            arrowStart.x = start.bBox.x + start.bBox.w;
            arrowStart.y = start.bBox.y + start.components['drop-zone'].h + (start.bBox.h - start.components['drop-zone'].h) / 2;

            arrowEnd.x = end.bBox.x;
            arrowEnd.y = arrowStart.y;
        } else {
			// start statement is on the left

            arrowStart.x = start.bBox.x;
            arrowStart.y = start.bBox.y + start.components['drop-zone'].h + (start.bBox.h - start.components['drop-zone'].h) / 2;

            arrowEnd.x = end.bBox.x + end.bBox.w;
            arrowEnd.y = arrowStart.y;
        }

        return (<ArrowDecorator start={arrowStart} end={arrowEnd} enable />);
    }
}

export default StatementArrowConnection;
