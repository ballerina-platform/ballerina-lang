import { configure } from '@storybook/react';

function loadStories() {
  // require('../stories/edit-file-with-ls.tsx');
  require('../stories/editable-diagram.tsx');
}

configure(loadStories, module);