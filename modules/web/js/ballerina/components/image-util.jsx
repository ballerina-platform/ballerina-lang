import path from 'path';

// require all react images
function requireAll(requireContext) {
    let components = {};
    requireContext.keys().map((item) => {
        var module = requireContext(item);
        if (module) {
            components[path.basename(item, '.svg')] = module;
        }
    });
    return components;
}

const images = requireAll(require.context('images', true, /\.svg$/));

class ImageUtils {
    static getSVGIcon (iconName) {
        return images[iconName];
    }
}

export default ImageUtils;
