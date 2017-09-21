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
            this.onPluginReRender = this.onPluginReRender.bind(this);
        }

        /**
         * @inheritdoc
         */
        componentDidMount() {
            const { on } = this.context.command;
            on(COMMANDS.RE_RENDER_PLUGIN, this.onPluginReRender);
        }

        /**
         * @inheritdoc
         */
        componentWillUnmount() {
            const { off } = this.context.command;
            off(COMMANDS.RE_RENDER_PLUGIN, this.onPluginReRender);
        }

        /**
         * Invoked when a plugin wants to re-render
         * @param {Object} command args
         */
        onPluginReRender({ id }) {
            if (id === pluginID) {
                this.forceUpdate();
            }
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