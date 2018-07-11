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
import Autosuggest from 'react-autosuggest';

/**
 * Interaction menu item component
 */
class Search extends React.Component {
    renderSuggestionsContainer({ containerProps, children, query }) {
        return (
            <div {...containerProps} className='suggestions-search-wrapper'>
                {children}
            </div>
        );
    }
    /**
     * render hover area and button
     * @return {object} button rendering object
     */
    render() {
        return (
            <Autosuggest
                suggestions={this.props.suggestions}
                onSuggestionsFetchRequested={this.props.onSuggestionsFetchRequested}
                onSuggestionSelected={(event, item) => {
                    this.props.onSuggestionSelected(event, item);
                    this.context.menuCloseCallback();
                }}
                renderSuggestionsContainer={this.renderSuggestionsContainer}
                getSuggestionValue={this.props.getSuggestionValue}
                renderSuggestion={this.props.renderSuggestion}
                alwaysRenderSuggestions={this.props.alwaysRenderSuggestions}
                inputProps={this.props.inputProps}
                ref={this.props.storeInputReference}
            />);
    }
}

Search.propTypes = {
    suggestions: PropTypes.isRequired,
    onSuggestionsFetchRequested: PropTypes.isRequired,
    onSuggestionSelected: PropTypes.isRequired,
    getSuggestionValue: PropTypes.isRequired,
    renderSuggestion: PropTypes.isRequired,
    alwaysRenderSuggestions: PropTypes.isRequired,
    inputProps: PropTypes.isRequired,
    storeInputReference: PropTypes.isRequired,
};

Search.contextTypes = {
    menuCloseCallback: PropTypes.func,
};

export default Search;
