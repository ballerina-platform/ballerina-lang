import React from "react";
import StatementDecorator from "./statement-decorator"

class FunctionInvocationStatement extends React.Component {

	render() {
		let model = this.props.model,
				bBox = model.viewState.bBox;
		return 	<StatementDecorator bBox={bBox}>
							<text x={bBox.x} y={bBox.y} className="statement-text">{model.expression}</text>
						</StatementDecorator> ;
    }
}

export default FunctionInvocationStatement;
