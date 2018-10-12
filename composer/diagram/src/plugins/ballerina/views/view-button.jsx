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

/**
 * utton to switch views
 */
class ViewButton extends React.Component {

    render() {
        const active = (this.props.active) ? 'active' : '';
        return (
            <div
                className={`view-split-view-btn btn-icon ${active}`}
                onClick={this.props.onClick}
            >
                <div className='bottom-label-icon-wrapper' title={this.props.label}>
                    <i className={`fw fw-${this.props.icon}`} />
                </div>
            </div>
        );
    }

}

ViewButton.propTypes = {
    onClick: PropTypes.func.isRequired,
    label: PropTypes.string,
    icon: PropTypes.string.isRequired,
    active: PropTypes.bool.isRequired,
};

ViewButton.defaultProps = {
    label: '',
    active: false,
};

export default ViewButton;
