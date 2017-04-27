import React from "react";
import StatementDecorator from "./statement-decorator";
import PropTypes from 'prop-types';

class FunctionInvocationStatement extends React.Component {

	render() {
		let model = this.props.model,
				bBox = model.viewState.bBox;
		const text_x = bBox.x + (bBox.w / 2);
		const text_y = bBox.y + (bBox.h / 2);
		return (<StatementDecorator bBox={bBox}>
							<text x={text_x} y={text_y} className="statement-text">{model.expression}</text>
						</StatementDecorator>) ;
    }
}

FunctionInvocationStatement.propTypes = {
	bBox: PropTypes.shape({
		x: PropTypes.number.isRequired,
		y: PropTypes.number.isRequired,
		w: PropTypes.number.isRequired,
		h: PropTypes.number.isRequired,
	}),
	model: PropTypes.shape({
		expression:  PropTypes.string
	})
}


export default FunctionInvocationStatement;