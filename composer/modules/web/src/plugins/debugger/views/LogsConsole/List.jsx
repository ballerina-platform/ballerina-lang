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
import { Table } from 'semantic-ui-react';

/**
 *
 * @extends React.Component
 */
class List extends React.Component {

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className='logs-console-list'>
                <Table celled inverted>
                    <Table.Header>
                        <Table.Row>
                            <Table.HeaderCell>Activity Id</Table.HeaderCell>
                            <Table.HeaderCell>Channel</Table.HeaderCell>
                            <Table.HeaderCell>Service</Table.HeaderCell>
                        </Table.Row>
                    </Table.Header>

                    <Table.Body>
                        <Table.Row>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                        </Table.Row>
                        <Table.Row>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                        </Table.Row>
                        <Table.Row>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                        </Table.Row>
                        <Table.Row>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                            <Table.Cell>Lorem ipsum dolor</Table.Cell>
                        </Table.Row>
                    </Table.Body>
                </Table>

            </div>
        );
    }
}

List.propTypes = {

};

export default List;
