let executionId = null;
let timerInterval = null;
let isRunning = false;
let currentSeconds = 0;

function updateDisplay() {
    let hours = Math.floor(currentSeconds / 3600);
    let minutes = Math.floor((currentSeconds % 3600) / 60);
    let seconds = currentSeconds % 60;

    document.getElementById('hours').textContent = String(hours).padStart(2, '0');
    document.getElementById('minutes').textContent = String(minutes).padStart(2, '0');
    document.getElementById('seconds').textContent = String(seconds).padStart(2, '0');
}

function startTimer() {
    const choreId = document.getElementById('choreId').value;

    fetch('/api/timer/start', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ choreId: parseInt(choreId) })
    })
    .then(response => response.json())
    .then(data => {
        executionId = data.executionId;
        isRunning = true;
        currentSeconds = 0;
        updateDisplay();

        timerInterval = setInterval(() => {
            if (isRunning) currentSeconds++;
            updateDisplay();
        }, 1000);

        document.getElementById('startBtn').style.display = 'none';
        document.getElementById('pauseBtn').style.display = 'inline-block';
        document.getElementById('stopBtn').style.display = 'inline-block';
    })
    .catch(error => {
        alert('Error starting timer: ' + error);
    });
}

function pauseTimer() {
    fetch('/api/timer/pause', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ executionId: executionId })
    })
    .then(response => response.json())
    .then(data => {
        isRunning = false;
        document.getElementById('pauseBtn').style.display = 'none';
        document.getElementById('resumeBtn').style.display = 'inline-block';
    });
}

function resumeTimer() {
    fetch('/api/timer/resume', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ executionId: executionId })
    })
    .then(response => response.json())
    .then(data => {
        isRunning = true;
        document.getElementById('resumeBtn').style.display = 'none';
        document.getElementById('pauseBtn').style.display = 'inline-block';
    });
}

function stopTimer() {
    if (!executionId) {
        alert('No active timer to stop');
        window.location.href = '/dashboard';
        return;
    }

    clearInterval(timerInterval);

    fetch('/api/timer/stop', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ executionId: executionId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.pointsAwarded && data.pointsAwarded > 0) {
            alert('✅ Task completed! You earned ' + data.pointsAwarded + ' points!');
        } else if (data.status === 'completed') {
            alert('⏳ Task completed! Waiting for parent approval.');
        } else {
            alert('Task completed!');
        }
        window.location.href = '/dashboard';
    })
    .catch(error => {
        console.error('Error:', error);
        alert('Task completed! Redirecting to dashboard...');
        window.location.href = '/dashboard';
    });
}