/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import { Dropdown, Icon } from 'semantic-ui-react';
import CompilationUnitNode from '../model/tree/compilation-unit-node';
import TopLevelElements from '../tool-palette/item-provider/compilation-unit-tools';


class AddDefinitionMenu extends React.Component {

    render() {
        return (
            <Dropdown icon='fw fw-add left-icon' text='Definition' pointing className='link item' button className=' primary definition-dropdown'>
                <Dropdown.Menu>
                    {
                        TopLevelElements.map((element) => {
                            if (element.id === 'constructs_seperator') {
                                return <Dropdown.Divider />;
                            }
                            return (<Dropdown.Item
                                onClick={
                                (event, item) => {
                                    if (item.data.id === 'struct') {
                                        this.context.command.dispatch('show-import-struct-dialog');
                                        return;
                                    }
                                    // Handle struct / transformer addition.
                                    const newNode = item.data.nodeFactoryMethod();
                                    item.model.acceptDrop(newNode);
                                }
                                }
                                data={element}
                                model={this.props.model}
                            >
                                <Icon name={`fw fw-${element.icon}`} size='mini' />
                                {element.name}
                            </Dropdown.Item>);
                        })
                    }
                </Dropdown.Menu>
            </Dropdown>
        );
    }
}

AddDefinitionMenu.propTypes = {
    model: PropTypes.instanceOf(CompilationUnitNode).isRequired,
};

AddDefinitionMenu.defaultProps = {

};

AddDefinitionMenu.contextTypes = {
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
};

AddDefinitionMenu.childContextTypes = {

};

export default AddDefinitionMenu;
