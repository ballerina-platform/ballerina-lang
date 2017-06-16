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
import './tool-palette.css';
import ToolGroup from './tool-group';

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
    constructor(props) {
        super(props);
        this.state = {
            activeGridStyle: 'list',
        };
        this.changeGridStyle = this.changeGridStyle.bind(this);
    }

    /**
     * Event handler for change grid styles.
     *
     * @param {any} event
     *
     * @memberof ToolGroupView
     */
    changeGridStyle(event) {
        this.setState({ activeGridStyle: event.currentTarget.dataset.style });
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
            if (element.attributes.seperator) {
                children.push(<div className="clear-fix " key="clear-fix" />);
                children.push(<div className="tool-separator" key="tool-separator" />);
            } else {
                children.push(
                    <ToolView tool={element} key={element.get('title')} toolOrder={group.get('toolOrder')} />);
            }
        }, this);

        // todo: find a proper solution, this is a hack to reduce connector package names.
        const toolGroupName = group.get('toolGroupName').replace('org.wso2.ballerina.connectors.', '');

        const trigger = (<div className="tool-group-header">
            <a className="tool-group-header-title">{toolGroupName}</a>
            <span className="collapse-icon fw fw-down" />
        </div>);

        const triggerWhenOpen = (<div className="tool-group-header">
            <a className="tool-group-header-title">{toolGroupName}</a>
            <span className="collapse-icon fw fw-up" />
        </div>);

        const disabled = false;
        let open = false;
        if (group.collapsed) {
            open = true;
        }

        return (
            <div id="tool-group-constructs-tool-group" className="tool-group">
                <Collapsible
                    trigger={trigger}
                    triggerDisabled={disabled}
                    open={open}
                    triggerWhenOpen={triggerWhenOpen}
                    transitionTime={200}
                >
                    <div className={`tool-group-body tool-group-body-${this.state.activeGridStyle}`}>
                        {this.props.showGridStyles && <div className="tools-view-modes-controls clearfix">
                            <a className="collapse-icon" onClick={this.changeGridStyle} data-style="tiles" >
                                <i className="fw fw-tiles" />
                            </a>
                            <a className="collapse-icon" onClick={this.changeGridStyle} data-style="grid">
                                <i className="fw fw-grid" />
                            </a>
                            <a className="collapse-icon" onClick={this.changeGridStyle} data-style="list" >
                                <i className="fw fw-list" />
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
    group: PropTypes.instanceOf(ToolGroup).isRequired,
};

export default ToolGroupView;
