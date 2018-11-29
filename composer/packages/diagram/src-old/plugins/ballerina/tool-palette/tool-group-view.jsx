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
import Collapsible from 'react-collapsible';
import PropTypes from 'prop-types';
import ToolView from './tool-view';
import { TOOL_GROUP } from './spec';
import './tool-palette.css';

const GRID_STYLE_HISTORY = 'composer.history.ballerina.tool-pallete-grid-style';

/**
 * Renders a tool group.
 *
 * @class ToolGroupView
 * @extends {React.Component}
 */
class ToolGroupView extends React.Component {

    /**
     * Creates an instance of ToolGroupView.
     * @param {any} props
     *
     * @memberof ToolGroupView
     */
    constructor(props, context) {
        super(props, context);
        this.state = {
            activeGridStyle: context.history.get(GRID_STYLE_HISTORY) || 'list',
        };
        this.changeGridStyle = this.changeGridStyle.bind(this);
    }

    /**
     * Get tool toolgroup name
     *
     * @returns string - tool toolgroup name
     * @memberof ToolGroupView
     */
    getToolGroupName() {
        // todo: find a proper solution, this is a hack to reduce connector package names.
        return this.props.group.name.replace('org.wso2.ballerina.connectors.', '');
    }


    /**
     * Handles mouse click on open documentation icon
     *
     * @param {any} e - Mouse event of icon click
     * @memberof ToolView
     */
    handleClickOpenDocumentation(e) {
        e.stopPropagation();
        const toolGroupName = this.getToolGroupName();
        this.context.editor.openDocumentation(toolGroupName, null);
    }

    /**
     * Event handler for change grid styles.
     *
     * @param {any} event
     *
     * @memberof ToolGroupView
     */
    changeGridStyle(event) {
        const { style } = event.currentTarget.dataset;
        this.context.history.put(GRID_STYLE_HISTORY, style);
        this.setState({ activeGridStyle: style });
    }

    /**
     * Render tool group.
     *
     * @returns {ReactElement} render tool group view.
     *
     * @memberof ToolGroupView
     */
    render() {
        const group = this.props.group;
        const children = [];
        group.tools.forEach((element) => {
            if (element.seperator) {
                children.push(<div className='clear-fix ' key={`${element.id}-clear-fix`} />);
                children.push(<div className='tool-separator' key={`${element.id}-tool-separator`} />);
            } else {
                children.push(
                    <ToolView
                        tool={element}
                        group={group}
                        key={element.id + element.name}
                        order={group.order}
                        isDocEnable={group.name !== 'Current Package'}
                    />);
            }
        }, this);

        const toolGroupName = this.getToolGroupName();
        const trigger = (currentStatus) => {
            // Do not show doc icon for Constructs and Current Package
            const canShowDoc = toolGroupName !== 'Constructs' && toolGroupName !== 'Current Package' &&
                !toolGroupName.includes(' Operators');
            const docIcon = canShowDoc ? (<span className='fw fw-document' />) : '';

            return (<div className='tool-group-header'>
                <span className='tool-group-header-title'>{toolGroupName}</span>
                <span className={`collapse-icon fw fw-${currentStatus}`} />
                <a onClick={e => this.handleClickOpenDocumentation(e)} className='pull-right'>
                    {docIcon}
                </a>
            </div>);
        };

        const disabled = false;
        let open = false;
        if (group.collapsed) {
            open = true;
        }

        return (
            <div id='tool-group-constructs-tool-group' className='tool-group'>
                <Collapsible
                    trigger={trigger('down')}
                    triggerDisabled={disabled}
                    open={open}
                    triggerWhenOpen={trigger('up')}
                    transitionTime={200}
                >
                    <div className={`tool-group-body tool-group-body-${this.state.activeGridStyle}`}>
                        {this.props.showGridStyles && <div className='tools-view-modes-controls clearfix'>
                            <a className='collapse-icon' onClick={this.changeGridStyle} data-style='tiles' title='Grid View' >
                                <i className='fw fw-tiles' />
                            </a>
                            <a className='collapse-icon' onClick={this.changeGridStyle} data-style='list' title='List View' >
                                <i className='fw fw-list' />
                            </a>
                        </div>
}
                        {children}
                    </div>
                </Collapsible>
            </div>
        );
    }
}

ToolGroupView.propTypes = {
    showGridStyles: PropTypes.bool.isRequired,
    group: TOOL_GROUP.isRequired,
};

ToolGroupView.contextTypes = {
    editor: PropTypes.instanceOf(Object).isRequired,
    history: PropTypes.shape({
        put: PropTypes.func,
        get: PropTypes.func,
    }).isRequired,
};

export default ToolGroupView;
