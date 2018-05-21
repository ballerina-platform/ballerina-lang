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
import { Segment, Icon } from 'semantic-ui-react';
import ReactJson from 'react-json-view';

function isJson(text) {
    if (typeof text !== 'string') {
        return false;
    }
    try {
        if (typeof JSON.parse(text) === 'string') {
            return false;
        }
        return true;
    } catch (error) {
        return false;
    }
}

class DetailView extends React.Component {
    render() {
        const { meta } = this.props;
        const payload = meta.payload;
        return (
            <Segment className='detail-view' inverted>
                <Icon name='close' className='close' onClick={this.props.hideDetailView} />
                <code>
                    <pre>
                        {meta.headers}
                    </pre>
                </code>
                {
                    meta.contentType === 'application/json' && isJson(payload) ?
                        <ReactJson
                            src={JSON.parse(payload)}
                            theme='eighties'
                            name={false}
                            displayDataTypes={false}
                            collapsed={1}
                            displayObjectSize={false}
                            style={{ marginTop: 10, background: 'inherit' }}
                        /> : <code><pre>{meta.payload}</pre></code>
                }

            </Segment>
        );
    }
}


DetailView.defaultProps = {

};

export default DetailView;
