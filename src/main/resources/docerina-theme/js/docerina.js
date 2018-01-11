$(document).ready(function() {
    var spyScroll = $("body").scrollspy({
        target: ".constructs-wrapper",
        offset: 70
    });
    console.log(spyScroll);
});