import React from 'react';
import LifeLine from './lifeline.jsx';
import StatementContainer from './statement-container';
import StatementView from './statement-decorator';
import PanelDecorator from './panel-decorator';

class FunctionDefinition extends React.Component {

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getFunctionName();
        return (<PanelDecorator title={name} bBox={bBox}>
                    <LifeLine title="FunctionWorker" bBox={{x: bBox.x + 50, w: 200 , h: bBox.h - 100, y: bBox.y + 50}}/>
                    <StatementContainer>
                      <StatementView bBox={{x:bBox.x + 60, y:bBox.y + 90, w:181.7, h:30}} expression="http:convertToResponse(m)">
                      </StatementView>
                    </StatementContainer>
                </PanelDecorator>);
    }
}

export default FunctionDefinition;
