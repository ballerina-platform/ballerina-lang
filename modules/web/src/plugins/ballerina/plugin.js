import Plugin from './../../core/plugin/plugin';
import { CONTRIBUTIONS } from './../../core/plugin/constants';
import Editor from './editor/Editor';
import { PLUGIN_ID, EDITOR_ID } from './constants';
import { CLASSES } from './../../../js/ballerina/views/constants';

/**
 * Plugin for Ballerina Lang
 */
class BallerinaPlugin extends Plugin {

    /**
     * @inheritdoc
     */
    getID() {
        return PLUGIN_ID;
    }

    /**
     * @inheritdoc
     */
    getContributions() {
        return {
            [CONTRIBUTIONS.EDITORS]: [
                {
                    id: EDITOR_ID,
                    extension: 'bal',
                    component: Editor,
                    customPropsProvider: () => {
                        return {

                        };
                    },
                    tabTitleClass: CLASSES.TAB_TITLE.DESIGN_VIEW,
                },
            ],
        };
    }

}

export default BallerinaPlugin;
