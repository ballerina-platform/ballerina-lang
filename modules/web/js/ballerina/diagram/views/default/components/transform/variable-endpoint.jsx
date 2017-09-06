/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import './variable-endpoint.css';

export default class VariableEndpoint extends React.Component {
    constructor(props, context) {
        super(props, context);
        this.state = {
            onEdit: false,
            statement: this.props.variable.varDeclarationString,
        };

        this.handleChange = this.handleChange.bind(this);
        this.onEdit = this.onEdit.bind(this);
        this.onComplete = this.onComplete.bind(this);
    }

    componentWillUnmount() {
        const { id, onRemove } = this.props;
        onRemove(id);
    }

    render() {
        const { variable, makeConnectPoint, level, id, removeTypeCallbackFunc, onClick, onRemove,
            updateVariable, isFolded, onConnectPointMouseEnter } = this.props;
        let iconType = 'fw-variable';
        let className = 'transform-endpoint variable';

        if (variable.type === 'struct') {
            iconType = 'fw-struct';
            className += ' transform-endpoint-struct';

            if (isFolded) {
                className += '-folded';
            }
        }

        let folderLeft = (level * 13) + 2;

        const variableRoot = variable.root || variable;
        if (variableRoot.endpointKind === 'output') {
            folderLeft += 30;
        }

        return (
            <div className={className} style={{ paddingLeft: level > 0 ? ((level - 1) * 13) + 7 : 7 }}>
                <span >
                    {(variable.type === 'struct' || variable.isField) &&
                    <span
                        className='folder'
                        style={{ left: folderLeft }}
                        onClick={(e) => { onClick && onClick(variable.name); }}
                    />}
                    {(level > 0) && <span className='tree-view-icon' />}
                    <span className='variable-icon'>
                        <i className={`transform-endpoint-icon fw ${iconType}`} />
                    </span>
                    <span className='variable-content' onClick={(e) => { onClick && onClick(variable.name); }}>
                        {!this.state.onEdit && variable.displayName &&
                            <span className='property-name'>
                                {variable.displayName}:
                            </span>
                        }
                        {!this.state.onEdit &&
                        <span className='property-type'>
                            {variable.typeName || variable.type}
                        </span>
                        }
                        {this.props.variable.varDeclarationString && !this.state.onEdit &&
                            <span>
                                <i className='btn fw fw-edit' onClick={this.onEdit} />
                            </span>
                        }
                        { this.state.onEdit &&
                        <input
                            type='text'
                            className='variable-edit-text'
                            value={this.state.statement}
                            onChange={this.handleChange}
                        />
                        }
                        { this.state.onEdit &&
                        <span>
                            <i className='btn fw fw-check' onClick={this.onComplete} />
                        </span>
                        }
                    </span>
                </span>
                <span
                    id={variable.id + '-button'}
                    className='btn connect-point'
                    onMouseEnter={(e) => { onConnectPointMouseEnter(variable, e); }}
                >
                    <i
                        id={id}
                        ref={icon => makeConnectPoint(icon, id, variable)}
                        className='variable-endpoint fw fw-circle-outline fw-stack-1x'
                    />
                </span>
            </div>
        );
    }
    handleChange(e) {
        this.setState({ statement: e.target.value });
    }

    onEdit() {
        this.setState({ onEdit: true });
    }
    onComplete() {
        this.setState({ onEdit: false });
        this.props.updateVariable(this.props.variable.name, this.state.statement, this.props.type);
    }
}
