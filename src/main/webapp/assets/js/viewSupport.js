/**
 * Support the Client UI (Animations, Fill dynamic content, etc.)
 *
 * Created by tom on 6/24/17.
 */

/**
 * Show Visit Type Card (Appointment vs. Walk in)
 */
function showTypeSelect() {
    $("div.card:not(.type)").parent().fadeOut(400, function () {
        $("div.card.type").parent().fadeIn(400);
    });

    // Load Appointment List and Final Quote in the background when an interaction starts
    loadAppointmentList();
    loadQuote();
}

/**
 * Show list of upcoming appointments
 */
function showAppointmentList() {
    $("div.card:not(.appointment-list)").parent().fadeOut(400, function () {
        $("div.card.appointment-list").parent().fadeIn(400);
    });
}

/**
 * If the client indicates that their appointment cannot be found, note their condition and
 * treat them like a non-appointment user. We will check again on the backend for a matching event.
 */
function apptNotFound() {
    appointmentMade = true;
    showIDEntry();
}

function showAppointmentDetail(e) {
    $('.info-parent .appt-time').html($(e).data("time"));
    $('.info-parent .appt-type').html($(e).data("type"));
    $('.info-parent .appt-own-name').html($(e).data("name"));
    $('.info-parent .appt-own-id').html(String($(e).data("owner-id")).replace(/\*/g, "&bull;"));
    $('.appointment-detail .finish-button').data("appt-id", $(e).data("appt-id"));

    $("div.card:not(.appointment-detail)").parent().fadeOut(400, function () {
        $("div.card.appointment-detail").parent().fadeIn(400, function () {
        });
    });
}

function showIDEntry() {
    $("div.card:not(.id-entry)").parent().fadeOut(400, function () {
        $("div.card.id-entry").parent().fadeIn(400, function () {
            $('#idBox').focus();
        });
    });
}

function showGoalList() {
    $("div.card:not(.goal-list)").parent().fadeOut(400, function () {
        $("div.card.goal-list").parent().fadeIn(400);
        hideKeyboard();
        $("#idEntryForm")[0].reset();
    });
}

function showFinalConfirmation() {
    $("div.card:not(.confirmation)").parent().fadeOut(400, function () {
        $("div.card.confirmation").parent().fadeIn(400);
    });

    // Show final message for 10 seconds, before resetting the page
    setTimeout(reset, 10000);
}

/**
 * Reset the UI if the session has expired
 */
function reset() {
    if (!$("div.card.welcome").is(":visible")) {
        $("div.card:not(.welcome)").parent().fadeOut(400, function () {
            $("div.card.welcome").parent().fadeIn(400);
        });
        hideKeyboard();
        swal.close();
        $("#idEntryForm")[0].reset();
    }
    appointmentMade = false;
}

function hideKeyboard() {
    document.activeElement.blur();
}

//noinspection JSUnusedGlobalSymbols
function showPage(pageName) {
    $("div.card:not(.goal-" + pageName.toLowerCase() + ")").parent().fadeOut(400, function () {
        $("div.card.goal-" + pageName.toLowerCase()).parent().fadeIn(400);
    });
}

//noinspection JSUnusedGlobalSymbols
function finish(goal, params) {
    logEvent(goal, params);
}