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

/**
 * React component for the popout button.
 * TODO: This needs to be updated.
 *
 * @class PopoutButton
 * @extends {React.Component}
 */
class PopoutButton extends React.Component {

    /**
     * Renders buttons of the popout-button.
     *
     * @returns {ReactElement[]} The list of buttons.
     * @memberof PopoutButton
     */
    renderButtons() {
        return this.props.buttons.map((button) => {
            return (
                <i
                    key={`popout-button-${button.text}`}
                    className={`icon fw ${button.icon}`}
                    onClick={e => button.onClick(e)}
                    title={button.text}
                />);
        });
    }

    /**
     * Renders the view of the popout button.
     *
     * @returns {ReactElement} The view.
     * @memberof PopoutButton
     */
    render() {
        const buttons = this.renderButtons();
        return (
            <span className='popout-button-wrapper'>
                {buttons}
            </span>);
    }
}

PopoutButton.propTypes = {
    buttons: PropTypes.arrayOf(PropTypes.shape({
        icon: PropTypes.string.isRequired,
        text: PropTypes.string.isRequired,
        onClick: PropTypes.func.isRequired,
    })),
};

PopoutButton.defaultProps = {
    buttons: [],
};

export default PopoutButton;
