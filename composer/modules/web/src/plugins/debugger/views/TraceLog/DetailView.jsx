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
import { Segment, Icon, Accordion } from 'semantic-ui-react';

class DetailView extends React.Component {
    constructor() {
        super();
        this.handleClick = this.handleClick.bind(this);

        this.state = {
            activeIndex: 0,
        };
    }

    handleClick(e, titleProps) {
        const { index } = titleProps;
        const { activeIndex } = this.state;
        const newIndex = activeIndex === index ? -1 : index;
        this.setState({ activeIndex: newIndex });
    }

    render() {
        const { details, rawLog } = this.props;
        const { activeIndex } = this.state;

        return (
            <Segment className='detail-view' inverted>
                <Icon name='close' className='close' onClick={this.props.hideDetailView} />
                <Accordion>
                    <Accordion.Title index={0} onClick={this.handleClick} active={activeIndex === 0}>
                        <Icon name='dropdown' />
                        Headers
                    </Accordion.Title>
                    <Accordion.Content active={activeIndex === 0}>
                        <code>{details}</code>
                    </Accordion.Content>
                    <Accordion.Title index={1} onClick={this.handleClick} active={activeIndex === 1}>
                        <Icon name='dropdown' />
                        Raw Log
                    </Accordion.Title>
                    <Accordion.Content active={activeIndex === 1}>
                        <code>{JSON.stringify(rawLog)}</code>
                    </Accordion.Content>
                </Accordion>
            </Segment>
        );
    }
}

export default DetailView;
