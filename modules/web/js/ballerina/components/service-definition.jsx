import React from 'react'
import ResourceDefinition from './resource-definition.jsx'
import StatementView from './statement-decorator.jsx'
import PanelDecorator from './panel-decorator';

class ServiceDefinition extends React.Component {

    render() {
        const bBox = this.props.bBox;
        return (<PanelDecorator title={this.props.name} bBox={bBox}>
                    <ResourceDefinition name="echo-resource" bBox={{x: 75, w:800, y:75, h:800}}>
                    </ResourceDefinition>
                </PanelDecorator>);
    }
}

export default ServiceDefinition;
