import React from "react";
import PropTypes from 'prop-types';

class Arrow extends React.Component {
	getArrowAngle() {
		const { start, end } = this.props;
		var rad = Math.atan2(deltaY, deltaX);
		var deg = rad * (180 / Math.PI);

		return deg;
	}
	render() {
		const { start, end, children, dashed } = this.props;
		const arrowSize = 5;
		var deltaX = end.x - start.x;
		var deltaY = end.y - start.y;


		let className = "action-arrow";
		if(dashed) {
			className = "action-arrow action-dash-line";
		}
		return (<g className={className}>
				<polygon
						points={`0,-${arrowSize} ${arrowSize},0 0,${arrowSize}`}
						transform={`translate(${end.x}, ${end.y})
						rotate(${this.getArrowAngle()}, 0, 0)`}
				/>
       	<line x1={start.x} x2={end.x} y1={start.y} y2={end.y} stroke="black" />
		</g>);
  }
}

Arrow.propTypes = {
	start: PropTypes.shape({
		x: React.PropTypes.number.isRequired,
		y: React.PropTypes.number.isRequired,
	}),
  end: PropTypes.shape({
    x: React.PropTypes.number.isRequired,
    y: React.PropTypes.number.isRequired,
  })
}

Arrow.defaultProps = {
	dashed: false
}

export default Arrow;
