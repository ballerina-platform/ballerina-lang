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
import Constants from './constants';
import DefaultDecorator from './default-decorator';
import HttpServiceDecorator from './http-service-decorator';

const ConsoleDecorator = (props) => {
    let decoratedMessage = (<DefaultDecorator {...props} />);
    if (props.message.message.startsWith(Constants.HTTP_SERVICE_PREFIX)) {
        decoratedMessage = (<HttpServiceDecorator {...props} />);
    }

    return (<div className={props.message.type}>
        {decoratedMessage}
    </div>);
};

ConsoleDecorator.propTypes = {
    message: PropTypes.instanceOf(Object).isRequired,
};

export default ConsoleDecorator;
