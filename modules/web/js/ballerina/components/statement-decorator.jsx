import React from "react";
import PropTypes from 'prop-types';

const text_offset = 50;

class StatementView extends React.Component {

	render() {
		const { bBox, children } = this.props;
		return (<g>
						<rect x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h} className="statement-rect" />
						<g className="statement-body">{ children }</g>
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
  children: PropTypes.node.isRequired
}


export default StatementView;
