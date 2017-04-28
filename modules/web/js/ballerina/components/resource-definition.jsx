import React from 'react';
import LifeLine from './lifeline.jsx';
import StatementContainer from './statement-container';
import StatementView from './statement-decorator';
import PanelDecorator from './panel-decorator';

class ResourceDefinition extends React.Component {

    render() {
        const bBox = this.props.bBox;
        return (<PanelDecorator title={this.props.name} bBox={bBox}>
                    <LifeLine title="ResourceWorker" bBox={{x: bBox.x + 50, w: 200 , h: bBox.h - 100, y: bBox.y + 50}}/>
                    <StatementContainer>
                      <StatementView bBox={{x:bBox.x + 60, y:bBox.y + 90, w:181.7, h:30}} expression="http:convertToResponse(m)">
                      </StatementView>
                    </StatementContainer>
                </PanelDecorator>);
    }
}

export default ResourceDefinition;
