function updateHeatingSection(heatingStatus) {
    $('#btn-heating').removeClass();
    if (heatingStatus == 'on') {
        $('#btn-heating').text(resourceBundle["label.statusOn"]);
        $('#btn-heating').addClass('btn btn-success');
    } else if (heatingStatus == 'off') {
        $('#btn-heating').text(resourceBundle["label.statusOff"]);
        $('#btn-heating').addClass('btn btn-danger');
    } else {
        $('#btn-heating').text(resourceBundle["label.statusNA"]);
        $('#btn-heating').addClass('btn btn-warning');
    }
}

function updateTimerSection(timerStatus) {
    $('#btn-timer').removeClass();
    if (timerStatus == 'off') {
        $('#btn-timer').text(resourceBundle["label.statusOff"]);
        $('#btn-timer').addClass('btn btn-danger');
    } else if (timerStatus.length == 5 && timerStatus[2] == ':') {
        $('#btn-timer').text(timerStatus);
        $('#btn-timer').addClass('btn btn-success');
    } else {
        $('#btn-heating').text(resourceBundle["label.statusNA"]);
        $('#btn-heating').addClass('btn btn-warning');
    }
}

function openHeatingModal() {
    if ($('#btn-heating').hasClass('btn-success')) {
        $('#btn-heating-modal-ok').removeClass();
        $('#btn-heating-modal-ok').addClass('btn btn-danger');
        $('#heating-modal-title').text(resourceBundle["label.heatingTurnOff"]);
    } else {
        $('#btn-heating-modal-ok').removeClass();
        $('#btn-heating-modal-ok').addClass('btn btn-success');
        $('#heating-modal-title').text(resourceBundle["label.heatingTurnOn"]);
    }
    $('#heating-modal').modal('show');
}

function openTimerModal(mode) {
    $('#timer-modal-title').text(resourceBundle["label.setupTimer"]);
    if ($('#btn-timer').hasClass('btn-success')) {
        let value = $('#btn-timer').text();
        let hours = value.substring(0, 2);
        let minutes = value.substring(3);
        $('#timer-hour-picker option').filter(function () {
            return $(this).html() == hours;
        }).prop('selected', true);
        $('#timer-minute-picker option').filter(function () {
            return $(this).html() == minutes;
        }).prop('selected', true);
    } else {
    }
    $('#timer-modal').modal('show');
}

function getHeatingStatus() {
    var result = 'unknown';
    $.ajax({
        url: 'api/heating/status/00',
        type: 'get',
        dataType: 'html',
        async: false,
        success: function (data) {
            if (data == "ON") {
                result = 'on';
            } else if (data == "OFF") {
                result = 'off';
            }
        }
    });
    return result;
}

function getTimerStatus() {
    var result = 'unknown';
    $.ajax({
        url: 'api/timer/status',
        type: 'get',
        dataType: 'html',
        async: false,
        success: function (data) {
            result = data.toLowerCase();
        }
    });
    return result;
}

function changeHeatingStatus() {
    var action = $('#btn-heating-modal-ok').hasClass('btn-success') ? 'on' : 'off';
    var result = false;
    $.ajax({
        url: 'api/heating/' + action + '/00',
        type: 'get',
        dataType: 'html',
        async: false,
        success: function (data) {
            if (data == "OK") {
                result = true;
            }
        }
    });
    if (result) {
        let heatingStatus = getHeatingStatus();
        updateHeatingSection(heatingStatus);
        $('#heating-modal').modal('hide');
    }
    // TODO: handle error messages
}

function changeTimerStatus(turnoff) {
    var value;
    if (turnoff) {
        value = 'off'
    } else {
        var hours = $('#timer-hour-picker').find(":selected").text();
        var minutes = $('#timer-minute-picker').find(":selected").text();
        value = hours + ':' + minutes;
    }

    var result = false;
    $.ajax({
        url: 'api/timer/' + value,
        type: 'get',
        dataType: 'html',
        async: false,
        success: function (data) {
            if (data == "OK") {
                result = true;
            }
        }
    });
    if (result) {
        let timer = getTimerStatus();
        updateTimerSection(timer);
        $('#timer-modal').modal('hide');
    }
    // TODO: handle error messages
}