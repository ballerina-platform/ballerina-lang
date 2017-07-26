import React from 'react';
import VariableEndpoint from './variable-endpoint.jsx';
import './tree.css'

export default class Tree extends React.Component {
    renderStruct(endpoint, structId, level) {
        const { makeConnectPoint } = this.props;
        const type = endpoint.typeName || endpoint.type;
        return <div>
            <VariableEndpoint
                key={structId}
                id={structId}
                type={endpoint.typeName || endpoint.type}
                variable={endpoint}
                makeConnectPoint={makeConnectPoint}
                level={level}
            />
            {
                endpoint.properties.map(property => {
                    const key =`${structId}.${property.name}:${property.typeName || property.type}`

                    if(property.innerType) {
                        return this.renderStruct(property.innerType, key, level+1);
                    }

                    return <VariableEndpoint
                        key={key}
                        id={key}
                        type={property.type}
                        variable={property}
                        makeConnectPoint={makeConnectPoint}
                        level={level+1}
                    />
                })
            }
        </div>
    }

    render() {
        const { endpoints, type, makeConnectPoint } = this.props;
        return (
            <div className='transform-endpoint-tree'>
                { endpoints.map(endpoint => {
                    if (endpoint.type != 'struct') {
                        const key = `${endpoint.name}:${endpoint.type}`
                        return (
                            <div className='transform-endpoint-container'>
                                <VariableEndpoint
                                    key={key}
                                    id={key}
                                    type={type}
                                    variable={endpoint}
                                    makeConnectPoint={makeConnectPoint}
                                    level={0}
                                />
                            </div>
                        );
                    }

                    return (
                        <div className='transform-endpoint-container'>
                            { this.renderStruct(endpoint, `${endpoint.name}:${endpoint.typeName}`, 0) }
                        </div>
                    );
                    }
                )}
            </div>
        );
    }
}
