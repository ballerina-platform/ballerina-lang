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

import React from "react";
import PropTypes from 'prop-types';
import {blockStatement, statement} from '../configs/designer-defaults.js';
import StatementContainer from './statement-container';
import ASTNode from '../ast/node';
import SimpleBBox from '../ast/simple-bounding-box';
import ExpressionEditor from 'expression_editor_utils';

class BlockStatementDecorator extends React.Component {

    render() {
        const {bBox, title, dropTarget, expression} = this.props;
        let title_h = blockStatement.heading.height;
        let title_w = blockStatement.heading.width;

        // If Available get the title width from given props.
        if (this.props.titleWidth) {
            title_w = this.props.titleWidth;
        }

        let p1_x = bBox.x;
        let p1_y = bBox.y + title_h;
        let p2_x = bBox.x + title_w;
        let p2_y = bBox.y + title_h;
        let p3_x = bBox.x + title_w + 10;
        let p3_y = bBox.y;

        let stc_y = bBox.y + title_h;
        let stc_h = bBox.h - title_h;

        let title_x = bBox.x + title_w / 2;
        let title_y = bBox.y + title_h / 2;

        let statementContainerBBox = new SimpleBBox(bBox.x, stc_y, bBox.w, stc_h);

        let expression_x = 0;
        if (expression) {
            expression_x = p3_x + statement.padding.left;
        }

        this.conditionBox = new SimpleBBox(bBox.x, bBox.y, bBox.w, title_h)

        return (<g>
            <rect x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h} className="background-empty-rect"/>
            <rect x={bBox.x} y={bBox.y} width={bBox.w} height={title_h} rx="0" ry="0" className="statement-title-rect"
                  onClick={(e) => this.openExpressionEditor(e)}/>
            <text x={title_x} y={title_y} className="statement-text">{title}</text>
            {(expression) ? <text x={expression_x} y={title_y} className="condition-text"
                                  onClick={(e) => this.openExpressionEditor(e)}>{expression.text}</text> : null}
            <polyline points={`${p1_x},${p1_y} ${p2_x},${p2_y} ${p3_x},${p3_y}`} className="statement-title-polyline"/>
            <StatementContainer bBox={statementContainerBBox} dropTarget={dropTarget}>
                {this.props.children}
            </StatementContainer>
        </g>);

    }

    openExpressionEditor(e) {
        let options = this.props.editorOptions;
        let packageScope = this.context.renderingContext.packagedScopedEnvironemnt;
        if (this.props.expression && options) {
            new ExpressionEditor(this.conditionBox, this.context.container, (text) => this.onUpdate(text), options, packageScope);
        }
    }

    onUpdate(text) {
    }
}

BlockStatementDecorator.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    }),
    dropTarget: PropTypes.instanceOf(ASTNode).isRequired,
};

BlockStatementDecorator.contextTypes = {
    container: PropTypes.instanceOf(Object).isRequired,
    renderingContext: PropTypes.instanceOf(Object).isRequired,
};


export default BlockStatementDecorator;
