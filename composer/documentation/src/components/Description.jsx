/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the ''License''); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * ''AS IS'' BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import React from 'react';
import ReactMarkdown from 'react-markdown/with-html';
import htmlParser from 'react-markdown/plugins/html-parser';
import { diffChars } from 'diff';
import escape from 'escape-html';
import './Description.css';

export default class Description extends React.Component {
    constructor(props) {
        super(props);
        this.diff = [{
            value: props.source,
        }];
    }

    shouldComponentUpdate(newProps) {
        return this.props.source !== newProps.source;
    }

    componentWillReceiveProps(newProps) {
        this.diff = diffChars(this.props.source, newProps.source);
    }

    render() {
        let source = '';
        this.diff.forEach((part) => {
            if (part.removed) {
                return;
            }

            const value = escape(part.value);
            if(part.added) {
                source += `<span class="__added">${value}</span>`;
            } else {
                source += value;
            }
        })

        return <ReactMarkdown 
            source={source}
            escapeHtml={false}
            className={this.props.className}
            renderers={{
                inlineCode: block => {
                    const value = block.value.replace(
                        new RegExp('<span class="__added">((?:.|\s)*)<\/span>'), '$1');
                    return <code>{value}</code>
                }
            }}
        />
    }
}

Description.defaultProps = {
    source: '',
}