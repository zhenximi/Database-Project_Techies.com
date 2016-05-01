/**
 * Created by zmi on 4/24/16.
 */
// Replace the search result table on load.
if (('localStorage' in window) && window['localStorage'] !== null) {
    if ('search' in localStorage && window.location.hash) {
        $("#search").html(localStorage.getItem('search'));
    }
}

// Save the search result table when leaving the page.
$(window).unload(function () {
    if (('localStorage' in window) && window['localStorage'] !== null) {
        var form = $("#search").html();
        localStorage.setItem('search', form);
    }
});