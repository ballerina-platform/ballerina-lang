import React from 'react';
import _ from 'lodash';
import PropTypes from 'prop-types';
import View from './../../view/view';
import { VIEW_IDS } from './../constants';
import Tool from './../components/Tool';
import './tool-bar.scss';

/**
 * ToolBar
 */
class ToolBar extends View {

    /**
     * @inheritdoc
     */
    getID() {
        return VIEW_IDS.APP_MENU;
    }

    /**
     * @inheritdoc
     */
    render() {
        const { tools, appContext: { command: { dispatch } } } = this.props.toolBarPlugin;
        const toolsByGroup = {};
        tools.forEach((tool) => {
            if (_.isNil(toolsByGroup[tool.group])) {
                toolsByGroup[tool.group] = [];
            }
            toolsByGroup[tool.group].push(tool);
        });
        const toolGroupComponents = _.toPairs(toolsByGroup).map((pair) => {
            return (
                <div className="tool-bar-group unselectable-content" key={pair[0]}>
                    {pair[1].map((tool) => {
                        return (
                            <Tool tool={tool} dispatch={dispatch} key={tool.id} />
                        );
                    })}
                </div>
            );
        });
        return (
            <div className="tool-bar">
                {toolGroupComponents}
            </div>
        );
    }
}

ToolBar.propTypes = {
    toolBarPlugin: PropTypes.objectOf(Object).isRequired,
};

export default ToolBar;
