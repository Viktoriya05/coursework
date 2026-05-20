// Timer functionality
let timerInterval = null;
let currentExecutionId = null;
let isActive = false;
let isPaused = false;

const API_BASE = '/api/timer';

document.addEventListener('DOMContentLoaded', function() {
    const choreId = document.getElementById('choreId')?.value;
    if (choreId) {
        checkActiveTimer();
    }

    // Обработка закрытия страницы
    window.addEventListener('beforeunload', function() {
        if (isActive && currentExecutionId) {
            // Не отправляем автоматически - пользователь должен сам остановить
            return "You have an active timer. Are you sure you want to leave?";
        }
    });
});

function checkActiveTimer() {
    fetch(API_BASE + '/active', {
        method: 'GET',
        headers: {
            'Content-Type': 'application/json'
        }
    })
    .then(response => response.json())
    .then(data => {
        if (data.success && data.data && data.data.status === 'active') {
            currentExecutionId = data.data.executionId;
            isActive = true;
            updateUIForActiveTimer();
            startTimerDisplay(data.data.startTime);
        }
    })
    .catch(error => {
        console.error('Error checking active timer:', error);
    });
}

function startTimer() {
    const choreId = document.getElementById('choreId').value;

    fetch(API_BASE + '/start', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ choreId: parseInt(choreId) })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            currentExecutionId = data.data.executionId;
            isActive = true;
            updateUIForActiveTimer();
            startTimerDisplay(data.data.startTime);
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error starting timer:', error);
        showError('Failed to start timer');
    });
}

function pauseTimer() {
    if (!currentExecutionId) return;

    fetch(API_BASE + '/pause', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ executionId: currentExecutionId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            isActive = false;
            isPaused = true;
            updateUIForPausedTimer();
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error pausing timer:', error);
        showError('Failed to pause timer');
    });
}

function resumeTimer() {
    if (!currentExecutionId) return;

    fetch(API_BASE + '/resume', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ executionId: currentExecutionId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            isActive = true;
            isPaused = false;
            updateUIForActiveTimer();
            startTimerDisplay(data.data.startTime);
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error resuming timer:', error);
        showError('Failed to resume timer');
    });
}

function stopTimer() {
    if (!currentExecutionId) return;

    fetch(API_BASE + '/stop', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ executionId: currentExecutionId })
    })
    .then(response => response.json())
    .then(data => {
        if (data.success) {
            isActive = false;
            isPaused = false;
            updateUIForStoppedTimer();
            if (timerInterval) {
                clearInterval(timerInterval);
                timerInterval = null;
            }
            showSuccess('Task completed! Waiting for parent approval.');

            // Перенаправление через 2 секунды
            setTimeout(() => {
                window.location.href = '/chores';
            }, 2000);
        } else {
            showError(data.message);
        }
    })
    .catch(error => {
        console.error('Error stopping timer:', error);
        showError('Failed to stop timer');
    });
}

let timerStartTime = null;

function startTimerDisplay(startTime) {
    timerStartTime = new Date(startTime);
    if (timerInterval) {
        clearInterval(timerInterval);
    }
    updateTimerDisplay();
    timerInterval = setInterval(updateTimerDisplay, 1000);
}

function updateTimerDisplay() {
    if (!timerStartTime) return;

    const now = new Date();
    let diffSeconds = Math.floor((now - timerStartTime) / 1000);

    // Проверка на отрицательное время (если startTime в будущем)
    if (diffSeconds < 0) diffSeconds = 0;

    const hours = Math.floor(diffSeconds / 3600);
    const minutes = Math.floor((diffSeconds % 3600) / 60);
    const seconds = diffSeconds % 60;

    const timerElement = document.getElementById('timer');
    if (timerElement) {
        timerElement.textContent = `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`;
    }
}

function updateUIForActiveTimer() {
    document.getElementById('btn-start')?.setAttribute('style', 'display: none');
    document.getElementById('btn-pause')?.setAttribute('style', 'display: inline-block');
    document.getElementById('btn-resume')?.setAttribute('style', 'display: none');
    document.getElementById('btn-stop')?.setAttribute('style', 'display: inline-block');
}

function updateUIForPausedTimer() {
    document.getElementById('btn-start')?.setAttribute('style', 'display: none');
    document.getElementById('btn-pause')?.setAttribute('style', 'display: none');
    document.getElementById('btn-resume')?.setAttribute('style', 'display: inline-block');
    document.getElementById('btn-stop')?.setAttribute('style', 'display: inline-block');
}

function updateUIForStoppedTimer() {
    document.getElementById('btn-start')?.setAttribute('style', 'display: inline-block');
    document.getElementById('btn-pause')?.setAttribute('style', 'display: none');
    document.getElementById('btn-resume')?.setAttribute('style', 'display: none');
    document.getElementById('btn-stop')?.setAttribute('style', 'display: none');
}

function showError(message) {
    const errorDiv = document.createElement('div');
    errorDiv.className = 'alert alert-danger';
    errorDiv.textContent = message;
    document.querySelector('.timer-container')?.insertBefore(errorDiv, document.querySelector('.timer-card'));
    setTimeout(() => errorDiv.remove(), 3000);
}

function showSuccess(message) {
    const successDiv = document.createElement('div');
    successDiv.className = 'alert alert-success';
    successDiv.textContent = message;
    document.querySelector('.timer-container')?.insertBefore(successDiv, document.querySelector('.timer-card'));
    setTimeout(() => successDiv.remove(), 3000);
}
