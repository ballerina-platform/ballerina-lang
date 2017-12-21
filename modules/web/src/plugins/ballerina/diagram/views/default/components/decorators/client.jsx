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
import { lifeLine, clientLine } from './../../designer-defaults';

class Client extends React.Component {

    render() {
        const bBox = this.props.bBox;
        const topBox = Object.assign({}, bBox);
        const bottomBox = Object.assign({}, bBox);

        const hLength = clientLine.head.length;
        bottomBox.h = lifeLine.head.height;
        topBox.h = lifeLine.head.height;

        bottomBox.y = bBox.y + bBox.h - bottomBox.h;

        // calculate the line coordinates
        const line = {};
        line.x1 = (hLength / 2) + bBox.x;
        line.x2 = (hLength / 2) + bBox.x;
        line.y1 = bBox.y;
        line.y2 = bBox.y + bBox.h;


        const topHeaderCentreX = bBox.x + (hLength / 2);
        const topHeaderCentreY = topBox.y;
        const bottomHeaderCentreX = topHeaderCentreX;
        const bottomHeaderCentreY = bBox.y + bBox.h;

        const invokeLineY = topBox.y + topBox.h + this.context.designer.config.statement.height;

        const titleTextArray = this.props.title.split(' ');

        return (<g
            className='client-line-group'
        >
            <line
                x1={line.x1}
                y1={line.y1}
                x2={line.x2}
                y2={line.y2}
                className='client-life-line unhoverable'
            />
            <rect
                x={topHeaderCentreX - (hLength / 2)}
                y={topHeaderCentreY - (hLength / 2)}
                width={hLength}
                height={hLength}
                rx='3'
                ry='3'
                className='client-line-header'
                transform={`rotate(45 ${topHeaderCentreX} ${topHeaderCentreY})`}
            />
            <rect
                x={bottomHeaderCentreX - (hLength / 2)}
                y={bottomHeaderCentreY - (hLength / 2)}
                width={hLength}
                height={hLength}
                rx='3'
                ry='3'
                className='client-line-header'
                transform={`rotate(45 ${bottomHeaderCentreX} ${bottomHeaderCentreY})`}
            />
            {(titleTextArray.length === 1) &&
                <g>
                    <text
                        x={topHeaderCentreX}
                        y={topHeaderCentreY}
                        className='client-line-text'
                    >{this.props.title}</text>
                    <text
                        x={bottomHeaderCentreX}
                        y={bottomHeaderCentreY}
                        className='client-line-text'
                    >{this.props.title}
                    </text>
                </g>
            }
            {(titleTextArray.length > 1) &&
                <g>
                    <text
                        x={topHeaderCentreX}
                        y={topHeaderCentreY}
                        className='client-line-text'
                    >
                        <tspan x={topHeaderCentreX} dy='-8' className='client-line-text'>{titleTextArray[0]}</tspan>
                        <tspan x={topHeaderCentreX} dy='15' className='client-line-text'>{titleTextArray[1]}</tspan>
                    </text>
                    <text
                        x={bottomHeaderCentreX}
                        y={bottomHeaderCentreY}
                        className='client-line-text'
                    >
                        <tspan x={bottomHeaderCentreX} dy='-8' className='client-line-text'>{titleTextArray[0]}</tspan>
                        <tspan x={bottomHeaderCentreX} dy='15' className='client-line-text'>{titleTextArray[1]}</tspan>
                    </text>
                </g>
            }
            <line
                x1={line.x1}
                y1={invokeLineY}
                x2={bBox.arrowLine}
                y2={invokeLineY}
                stroke='black'
                strokeWidth='1'
            />
            <text
                x={line.x1 + this.context.designer.config.statement.gutter.h}
                y={topBox.y + topBox.h + (this.context.designer.config.statement.height / 2)}
                dominantBaseline='central'
            >{bBox.text}</text>
        </g>);
    }
}

Client.propTypes = {
    title: PropTypes.string,
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }).isRequired,
};

Client.defaultProps = {
    title: 'Caller',
};

Client.contextTypes = {
    model: PropTypes.instanceOf(Object),
    designer: PropTypes.instanceOf(Object),
};

export default Client;
