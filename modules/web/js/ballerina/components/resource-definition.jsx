import React from 'react'
import LifeLine from './lifeline.jsx'
import StatementView from './statement-decorator.jsx'
import PanelDecorator from './panel-decorator';

class ResourceDefinition extends React.Component {

    render() {
        const bBox = this.props.bBox;
        return (<PanelDecorator title={this.props.name} bBox={bBox}>
                    <LifeLine title="ResourceWorker" bBox={{x: bBox.x + 50, w: 200 , h: bBox.h - 100, y: bBox.y + 50}}/>
                    {this.props.children}
                </PanelDecorator>);
    }
}

export default ResourceDefinition;
