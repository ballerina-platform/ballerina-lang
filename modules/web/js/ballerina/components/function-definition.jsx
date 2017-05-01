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
import LifeLine from './lifeline.jsx';
import StatementContainer from './statement-container';
import PanelDecorator from './panel-decorator';
import {getComponentForNodeArray} from './utils';

class FunctionDefinition extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        const bBox = this.props.model.viewState.bBox;
        const name = this.props.model.getFunctionName();
        const statementContainer = this.props.model.viewState.components.statementContainer;
        let func_worker_bBox = this.props.model.viewState.components.defaultWorker;
        var children = getComponentForNodeArray(this.props.model.getChildren());
        return (<PanelDecorator icon="tool-icons/function" title={name} bBox={bBox}>
                    <StatementContainer title="StatementContainer" bBox={statementContainer}/>
                    <LifeLine title="FunctionWorker" bBox={func_worker_bBox}/>
                    {children}
                </PanelDecorator>);
    }
}

export default FunctionDefinition;
