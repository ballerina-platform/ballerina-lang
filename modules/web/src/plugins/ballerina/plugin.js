import React from 'react';
import ReactDOM from 'react-dom';
import Plugin from './../../core/plugin/plugin';
import { CONTRIBUTIONS } from './../../core/plugin/constants';

class BallerinaPlugin extends Plugin {

    getID() {
        return 'composer.plugin.lang.ballerina';
    }

    init(){}

    activate(){}

    getContributions() {
        return {
            [CONTRIBUTIONS.EDITORS]: [
                {
                    id: 'composer.editor.ballerina',
                    extension: 'bal',
                    component: (props) => {
                        return (
                            <div>
                                <p>{props.file.content}</p>
                            </div>
                        );
                    },
                },
            ],
        };
    }

}

export default BallerinaPlugin;
