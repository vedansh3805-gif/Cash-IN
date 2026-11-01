/* ============================================
   CashFlow — Reports & Export Page Module
   ============================================ */

const ReportsPage = {
    init() {
        // Handlers wired cleanly
    },

    async generateReport() {
        const from = document.getElementById('report-from').value;
        const to = document.getElementById('report-to').value;

        const params = {};
        if (from) params.from = from;
        if (to) params.to = to;

        try {
            const data = await API.getReport(params);
            this.renderReport(data);
            document.getElementById('report-actions').style.display = '';
        } catch (error) {
            Utils.showToast('Failed to generate report: ' + error.message, 'error');
        }
    },

    renderReport(data) {
        const container = document.getElementById('report-content');
        if (!data) {
            container.innerHTML = '<div class="empty-state">No report data available</div>';
            return;
        }

        container.innerHTML = `
            <div class="report-section">
                <h4>Report Summary</h4>
                <div class="report-row"><span class="label">Date Range</span><span class="value">${Utils.formatDate(data.dateFrom || data.from)} — ${Utils.formatDate(data.dateTo || data.to)}</span></div>
                <div class="report-row"><span class="label">Dataset Version</span><span class="value">V${data.versionNumber || data.version || '—'}</span></div>
                <div class="report-row"><span class="label">Generated</span><span class="value">${Utils.formatDate(new Date().toISOString())}</span></div>
            </div>

            <div class="report-section">
                <h4>Financial Overview</h4>
                <div class="report-row"><span class="label">Current Cash</span><span class="value">${Utils.formatCurrency(data.currentCash)}</span></div>
                <div class="report-row"><span class="label">Total Income</span><span class="value" style="color:var(--success)">${Utils.formatCurrency(data.totalIncome)}</span></div>
                <div class="report-row"><span class="label">Total Expenses</span><span class="value" style="color:var(--danger)">${Utils.formatCurrency(data.totalExpenses)}</span></div>
                <div class="report-row"><span class="label">Net Cash Flow</span><span class="value">${Utils.formatCurrency(data.netCashFlow)}</span></div>
            </div>

            <div class="report-section">
                <h4>Receivables</h4>
                <div class="report-row"><span class="label">Total Receivables</span><span class="value">${Utils.formatCurrency(data.totalReceivables)}</span></div>
                <div class="report-row"><span class="label">Overdue Amount</span><span class="value" style="color:var(--danger)">${Utils.formatCurrency(data.overdueAmount)}</span></div>
                <div class="report-row"><span class="label">Overdue Count</span><span class="value">${data.overdueCount || 0}</span></div>
            </div>

            <div class="report-section">
                <h4>Forecast</h4>
                <div class="report-row"><span class="label">Forecast Period</span><span class="value">${data.forecastDays || 90} days</span></div>
                <div class="report-row"><span class="label">Projected Min Cash</span><span class="value">${Utils.formatCurrency(data.forecastedMinCash)}</span></div>
                <div class="report-row"><span class="label">Projected End Cash</span><span class="value">${Utils.formatCurrency(data.forecastedEndCash)}</span></div>
            </div>

            <div class="report-section">
                <h4>Warnings</h4>
                <div class="report-row"><span class="label">Active Warnings</span><span class="value">${data.warningCount || 0}</span></div>
                ${(data.warnings || []).map(w => `
                    <div class="report-row"><span class="label">${Utils.statusBadge(w.severity)}</span><span class="value">${Utils.escapeHtml(w.message)}</span></div>
                `).join('')}
            </div>

            <div class="report-section">
                <h4>Anomalies</h4>
                <div class="report-row"><span class="label">Detected Anomalies</span><span class="value">${data.anomalyCount || 0}</span></div>
                ${(data.anomalies || []).map(a => `
                    <div class="report-row"><span class="label">${Utils.escapeHtml(a.anomalyType || a.type)}</span><span class="value">${Utils.escapeHtml(a.explanation || '')}</span></div>
                `).join('')}
            </div>
        `;
    },

    exportCsv() {
        const from = document.getElementById('report-from').value;
        const to = document.getElementById('report-to').value;
        const params = {};
        if (from) params.from = from;
        if (to) params.to = to;
        API.exportCsv(params);
    },

    async load() {
        // Page is ready — user generates on demand
    },
};
