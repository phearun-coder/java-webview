// API Base URL - Use relative path to work with any host/port
const API_BASE = '/api';

// Detect if running in JavaFX WebView vs browser
const isJavaFX = navigator.userAgent.includes('JavaWebView') ||
                 window.javafx ||
                 typeof java !== 'undefined';

// WebSocket connection
let ws = null;
let wsReconnectTimeout = null;

// Initialize app when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    console.log('Application initialized');
    detectRuntimeEnvironment();
    updateTimestamp();
    checkBackendHealth();
    loadSystemInfo();
    connectWebSocket();
    setInterval(updateTimestamp, 1000);
});

// Detect runtime environment and update UI accordingly
function detectRuntimeEnvironment() {
    const runtimeIndicator = document.createElement('div');
    runtimeIndicator.id = 'runtimeIndicator';
    runtimeIndicator.style.cssText = `
        position: fixed;
        top: 10px;
        right: 10px;
        padding: 5px 10px;
        border-radius: 15px;
        font-size: 0.8em;
        font-weight: bold;
        z-index: 1000;
        box-shadow: 0 2px 4px rgba(0,0,0,0.2);
    `;

    if (isJavaFX) {
        runtimeIndicator.textContent = '🖥️ Desktop App';
        runtimeIndicator.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
        runtimeIndicator.style.color = 'white';
        console.log('Running in JavaFX WebView (Native Desktop App)');
    } else {
        runtimeIndicator.textContent = '🌐 Web Browser';
        runtimeIndicator.style.background = 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)';
        runtimeIndicator.style.color = 'white';
        console.log('Running in Web Browser');
    }

    document.body.appendChild(runtimeIndicator);
}

// Update timestamp in footer
function updateTimestamp() {
    document.getElementById('timestamp').textContent = new Date().toLocaleString();
}

// Check backend health
async function checkBackendHealth() {
    const statusDiv = document.getElementById('backendStatus');
    try {
        const response = await fetch(`${API_BASE}/health`);
        const data = await response.json();
        
        statusDiv.innerHTML = `
            <div class="success">
                <p>✅ Backend is connected!</p>
                <p><strong>Status:</strong> ${data.status}</p>
                <p><strong>Timestamp:</strong> ${new Date(data.timestamp).toLocaleString()}</p>
            </div>
        `;
    } catch (error) {
        statusDiv.innerHTML = `
            <div class="error">
                <p>❌ Backend connection failed</p>
                <p>${error.message}</p>
            </div>
        `;
    }
}

// Load system information
async function loadSystemInfo() {
    const infoDiv = document.getElementById('systemInfo');
    
    try {
        // Get info from backend API
        const response = await fetch(`${API_BASE}/data`);
        const data = await response.json();
        
        // Try to get native system info if available
        let nativeInfo = null;
        if (typeof getSystemInfo !== 'undefined') {
            try {
                const result = await getSystemInfo();
                nativeInfo = JSON.parse(result);
            } catch (e) {
                console.log('Native bridge not available:', e);
            }
        }
        
        infoDiv.innerHTML = `
            <div class="info-grid">
                <div>
                    <strong>Backend Message:</strong>
                    <p>${data.message}</p>
                </div>
                <div>
                    <strong>Platform:</strong>
                    <p>${data.platform}</p>
                </div>
                <div>
                    <strong>Version:</strong>
                    <p>${data.version}</p>
                </div>
                ${nativeInfo ? `
                <div>
                    <strong>OS (Native):</strong>
                    <p>${nativeInfo.os} ${nativeInfo.version}</p>
                </div>
                <div>
                    <strong>Architecture:</strong>
                    <p>${nativeInfo.arch}</p>
                </div>
                ` : ''}
                <div>
                    <strong>User Agent:</strong>
                    <p>${navigator.userAgent}</p>
                </div>
            </div>
        `;
    } catch (error) {
        infoDiv.innerHTML = `<div class="error">Error loading system info: ${error.message}</div>`;
    }
}

// Calculate using backend API
async function calculate() {
    const num1 = parseFloat(document.getElementById('num1').value);
    const num2 = parseFloat(document.getElementById('num2').value);
    const operation = document.getElementById('operation').value;
    const resultDiv = document.getElementById('calcResult');
    
    if (isNaN(num1) || isNaN(num2)) {
        resultDiv.innerHTML = '<div class="error">Please enter valid numbers</div>';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/calculate`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                num1: num1,
                num2: num2,
                operation: operation
            })
        });
        
        const data = await response.json();
        
        if (response.ok) {
            const operationSymbol = {
                'add': '+',
                'subtract': '-',
                'multiply': '×',
                'divide': '÷'
            }[operation];
            
            resultDiv.innerHTML = `
                <div class="success">
                    <h3>Result: ${data.result}</h3>
                    <p>${num1} ${operationSymbol} ${num2} = ${data.result}</p>
                </div>
            `;
        } else {
            resultDiv.innerHTML = `<div class="error">Error: ${data.error}</div>`;
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
    }
}

// Send message to backend
async function sendMessage() {
    const message = document.getElementById('messageInput').value;
    const resultDiv = document.getElementById('messageResult');
    
    try {
        const response = await fetch(`${API_BASE}/process`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                message: message,
                timestamp: Date.now()
            })
        });
        
        const data = await response.json();
        
        resultDiv.innerHTML = `
            <div class="success">
                <h4>Response from Backend:</h4>
                <pre>${JSON.stringify(data, null, 2)}</pre>
            </div>
        `;
    } catch (error) {
        resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
    }
}

// Test native bridge (WebView Java bindings)
async function testNativeBridge() {
    const resultDiv = document.getElementById('nativeResult');
    
    if (typeof getSystemInfo === 'undefined') {
        resultDiv.innerHTML = `
            <div class="warning">
                <p>⚠️ Native bridge not available</p>
                <p>This feature only works when running in the WebView application.</p>
            </div>
        `;
        return;
    }
    
    try {
        const result = await getSystemInfo();
        const info = JSON.parse(result);
        
        resultDiv.innerHTML = `
            <div class="success">
                <h4>Native System Info:</h4>
                <pre>${JSON.stringify(info, null, 2)}</pre>
            </div>
        `;
    } catch (error) {
        resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
    }
}

// Show native alert
async function showNativeAlert() {
    const resultDiv = document.getElementById('nativeResult');
    
    if (typeof showAlert === 'undefined') {
        resultDiv.innerHTML = `
            <div class="warning">
                <p>⚠️ Native bridge not available</p>
                <p>This feature only works when running in the WebView application.</p>
            </div>
        `;
        return;
    }
    
    try {
        const result = await showAlert('Hello from JavaScript!');
        resultDiv.innerHTML = `
            <div class="success">
                <p>✅ Alert sent to Java backend</p>
                <p>Response: ${result}</p>
            </div>
        `;
    } catch (error) {
        resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
    }
}

// WebSocket Functions
function connectWebSocket() {
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const wsUrl = `${protocol}//${window.location.host}/ws`;
    
    try {
        ws = new WebSocket(wsUrl);
        
        ws.onopen = () => {
            console.log('WebSocket connected');
            updateWSStatus('connected');
        };
        
        ws.onmessage = (event) => {
            console.log('WebSocket message:', event.data);
            const data = JSON.parse(event.data);
            handleWebSocketMessage(data);
        };
        
        ws.onclose = () => {
            console.log('WebSocket disconnected');
            updateWSStatus('disconnected');
            wsReconnectTimeout = setTimeout(connectWebSocket, 3000);
        };
        
        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
            updateWSStatus('error');
        };
    } catch (error) {
        console.error('Failed to create WebSocket:', error);
        updateWSStatus('error');
    }
}

function handleWebSocketMessage(data) {
    switch(data.type) {
        case 'welcome':
            showNotification('Connected to server', 'success');
            break;
        case 'connectionCount':
            updateConnectionCount(data.count);
            break;
        case 'broadcast':
            showNotification(`Broadcast: ${data.message}`, 'info');
            break;
    }
}

function updateWSStatus(status) {
    const statusElement = document.getElementById('wsStatus');
    if (statusElement) {
        const statusIcons = {'connected': '🟢 Connected', 'disconnected': '🔴 Disconnected', 'error': '🟡 Error'};
        statusElement.textContent = statusIcons[status] || status;
    }
}

function updateConnectionCount(count) {
    const countElement = document.getElementById('wsConnections');
    if (countElement) {
        countElement.textContent = `${count} connection${count !== 1 ? 's' : ''}`;
    }
}

function sendWebSocketMessage() {
    const input = document.getElementById('wsMessageInput');
    const message = input.value.trim();
    if (!message) return showNotification('Please enter a message', 'warning');
    if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send(message);
        showNotification('Message sent', 'success');
        input.value = '';
    } else {
        showNotification('WebSocket not connected', 'error');
    }
}

async function broadcastMessage() {
    const input = document.getElementById('broadcastInput');
    const message = input.value.trim();
    if (!message) return showNotification('Enter a message', 'warning');
    try {
        const response = await fetch(`${API_BASE}/broadcast`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ message })
        });
        const data = await response.json();
        if (response.ok) {
            showNotification(`Sent to ${data.recipients} client(s)`, 'success');
            input.value = '';
        }
    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
}

function showNotification(message, type = 'info') {
    const notification = document.createElement('div');
    notification.textContent = message;
    notification.style.cssText = `position:fixed;top:20px;right:20px;padding:15px 20px;border-radius:8px;background:${type==='success'?'#d4edda':type==='error'?'#f8d7da':type==='warning'?'#fff3cd':'#d1ecf1'};color:${type==='success'?'#155724':type==='error'?'#721c24':type==='warning'?'#856404':'#0c5460'};box-shadow:0 4px 6px rgba(0,0,0,0.1);z-index:10000;`;
    document.body.appendChild(notification);
    setTimeout(() => notification.remove(), 3000);
}

// Settings Management Functions
async function loadSettings() {
    const container = document.getElementById('settingsContainer');
    
    try {
        // Get all settings from backend
        const response = await fetch(`${API_BASE}/settings/all`);
        const settings = await response.json();
        
        container.innerHTML = createSettingsForm(settings);
    } catch (error) {
        container.innerHTML = `<div class="error">Error loading settings: ${error.message}</div>`;
    }
}

function createSettingsForm(settings) {
    return `
        <div class="settings-grid">
            <div class="setting-group">
                <h4>Window Settings</h4>
                <div class="setting-item">
                    <label>Window Width:</label>
                    <input type="number" id="setting-window-width" value="${settings['window.width'] || 1200}">
                </div>
                <div class="setting-item">
                    <label>Window Height:</label>
                    <input type="number" id="setting-window-height" value="${settings['window.height'] || 800}">
                </div>
                <div class="setting-item">
                    <label>Window X Position:</label>
                    <input type="number" id="setting-window-x" value="${settings['window.x'] || 100}">
                </div>
                <div class="setting-item">
                    <label>Window Y Position:</label>
                    <input type="number" id="setting-window-y" value="${settings['window.y'] || 100}">
                </div>
                <div class="setting-item">
                    <label><input type="checkbox" id="setting-window-maximized" ${settings['window.maximized'] === 'true' ? 'checked' : ''}> Window Maximized</label>
                </div>
            </div>
            
            <div class="setting-group">
                <h4>Application Settings</h4>
                <div class="setting-item">
                    <label>Theme:</label>
                    <select id="setting-theme">
                        <option value="light" ${settings.theme === 'light' ? 'selected' : ''}>Light</option>
                        <option value="dark" ${settings.theme === 'dark' ? 'selected' : ''}>Dark</option>
                    </select>
                </div>
                <div class="setting-item">
                    <label><input type="checkbox" id="setting-autostart" ${settings.autoStart === 'true' ? 'checked' : ''}> Auto Start</label>
                </div>
                <div class="setting-item">
                    <label><input type="checkbox" id="setting-minimize-tray" ${settings.minimizeToTray === 'true' ? 'checked' : ''}> Minimize to Tray</label>
                </div>
                <div class="setting-item">
                    <label><input type="checkbox" id="setting-check-updates" ${settings.checkForUpdates === 'true' ? 'checked' : ''}> Check for Updates</label>
                </div>
                <div class="setting-item">
                    <label><input type="checkbox" id="setting-show-notifications" ${settings['ui.showNotifications'] === 'true' ? 'checked' : ''}> Show Notifications</label>
                </div>
            </div>
            
            <div class="setting-group">
                <h4>Server Settings</h4>
                <div class="setting-item">
                    <label>Server Port:</label>
                    <input type="number" id="setting-server-port" value="${settings['server.port'] || 8080}">
                </div>
                <div class="setting-item">
                    <label>Server Host:</label>
                    <input type="text" id="setting-server-host" value="${settings['server.host'] || 'localhost'}">
                </div>
            </div>
            
            <div class="setting-group">
                <h4>WebSocket Settings</h4>
                <div class="setting-item">
                    <label><input type="checkbox" id="setting-ws-autoreconnect" ${settings['websocket.autoReconnect'] === 'true' ? 'checked' : ''}> Auto Reconnect</label>
                </div>
                <div class="setting-item">
                    <label>Reconnect Delay (ms):</label>
                    <input type="number" id="setting-ws-reconnect-delay" value="${settings['websocket.reconnectDelay'] || 3000}">
                </div>
            </div>
        </div>
    `;
}

async function saveSettings() {
    const settings = {};
    
    // Collect all setting values
    document.querySelectorAll('[id^="setting-"]').forEach(element => {
        const key = element.id.replace('setting-', '').replace(/-/g, '.');
        if (element.type === 'checkbox') {
            settings[key] = element.checked;
        } else {
            settings[key] = element.value;
        }
    });
    
    try {
        const response = await fetch(`${API_BASE}/settings/save`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(settings)
        });
        
        if (response.ok) {
            showNotification('Settings saved successfully', 'success');
        } else {
            throw new Error('Failed to save settings');
        }
    } catch (error) {
        showNotification(`Error saving settings: ${error.message}`, 'error');
    }
}

// File System Functions
async function browseDirectory() {
    const pathInput = document.getElementById('currentPath');
    let path = pathInput.value.trim();
    
    if (!path) {
        // Use current directory if empty
        path = '.';
    }
    
    try {
        const response = await fetch(`${API_BASE}/files/list?path=${encodeURIComponent(path)}`);
        const result = await response.json();
        
        if (result.success) {
            displayFileList(result.files);
            pathInput.value = path;
        } else {
            document.getElementById('fileList').innerHTML = `<div class="error">${result.errorMessage}</div>`;
        }
    } catch (error) {
        document.getElementById('fileList').innerHTML = `<div class="error">Error: ${error.message}</div>`;
    }
}

function displayFileList(files) {
    const fileList = document.getElementById('fileList');
    
    if (!files || files.length === 0) {
        fileList.innerHTML = '<p>No files found</p>';
        return;
    }
    
    fileList.innerHTML = files.map(file => `
        <div class="file-item" onclick="selectFile('${file.path}')">
            <span class="file-icon">${file.directory ? '📁' : '📄'}</span>
            <div class="file-info">
                <div class="file-name">${file.name}</div>
                <div class="file-details">
                    ${file.directory ? 'Directory' : `Size: ${file.sizeFormatted}`} • Modified: ${file.lastModified}
                </div>
            </div>
        </div>
    `).join('');
}

function selectFile(path) {
    document.getElementById('filePath').value = path;
    document.getElementById('newFilePath').value = path;
}

async function readFile() {
    const path = document.getElementById('filePath').value.trim();
    const resultDiv = document.getElementById('fileResult');
    
    if (!path) {
        resultDiv.innerHTML = '<div class="error">Please enter a file path</div>';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/files/read?path=${encodeURIComponent(path)}`);
        const result = await response.json();
        
        if (result.success) {
            resultDiv.innerHTML = `
                <div class="success">
                    <h4>File Content (${result.fileSize} bytes):</h4>
                    <pre>${result.message}</pre>
                </div>
            `;
        } else {
            resultDiv.innerHTML = `<div class="error">${result.message}</div>`;
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
    }
}

async function writeFile() {
    const path = document.getElementById('newFilePath').value.trim();
    const content = document.getElementById('fileContent').value;
    const resultDiv = document.getElementById('fileResult');
    
    if (!path) {
        resultDiv.innerHTML = '<div class="error">Please enter a file path</div>';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/files/write`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ path, content })
        });
        
        const result = await response.json();
        
        if (result.success) {
            resultDiv.innerHTML = `<div class="success">${result.message}</div>`;
            // Trigger file operation notification
            triggerFileNotification('write', path, true);
        } else {
            resultDiv.innerHTML = `<div class="error">${result.message}</div>`;
            triggerFileNotification('write', path, false);
        }
    } catch (error) {
        resultDiv.innerHTML = `<div class="error">Error: ${error.message}</div>`;
    }
}

// Notification Functions
async function loadNotificationStatus() {
    const statusDiv = document.getElementById('notificationStatus');
    
    try {
        const response = await fetch(`${API_BASE}/notifications/status`);
        const status = await response.json();
        
        statusDiv.innerHTML = `
            <div class="info">
                <p><strong>Supported:</strong> ${status.supported ? '✅ Yes' : '❌ No'}</p>
                <p><strong>Initialized:</strong> ${status.initialized ? '✅ Yes' : '❌ No'}</p>
                <p><strong>Available Types:</strong> ${status.availableTypes.join(', ')}</p>
            </div>
        `;
    } catch (error) {
        statusDiv.innerHTML = `<div class="error">Error checking notification status: ${error.message}</div>`;
    }
}

async function showTestNotification() {
    const title = document.getElementById('notificationTitle').value;
    const message = document.getElementById('notificationMessage').value;
    const type = document.getElementById('notificationType').value;
    
    try {
        const response = await fetch(`${API_BASE}/notifications/show`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ title, message, type })
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Notification sent successfully', 'success');
        } else {
            showNotification('Failed to send notification', 'error');
        }
    } catch (error) {
        showNotification(`Error: ${error.message}`, 'error');
    }
}

async function triggerFileNotification(operation, filePath, success) {
    try {
        await fetch(`${API_BASE}/notifications/file-operation`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ operation, filePath, success })
        });
    } catch (error) {
        console.error('Failed to trigger file notification:', error);
    }
}

// Update Manager Functions
async function checkForUpdates() {
    const statusDiv = document.getElementById('updateStatus');
    const downloadBtn = document.getElementById('downloadBtn');
    
    statusDiv.innerHTML = '<p>🔍 Checking for updates...</p>';
    
    try {
        const response = await fetch(`${API_BASE}/updates/check`);
        const result = await response.json();
        
        if (result.updateAvailable) {
            statusDiv.innerHTML = `
                <div class="update-available">
                    <h4>🎉 Update Available!</h4>
                    <p><strong>Current Version:</strong> ${result.currentVersion}</p>
                    <p><strong>Latest Version:</strong> ${result.latestVersion}</p>
                    <p><strong>Release Notes:</strong> ${result.releaseNotes}</p>
                </div>
            `;
            downloadBtn.disabled = false;
        } else {
            statusDiv.innerHTML = `
                <div class="update-current">
                    <h4>✅ Up to Date</h4>
                    <p>You are running the latest version (${result.currentVersion})</p>
                </div>
            `;
            downloadBtn.disabled = true;
        }
    } catch (error) {
        statusDiv.innerHTML = `<div class="error">Error checking for updates: ${error.message}</div>`;
        downloadBtn.disabled = true;
    }
}

async function downloadUpdate() {
    const downloadBtn = document.getElementById('downloadBtn');
    const originalText = downloadBtn.textContent;
    
    downloadBtn.textContent = 'Downloading...';
    downloadBtn.disabled = true;
    
    try {
        const response = await fetch(`${API_BASE}/updates/download`, {
            method: 'POST'
        });
        
        const result = await response.json();
        
        if (result.success) {
            showNotification('Update downloaded successfully. Restart the application to apply.', 'success');
            downloadBtn.textContent = 'Downloaded';
        } else {
            throw new Error(result.message || 'Download failed');
        }
    } catch (error) {
        showNotification(`Update download failed: ${error.message}`, 'error');
        downloadBtn.textContent = originalText;
        downloadBtn.disabled = false;
    }
}

// Initialize new features when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    // ... existing code ...
    
    // Initialize new features
    loadSettings();
    loadNotificationStatus();
    checkForUpdates();
    
    // Initialize theme toggle
    initializeThemeToggle();
    
    // Initialize keyboard shortcuts
    initializeKeyboardShortcuts();
    
    // Initialize context menu
    initializeContextMenu();
    
    // Initialize task manager
    initializeTaskManager();
});

// Theme toggle functionality
async function initializeThemeToggle() {
    const themeToggle = document.getElementById('themeToggle');
    if (!themeToggle) return;
    
    // Load current theme from settings
    try {
        const response = await fetch(`${API_BASE}/settings/get?key=theme`);
        const data = await response.json();
        const currentTheme = data.value || 'light';
        applyTheme(currentTheme);
        updateThemeToggleButton(currentTheme);
    } catch (error) {
        console.warn('Could not load theme setting, using light theme');
        applyTheme('light');
    }
    
    // Add click handler
    themeToggle.addEventListener('click', async () => {
        const currentTheme = document.documentElement.getAttribute('data-theme') || 'light';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';
        
        try {
            // Save theme setting
            await fetch(`${API_BASE}/settings/set`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ key: 'theme', value: newTheme })
            });
            
            applyTheme(newTheme);
            updateThemeToggleButton(newTheme);
            showNotification(`Switched to ${newTheme} theme`, 'info');
        } catch (error) {
            showNotification('Failed to save theme preference', 'error');
        }
    });
}

function applyTheme(theme) {
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
}

function updateThemeToggleButton(theme) {
    const themeToggle = document.getElementById('themeToggle');
    if (themeToggle) {
        themeToggle.textContent = theme === 'light' ? '🌙' : '☀️';
        themeToggle.title = `Switch to ${theme === 'light' ? 'dark' : 'light'} theme`;
    }
}

// Keyboard shortcuts functionality
function initializeKeyboardShortcuts() {
    document.addEventListener('keydown', (e) => {
        // Only handle shortcuts when not typing in input fields
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA' || e.target.contentEditable === 'true') {
            return;
        }
        
        const ctrl = e.ctrlKey || e.metaKey;
        
        // Ctrl+T: New Window
        if (ctrl && e.key === 't') {
            e.preventDefault();
            if (isJavaFX && window.javafx) {
                window.javafx.newWindow();
            } else {
                window.open(window.location.href, '_blank');
            }
            return;
        }
        
        // Ctrl+W: Close Window
        if (ctrl && e.key === 'w') {
            e.preventDefault();
            if (isJavaFX && window.javafx) {
                window.javafx.closeWindow();
            } else {
                window.close();
            }
            return;
        }
        
        // Ctrl+R: Reload Page
        if (ctrl && e.key === 'r') {
            e.preventDefault();
            window.location.reload();
            return;
        }
        
        // Ctrl+Shift+I: Developer Tools
        if (ctrl && e.shiftKey && e.key === 'I') {
            e.preventDefault();
            if (isJavaFX && window.javafx) {
                window.javafx.openDevTools();
            }
            return;
        }
        
        // Ctrl+O: Open File
        if (ctrl && e.key === 'o') {
            e.preventDefault();
            document.getElementById('fileInput').click();
            return;
        }
        
        // Ctrl+S: Save File
        if (ctrl && e.key === 's') {
            e.preventDefault();
            // Trigger save operation if file is open
            const saveBtn = document.querySelector('#fileOperations button[onclick*="saveFile"]');
            if (saveBtn) saveBtn.click();
            return;
        }
        
        // Ctrl+N: New File
        if (ctrl && e.key === 'n') {
            e.preventDefault();
            // Clear file content and reset
            const fileContent = document.getElementById('fileContent');
            if (fileContent) {
                fileContent.value = '';
                document.getElementById('currentFile').textContent = 'New File';
            }
            return;
        }
        
        // Ctrl+, : Open Settings
        if (ctrl && e.key === ',') {
            e.preventDefault();
            showPanel('settings');
            return;
        }
        
        // Ctrl+Shift+T: Toggle Theme
        if (ctrl && e.shiftKey && e.key === 'T') {
            e.preventDefault();
            document.getElementById('themeToggle').click();
            return;
        }
        
        // Ctrl+Tab: Next Window (if JavaFX)
        if (ctrl && e.key === 'Tab') {
            e.preventDefault();
            if (isJavaFX && window.javafx) {
                window.javafx.nextWindow();
            }
            return;
        }
        
        // Ctrl+1-9: Switch to specific window
        if (ctrl && e.key >= '1' && e.key <= '9') {
            e.preventDefault();
            const windowIndex = parseInt(e.key) - 1;
            if (isJavaFX && window.javafx) {
                window.javafx.switchToWindow(windowIndex);
            }
            return;
        }
        
        // F1 or ?: Show shortcuts
        if (e.key === 'F1' || e.key === '?') {
            e.preventDefault();
            showShortcutsModal();
            return;
        }
    });
}

// Modal functionality
function showShortcutsModal() {
    const modal = document.getElementById('shortcutsModal');
    if (modal) {
        modal.style.display = 'block';
        document.body.style.overflow = 'hidden';
    }
}

function closeModal() {
    const modal = document.getElementById('shortcutsModal');
    if (modal) {
        modal.style.display = 'none';
        document.body.style.overflow = 'auto';
    }
}

// Close modal when clicking outside
window.addEventListener('click', (e) => {
    const modal = document.getElementById('shortcutsModal');
    if (e.target === modal) {
        closeModal();
    }
});

// Context menu functionality
function initializeContextMenu() {
    document.addEventListener('contextmenu', (e) => {
        // Only show context menu on text areas and inputs
        if (e.target.tagName === 'TEXTAREA' || e.target.tagName === 'INPUT' || e.target.contentEditable === 'true') {
            e.preventDefault();
            showContextMenu(e.pageX, e.pageY);
        }
    });
    
    // Hide context menu when clicking elsewhere
    document.addEventListener('click', () => {
        hideContextMenu();
    });
}

function showContextMenu(x, y) {
    const menu = document.getElementById('contextMenu');
    if (menu) {
        menu.style.left = x + 'px';
        menu.style.top = y + 'px';
        menu.style.display = 'block';
    }
}

function hideContextMenu() {
    const menu = document.getElementById('contextMenu');
    if (menu) {
        menu.style.display = 'none';
    }
}

function contextAction(action) {
    const activeElement = document.activeElement;
    
    switch (action) {
        case 'copy':
            if (activeElement && typeof activeElement.select === 'function') {
                activeElement.select();
                document.execCommand('copy');
            }
            break;
        case 'paste':
            // Note: paste requires clipboard permission
            navigator.clipboard.readText().then(text => {
                if (activeElement) {
                    const start = activeElement.selectionStart;
                    const end = activeElement.selectionEnd;
                    activeElement.value = activeElement.value.substring(0, start) + text + activeElement.value.substring(end);
                    activeElement.selectionStart = activeElement.selectionEnd = start + text.length;
                }
            }).catch(err => console.warn('Paste failed:', err));
            break;
        case 'selectAll':
            if (activeElement && typeof activeElement.select === 'function') {
                activeElement.select();
            }
            break;
        case 'clear':
            if (activeElement && activeElement.value !== undefined) {
                activeElement.value = '';
            }
            break;
    }
    
    hideContextMenu();
}

// Task Manager Functions
let taskUpdateInterval;

async function loadTaskStats() {
    try {
        const response = await fetch(`${API_BASE}/tasks/stats`);
        const stats = await response.json();
        
        document.getElementById('totalTasks').textContent = stats.total || 0;
        document.getElementById('runningTasks').textContent = stats.running || 0;
        document.getElementById('completedTasks').textContent = stats.completed || 0;
        document.getElementById('failedTasks').textContent = stats.failed || 0;
    } catch (error) {
        console.warn('Failed to load task stats:', error);
    }
}

async function loadActiveTasks() {
    try {
        const response = await fetch(`${API_BASE}/tasks`);
        const data = await response.json();
        const tasks = data.tasks || [];
        
        const taskList = document.getElementById('taskList');
        
        if (tasks.length === 0) {
            taskList.innerHTML = '<p>No active tasks</p>';
            return;
        }
        
        taskList.innerHTML = tasks.map(task => createTaskElement(task)).join('');
    } catch (error) {
        console.warn('Failed to load tasks:', error);
        document.getElementById('taskList').innerHTML = '<p>Failed to load tasks</p>';
    }
}

function createTaskElement(task) {
    const statusClass = task.status.toLowerCase();
    const progressPercent = Math.round(task.progress || 0);
    
    return `
        <div class="task-item" data-task-id="${task.taskId}">
            <div class="task-info">
                <div class="task-name">${escapeHtml(task.name)}</div>
                <div class="task-description">${escapeHtml(task.description)}</div>
                <div class="task-meta">
                    <span class="task-status ${statusClass}">${task.status}</span>
                    <span>Created: ${new Date(task.createdAt).toLocaleTimeString()}</span>
                    ${task.completedAt ? `<span>Completed: ${new Date(task.completedAt).toLocaleTimeString()}</span>` : ''}
                </div>
            </div>
            <div class="task-progress">
                <div class="task-progress-bar" style="width: ${progressPercent}%"></div>
            </div>
            <div class="task-actions">
                ${task.status === 'RUNNING' ? `<button class="task-action-btn cancel" onclick="cancelTask('${task.taskId}')">Cancel</button>` : ''}
                ${task.status !== 'RUNNING' ? `<button class="task-action-btn remove" onclick="removeTask('${task.taskId}')">Remove</button>` : ''}
            </div>
        </div>
    `;
}

async function refreshTasks() {
    await Promise.all([loadTaskStats(), loadActiveTasks()]);
}

async function submitDemoTask() {
    try {
        const response = await fetch(`${API_BASE}/tasks/submit`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                name: 'Demo Task',
                description: 'A demonstration task that simulates work with progress updates',
                type: 'simple'
            })
        });
        
        const result = await response.json();
        if (result.taskId) {
            showNotification('Demo task submitted successfully', 'success');
            await refreshTasks();
        } else {
            showNotification('Failed to submit task', 'error');
        }
    } catch (error) {
        showNotification('Failed to submit demo task', 'error');
        console.error('Task submission error:', error);
    }
}

async function cancelTask(taskId) {
    try {
        const response = await fetch(`${API_BASE}/tasks/${taskId}/cancel`, {
            method: 'POST'
        });
        
        const result = await response.json();
        if (result.cancelled) {
            showNotification('Task cancelled successfully', 'info');
            await refreshTasks();
        } else {
            showNotification('Failed to cancel task', 'warning');
        }
    } catch (error) {
        showNotification('Failed to cancel task', 'error');
        console.error('Task cancellation error:', error);
    }
}

async function removeTask(taskId) {
    try {
        const response = await fetch(`${API_BASE}/tasks/${taskId}`, {
            method: 'DELETE'
        });
        
        const result = await response.json();
        if (result.removed) {
            showNotification('Task removed successfully', 'info');
            await refreshTasks();
        } else {
            showNotification('Failed to remove task', 'warning');
        }
    } catch (error) {
        showNotification('Failed to remove task', 'error');
        console.error('Task removal error:', error);
    }
}

async function clearCompletedTasks() {
    try {
        const response = await fetch(`${API_BASE}/tasks`);
        const data = await response.json();
        const tasks = data.tasks || [];
        
        const completedTasks = tasks.filter(task => 
            task.status === 'COMPLETED' || task.status === 'FAILED' || task.status === 'CANCELLED'
        );
        
        let removedCount = 0;
        for (const task of completedTasks) {
            try {
                await fetch(`${API_BASE}/tasks/${task.taskId}`, { method: 'DELETE' });
                removedCount++;
            } catch (e) {
                console.warn(`Failed to remove task ${task.taskId}:`, e);
            }
        }
        
        if (removedCount > 0) {
            showNotification(`Cleared ${removedCount} completed task(s)`, 'success');
            await refreshTasks();
        } else {
            showNotification('No completed tasks to clear', 'info');
        }
    } catch (error) {
        showNotification('Failed to clear completed tasks', 'error');
        console.error('Clear tasks error:', error);
    }
}

// WebSocket task update handler
function handleTaskUpdate(data) {
    // Update the specific task in the UI
    const taskElement = document.querySelector(`[data-task-id="${data.taskId}"]`);
    if (taskElement) {
        // Update the existing task element
        const newElement = createTaskElement(data);
        taskElement.outerHTML = newElement;
    } else {
        // Add new task to the list
        const taskList = document.getElementById('taskList');
        if (taskList.querySelector('p')) {
            taskList.innerHTML = ''; // Remove "No active tasks" message
        }
        taskList.insertAdjacentHTML('afterbegin', createTaskElement(data));
    }
    
    // Update stats
    loadTaskStats();
}

// Initialize task manager
function initializeTaskManager() {
    // Load initial data
    refreshTasks();
    
    // Set up periodic refresh for stats
    taskUpdateInterval = setInterval(loadTaskStats, 5000); // Update stats every 5 seconds
    
    // Listen for WebSocket task updates
    if (ws) {
        const originalOnMessage = ws.onmessage;
        ws.onmessage = function(event) {
            try {
                const data = JSON.parse(event.data);
                if (data.type === 'task-update') {
                    handleTaskUpdate(data.data);
                }
            } catch (e) {
                console.warn('Failed to parse WebSocket message:', e);
            }
            
            // Call original handler if it exists
            if (originalOnMessage) {
                originalOnMessage.call(this, event);
            }
        };
    }
}

// Utility function to escape HTML
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
