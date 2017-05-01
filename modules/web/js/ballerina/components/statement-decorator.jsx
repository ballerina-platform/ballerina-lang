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
import {statement} from './../configs/designer-defaults';

const text_offset = 50;

class StatementView extends React.Component {

	render() {
		const { bBox, expression } = this.props;
		// we need to draw a drop box above and a statement box
		let statement_h = bBox.h - statement.gutter.v;
		let statement_y = bBox.y + statement.gutter.v;
		const text_x = bBox.x + (bBox.w / 2);
		const text_y = statement_y + (statement_h / 2);
		let drop_zone_x = text_x - (statement.width/2);
		return (<g>
			<rect x={bBox.x} y={bBox.y} width={statement.width} height={statement.gutter.v} className="inner-drop-zone" />
			<rect x={bBox.x} y={statement_y} width={bBox.w} height={statement_h} className="statement-rect" />
			<g className="statement-body">
				<text x={text_x} y={text_y} className="statement-text">{expression}</text>
			</g>
		</g>);
	}
}

StatementView.propTypes = {
	bBox: PropTypes.shape({
		x: React.PropTypes.number.isRequired,
		y: React.PropTypes.number.isRequired,
		w: React.PropTypes.number.isRequired,
		h: React.PropTypes.number.isRequired,
	}),
	expression: PropTypes.string.isRequired
}


export default StatementView;
