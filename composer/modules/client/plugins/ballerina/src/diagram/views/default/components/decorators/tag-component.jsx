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
import ExpressionEditor from '../../../../../expression-editor/expression-editor-utils';
import SuggestionsText from './../nodes/suggestions-text';
import ParameterDefinition from './../nodes/parameter-definition';
import SizingUtils from './../../sizing-util';
import './tag-component.css';
import SimpleBBox from './../../../../../model/view/simple-bounding-box';

let defaultInputValue = '+ Add Value';

/**
 * Common Tag Controller to render tag input box.
 *
 * @class TagController
 * @extends {React.Component}
 * */
class TagController extends React.Component {

    /**
     * calculate the component BBox for parameter.
     * @param {object} props - props for the parameter.
     * @return {SimpleBBox} bBox.
     * */
    static calculateComponentBBoxForArgParam(props) {
        return new SimpleBBox(props.componentData.components.closingBracket.x - 121,
            props.componentData.components.closingBracket.y + (5 / 2), 101, 17);
    }

    /**
     * Constructor for TagController class.
     * */
    constructor(props) {
        super();
        this.state = {
            editing: false,
            editValue: '',
            tagBoxArgParam: TagController.calculateComponentBBoxForArgParam(props),
        };
    }

    componentWillReceiveProps(props) {
        this.setState({
            tagBoxArgParam: TagController.calculateComponentBBoxForArgParam(props),
        });
    }

    /**
     * Click event handler for on select event in select box.
     * @return {object} state.
     * */
    onSelectClick() {
        return this.setState({ editing: true, editValue: '' });
    }

    /**
     * Enter key event handler.
     * @param {string} input - input from select box.
     * @return {object} state.
     * */
    onEnter(input) {
        const setter = this.props.setter;
        setter(input);
        return this.setState({ editing: false, editValue: '' });
    }

    /**
     * Blur event handler for select box.
     * @return {object} state.
     * */
    onSelectBlur() {
        return this.setState({ editing: false, editValue: '' });
    }

    /**
     * Click event handler for input.
     * @return {object} state.
     * */
    onInputClick() {
        const options = this.props.editorOptions;
        const packageScope = this.context.environment;
        if (options) {
            new ExpressionEditor(this.state.tagBoxArgParam,
                text => this.onUpdate(text), options, packageScope).render(this.context.getOverlayContainer());
        }
        return this.setState({ editing: true, editValue: '' });
    }

    /**
     * on update.
     * @param {string} text - text value.
     * */
    onUpdate(text) {
        // TODO: Implement the callback.
    }

    /**
     * Blur event handler for input
     * @param {object} e - Event
     * @return {object} state.
     * */
    onInputBlur(e) {
        const setter = this.props.setter;
        if (defaultInputValue !== this.state.editValue && this.state.editValue !== '') {
            if (!setter(this.state.editValue)) {
                e.preventDefault();
            }
        }

        return this.setState({ editing: false, editValue: '' });
    }

    /**
     * Handle key down event of the tag controller.
     * @param {object} e - Event
     * @return {type} null.
     * */
    onKeyDown(e) {
        if (e.keyCode === 13) {
            const validate = this.props.validateInput;
            const variableDeclaration = this.state.editValue.replace('=', '').replace(';', '');

            if (!validate(variableDeclaration)) {
                return;
            }

            const setter = this.props.setter;
            if (!setter(this.state.editValue)) {
                return;
            }

            this.setState({ editing: false, editValue: '' });
        }
    }

    /**
     * Change event handler for input
     * @param {object} e - Event
     * @return {object} state.
     * */
    onInputChange(e) {
        return this.setState({ editing: true, editValue: e.target.value });
    }

    /**
     * Get the select box controller html content.
     * @param {object} componentData - component data to render component.
     * @param {object} modelComponents - models to render as tags.
     * @return {object} React HTML Content
     * */
    getSelectBoxController(componentData, modelComponents) {
        if (this.props.label) {
            // Get the width of the label text.
            const labelWidth = new SizingUtils().getTextWidth(this.props.label, 80, 80).w;
            return (<g key={componentData.title}>
                <rect
                    x={componentData.components.openingBracket.x - labelWidth}
                    y={componentData.components.openingBracket.y}
                    width={labelWidth}
                    height={25}
                    className={this.props.groupClass}
                />
                <text
                    x={componentData.components.openingBracket.x - (labelWidth - 20)}
                    y={componentData.components.openingBracket.y + 20}
                >
                    attach
                </text>
                <rect
                    x={componentData.components.openingBracket.x}
                    y={componentData.components.openingBracket.y}
                    width={componentData.components.closingBracket.w +
                    (componentData.components.closingBracket.x - (componentData.components.openingBracket.x - 3))}
                    height={25}
                    className={this.props.groupClass}
                />
                <text
                    x={componentData.components.openingBracket.x + 7}
                    y={componentData.components.openingBracket.y + 5}
                    className={componentData.openingBracketClassName}
                >(
                </text>
                {modelComponents}

                <g onClick={() => {
                    this.onSelectClick();
                }}
                >
                    <rect
                        x={componentData.components.closingBracket.x - 130}
                        y={componentData.components.closingBracket.y + 5}
                        width={120}
                        height={20}
                        className='text-placeholder'
                    />
                    <text
                        x={componentData.components.closingBracket.x - 124}
                        y={componentData.components.closingBracket.y + 19}
                        className='tag-component-attachment-text'
                    >
                        {defaultInputValue}
                    </text>
                    <SuggestionsText
                        x={componentData.components.closingBracket.x - 130}
                        y={componentData.components.closingBracket.y + 5}
                        width={123}
                        height={22}
                        className='tag-component-editable-text-box'
                        onEnter={(input) => {
                            this.onEnter(input);
                        }}
                        onBlur={() => {
                            this.onSelectBlur();
                        }}
                        show={this.state.editing}
                        suggestionsPool={this.props.suggestions}
                        onSuggestionSelected={(event, { suggestionValue }) => {
                            this.onEnter(suggestionValue);
                        }}
                    />
                </g>
                <text
                    x={componentData.components.closingBracket.x}
                    y={componentData.components.closingBracket.y + 5}
                    className={componentData.closingBracketClassName}
                >)
                </text>
            </g>);
        }
        return (<g key={componentData.title}>
            <rect
                x={componentData.components.openingBracket.x - 3}
                y={componentData.components.openingBracket.y}
                width={(componentData.components.closingBracket.x - (componentData.components.openingBracket.x - 3))
                + componentData.components.closingBracket.w}
                height={25}
                className={this.props.groupClass}
            />
            <text
                x={componentData.components.openingBracket.x + 7}
                y={componentData.components.openingBracket.y + 5}
                className={componentData.openingBracketClassName}
            >(
            </text>
            {modelComponents}

            <g onClick={() => {
                this.onSelectClick();
            }}
            >
                <rect
                    x={componentData.components.closingBracket.x - 130}
                    y={componentData.components.closingBracket.y + 5}
                    width={120}
                    height={20}
                    className='text-placeholder'
                />
                <text
                    x={componentData.components.closingBracket.x - 124}
                    y={componentData.components.closingBracket.y + 19}
                    className='tag-component-attachment-text'
                >
                    {defaultInputValue}
                </text>
                <SuggestionsText
                    x={componentData.components.closingBracket.x - 124}
                    y={componentData.components.closingBracket.y + 6}
                    width={123}
                    height={20}
                    className='tag-component-editable-text-box'
                    onEnter={(input) => {
                        this.onEnter(input);
                    }}
                    onBlur={() => {
                        this.onSelectBlur();
                    }}
                    show={this.state.editing}
                    suggestionsPool={this.props.suggestions}
                    onSuggestionSelected={(event, { suggestionValue }) => {
                        this.onEnter(suggestionValue);
                    }}
                />
            </g>
            <text
                x={componentData.components.closingBracket.x}
                y={componentData.components.closingBracket.y + 5}
                className={componentData.closingBracketClassName}
            >)
            </text>
        </g>);
    }

    /**
     * Get return parameter controller.
     * @param {object} componentData - component data to render component.
     * @param {object} modelComponents - models to render as tags.
     * @return {object} React HTML Content
     * */
    getReturnParameterController(componentData, modelComponents) {
        const parameterDefinitionsViews = modelComponents.map((item) => {
            return (<ParameterDefinition model={item} key={item.id} />);
        });
        return (
            <g key={componentData.title}>
                <rect
                    x={componentData.components.typesIcon.x - 3}
                    y={componentData.components.openingBracket.y}
                    width={(componentData.components.closingBracket.x - (componentData.components.openingBracket.x - 3))
                    + componentData.components.closingBracket.w + componentData.components.typesIcon.w}
                    height={25}
                    className={this.props.groupClass}
                />
                <text
                    x={componentData.components.typesIcon.x}
                    y={componentData.components.typesIcon.y}
                >returns
                </text>
                <text
                    x={componentData.components.openingBracket.x + 7}
                    y={componentData.components.openingBracket.y + 5}
                    className={componentData.openingBracketClassName}
                >(
                </text>
                {parameterDefinitionsViews}
                <g
                    onClick={() => {
                        this.onInputClick();
                    }}
                >
                    <rect
                        x={componentData.components.closingBracket.x - 121}
                        y={componentData.components.closingBracket.y + 5}
                        width={100}
                        height={20}
                        className='text-placeholder'
                    />
                    <text
                        x={componentData.components.closingBracket.x - 112}
                        y={componentData.components.closingBracket.y + 5 + (28 / 2)}
                        className={'tag-component-label'}
                    >
                        +Add Return
                    </text>
                </g>
                <text
                    x={componentData.components.closingBracket.x - 10}
                    y={componentData.components.closingBracket.y + 5}
                    className={componentData.closingBracketClassName}
                >)
                </text>
            </g>
        );
    }

    /**
     * Get the argument parameter controller html content.
     * @param {object} componentData - component data to render component.
     * @param {object} modelComponents - models to render as tags.
     * @return {object} React HTML Content
     * */
    getArgumentParameterController(componentData, modelComponents) {
        const parameterDefinitionsViews = modelComponents.map((item) => {
            return (<ParameterDefinition model={item} key={item.id} />);
        });
        return (
            <g key={componentData.title}>
                <rect
                    x={componentData.components.openingBracket.x}
                    y={componentData.components.openingBracket.y}
                    width={(componentData.components.closingBracket.x - (componentData.components.openingBracket.x - 3))
                    + componentData.components.closingBracket.w}
                    height={25}
                    className={this.props.groupClass}
                />
                <text
                    x={componentData.components.openingBracket.x + 7}
                    y={componentData.components.openingBracket.y + 5}
                    className={componentData.openingBracketClassName}
                >(
                </text>
                {parameterDefinitionsViews}
                <g
                    onClick={() => {
                        this.onInputClick();
                    }}
                >
                    <rect
                        x={componentData.components.closingBracket.x - 120}
                        y={componentData.components.closingBracket.y + 5}
                        width={100}
                        height={20}
                        className='text-placeholder'
                    />
                    <text
                        x={componentData.components.closingBracket.x - 112}
                        y={componentData.components.closingBracket.y + 5 + (28 / 2)}
                        className={'tag-component-label'}
                    >
                        +Add Param
                    </text>
                </g>
                <text
                    x={componentData.components.closingBracket.x - 10}
                    y={componentData.components.closingBracket.y + 5}
                    className={componentData.closingBracketClassName}
                >)
                </text>
            </g>
        );
    }

    /**
     * Render method to render the component.
     * @return {object} react component.
     * */
    render() {
        const modelComponents = this.props.modelComponents;
        const componentData = this.props.componentData;
        defaultInputValue = componentData.defaultText;
        if (this.props.isSelectBox) {
            return this.getSelectBoxController(componentData, modelComponents);
        }
        // If parameter is a return type
        if (componentData.components.typesIcon) {
            return this.getReturnParameterController(componentData, modelComponents);
        }
        // If parameter is a argument parameter
        return this.getArgumentParameterController(componentData, modelComponents);
    }
}

TagController.propTypes = {
    setter: PropTypes.func.isRequired,
    validateInput: PropTypes.func.isRequired,
    suggestions: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string,
    })),
    modelComponents: PropTypes.arrayOf(PropTypes.shape({
        model: PropTypes.instanceOf(ParameterDefinition),
        key: PropTypes.string,
    })),
    componentData: PropTypes.shape({
        title: PropTypes.string,
    }),
    label: PropTypes.string,
    groupClass: PropTypes.string,
    isSelectBox: PropTypes.bool,
    editorOptions: PropTypes.instanceOf(Object).isRequired,
};

TagController.defaultProps = {
    suggestions: [{
        name: 'service',
    }],
    modelComponents: [],
    componentData: {},
    label: 'attach',
    groupClass: '',
    isSelectBox: false,
};

TagController.contextTypes = {
    getOverlayContainer: PropTypes.instanceOf(Object).isRequired,
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default TagController;
