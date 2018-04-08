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
import { Header, Icon, Grid, Popup, Button, Label } from 'semantic-ui-react';
import CompilationUnitNode from '../model/tree/compilation-unit-node';
import TreeUtil from '../model/tree-util';

class DefinitionViewMenu extends React.Component {

    constructor(props) {
        super(props);
        this.onView = this.onView.bind(this);
        this.getItem = this.getItem.bind(this);
        this.handleClose = this.handleClose.bind(this);
        this.handleOpen = this.handleOpen.bind(this);
        this.state = { isOpen: false };
    }

    onView(header) {
        const { designView } = this.context;
        designView.setTransformActive(true, header);
        this.setState({ isOpen: false });
    }

    onDelete(node) {
        this.props.model.removeTopLevelNodes(node);
        this.setState({ isOpen: false });
    }


    getItem(name, onDelete, onView) {
        return (
            <p className='definition-name'>{name}
                {onDelete && <Icon className='definition-icon' onClick={onDelete} name={'fw fw-delete'} />}
                {onView && <Icon className='definition-icon' onClick={onView} name={'fw fw-view'} />}
            </p>);
    }

    handleClose(node) {
        this.setState({ isOpen: false });
    }

    handleOpen(node) {
        this.setState({ isOpen: true });
    }

    render() {
        let structs = [];
        let endpoints = [];

        if (this.props.model) {
            structs = this.props.model.topLevelNodes.filter((node) => { return TreeUtil.isStruct(node); });
            endpoints = this.props.model.topLevelNodes.filter((node) => { return TreeUtil.isEndpoint(node); });
        }
        return (
            <Popup 
                trigger={ 
                    <Button as='div' labelPosition='right'>
                        <Grid divided className='top-bar' columns={2}>
                            <Grid.Row>
                                <Grid.Column width={8}>
                                    <Grid.Row className='top-bar-row'>
                                        <Label> 
                                            <Icon name={'fw fw-struct'} />
                                        </Label>
                                        <Label >Objects</Label>
                                        <Label >{structs.length}</Label>
                                    </Grid.Row>
                                </Grid.Column>
                                <Grid.Column width={8}>
                                    <Grid.Row>
                                        <Label> 
                                            <Icon name={'fw fw-endpoint'} />
                                        </Label>
                                        <Label left >Endpoints</Label>
                                        <Label >{endpoints.length}</Label>
                                    </Grid.Row>
                                </Grid.Column>
                            </Grid.Row>
                        </Grid>
                    </Button>
                    }
                flowing
                hoverable
                wide
                position='bottom center'
                open={this.state.isOpen}
                onClose={this.handleClose}
                onOpen={this.handleOpen}
            >
                {
                    <Grid divided columns={2} className='menu-pop-content'>
                        <Grid.Row>
                            <Grid.Column>
                                <Header as='h5'>
                                    <Icon name={'fw fw-struct'} />
                                    <Header.Content>Objects</Header.Content>
                                </Header>
                                {
                                    structs.map((element) => {
                                        return this.getItem(element.getName().getValue(),
                                                            () => { this.onDelete(element); });
                                    })
                                }
                            </Grid.Column>
                            <Grid.Column>
                                <Header as='h5'>
                                    <Icon size='mini' name={'fw fw-endpoint'} />
                                    <Header.Content>Endpoints</Header.Content>
                                </Header>
                                {
                                    endpoints.map((element) => {
                                        return this.getItem(element.getName().getValue(),
                                                            () => { this.onDelete(element); });
                                    })
                                }
                            </Grid.Column>
                        </Grid.Row>
                    </Grid>
            }

            </Popup>
        );
    }
}

DefinitionViewMenu.propTypes = {
    model: PropTypes.instanceOf(CompilationUnitNode).isRequired,
};

DefinitionViewMenu.defaultProps = {

};

DefinitionViewMenu.contextTypes = {
    command: PropTypes.shape({
        on: PropTypes.func,
        dispatch: PropTypes.func,
    }).isRequired,
    designView: PropTypes.instanceOf(Object).isRequired,
};

DefinitionViewMenu.childContextTypes = {

};

export default DefinitionViewMenu;
