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
import './toolbar.scss';
import DebugManager from './../../DebugManager';
import ToolSetView from './tool-set';
import ToolItemView from './tool-item-view';
import { COMMANDS } from './../../constants';

/**
 * Tool Component which render a tool in toolbar.
 *
 * @class Tool
 * @extends {React.Component}
 */
class DebugToolbarView extends React.Component {

    constructor() {
        super();
        this.onToolClickHandler = this.onToolClickHandler.bind(this);
    }

    /**
     * Click function of the tool
     * @param app
     * @param tool
     */
    onToolClickHandler(id) {
        const {dispatch, threadId} = this.props;
        switch (id) {
            case 'debugStop':
                dispatch(COMMANDS.STOP, threadId);
                break;
            case 'debugResume':
                dispatch(COMMANDS.RESUME, threadId);
                break;
            case 'debugStepOver':
                dispatch(COMMANDS.STEP_OVER, threadId);
                break;
            case 'debugStepIn':
                dispatch(COMMANDS.STEP_IN, threadId);
                break;
            case 'debugStepOut':
                dispatch(COMMANDS.STEP_OUT, threadId);
                break;
        }
        DebugManager.trigger('resume-execution');
    }
    /**
      * Render tool view.
      *
      * @returns {ReactElement} render tool view.
      *
      * @memberof Tool

      */
    render() {
        let disabled = false;
        if (!this.props.navigation) {
            disabled = true;
        }
        return (
            <ToolSetView
                className='debugger-tools'
            >
                {/* Stop */}
                <ToolItemView
                    id='debugStop'
                    className='btn toolbar-icons'
                    title='Stop Debug ( Alt + P )'
                    disabled=''
                    iconClass='fw-stop stop'
                    onToolClick={this.onToolClickHandler}
                />

                {/* Resume */}
                <ToolItemView
                    id='debugResume'
                    className='btn toolbar-icons'
                    title='Resume ( Alt + R )'
                    disabled={disabled}
                    iconClass='fw-start resume'
                    onToolClick={this.onToolClickHandler}
                />

                {/* Step Over */}
                <ToolItemView
                    id='debugStepOver'
                    className='btn toolbar-icons'
                    title='Step Over ( Alt + O )'
                    disabled={disabled}
                    iconClass='fw-stepover step'
                    onToolClick={this.onToolClickHandler}
                />

                {/* Step In */}
                <ToolItemView
                    id='debugStepIn'
                    className='btn toolbar-icons'
                    title='Step In ( Alt + I )'
                    disabled={disabled}
                    iconClass='fw-stepin step'
                    onToolClick={this.onToolClickHandler}
                />

                {/* Step Out */}
                <ToolItemView
                    id='debugStepOut'
                    className='btn toolbar-icons'
                    title='Step Out ( Alt + U )'
                    disabled={disabled}
                    iconClass='fw-stepout step'
                    onToolClick={this.onToolClickHandler}
                />
            </ToolSetView>
        );
    }
}

export default DebugToolbarView;
