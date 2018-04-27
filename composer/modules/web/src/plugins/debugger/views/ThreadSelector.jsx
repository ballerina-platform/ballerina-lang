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
 * ThreadSelector Component which render a tool in toolbar.
 *
 * @class ThreadSelector
 * @extends {React.Component}
 */
class ThreadSelector extends React.Component {

    /**
     * Render tool view.
     *
     * @returns {ReactElement} render tool view.
     *
     * @memberof ThreadSelector
     */
    render() {
        return (
            <div className='debugger-threads'>
                <div className='debug-panel-header debug-threads-header'>
                    <div><a className='tool-group-header-title'>Threads</a></div>
                </div>
                <ul>
                    {this.props.threads.map((thread) => {
                        const className = this.props.threadId === thread ? 'active' : '';
                        return (<li onClick={() => this.props.onChangeThread(thread)} className={className} >
                            {thread}
                        </li>);
                    })}
                </ul>
            </div>
        );
    }
}

ThreadSelector.propTypes = {
    threads: PropTypes.arrayOf(String),
    onChangeThread: PropTypes.func,
};

ThreadSelector.defaultProps = {
    threads: [],
    onChangeThread: () => { },
};


export default ThreadSelector;
