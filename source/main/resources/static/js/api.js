/* ============================================
   CashFlow — API Communication Layer
   ============================================ */

const API = {
    BASE_URL: '/api',

    async request(endpoint, options = {}) {
        const url = `${this.BASE_URL}${endpoint}`;
        const config = {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            ...options,
        };

        // Remove Content-Type for FormData
        if (options.body instanceof FormData) {
            delete config.headers['Content-Type'];
        }

        try {
            const response = await fetch(url, config);
            if (!response.ok) {
                const errorText = await response.text();
                let errorMsg;
                try {
                    const errorJson = JSON.parse(errorText);
                    errorMsg = errorJson.message || errorJson.error || errorText;
                } catch {
                    errorMsg = errorText || `HTTP ${response.status}`;
                }
                throw new Error(errorMsg);
            }
            const contentType = response.headers.get('content-type');
            if (contentType && contentType.includes('application/json')) {
                return await response.json();
            }
            return await response.text();
        } catch (error) {
            if (error.name === 'TypeError' && error.message === 'Failed to fetch') {
                throw new Error('Unable to connect to the server. Please check if the backend is running.');
            }
            throw error;
        }
    },

    get(endpoint) {
        return this.request(endpoint, { method: 'GET' });
    },

    post(endpoint, body) {
        return this.request(endpoint, {
            method: 'POST',
            body: JSON.stringify(body),
        });
    },

    put(endpoint, body) {
        return this.request(endpoint, {
            method: 'PUT',
            body: JSON.stringify(body),
        });
    },

    delete(endpoint) {
        return this.request(endpoint, { method: 'DELETE' });
    },

    uploadFile(endpoint, file) {
        const formData = new FormData();
        formData.append('file', file);
        return this.request(endpoint, {
            method: 'POST',
            body: formData,
        });
    },

    // ---- Dashboard ----
    getDashboard(versionId = '') {
        const q = versionId ? `?versionId=${encodeURIComponent(versionId)}` : '';
        return this.get(`/dashboard${q}`);
    },

    // ---- CSV Upload & Demos ----
    uploadCsv(file) { return this.uploadFile('/csv/upload', file); },
    loadDemo(name) { return this.post(`/csv/demo/${encodeURIComponent(name)}`, {}); },

    // ---- Transactions ----
    getTransactions(params = {}) {
        const query = new URLSearchParams(params).toString();
        return this.get(`/transactions${query ? '?' + query : ''}`);
    },
    getTransactionDetail(id) { return this.get(`/transactions/${id}`); },

    // ---- Forecast ----
    getForecast(days = 30, versionId = '') {
        const params = new URLSearchParams({ days });
        if (versionId) params.append('versionId', versionId);
        return this.get(`/forecast?${params.toString()}`);
    },

    // ---- Warnings ----
    getWarnings(versionId = '') {
        const q = versionId ? `?versionId=${encodeURIComponent(versionId)}` : '';
        return this.get(`/warnings${q}`);
    },

    // ---- Anomalies ----
    getAnomalies(versionId = '') {
        const q = versionId ? `?versionId=${encodeURIComponent(versionId)}` : '';
        return this.get(`/anomalies${q}`);
    },

    // ---- Versions ----
    getVersions() { return this.get('/versions'); },
    getVersionDetail(id) { return this.get(`/versions/${id}`); },
    getVersionChanges(id) { return this.get(`/versions/${id}/changes`); },
    compareVersions(v1, v2) { return this.get(`/versions/compare?v1=${v1}&v2=${v2}`); },

    // ---- Receivables ----
    getReceivables(versionId = '') {
        const q = versionId ? `?versionId=${encodeURIComponent(versionId)}` : '';
        return this.get(`/receivables${q}`);
    },

    // ---- Expenses ----
    getExpenses(versionId = '') {
        const q = versionId ? `?versionId=${encodeURIComponent(versionId)}` : '';
        return this.get(`/expenses${q}`);
    },
    getRecurringPayments() { return this.get('/expenses/recurring'); },
    addRecurringPayment(data) { return this.post('/expenses/recurring', data); },
    updateRecurringPayment(id, data) { return this.put(`/expenses/recurring/${id}`, data); },
    deleteRecurringPayment(id) { return this.delete(`/expenses/recurring/${id}`); },

    // ---- Timeline ----
    getTimeline() { return this.get('/timeline'); },

    // ---- Scenarios ----
    createScenario(data) { return this.post('/scenarios', data); },
    getScenarioForecast(id) { return this.get(`/scenarios/${id}/forecast`); },

    // ---- Reports ----
    getReport(params = {}) {
        const query = new URLSearchParams(params).toString();
        return this.get(`/reports${query ? '?' + query : ''}`);
    },
    exportCsv(params = {}) {
        const query = new URLSearchParams(params).toString();
        window.open(`${this.BASE_URL}/reports/export/csv${query ? '?' + query : ''}`, '_blank');
    },

    // ---- Settings ----
    getSettings() { return this.get('/settings'); },
    updateSettings(data) { return this.put('/settings', data); },
    getThemes() { return this.get('/settings/themes'); },

    // ---- Communication & Multi-Channel Outreach ----
    initiateCall(transactionId, notes = '') {
        return this.post('/communication/call', { transactionId, notes });
    },
    sendSms(transactionId, message = '') {
        return this.post('/communication/sms', { transactionId, message });
    },
    sendEmail(transactionId, subject = '', body = '') {
        return this.post('/communication/email', { transactionId, subject, body });
    },
    notifyBoth(transactionId) {
        return this.post('/communication/notify-both', { transactionId });
    },
    bulkNotifyDefaulters() {
        return this.post('/communication/bulk-notify', {});
    },
    getCommunicationLogs(transactionId = null) {
        const query = transactionId ? `?transactionId=${encodeURIComponent(transactionId)}` : '';
        return this.get(`/communication/logs${query}`);
    },
};
