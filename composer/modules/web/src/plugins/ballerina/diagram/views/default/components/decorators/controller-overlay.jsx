/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import _ from 'lodash';
import PropTypes from 'prop-types';
import Node from '../../../../../model/tree/node';
import ControllerVisitor from '../../../../visitors/controller-visitor';
import OverlayComponentsRenderingVisitor from '../../../../../visitors/overlay-comp-rendering-visitor';
import {
    getOverlayComponent,
//    getErrorCollectorUtil,
} from '../../../../diagram-util';

/**
 * React component for a canvas decorator.
 *
 * @class ControllerOverlay
 * @extends {React.Component}
 */
class ControllerOverlay extends React.Component {

    /**
     * Renders view for a controller overlay.
     *
     * @returns {ReactElement} The view.
     * @memberof ControllerOverlay
     */
    render() {
        const model = this.props.model;

        // Filter out the overlay components so we can overlay html on top of svg.
        const overlayCompRender = new OverlayComponentsRenderingVisitor();
        this.props.model.accept(overlayCompRender);
        let overlayComponents = [];
        if (overlayCompRender.getOverlayComponents()) {
            overlayComponents = getOverlayComponent(overlayCompRender.getOverlayComponents(), this.props.mode);
        }

        const controllerVisitor = new ControllerVisitor(this.context.mode);
        this.props.model.accept(controllerVisitor);
        let controllers = controllerVisitor.getComponents();

        controllers = _.concat(controllers, overlayComponents);

        return (
            <div className='overlay' style={{ width: model.viewState.bBox.w }} >
                {controllers}
            </div>
        );
    }
}

ControllerOverlay.propTypes = {
    model: PropTypes.instanceOf(Node).isRequired,
};

ControllerOverlay.defaultProps = {
    annotations: [],
    overlayComponents: [],
    errorList: [],
};

ControllerOverlay.contextTypes = {
    mode: PropTypes.string,
};

export default ControllerOverlay;
