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
 *
 */

import React from 'react';
import PropTypes from 'prop-types';
import { createViewFromViewDef } from './utils';

/**
 * React component for Header Region.
 *
 * @class Header
 * @extends {React.Component}
 */
class Header extends React.Component {

    /**
     * @inheritdoc
     */
    shouldComponentUpdate(nextProps, nextState) {
        return !nextProps.panelResizeInProgress;
    }

    /**
     * @inheritdoc
     */
    render() {
        const { width, height, panelResizeInProgress } = this.props;
        const viewProps = { width, height, panelResizeInProgress };
        return (
            <header className='header header-default'>
                <div id='header-container'>
                    <div className='pull-left brand'>
                        <i className='fw fw-composer composer-logo'/>
                    </div>
                    <div className='pull-left'>
                        {this.props.views.map((viewDef) => {
                            return createViewFromViewDef(viewDef, viewProps);
                        })}
                    </div>
                </div>
            </header>
        );
    }
}

Header.propTypes = {
    views: PropTypes.arrayOf(Object).isRequired,
    panelResizeInProgress: PropTypes.bool.isRequired,
    width: PropTypes.number.isRequired,
    height: PropTypes.number.isRequired,
};

export default Header;
