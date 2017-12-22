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
import PropTypes from 'prop-types';
import Button from './button';
import Menu from './menu';
import Item from './item';

/**
 * Interaction lifeline button component
 */
class LifelineButton extends React.Component {

    constructor(props) {
        super(props);
        this.state = {
            listConnectors: false,
        };
        this.showConnectors = this.showConnectors.bind(this);
        this.hideConnectors = this.hideConnectors.bind(this);
    }

    showConnectors() {
        this.setState({ listConnectors: true });
    }

    hideConnectors() {
        this.setState({ listConnectors: false });
    }
    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        return (
            <Button
                bBox={this.props.bBox}
                buttonX={0}
                buttonY={0}
                showAlways
            >
                <Menu>
                    { !this.state.listConnectors &&
                    <div>
                        {this.props.items}
                        <Item
                            label='Endpoint'
                            icon='fw fw-endpoint'
                            callback={this.showConnectors}
                        />
                    </div>
                    }
                    { this.state.listConnectors &&
                        <div
                            className='connector-select'
                            onMouseOut={this.hideConnectors}
                        >
                            Connector Search
                        </div>
                    }
                </Menu>
            </Button>
        );
    }
}

LifelineButton.propTypes = {

};

LifelineButton.defaultProps = {

};

export default LifelineButton;
