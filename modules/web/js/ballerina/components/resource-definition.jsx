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
