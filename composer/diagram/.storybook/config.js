import { configure } from '@storybook/react';

function loadStories() {
  require('../stories/editable-diagram.tsx');
}

configure(loadStories, module);