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
import { RESPOSIVE_MENU_TRIGGER } from '../constants';

class DiagramMenu extends React.Component {

    constructor(props) {
        super(props);
    }

    render() {
        return (
            <Menu className={'top-menu ' + (this.props.width > RESPOSIVE_MENU_TRIGGER.HIDDEN_MODE ? '' : 'hidden' ) +
                (this.props.width > RESPOSIVE_MENU_TRIGGER.ICON_MODE ? '' : ' mobile-top-bar')}
                style={{ width: this.props.width }}>
                <Menu.Menu position='left'>
                    { !this.props.editMode &&
                    <Menu.Item onClick={() => { this.props.onModeChange({ editMode: true }); }}
                        className='menu-button ui button secondary'
                    >
                        <Icon name='fw fw-uneditable menu-icon-right' />
                        <span className='text'>Close Edit</span>
                    </Menu.Item>
                    }
                    { this.props.editMode &&
                        <Menu.Item onClick={() => { this.props.onModeChange({ editMode: false }); }}
                            className='menu-button ui button primary'
                        >
                            <Icon name='fw fw-edit menu-icon-right' />
                            <span className='text'>Edit</span>
                        </Menu.Item>
                    }
                    { this.props.mode === 'action' &&
                    <Button.Group>
                        <Button icon onClick={() => { this.props.onCodeExpandToggle({ mode: 'default' }); }}>
                            <Icon name='fw fw-expand' title='Expand Code' />
                        </Button>
                    </Button.Group>
                    }
                    { this.props.mode === 'default' &&
                    <Button.Group>
                        <Button icon onClick={() => { this.props.onCodeExpandToggle({ mode: 'action' }); }}>
                            <Icon name='fw fw-collapse' title='Collapse Code' />
                        </Button>
                    </Button.Group>
                    }
                    { this.props.editMode &&
                    <Button.Group>
                        <Button icon onClick={(e) => { this.props.zoomInHandle(e); }}>
                            <Icon name='fw fw-zoom-in' title='Zoom In' />
                        </Button>
                        <Button icon onClick={(e) => { this.props.zoomOutHandle(e); }}>
                            <Icon name='fw fw-zoom-out' title='Zoom Out' />
                        </Button>
                        <Button icon onClick={(e) => { this.props.zoomFitHandle(e); }}>
                            <Icon name='fw fw-fit' title='Zoom Fit' />
                        </Button>
                    </Button.Group>
                    }
                    { !this.props.editMode &&
                    <Menu.Item>
                        <AddDefinitionMenu model={this.props.model} />
                    </Menu.Item>
                    }
                </Menu.Menu>
                <Menu.Menu position='right' className='definitions-menu'>
                    <DefinitionViewMenu on model={this.props.model} width={this.props.width} />
                </Menu.Menu>
            </Menu>
        );
    }
}

DiagramMenu.propTypes = {
    width: PropTypes.number.isRequired,
    model: PropTypes.instanceOf(CompilationUnitNode).isRequired,
    mode: PropTypes.string.isRequired,
};

DiagramMenu.defaultProps = {

};

DiagramMenu.contextTypes = {

};

DiagramMenu.childContextTypes = {

};

export default DiagramMenu;
