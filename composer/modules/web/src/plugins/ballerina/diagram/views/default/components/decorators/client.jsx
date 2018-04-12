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

class Client extends React.Component {

    render() {
        const bBox = this.props.bBox;
        const topBox = Object.assign({}, bBox);
        const bottomBox = Object.assign({}, bBox);

        const hWidth = this.context.designer.config.clientLine.head.width;
        const hHeight = this.context.designer.config.clientLine.head.height;
        bottomBox.h = this.context.designer.config.lifeLine.head.height;
        topBox.h = this.context.designer.config.lifeLine.head.height;

        bottomBox.y = bBox.y + bBox.h - bottomBox.h;

        // calculate the line coordinates
        const line = {};
        line.x1 = (hWidth / 2) + bBox.x;
        line.x2 = (hWidth / 2) + bBox.x;
        line.y1 = bBox.y;
        line.y2 = bBox.y + bBox.h;


        const topHeaderCentreX = bBox.x + (hWidth / 2);
        const topHeaderCentreY = topBox.y;
        const bottomHeaderCentreX = topHeaderCentreX;
        const bottomHeaderCentreY = bBox.y + bBox.h;

        const invokeLineY = topBox.y + topBox.h + this.context.designer.config.statement.height;

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
                x={topHeaderCentreX - (hWidth / 2)}
                y={topHeaderCentreY - (hHeight / 2)}
                width={hWidth}
                height={hHeight}
                rx={hHeight / 2}
                ry={hHeight / 2}
                className='client-line-header'
            >
                <title> {this.props.title} </title>
            </rect>
            <rect
                x={bottomHeaderCentreX - (hWidth / 2)}
                y={bottomHeaderCentreY - (hHeight / 2)}
                width={hWidth}
                height={hHeight}
                rx={hHeight / 2}
                ry={hHeight / 2}
                className='client-line-header'
            >
                <title> {this.props.title} </title>
            </rect>
            <g>
                <text
                    x={topHeaderCentreX}
                    y={topHeaderCentreY}
                    className='client-line-text'
                >
                    {this.props.title}
                    <title> {this.props.title} </title>
                </text>
                <text
                    x={bottomHeaderCentreX}
                    y={bottomHeaderCentreY}
                    className='client-line-text'
                >
                    {this.props.title}
                    <title> {this.props.title} </title>
                </text>
            </g>
            <g>
                <text
                    x={line.x1 + this.context.designer.config.statement.gutter.h}
                    y={topBox.y + topBox.h + (this.context.designer.config.statement.height - 5)}
                >{bBox.text}
                    <title> {bBox.fullText} </title>
                </text>
            </g>
            <line
                x1={line.x1}
                y1={invokeLineY}
                x2={bBox.arrowLine}
                y2={invokeLineY}
                className='client-invocation-arrow'
            />
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
    title: 'caller',
};

Client.contextTypes = {
    model: PropTypes.instanceOf(Object),
    designer: PropTypes.instanceOf(Object),
};

export default Client;
