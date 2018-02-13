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
import PropTypes from 'prop-types';
import { Button } from 'react-bootstrap';
import Linkify from 'react-linkify';
import { TryItConstants } from '@ballerina-lang/composer-try-it-plugin';
import { ToolBarConstants } from '@ballerina-lang/composer-core';
import LaunchManager from '../../LaunchManager/LaunchManager';

import './http-service-decorator.scss';

/**
 * Component for http service message in the console.
 * @class HttpServiceDecorator
 * @extends {React.Component}
 */
class HttpServiceDecorator extends React.Component {
    /**
     * @override
     */
    componentDidMount() {
        this.props.command.dispatch(ToolBarConstants.COMMANDS.UPDATE_TOOL_BAR);
    }

    /**
     * Renders the message.
     * @returns {ReactElement} The view.
     * @memberof HttpServiceDecorator
     */
    render() {
        return (<div className='console-http-service-decorator'>
            <Linkify properties={{ target: '_blank' }}>{this.props.message.message}</Linkify>
            <Button
                bsStyle='primary'
                disabled={!LaunchManager.active}
                onClick={() => {
                    this.props.command.dispatch(TryItConstants.COMMANDS.SHOW_TRY_IT);
                }}
            >
                Try-It
        </Button>
        </div>);
    }
}

HttpServiceDecorator.propTypes = {
    message: PropTypes.instanceOf(Object).isRequired,
    command: PropTypes.instanceOf(Object).isRequired,
};

export default HttpServiceDecorator;
