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
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { Menu } from 'semantic-ui-react';

import WorkerTools from 'plugins/ballerina/tool-palette/item-provider/worker-tools';
import ControllerUtil from 'plugins/ballerina/diagram/views/default/components/controllers/controller-util';
import ActionBox from 'plugins/ballerina/diagram/views/default/components/decorators/action-box';
import HoverItemVisiter from '../visitors/hover-item-visitor';
import './controller-overlay.scss';
import HoverButton from './hover-button';

/**
 * React component for a canvas decorator.
 *
 * @class ControllerOverlay
 * @extends {React.Component}
 */
class ControllerOverlay extends React.Component {

    constructor() {
        super();
        this.state = {
            showMenu: false,
        };
        this.onMouseEnter = this.onMouseEnter.bind(this);
        this.onMouseLeave = this.onMouseLeave.bind(this);
    }
    componentDidMount() {
        this.props.model.on('mouse-enter', this.onMouseEnter);
        this.props.model.on('mouse-leave', this.onMouseLeave);
    }
    componentWillReceiveProps(nextProps) {
        if (this.props.model !== nextProps.model) {
            this.props.model.off('mouse-enter', this.onMouseEnter);
            this.props.model.off('mouse-leave', this.onMouseLeave);
            nextProps.model.on('mouse-enter', this.onMouseEnter);
            nextProps.model.on('mouse-leave', this.onMouseLeave);
        }
    }
    componentWillUnmount() {
        this.props.model.off('mouse-enter', this.onMouseEnter);
        this.props.model.off('mouse-leave', this.onMouseLeave);
    }
    onMouseEnter({ origin, region }) {
        this.setState({
            region,
        });
        this.forceUpdate();
    }
    onMouseLeave({ origin, region }) {
        // clean up previous menu items
        origin.trigger('render-menu', { content: null, region });
        this.forceUpdate();
    }

    renderControllers(model, controllers, region) {
        model.trigger('render-menu', { content: controllers, region });
    }

    renderToRegions(node, regionToComponent = {}) {
        const region = node.viewState.hoveredRegion;
        const component = regionToComponent[region];
        this.renderControllers(node, component, region);
    }

    /**
     * Renders view for a controller overlay.
     *
     * @returns {ReactElement} The view.
     * @memberof ControllerOverlay
     */
    render() {
        const hoverItemVisiter = new HoverItemVisiter();
        this.props.model.accept(hoverItemVisiter);
        const nodes = hoverItemVisiter.getHoveredItems();
        
        nodes.forEach((node) => {
            if (node.kind === 'If') {
                this.renderToRegions(node, {
                    main: <HoverButton
                        style={{
                            top: node.viewState.components['statement-box'].y - 10,
                            left: node.viewState.components['statement-box'].x - 15,
                        }}
                    >
                        <Menu vertical>
                            {ControllerUtil.convertToAddItems(WorkerTools, node.getBody())}
                        </Menu>
                    </HoverButton>,
                    actionBox: <ActionBox
                        onDelete={() => { node.remove(); }}
                        onJumptoCodeLine={() => {
                            const { editor } = this.context;
                            editor.goToSource(node);
                        }}
                        show
                        style={{
                            top: node.viewState.components['statement-box'].y - 50,
                            left: node.viewState.components['statement-box'].x - 38,
                        }}
                    />,
                });
            } else if (node.kind === 'Assignment' || node.kind === 'VariableDef' || node.kind === 'ExpressionStatement') {
                this.renderToRegions(node, {
                    main: <HoverButton size='mini' style={{ top: -5, left: -20 }}>
                        <Menu vertical>
                            {ControllerUtil.convertToAddItems(WorkerTools, node)}
                        </Menu>
                    </HoverButton>
                });
            } else {
                console.log('Could not render ctrl for', node);
            }
        });

        return null;
    }
}

ControllerOverlay.propTypes = {
    model: PropTypes.instanceOf(Node).isRequired,
};

ControllerOverlay.defaultProps = {
};

ControllerOverlay.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
};

export default ControllerOverlay;
