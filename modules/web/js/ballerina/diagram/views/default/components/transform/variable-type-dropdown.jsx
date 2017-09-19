import _ from 'lodash';
import PropTypes from 'prop-types';
import React from 'react';
import Autosuggest from 'react-autosuggest';

const getSuggestionValue = suggestion => suggestion;

const renderSuggestion = suggestion => (
    <div>
        { suggestion }
    </div>
);

const shouldRenderSuggestions = () => true;

class VariableTypeDropdown extends React.Component {
    constructor(props, context) {
        super(props, context);
        this.typeChange = props.onChange;
        this.state = {
            value: this.props.value,
            suggestionPool: context.environment.getTypes(),
            suggestions: [],
        };

        this.onSuggestionsFetchRequested = this.onSuggestionsFetchRequested.bind(this);
        this.onSuggestionsClearRequested = this.onSuggestionsClearRequested.bind(this);
        this.onKeyDown = this.onKeyDown.bind(this);
        this.onChange = this.onChange.bind(this);
    }

    onSuggestionsFetchRequested(query) {
        const matches = [];
        const substrRegex = new RegExp(_.escapeRegExp(query.value), 'i');

        this.state.suggestionPool.forEach((sug) => {
            if (substrRegex.test(sug)) {
                matches.push(sug);
            }
        });
        this.setState({ suggestions: matches });
    }

    onSuggestionsClearRequested() {
        this.setState({
            suggestions: [],
        });
    }

    onKeyDown(event) {
        if (event.keyCode === 13) {
            this.props.onEnter(event);
        }
    }

    onChange(e, selectedVal) {
        this.setState({ value: selectedVal.newValue });
        this.typeChange(selectedVal.newValue);
    }

    render() {
        const inputProps = {
            value: this.state.value,
            onChange: this.onChange,
        };

        return (
            <Autosuggest
                suggestions={this.state.suggestions}
                onSuggestionsFetchRequested={this.onSuggestionsFetchRequested}
                onSuggestionsClearRequested={this.onSuggestionsClearRequested}
                onSuggestionSelected={this.props.onSuggestionSelected}
                shouldRenderSuggestions={shouldRenderSuggestions}
                getSuggestionValue={getSuggestionValue}
                renderSuggestion={renderSuggestion}
                inputProps={inputProps}
                focusInputOnSuggestionClick={false}
            />
        );
    }
}

VariableTypeDropdown.propTypes = {
    value: PropTypes.string.isRequired,
    onChange: PropTypes.func.isRequired,
    onSuggestionSelected: PropTypes.instanceOf(Object).isRequired,
    onEnter: PropTypes.func.isRequired,
};

VariableTypeDropdown.contextTypes = {
    environment: PropTypes.instanceOf(Object).isRequired,
};

export default VariableTypeDropdown;
