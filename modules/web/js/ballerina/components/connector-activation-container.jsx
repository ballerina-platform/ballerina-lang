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
import ASTNode from '../ast/node';
import MessageManager from './../visitors/message-manager';
import './connector-activation-container.css';

class ConnectorActivationContainer extends React.Component {

    constructor(props) {
        super(props);
        this.state = { activationZoneActivated: false, dropZoneDropNotAllowed: false };
    }

    render() {
        const bBox = this.props.bBox;
        const dropZoneActivated = this.state.activationZoneActivated;
        const dropZoneNotAllowed = this.state.dropZoneDropNotAllowed;
        const dropZoneClassName = (dropZoneActivated ? 'activation-zone active' : 'activation-zone') + (dropZoneNotAllowed ? ' block' : '');
        return (<g className="connector-activation-container">
          <rect
            x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h}
            className={dropZoneClassName}
            onMouseOver={e => this.onOverActivationZone(e)}
            onMouseOut={e => this.onOutActivationZone(e)}
            onMouseUp={e => this.onMouseUp(e)}
          />
          {this.props.children}
        </g>);
    }

    onOverActivationZone(e) {
        const messageManager = this.context.messageManager;
        const activationTarget = this.props.activationTarget;
        if (messageManager.isOnDrag()) {
            messageManager.setDestination(activationTarget);
            this.setState({ activationZoneActivated: true, dropZoneDropNotAllowed: !messageManager.isAtValidDestination() });
        }
        e.stopPropagation();
    }

    onOutActivationZone(e) {
        const messageManager = this.context.messageManager;
        if (messageManager.isOnDrag()) {
            messageManager.setDestination(undefined);
            this.setState({ activationZoneActivated: false });
        }
        e.stopPropagation();
    }

    onMouseUp(e) {
        this.setState({ activationZoneActivated: false });
    }
}


ConnectorActivationContainer.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    activationTarget: PropTypes.instanceOf(ASTNode).isRequired,
};

ConnectorActivationContainer.contextTypes = {
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
};

export default ConnectorActivationContainer;
