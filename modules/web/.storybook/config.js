import { configure } from '@kadira/storybook';

function loadStories() {
  requireAll(require.context('../stories', false,  /\.(js)$/));
}

function requireAll(requireContext) {
    return requireContext.keys().map(requireContext);
}


configure(loadStories, module);
