import { configure } from '@storybook/react';

function loadStories() {
    document.body.classList.add("diagram");
    require('../stories/editable-diagram.tsx');
}

configure(loadStories, module);
