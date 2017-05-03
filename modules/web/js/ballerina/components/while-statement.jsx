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
import {getComponentForNodeArray} from './utils';
import BlockStatementDecorator from "./block-statement-decorator";
import SimpleBBox from './../ast/simple-bounding-box';
import * as DesignerDefaults from './../configs/designer-defaults';

class WhileStatement extends React.Component {

	render() {
		let model = this.props.model,
			bBox = model.viewState.bBox;
		let blockStatementBBox = new SimpleBBox();
		blockStatementBBox.x = bBox.x;
		blockStatementBBox.y = bBox.y + DesignerDefaults.statement.gutter.v;
		blockStatementBBox.h = bBox.h - DesignerDefaults.statement.gutter.v;
		blockStatementBBox.w = bBox.w;
		const children = getComponentForNodeArray(this.props.model.getChildren());
		return (<g>
			<rect x={bBox.x} y={bBox.y} width={bBox.w} height={DesignerDefaults.statement.gutter.v} className="inner-drop-zone" />
			<BlockStatementDecorator bBox={blockStatementBBox} title={"While"}>
				{children}
			</BlockStatementDecorator>
		</g>);
	}
}

WhileStatement.propTypes = {
	bBox: PropTypes.shape({
		x: PropTypes.number.isRequired,
		y: PropTypes.number.isRequired,
		w: PropTypes.number.isRequired,
		h: PropTypes.number.isRequired,
	})
}


export default WhileStatement;
