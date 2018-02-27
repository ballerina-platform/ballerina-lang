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
import './iterable-list.css';

/**
 * Iterable Operator List
 */
class IterableList extends React.Component {

    /**
     * IterableList extended component constructor
     * @param {any} props props for the component
     */
    constructor(props) {
        super(props);
        this.state = { showIterables: true };
        this.buttonClick = this.buttonClick.bind(this);
        this.setWrapperRef = this.setWrapperRef.bind(this);
        this.handleClickOutside = this.handleClickOutside.bind(this);
        this.addIterable = this.addIterable.bind(this);
    }

    /**
     * Bind mouse down event to handle out side click events
     */
    componentDidMount() {
        document.addEventListener('mousedown', this.handleClickOutside);
    }

    /**
     * remove mouse down event bound  when mounting component
     */
    componentWillUnmount() {
        document.removeEventListener('mousedown', this.handleClickOutside);
    }

    /**
     * Set specified node as wrapper reference for check outside clicks
     * @param {*} node node click event needs to be detected
     */
    setWrapperRef(node) {
        this.wrapperRef = node;
    }

    /**
     * Handle outside click event and close menu accordingly
     * @param {*} event triggerred event
     */
    handleClickOutside(event) {
        if (this.wrapperRef && !this.wrapperRef.contains(event.target)) {
            this.setState({ showIterables: false });
            this.props.callback();
        } else {
            this.buttonClick();
        }
    }

    /**
     * Handles button click event
     */
    buttonClick() {
        if (this.props.showIterables) {
            this.setState({ showIterables: true });
        }
    }

    /**
     * Handles add iterable click event
     * @param {string} type iterable operation type
     * @param {boolean} isLambda is given iterable operation has a lambda function
     */
    addIterable(type, isLambda) {
        this.props.transformNodeManager.addIterableOperator(this.props.currrentConnection, type, isLambda);
        this.setState({ showIterables: false });
        this.props.callback();
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        if (this.props.showIterables && this.state.showIterables) {
            return (
                <div
                    ref={this.setWrapperRef}
                    style={{ top: this.props.bBox.y - 10, left: this.props.bBox.x - 20 }}
                    className='iterable-menu'
                >
                    <a onClick={() => { this.addIterable('filter', true); }} className='iterable-menu-item'>
                        <i className={'fw fw-iterable-operations'} /> Add Filter
                    </a>
                    <a onClick={() => { this.addIterable('map', true); }} className='iterable-menu-item'>
                        <i className={'fw fw-iterable-operations'} /> Add Map
                    </a>
                    <a onClick={() => { this.addIterable('sum', false); }} className='iterable-menu-item'>
                        <i className={'fw fw-iterable-operations'} /> Add Sum
                    </a>
                    <a onClick={() => { this.addIterable('average', false); }} className='iterable-menu-item'>
                        <i className={'fw fw-iterable-operations'} /> Add Average
                    </a>
                    <a onClick={() => { this.addIterable('min', false); }} className='iterable-menu-item'>
                        <i className={'fw fw-iterable-operations'} /> Add Min
                    </a>
                    <a onClick={() => { this.addIterable('max', false); }} className='iterable-menu-item'>
                        <i className={'fw fw-iterable-operations'} /> Add Max
                    </a>
                    <a onClick={() => { this.addIterable('count', false); }} className='iterable-menu-item'>
                        <i className={'fw fw-iterable-operations'} /> Add Count
                    </a>
                </div>);
        } else {
            return (<div />);
        }
    }
}

IterableList.propTypes = {
    bBox: PropTypes.valueOf(PropTypes.object).isRequired,
    showIterables: PropTypes.valueOf(PropTypes.bool).isRequired,
    currrentConnection: PropTypes.valueOf(PropTypes.object).isRequired,
    transformNodeManager: PropTypes.valueOf(PropTypes.object).isRequired,
    callback: PropTypes.valueOf(PropTypes.object).isRequired,
};

export default IterableList;
