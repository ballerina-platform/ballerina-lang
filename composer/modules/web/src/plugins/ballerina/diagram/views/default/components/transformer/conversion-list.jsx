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
import TransformFactory from '../../../../../model/transformer-factory';
import './iterable-list.css';

/**
 * ConversionList List
 */
class ConversionList extends React.Component {

    /**
     * ConversionList extended component constructor
     * @param {any} props props for the component
     */
    constructor(props) {
        super(props);
        this.state = { showConvesrsion: true };
        this.buttonClick = this.buttonClick.bind(this);
        this.setWrapperRef = this.setWrapperRef.bind(this);
        this.handleClickOutside = this.handleClickOutside.bind(this);
        this.addConversion = this.addConversion.bind(this);
        this.assignOnlyClick = this.assignOnlyClick.bind(this);
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
            this.setState({ showConvesrsion: false });
            this.props.callback();
        } else {
            this.buttonClick();
        }
    }

    /**
     * Handles button click event
     */
    buttonClick() {
        if (this.props.showConvesrsion) {
            this.setState({ showConvesrsion: true });
        }
    }

    /**
     * Convert the statment using given transformer
     * @param {object} transformer Transformer definition
     */
    addConversion(transformer) {
        const statement = this.props.currentStatement;
        const expr = TransformFactory.createTransformerConversion(
                this.props.currrentConnection.target.typeName, this.props.currrentConnection.source.name,
                transformer.getName().getValue(), transformer.getParameters());
        statement.setVariables([statement.getVariables()[0]]);
        statement.setExpression(expr);
        this.setState({ showConvesrsion: false });
        this.props.callback();
    }

    /**
     * Remove conversion from current statement
    */
    assignOnlyClick() {
        const statement = this.props.currentStatement;
        statement.setVariables([statement.getVariables()[0]]);
        statement.setExpression(statement.getExpression().getExpression());
        this.setState({ showConvesrsion: false });
        this.props.callback();
    }

    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        if (this.props.showConvesrsion && this.state.showConvesrsion) {
            const transformers = this.props.transformNodeManager.getCompatibleTransformers(
                                                this.props.currrentConnection.source.typeName,
                                                this.props.currrentConnection.target.typeName);
            return (
                <div
                    ref={this.setWrapperRef}
                    style={{ top: this.props.bBox.y - 10, left: this.props.bBox.x - 20 }}
                    className='iterable-menu'
                >
                    <a onClick={() => { this.assignOnlyClick(); }} className='iterable-menu-item'>
                        <i className={'fw fw-assign'} /> Assign only
                    </a>
                    {
                    transformers.map((transformer) => {
                        return (
                            <a onClick={() => { this.addConversion(transformer); }} className='iterable-menu-item'>
                                <i className={'fw fw-type-converter'} /> {transformer.getSignature()}
                            </a>);
                    })
                    }
                </div>);
        } else {
            return (<div />);
        }
    }
}

ConversionList.propTypes = {
    bBox: PropTypes.valueOf(PropTypes.object).isRequired,
    showConvesrsion: PropTypes.valueOf(PropTypes.bool).isRequired,
    currrentConnection: PropTypes.valueOf(PropTypes.object).isRequired,
    currentStatement: PropTypes.valueOf(PropTypes.object).isRequired,
    transformNodeManager: PropTypes.valueOf(PropTypes.object).isRequired,
    callback: PropTypes.valueOf(PropTypes.object).isRequired,
};

export default ConversionList;
