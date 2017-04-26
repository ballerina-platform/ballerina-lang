// require all react components
function requireAll(requireContext) {
    let components = {};
    requireContext.keys().map((item, index) => {
        var module = requireContext(item);
        if (module.default) {
            components[module.default.name] = module.default;
        }
    });
    return components;
}

export default requireAll(require.context('./', true, /\.jsx$/));
