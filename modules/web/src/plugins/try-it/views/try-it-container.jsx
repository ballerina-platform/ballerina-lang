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

import _ from 'lodash';
import React from 'react';
import PropTypes from 'prop-types';
import { parseFile } from 'api-client/api-client';
import TreeBuilder from 'ballerina/model/tree-builder';
import File from 'core/workspace/model/file';
import HttpClient from './http-client';

import './try-it-container.scss';


/**
 * Container for the try-it client.
 * @class TryItContainer
 * @extends {React.Component}
 */
class TryItContainer extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            compilationUnit: undefined,
        };
    }

    componentDidMount() {
        this.updateView();
        this.forceUpdate();
    }

    updateView() {
        if (this.props.balFile) {
            parseFile(this.props.balFile)
                .then((jsonTree) => {
                    if (!(_.isNil(jsonTree.model) || _.isNil(jsonTree.model.kind))) {
                        const ast = TreeBuilder.build(jsonTree.model);
                        this.setState({
                            compilationUnit: ast,
                        });
                    }
                }).catch((error) => {
                    console.log(error);
                    this.setState({
                        compilationUnit: undefined,
                    });
                });
        }
    }

    /**
     * Renders the client
     * @returns {ReactElement} The client.
     * @memberof TryItContainer
     */
    renderClient() {
        // if (this.props.serviceDefinition.getProtocolPkgName() === 'http') {
        return (<HttpClient compilationUnit={this.state.compilationUnit} />);
        // } else {
            // return (null);
        // }
    }

    /**
     * Renders the client.
     * @returns {ReactElement} The view.
     * @memberof TryItContainer
     */
    render() {
        const client = this.renderClient();
        return (<div className='try-it-container'>
            {client}
        </div>);
    }
}

TryItContainer.propTypes = {
    balFile: PropTypes.instanceOf(File),
};

TryItContainer.defaultProps = {
    balFile: undefined,
};

export default TryItContainer;
