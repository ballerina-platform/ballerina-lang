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
import React from "react";
import StatementDecorator from "./statement-decorator";
import StatementArrowConnection from './statement-arrow-connection';
import PropTypes from 'prop-types';
import BallerinaASTFactory from './../ast/ballerina-ast-factory';

class WorkerReplyStatement extends React.Component {

    constructor(props){
        super(props);
        this.editorOptions = {
            propertyType: 'text',
            key: 'WorkerReplyStatement',
            model: props.model,
            getterMethod: props.model.getStatementString,
            setterMethod: props.model.setStatementFromString
        };
    }

    render() {
        let model = this.props.model,
            expression = model.viewState.expression;
        if (!_.isUndefined(model.getDestination())) {
            let workerReplyStatement = model.getDestination().findChild(BallerinaASTFactory.isReplyStatement);
            if (!_.isUndefined(workerReplyStatement)) {
                    return (<g>
                      <StatementDecorator model={model} viewState={model.viewState}
                                          expression={expression} editorOptions={this.editorOptions} />);
                      <StatementArrowConnection start={workerReplyStatement.viewState} end={model.viewState} />
                    </g>);
            } else {
                return (<StatementDecorator model={model} viewState={model.viewState}
                                            expression={expression} editorOptions={this.editorOptions} />);
            }
        } else {
            return (<StatementDecorator model={model} viewState={model.viewState}
                                        expression={expression} editorOptions={this.editorOptions} />);
        }
    }
}

WorkerReplyStatement.propTypes = {
    bBox: PropTypes.shape({
        x: PropTypes.number.isRequired,
        y: PropTypes.number.isRequired,
        w: PropTypes.number.isRequired,
        h: PropTypes.number.isRequired,
    })
}


export default WorkerReplyStatement;
