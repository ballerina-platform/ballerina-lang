import { configure } from '@storybook/react';

function loadStories() {
  // require('../stories/editable-diagram-with-ls.tsx');
  require('../stories/editable-diagram.tsx');
}

configure(loadStories, module);