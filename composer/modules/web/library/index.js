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
 *
 */

import 'font-ballerina/css/font-ballerina.css';
import { renderToStaticMarkup } from 'react-dom/server';
import ReactDOM from 'react-dom';
import React, { createElement } from 'react';
import { DragDropContext } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';
import PropTypes from 'prop-types';

import Diagram from 'plugins/ballerina/diagram/diagram.jsx';
import TreeBuilder from 'plugins/ballerina/model/tree-builder.js';
import '../src/ballerina-theme/semantic.less';

const BalDiagram = DragDropContext(HTML5Backend)(Diagram);

function renderDiagram(target, modelJson, props = {}) {
    const defaultProps = {
        model: TreeBuilder.build(modelJson),
        mode: 'action',
        editMode: true,
        height: 300,
        width: 300,
    };
    Object.assign(defaultProps, props);
    const el = createElement(BalDiagram, defaultProps);
    target.innerHTML = renderToStaticMarkup(el);
}

class BallerinaDiagramWrapper extends React.Component {

    constructor(...args) {
        super(...args);
        this.overlayElement = undefined;
        this.diagramParentElement = undefined;
    }

    getChildContext() {
        return {
            getOverlayContainer: () => this.overlayElement,
            getDiagramContainer: () => this.diagramParentElement,
        };
    }

    render() {
        return (
            <div className='ballerina-editor design-view-container'>
                <div
                    className='html-overlay'
                    ref={(overlay) => {
                        this.overlayElement = overlay;
                    }}
                />
                <div
                    className='diagram-root'
                    ref={(parent) => {
                        this.diagramParentElement = parent;
                    }}
                >
                    {this.props.children}
                </div>
            </div>
        );
    }
}

BallerinaDiagramWrapper.childContextTypes = {
    getOverlayContainer: PropTypes.func.isRequired,
    getDiagramContainer: PropTypes.func.isRequired,
};

function createContextProvider(context) {
    class ContextProvider extends React.Component {
        getChildContext() {
            return context;
        }

        render() {
            return this.props.children;
        }
    }

    ContextProvider.childContextTypes = {};
    Object.keys(context).forEach(((key) => {
        ContextProvider.childContextTypes[key] = PropTypes.any.isRequired;
    }));

    return ContextProvider;
}

function renderEditableDiagram(target, modelJson, props = {}) {
    const defaultProps = {
        model: TreeBuilder.build(modelJson),
        mode: 'action',
        editMode: true,
        height: 300,
        width: 300,
    };

    const overlayElement = document.createElement('div');
    overlayElement.classList.add('html-overlay');
    target.appendChild(overlayElement);

    const diagramParentElement = document.createElement('div');
    diagramParentElement.classList.add('diagram-root');
    target.appendChild(diagramParentElement);

    const context = {
        getOverlayContainer: () => overlayElement,
        getDiagramContainer: () => diagramParentElement,
    };

    Object.assign(defaultProps, props);
    const ContextProvider = createContextProvider(context);
    const diagram = createElement(BalDiagram, defaultProps);
    const diagramWithContext = createElement(ContextProvider, {}, diagram);
    ReactDOM.render(diagramWithContext, diagramParentElement);
}

export {
    TreeBuilder,
    Diagram,
    BalDiagram,
    BallerinaDiagramWrapper,
    renderDiagram,
    renderEditableDiagram,
    createContextProvider,
};
