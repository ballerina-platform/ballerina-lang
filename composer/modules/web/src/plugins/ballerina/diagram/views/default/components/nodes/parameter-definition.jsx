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
import log from 'log';
import _ from 'lodash';
import ImageUtil from './../../../../image-util';
import SimpleBBox from '../../../../../model/view/simple-bounding-box';
import TreeBuilder from './../../../../../model/tree-builder';
import FragmentUtils from './../../../../../utils/fragment-utils';

class ParameterDefinition extends React.Component {
    /**
     * calculate the component BBox.
     * @param {object} props - props.
     * @return {SimpleBBox} bBox.
     * */
    static calculateComponentBBox(props) {
        const viewState = props.model.viewState;
        const bBox = viewState.bBox;
        return new SimpleBBox(bBox.x + 7,
            bBox.y + 2, viewState.w + 5, viewState.h);
    }

    constructor(props) {
        super(props);
        this.state = {
            inputBox: ParameterDefinition.calculateComponentBBox(props),
        };
        this.onDelete = this.onDelete.bind(this);
    }

    componentWillReceiveProps(props) {
        this.setState({
            inputBox: ParameterDefinition.calculateComponentBBox(props),
        });
    }

    onDelete() {
        if (this.props.model.parent.filterParameters({ id: this.props.model.id }).length > 0) {
            this.props.model.parent.removeParameters(this.props.model);
        } else {
            this.props.model.parent.removeReturnParameters(this.props.model);
        }
    }

    setEditedSource(value) {
        const oldNode = this;
        value = value.replace(';', '');
        let fragment = '';
        let type;
        // Check if the parameter is an argument parameter
        if (this.parent.filterParameters({ id: this.id }).length > 0) {
            type = 'argumentParameter';
            fragment = FragmentUtils.createArgumentParameterFragment(value);
        } else {
            type = 'returnParameter';
            fragment = FragmentUtils.createReturnParameterFragment(value);
        }
        const parsedJson = FragmentUtils.parseFragment(fragment);
        if ((!_.has(parsedJson, 'error') && !_.has(parsedJson, 'syntax_errors'))) {
            if (_.isEqual(parsedJson.kind, 'Variable')) {
                const newNode = TreeBuilder.build(parsedJson);
                if (type === 'argumentParameter') {
                    this.parent.replaceParameters(oldNode, newNode);
                } else {
                    this.parent.replaceReturnParameters(oldNode, newNode);
                }
            } else {
                log.error('Error while parsing parameter. Error response' + JSON.stringify(parsedJson));
            }
        } else {
            log.error('Error while parsing parameter. Error response' + JSON.stringify(parsedJson));
        }
    }

    render() {
        const model = this.props.model;
        const viewState = model.viewState;
        return (<g>
            <g onClick={() => this.onEditRequest()}>
                <rect
                    x={viewState.bBox.x + 7}
                    y={viewState.bBox.y + 5}
                    width={viewState.w + 5}
                    height={viewState.h + 2}
                    rx='0'
                    ry='0'
                    className='parameter-wrapper'
                />
                <text
                    x={viewState.bBox.x + 10}
                    y={viewState.bBox.y + 6}
                    className='parameter-text'
                >
                    {model.getSource()}
                </text>
            </g>
            <g onClick={this.onDelete}>
                <rect
                    x={viewState.components.deleteIcon.x}
                    y={viewState.components.deleteIcon.y + 5}
                    width={viewState.components.deleteIcon.w + 2}
                    height={viewState.components.deleteIcon.h + 2}
                    rx='0'
                    ry='0'
                    className='parameter-delete-icon-wrapper'
                />
                <image
                    x={viewState.components.deleteIcon.x + 5}
                    y={viewState.components.deleteIcon.y + 10}
                    width={10}
                    height={10}
                    className='parameter-delete-icon'
                    xlinkHref={ImageUtil.getSVGIconString('cancel')}
                >
                    <title>Remove</title>
                </image>
                <rect
                    x={viewState.components.deleteIcon.x + viewState.components.deleteIcon.w + 4}
                    y={viewState.components.deleteIcon.y + 4}
                    width={1}
                    height={viewState.h - 2}
                    rx='0'
                    ry='0'
                    className='parameter-space'
                />
            </g>

        </g>);
    }
}

ParameterDefinition.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default ParameterDefinition;
