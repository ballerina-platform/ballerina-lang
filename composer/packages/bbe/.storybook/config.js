import { configure } from '@storybook/react';

function loadStories() {
  require('../stories/sample-list.tsx');
  // You can require as many stories as you need.
}

configure(loadStories, module);