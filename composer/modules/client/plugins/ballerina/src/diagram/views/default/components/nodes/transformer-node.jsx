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
import PanelDecorator from '../decorators/panel-decorator';
import TransformerNodeModel from '../../../../../model/tree/transformer-node';
import TransformerNodeDetails from '../transformer/transformer-node-details';
import ImageUtil from '../../../../image-util';
import PanelDecoratorButton from '../decorators/panel-decorator-button';

class TransformerNode extends React.Component {

    constructor(props) {
        super(props);
        this.onExpand = this.onExpand.bind(this);
    }

    onExpand() {
        const { designView } = this.context;
        designView.setTransformActive(true, this.props.model.getSignature());
    }

    render() {
        const icon = 'tool-icons/transformer';
        const { model } = this.props;
        const { viewState } = model;
        const { bBox } = viewState;

        const nodeDetails = ({ x, y }) => (
            <TransformerNodeDetails
                x={x}
                y={y}
                model={this.props.model}
                onExpand={this.onExpand}
            />
        );

        return (
            <PanelDecorator
                bBox={model.viewState.bBox}
                icon={icon}
                model={model}
                headerComponent={nodeDetails}
                rightComponents={[{
                    component: PanelDecoratorButton,
                    props: {
                        icon: ImageUtil.getSVGIconString('expand'),
                        tooltip: 'Go to expanded view',
                        onClick: this.onExpand,
                        key: `${this.props.model.getID()}-transform-expand-button`,
                    },
                }]}
            />
        );
    }
}

TransformerNode.propTypes = {
    model: PropTypes.instanceOf(TransformerNodeModel).isRequired,
};

TransformerNode.contextTypes = {
    mode: PropTypes.string,
    designView: PropTypes.instanceOf(Object).isRequired,
};

export default TransformerNode;
