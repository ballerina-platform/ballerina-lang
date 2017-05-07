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
import CanvasDecorator from './canvas-decorator';
import FunctionDefinition from './function-definition';
import PositionCalcVisitor from '../visitors/position-calculator-visitor';
import DimensionCalcVisitor from '../visitors/dimension-calculator-visitor';
import {getComponentForNodeArray} from './utils';
import DragDropManager from '../tool-palette/drag-drop-manager';
import MessageManager from './../visitors/message-manager';
import ASTRoot from '../ast/ballerina-ast-root';
import Renderer from './renderer';

class Diagram extends React.Component {

    constructor(props) {
        super(props);
        this.editor = props.editor;
        this.container = props.container;

        this.setModel(this.editor.getModel());

        this.dimentionCalc = new DimensionCalcVisitor();
        this.positionCalc = new PositionCalcVisitor();

        this.editor.on("update-diagram",()=>{
            this.setModel(this.editor.getModel());
            this.forceUpdate();
        });
    }

    setModel(model) {
        this.model = model;
        //pass the container width and height to root view state.
        let viewState = this.model.getViewState();
        viewState.container = {
            width : this.container.width(),
            height : this.container.height()
        };
        this.model.on('tree-modified', () => {
            this.forceUpdate();
        });
    }

    getModel() {
        return this.model;
    }

    render() {
        // Following is how we render the diagram.
        // 1. We will visit the model tree and calculate width and height of all
        //    the elements. We will use DimensionCalcVisitor.
        this.model.accept(this.dimentionCalc);
        // 2. Now we will visit the model again and calculate position of each node
        //    in the tree. We will use PositionCalcVisitor for this.
        this.model.accept(this.positionCalc);
        // 3. Now we need to create component for each child of root node.
        let [pkgDef, imports, constants, others] = [undefined, [], [], []];
        let otherNodes = [];
        this.model.children.forEach((child) => {
            switch (child.constructor.name) {
                case 'ImportDeclaration':
                    break;
                case 'ConstantDefinition':
                    break;
                default:
					otherNodes.push(child);
            }
        });
        others = getComponentForNodeArray(otherNodes);
        // 4. Ok we are all set, now lets render the diagram with React. We will create
        //    s CsnvasDecorator and pass child components for that.
        let viewState = this.model.getViewState();
        return <CanvasDecorator dropTarget={this.model} title="StatementContainer" bBox={viewState.bBox}>
                   {others}
               </CanvasDecorator>
    }

    getChildContext() {
        return {
            dragDropManager: this.props.dragDropManager ,
            messageManager: this.props.messageManager,
            container : this.props.container,
            renderer: this.props.renderer,
            renderingContext: this.props.renderingContext
        };
    }
}

Diagram.propTypes = {
	bBox: PropTypes.shape({
		x: PropTypes.number.isRequired,
		y: PropTypes.number.isRequired,
		w: PropTypes.number.isRequired,
		h: PropTypes.number.isRequired,
	}),
  editor: PropTypes.instanceOf(Object).isRequired,
  dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
  renderer: PropTypes.instanceOf(Renderer).isRequired
}

Diagram.childContextTypes = {
    dragDropManager: PropTypes.instanceOf(DragDropManager).isRequired,
    messageManager: PropTypes.instanceOf(MessageManager).isRequired,
    container: PropTypes.instanceOf(Object).isRequired,
    renderer: PropTypes.instanceOf(Renderer).isRequired,
    renderingContext: PropTypes.instanceOf(Object).isRequired
};

export default Diagram;
