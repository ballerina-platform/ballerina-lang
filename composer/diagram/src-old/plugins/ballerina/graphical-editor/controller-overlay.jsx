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
import PropTypes from 'prop-types';
import HoverItemVisitor from '../visitors/hover-item-visitor';
import NodeDefaultControllerVisitor from '../visitors/node-default-controller-visitor';
import ToolsOverlay from './controller-utils/tools-overlay';

// require all react components
function requireAll(requireContext, type) {
    const comp = {};
    requireContext.keys().forEach((item) => {
        const module = requireContext(item);
        if (module.default && module.default[type]) {
            comp[module.default.name] = module.default[type];
        }
    });
    return comp;
}

/**
 * React component for a canvas decorator.
 *
 * @class ControllerOverlay
 * @extends {React.Component}
 */
class ControllerOverlay extends React.Component {

    constructor() {
        super();
        this.regionCtrlComponents = requireAll(require.context('./controllers', true, /\.js$/), 'regions');
        this.defaultCtrlComponents = requireAll(require.context('./controllers', true, /\.js$/), 'defaults');
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
        // menu rendering is done in render function.
        this.forceUpdate();
    }
    onMouseLeave({ origin, region }) {
        // clean up previous menu items
        origin.trigger('render-menu', { content: null, region });
    }

    renderControllers(model, controllers, region) {
        // wait for existing state modifications to complete
        setTimeout(() => {
            model.trigger('render-menu', { content: controllers, region });
        }, 0);
    }

    /**
     * Renders view for a controller overlay.
     *
     * @returns {ReactElement} The view.
     * @memberof ControllerOverlay
     */
    render() {
        const hoverItemVisitor = new HoverItemVisitor();
        this.props.model.accept(hoverItemVisitor);
        const nodes = hoverItemVisitor.getHoveredItems();

        const nodeDefaultControllerVisitor = new NodeDefaultControllerVisitor();
        nodeDefaultControllerVisitor.setNodeDefaultControllers(this.defaultCtrlComponents);
        this.props.model.accept(nodeDefaultControllerVisitor);
        const defaultControllerNodes = nodeDefaultControllerVisitor.getNodeDefaultcontrollers();
        nodes.forEach((node) => {
            const region = node.viewState.hoveredRegion;
            const componentWithRegions = this.regionCtrlComponents[`${node.kind}`];
            if (!componentWithRegions) {
                console.warn(`no regions found for ${node.kind}`);
            } else {
                const component = this.regionCtrlComponents[`${node.kind}`][region];
                if (component) {
                    const element = React.createElement(component, {
                        model: node,
                    }, null);

                    this.renderControllers(node, element, region);
                }
            }
        });
        return (
            <ToolsOverlay>
                {defaultControllerNodes}
            </ToolsOverlay>
        );
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
