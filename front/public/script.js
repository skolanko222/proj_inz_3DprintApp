let stompClient;

// Obsługa połączenia z serwerem
document.getElementById('connectButton').addEventListener('click', function () {
    const printerId = document.getElementById('printerId').value;
    if (!printerId) {
        alert("Podaj ID drukarki!");
        return;
    }

    stompClient = Stomp.client('ws://localhost:8080/ws');
    stompClient.connect({}, function (frame) {
        console.log('Połączono: ' + frame);
        alert("Połączono z drukarką: " + printerId);

        // Subskrypcja statusów
        stompClient.subscribe('/queue/status', function (message) {
            console.log('Otrzymano status: ' + message.body);
            const statusData = JSON.parse(message.body);

            // Aktualizacja danych
            document.getElementById('coordinates').innerText = statusData.cords || 'N/A';
            document.getElementById('tempPrintbed').innerText = statusData.tempPrintbed || 'N/A';
            document.getElementById('tempNozzle').innerText = statusData.tempNozzle || 'N/A';
        });
    });
});

// Obsługa wysyłania komend
document.getElementById('commandForm').addEventListener('submit', function (event) {
    event.preventDefault();

    const printerId = document.getElementById('printerId').value;
    const command = document.getElementById('command').value;

    if (!stompClient || !printerId) {
        alert("Połącz się z drukarką przed wysłaniem komendy!");
        return;
    }

    const payload = JSON.stringify({ printerId, command });
    stompClient.send('/app/command', {}, payload);

    document.getElementById('response').innerText = "Wysłano komendę: " + command;
});

// Obsługa suwaków i checkboxów
document.getElementById('enablePrintbed').addEventListener('change', function () {
    const printbedTempInput = document.getElementById('printbedTemp');
    printbedTempInput.disabled = !this.checked;

    if (this.checked) {
        sendTemperatureCommand('printbed', printbedTempInput.value);
    }
});

document.getElementById('printbedTemp').addEventListener('input', function () {
    document.getElementById('printbedTempValue').innerText = this.value + "°C";
    sendTemperatureCommand('printbed', this.value);
});

document.getElementById('enableNozzle').addEventListener('change', function () {
    const nozzleTempInput = document.getElementById('nozzleTemp');
    nozzleTempInput.disabled = !this.checked;

    if (this.checked) {
        sendTemperatureCommand('nozzle', nozzleTempInput.value);
    }
});

document.getElementById('nozzleTemp').addEventListener('input', function () {
    document.getElementById('nozzleTempValue').innerText = this.value + "°C";
    sendTemperatureCommand('nozzle', this.value);
});

function sendTemperatureCommand(type, value) {
    if (!stompClient) {
        console.log("Nie można wysłać polecenia: brak połączenia z drukarką.");
        return;
    }

    const command = {
        type: type,
        temperature: value
    };
    stompClient.send('/app/temperature', {}, JSON.stringify(command));
    console.log("Wysłano polecenie temperatury: ", command);
}
