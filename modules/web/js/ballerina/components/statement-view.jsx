import React from "react"
import * as defaults from "../configs/designer-defaults.js"
import PropTypes from 'prop-types';

class StatementView extends React.Component {

	render() {
    const {x, y, width, height, children} = this.props;
		let text_y = y + defaults.text_offset;
		let text_x = defaults.text_offset / 2;

		return (<g>
			<rect x={x} y={y} width={width} height={ height } rx="0" ry="0" className="statement-rect" />
			<text x={text_x} y={text_y} className="statement-text">{ children }</text>
		</g>);
    }
}

StatementView.propTypes = {
	width: PropTypes.number,
	height: PropTypes.number,
	x: PropTypes.number,
	y: PropTypes.number,
  children: PropTypes.string
}

StatementView.defaultProps = {
	width: 0,
	height: 0,
	x: 0,
	y: 0,
  children: "Untitled"
}


export default StatementView;
