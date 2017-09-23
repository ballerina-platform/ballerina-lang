import React from 'react';
import PropTypes from 'prop-types';
import Dialog from './../../view/Dialog';
import { DIALOGS } from './../constants';

/**
 * PreferencesDialog
 */
class PreferencesDialog extends Dialog {

    /**
     * @inheritdoc
     */
    getID() {
        return DIALOGS.PREFERENCES_DIALOG;
    }

    /**
     * @inheritdoc
     */
    render() {
        return (
            <div>
            </div>
        );
    }
}

export default PreferencesDialog;
