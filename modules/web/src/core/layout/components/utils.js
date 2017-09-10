import React from 'react';
import PropTypes from 'prop-types';
import { COMMANDS } from './../constants';

export function withReRenderSupport(View, pluginID) {
    class ViewWrapper extends React.Component {
         /**
         * @inheritdoc
         */
        constructor(props, context) {
            super(props, context);
            const { on } = this.context.command;
            on(COMMANDS.RE_RENDER_PLUGIN, ({id}) => {
                if (id === pluginID) {
                    this.forceUpdate();
                }
            });
        }

         /**
         * @inheritdoc
         */
        render() {
            return <View {...this.props} />;
        }
    };

    ViewWrapper.contextTypes = {
        command: PropTypes.shape({
            on: PropTypes.func,
            dispatch: PropTypes.func,
        }).isRequired,
    };

    return ViewWrapper;
}