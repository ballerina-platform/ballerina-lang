import Plugin from 'core/plugin/plugin';
import { CONTRIBUTIONS } from 'core/plugin/constants';
import { REGIONS } from 'core/layout/constants';
import Editor from './editor/Editor';
import TryIt from './try-it/try-it-container';
import { PLUGIN_ID, EDITOR_ID, TRY_IT_ID } from './constants';
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
        const { EDITORS, VIEWS } = CONTRIBUTIONS;
        return {
            [EDITORS]: [
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
            [VIEWS]: [
                {
                    id: TRY_IT_ID,
                    component: TryIt,
                    propsProvider: () => {
                        return {
                        };
                    },
                    region: REGIONS.BOTTOM_PANEL,
                    regionOptions: {
                        panelTitle: 'Try It',
                    },
                    displayOnLoad: true,
                },
            ],
        };
    }

}

export default BallerinaPlugin;
