import React from 'react';
import PropTypes from 'prop-types';
import GLRegion from './GLRegion';
import Header from './Header';
import ActivityBar from './ActivityBar';
import { REGIONS } from './../constants';

/**
 * React component for App.
 *
 * @class App
 * @extends {React.Component}
 */
class App extends React.Component {

    /**
     * Get views of Header Region
     * @returns {Array<React.Component>}
     */
    getHeaderViews() {
        return this.props.layout[REGIONS.HEADER].map((viewDef) => {
            return this.createViewFromViewDef(viewDef);
        });
    }

    /**
     * Creates View from view Def
     * @param {Object} viewDef View Definition
     */
    createViewFromViewDef(viewDef) {
        const { component, propsProvider } = viewDef;
        const View = component;
        return (
            <View {...propsProvider()} key={viewDef.id} />
        );
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div className="">
                <Header>
                    {this.getHeaderViews()}
                </Header>
                <ActivityBar />
                <GLRegion />
            </div>
        );
    }
}

App.propTypes = {
    layout: PropTypes.objectOf(Object).isRequired,
};

export default App;
