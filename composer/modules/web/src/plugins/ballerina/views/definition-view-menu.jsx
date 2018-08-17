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
import { Icon, Popup, Label } from 'semantic-ui-react';
import CompilationUnitNode from '../model/tree/compilation-unit-node';
import TreeUtil from '../model/tree-util';
import { RESPOSIVE_MENU_TRIGGER } from '../constants';

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
        let records = [];

        if (this.props.model) {
            structs = this.props.model.topLevelNodes.filter((node) => { return TreeUtil.isObject(node); });
            endpoints = this.props.model.topLevelNodes.filter((node) => { return TreeUtil.isEndpoint(node); });
            records = this.props.model.topLevelNodes.filter((node) => { return TreeUtil.isRecord(node); });
        }

        return (
            <div className={'top-bar ' + (this.props.width > RESPOSIVE_MENU_TRIGGER.ICON_MODE ? '' : 'mobile-top-bar')}>
                { structs.length > 0 &&
                    <Popup
                        trigger={
                            <Label as='a' color='white'>
                                <Icon name={'fw fw-struct'} />
                                <span>Objects</span>
                                <Label.Detail>{structs.length}</Label.Detail>
                            </Label>
                        }
                        className='definitions-popup-window'
                        position='bottom right'
                        flowing
                        wide
                        hideOnScroll
                    >
                        {
                            <div>
                                { this.props.width < RESPOSIVE_MENU_TRIGGER.ICON_MODE &&
                                    <div className='definitions-popup-window-header'>
                                        <Label as='span' color='white'>
                                            <Icon name={'fw fw-struct'} />
                                            <span>Objects</span>
                                        </Label>
                                        <hr />
                                    </div>
                                }
                                {
                                    structs.map((element) => {
                                        return this.getItem(element.getName().getValue(),
                                                            () => { this.onDelete(element); });
                                    })
                                }
                            </div>
                        }
                    </Popup>
                }
                { records.length > 0 &&
                    <Popup
                        trigger={
                            <Label as='a' color='white'>
                                <Icon name={'fw fw-records'} />
                                <span>Records</span>
                                <Label.Detail>{records.length}</Label.Detail>
                            </Label>
                        }
                        className='definitions-popup-window'
                        position='bottom right'
                        flowing
                        wide
                        hideOnScroll
                    >
                        {
                            <div>
                                { this.props.width < RESPOSIVE_MENU_TRIGGER.ICON_MODE &&
                                    <div className='definitions-popup-window-header'>
                                        <Label as='span' color='white'>
                                            <Icon name={'fw fw-records'} />
                                            <span>Records</span>
                                        </Label>
                                        <hr />
                                    </div>
                                }
                                {
                                    records.map((element) => {
                                        return this.getItem(element.getName().getValue(),
                                                            () => { this.onDelete(element); });
                                    })
                                }
                            </div>
                        }
                    </Popup>
                }
                { endpoints.length > 0 &&
                    <Popup
                        trigger={
                            <Label as='a' color='white'>
                                <Icon name={'fw fw-endpoint'} />
                                <span>Endpoints</span>
                                <Label.Detail>{endpoints.length}</Label.Detail>
                            </Label>
                        }
                        className='definitions-popup-window'
                        position='bottom right'
                        flowing
                        wide
                        hideOnScroll
                    >
                            {
                                <div>
                                    { this.props.width < RESPOSIVE_MENU_TRIGGER.ICON_MODE &&
                                        <div className='definitions-popup-window-header'>
                                            <Label as='span' color='white'>
                                                <Icon name={'fw fw-endpoint'} />
                                                <span>Endpoints</span>
                                            </Label>
                                            <hr />
                                        </div>
                                    }
                                    {
                                        endpoints.map((element) => {
                                            return this.getItem(element.getName().getValue(),
                                                                () => { this.onDelete(element); });
                                        })
                                    }
                                </div>
                            }
                    </Popup>
                }
            </div>
        );
    }
}

DefinitionViewMenu.propTypes = {
    model: PropTypes.instanceOf(CompilationUnitNode).isRequired,
    width: PropTypes.number.isRequired,
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
