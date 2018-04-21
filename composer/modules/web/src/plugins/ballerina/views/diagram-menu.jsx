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
import { Icon, Input, Button, Menu, Dropdown } from 'semantic-ui-react';
import CompilationUnitNode from '../model/tree/compilation-unit-node';
import AddDefinitionMenu from './add-definition-menu';
import DefinitionViewMenu from './definition-view-menu';

class DiagramMenu extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Menu className='top-menu' style={{ width: this.props.width }}>
                { !this.props.fitToWidth &&
                <Menu.Menu position='left'>
                    <Menu.Item>
                        <Input className='package-input' icon='fw fw-package' iconPosition='left' placeholder='Package...' />
                        <AddDefinitionMenu model={this.props.model} />
                    </Menu.Item>
                    <Menu.Item onClick={() => { this.props.onModeChange({ mode: 'action', fitToWidth: true }); }}>
                        <Icon name='fw fw-uneditable menu-icon' />
                    </Menu.Item>
                </Menu.Menu>
                }
                { !this.props.fitToWidth &&
                <Menu.Menu position='right'>
                    <DefinitionViewMenu model={this.props.model} />
                    { this.props.mode === 'action' &&
                    <Menu.Item onClick={() => { this.props.onModeChange({ mode: 'default', fitToWidth: false }); }}>
                        <Icon name='fw fw-zoom-in menu-icon-right' />
                    </Menu.Item>}
                    { this.props.mode === 'default' &&
                    <Menu.Item onClick={() => { this.props.onModeChange({ mode: 'action', fitToWidth: false }); }}>
                        <Icon name='fw fw-zoom-out menu-icon-right' />
                    </Menu.Item>}
                </Menu.Menu>
                }
                { this.props.fitToWidth &&
                <Menu.Menu position='left'>
                    <Menu.Item onClick={() => { this.props.onModeChange({ mode: 'action', fitToWidth: false }); }} className='menu-button ui button primary'>
                        <Icon name='fw fw-edit' />
                        Edit
                    </Menu.Item>
                </Menu.Menu>
                }
            </Menu>
        );
    }
}

DiagramMenu.propTypes = {
    width: PropTypes.number.isRequired,
    model: PropTypes.instanceOf(CompilationUnitNode).isRequired,
};

DiagramMenu.defaultProps = {

};

DiagramMenu.contextTypes = {

};

DiagramMenu.childContextTypes = {

};

export default DiagramMenu;
