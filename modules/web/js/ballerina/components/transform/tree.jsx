import React from 'react';
import VariableEndpoint from './variable-endpoint.jsx';
import './tree.css'

export default class Tree extends React.Component {
    renderStruct(endpoint, level) {
        const { makeConnectPoint, viewId, type, removeTypeCallbackFunc } = this.props;
        const key = `${endpoint.fieldName || endpoint.name}:${viewId}`;
        return <div>
            <VariableEndpoint
                key={key}
                id={key}
                type={type}
                variable={endpoint}
                endpointKind={endpoint.isInner ? 'property' : 'struct-head'}
                makeConnectPoint={makeConnectPoint}
                level={level}
                removeTypeCallbackFunc={removeTypeCallbackFunc}
            />
            {
                endpoint.properties.map(property => {
                    const key = `${property.fieldName}:${viewId}`;

                    if(property.innerType) {
                        return this.renderStruct(property.innerType, level+1);
                    }

                    return <VariableEndpoint
                        key={key}
                        id={key}
                        type={type}
                        variable={property}
                        endpointKind='property'
                        makeConnectPoint={makeConnectPoint}
                        level={level+1}
                        removeTypeCallbackFunc={removeTypeCallbackFunc}
                    />
                })
            }
        </div>
    }

    render() {
        const { endpoints, type, makeConnectPoint, viewId, removeTypeCallbackFunc } = this.props;
        return (
            <div className='transform-endpoint-tree'>
                { endpoints.map(endpoint => {
                    if (endpoint.type != 'struct') {
                        const key = `${endpoint.fieldName || endpoint.name}:${viewId}`;

                        return (
                            <div className='transform-endpoint-container'>
                                <VariableEndpoint
                                    key={key}
                                    id={key}
                                    type={type}
                                    variable={endpoint}
                                    endpointKind='variable'
                                    makeConnectPoint={makeConnectPoint}
                                    level={0}
                                    removeTypeCallbackFunc={removeTypeCallbackFunc}
                                />
                            </div>
                        );
                    }

                    return (
                        <div className='transform-endpoint-container'>
                            { this.renderStruct(endpoint, 0) }
                        </div>
                    );
                    }
                )}
            </div>
        );
    }
}
