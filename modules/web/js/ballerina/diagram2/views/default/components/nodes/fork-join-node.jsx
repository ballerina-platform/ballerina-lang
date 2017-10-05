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
import CompoundStatementDecorator from './compound-statement-decorator';

class ForkJoinNode extends React.Component {
    constructor(props) {
        super(props);

        this.state = {
            innerDropZoneActivated: false,
            innerDropZoneDropNotAllowed: false,
            innerDropZoneExist: false,
            active: 'hidden',
        };

        this.conditionEditorOptions = {
            propertyType: 'text',
            key: 'join condition',
            model: props.model.joinBody,
            getterMethod: props.model.getJoinType,
            setterMethod: props.model.setJoinType,
        };

        this.parameterEditorOptions = {
            propertyType: 'text',
            key: 'Join parameter',
            value: ' ',
            model: props.model.joinBody,
            getterMethod: props.model.getJoinResultVar,
            setterMethod: props.model.setJoinResultVar,
        };
    }

    render(){
        const model = this.props.model;
        const bBox = model.viewState.bBox;
        return (
            <g>
                <CompoundStatementDecorator
                    dropTarget={model}
                    bBox={bBox}
                    title={'Fork'}
                    model={model}
                    body={model.workers}
                />
                <CompoundStatementDecorator
                    dropTarget={model.joinBody}
                    bBox={model.joinBody.viewState.bBox}
                    expression={{ text: 'all' }}
                    title={'Join'}
                    model={model.joinBody}
                    body={model.joinBody.body}
                    editorOptions={this.conditionEditorOptions}
                />
            </g>
        );
    }
}

ForkJoinNode.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
};

ForkJoinNode.contextTypes = {
    mode: PropTypes.string,
};

export default ForkJoinNode;
