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

class Arrow extends React.Component {
	getArrowAngle() {
		const { start, end } = this.props;
		var deltaX = end.x - start.x;
		var deltaY = end.y - start.y;
		var rad = Math.atan2(deltaY, deltaX);
		var deg = rad * (180 / Math.PI);

		return deg;
	}
	render() {
		const { start, end, children, dashed, arrowSize } = this.props;

		let className = "action-arrow";
		if(dashed) {
			className = "action-arrow action-dash-line";
		}
		return (<g >
				<polygon
						points={`0,-${arrowSize} ${arrowSize},0 0,${arrowSize}`}
						transform={`translate(${end.x}, ${end.y})
						rotate(${this.getArrowAngle()}, 0, 0)`}
						className="action-arrow-head"
				/>
       	<line x1={start.x} x2={end.x} y1={start.y} y2={end.y} className={className} />
		</g>);
  }
}

Arrow.propTypes = {
	start: PropTypes.shape({
		x: PropTypes.number.isRequired,
		y: PropTypes.number.isRequired,
	}),
  end: PropTypes.shape({
    x: PropTypes.number.isRequired,
    y: PropTypes.number.isRequired,
  })
}

Arrow.defaultProps = {
	dashed: false,
	arrowSize: 5
}

export default Arrow;
