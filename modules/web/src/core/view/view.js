import React from 'react';

/**
 * Base class for the view
 * @extends React.Component
 */
class View extends React.Component {

    /**
     * Method to get the unique ID of the view.
     *
     * @returns {String} A unique ID for the view
     */
    getID() {
        return 'composer.view.generic';
    }
}

export default View;
