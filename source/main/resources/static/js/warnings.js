/* ============================================
   CashFlow — Warnings & Anomalies Page Module
   ============================================ */

const WarningsPage = {
    init() {
        document.querySelectorAll('#page-warnings .tab-btn').forEach(btn => {
            btn.addEventListener('click', () => {
                document.querySelectorAll('#page-warnings .tab-btn').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('#page-warnings .tab-content').forEach(t => t.classList.remove('active'));
                btn.classList.add('active');
                const target = document.getElementById(btn.dataset.tab);
                if (target) target.classList.add('active');
            });
        });
    },

    switchTab(tabId) {
        document.querySelectorAll('#page-warnings .tab-btn').forEach(b => b.classList.toggle('active', b.dataset.tab === tabId));
        document.querySelectorAll('#page-warnings .tab-content').forEach(t => t.classList.toggle('active', t.id === tabId));
    },

    async load() {
        try {
            const versionId = document.getElementById('filter-version')?.value || '';
            const [warnings, anomalies] = await Promise.all([
                API.getWarnings(versionId),
                API.getAnomalies(versionId),
            ]);
            this.renderWarnings(warnings || []);
            this.renderAnomalies(anomalies || []);
        } catch (error) {
            console.error('Warnings load error:', error);
            Utils.showToast('Failed to load warnings: ' + error.message, 'error');
        }
    },

    renderWarnings(warnings) {
        const container = document.getElementById('warnings-list');
        if (!warnings || warnings.length === 0) {
            container.innerHTML = '<div class="empty-state">No warnings detected — your finances look healthy.</div>';
            return;
        }

        container.innerHTML = warnings.map(w => `
            <div class="warning-card severity-${(w.severity || 'info').toLowerCase()}">
                <div class="warning-type">
                    ${Utils.statusBadge(w.severity)} ${Utils.escapeHtml(w.warningType || w.type || '')}
                </div>
                <div class="warning-message">${Utils.escapeHtml(w.message)}</div>
                <div class="warning-details">
                    ${w.projectedDate ? `<strong>When:</strong> ${Utils.formatDate(w.projectedDate)}<br>` : ''}
                    ${w.details ? Utils.escapeHtml(w.details) : ''}
                    ${w.contributingTransactions ? `<br><strong>Contributing transactions:</strong> ${Utils.escapeHtml(w.contributingTransactions)}` : ''}
                    ${w.amount ? `<br><strong>Amount involved:</strong> ${Utils.formatCurrency(w.amount)}` : ''}
                    ${(w.warningType || '').includes('OVERDUE') || (w.message || '').toLowerCase().includes('overdue') ? `
                        <div style="margin-top: 10px;">
                            <button class="btn btn-sm btn-primary" onclick="App.navigateTo('communication')">
                                ${Utils.icons.communication} Open Communication Hub
                            </button>
                        </div>
                    ` : ''}
                </div>
            </div>
        `).join('');
    },

    renderAnomalies(anomalies) {
        const tbody = document.getElementById('anomalies-tbody');
        if (!anomalies || anomalies.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="6"><div class="empty-state">No anomalies detected</div></td></tr>';
            return;
        }

        tbody.innerHTML = anomalies.map(a => `
            <tr>
                <td>${Utils.escapeHtml(a.anomalyType || a.type || '')}</td>
                <td>${Utils.statusBadge(a.severity)}</td>
                <td>${Utils.escapeHtml(a.transactionId || '—')}</td>
                <td class="amount-col">${Utils.formatCurrency(a.expectedValue)}</td>
                <td class="amount-col">${Utils.formatCurrency(a.actualValue)}</td>
                <td>${Utils.escapeHtml(a.explanation || '')}</td>
            </tr>
        `).join('');
    },
};
