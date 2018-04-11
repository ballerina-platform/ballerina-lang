import 'font-ballerina/css/font-ballerina.css';
import { renderToStaticMarkup } from 'react-dom/server';
import { createElement } from 'react';
import { DragDropContext } from 'react-dnd';
import HTML5Backend from 'react-dnd-html5-backend';

import Diagram from 'plugins/ballerina/diagram/diagram.jsx';
import TreeBuilder from 'plugins/ballerina/model/tree-builder.js';

function renderDiagram(target, modelJson, props={}) {
    const defaultProps = {
        model: TreeBuilder.build(modelJson),
        mode: 'action',
        fitToWidth: true,
        height: 300,
        width: 300,
    };
    Object.assign(defaultProps, props);
    const el = createElement(DragDropContext(HTML5Backend)(Diagram), defaultProps);
    target.innerHTML = renderToStaticMarkup(el);
}

const BalDiagram = DragDropContext(HTML5Backend)(Diagram);

export {
    TreeBuilder,
    Diagram,
    BalDiagram,
    renderDiagram
}