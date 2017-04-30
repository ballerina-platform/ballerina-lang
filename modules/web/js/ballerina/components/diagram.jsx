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
import CanvasDecorator from './canvas-decorator';
import FunctionDefinition from './function-definition';
import PositionCalcVisitor from '../visitors/position-calculator-visitor';
import DimensionCalcVisitor from '../visitors/dimension-calculator-visitor';
import {getComponentForNodeArray} from './utils';

class Diagram extends React.Component {

    constructor(props) {
        super(props);
        this.model = props.model;
        this.model.on('tree-modified', () => {
            this.forceUpdate();
        });
        this.dimentionCalc = new DimensionCalcVisitor();
        this.positionCalc = new PositionCalcVisitor();
    }

    setModel(model) {
        this.model = model;
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
        this.props.model.children.forEach((child) => {
            switch (child.constructor.name) {
                case 'PackageDefinition':
                    break;
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
        return <CanvasDecorator title="StatementContainer" bBox={viewState.bBox}>
                   {others}
               </CanvasDecorator>
    }
}

export default Diagram;
