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
import ReactDOM from 'react-dom';
import PropTypes from 'prop-types';
import { getComponentForNodeArray } from './utils';

class AnnotationEntry extends React.Component {
    constructor(props) {
        super(props);
    }

    render() {
        const model = this.props.model;

        const rValue = model.getRightValue();
        let rPrint = '';
        if (typeof rValue === 'string') {
            rPrint = rValue;
        } else if (rValue.constructor.name == 'AnnotationEntryArray') {
            rPrint = rValue.toString();
        }

        return (<span>
          <span className="annotation-value-key">{`${model.getLeftValue()} :`}</span>
          <span className="annotation-value">{ rPrint }</span>
        </span>);
    }
}

export default AnnotationEntry;
