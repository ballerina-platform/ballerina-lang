// require all react images
function requireAll(requireContext) {
    let components = {};
    requireContext.keys().map((item) => {
        var module = requireContext(item);
        if (module) {
            components[_getBasename(item, '.svg')] = module;
        }
    });
    return components;
}

function _getBasename(filename, ext) {
    return filename.substring(2, filename.indexOf(ext));
}

const images = requireAll(require.context('images', true, /\.svg$/));

class ImageUtils {
    static getSVGIcon (iconName) {
        return images[iconName];
    }
}

export default ImageUtils;
