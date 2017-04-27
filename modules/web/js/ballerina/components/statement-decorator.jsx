import React from "react"

const text_offset = 50;

class StatementView extends React.Component {

	render() {
		const bBox = this.props.bBox;
		return <g>
						<rect x={bBox.x} y={bBox.y} width={bBox.w} height={bBox.h} rx="0" ry="0" className="statement-rect" />
						<g className="statement-body">{ this.props.children }</g>
					</g>
    }
}

export default StatementView;
