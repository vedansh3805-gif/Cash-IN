/* ==========================================================================
   CashFlow — Enterprise Dashboard Page Module
   ========================================================================== */

const DashboardPage = {
    async load() {
        try {
            const versionId = document.getElementById('filter-version')?.value || '';
            const data = await API.getDashboard(versionId);
            this.renderKPIs(data);
            this.renderCashDynamicsChart(data);
            this.renderReceivablesRatioChart(data);
            this.renderCashFlowChart(data);
            this.renderUpcomingPayments(data);
            this.renderWarnings(data);
            this.renderRecentChanges(data);
        } catch (error) {
            console.error('Dashboard load error:', error);
            Utils.showToast('Failed to load dashboard: ' + error.message, 'error');
        }
    },

    renderKPIs(data) {
        const kpis = data || {};
        document.getElementById('kpi-val-current-cash').textContent = Utils.formatCurrency(kpis.currentCash || 0);
        document.getElementById('kpi-val-incoming').textContent = Utils.formatCurrency(kpis.expectedIncoming || 0);
        document.getElementById('kpi-val-expenses').textContent = Utils.formatCurrency(kpis.upcomingExpenses || 0);
        document.getElementById('kpi-val-receivables').textContent = Utils.formatCurrency(kpis.pendingReceivables || 0);

        const overdueVal = kpis.overdueReceivables || 0;
        const overdueEl = document.getElementById('kpi-val-overdue');
        overdueEl.textContent = Utils.formatCurrency(overdueVal);
        overdueEl.className = 'kpi-value' + (overdueVal > 0 ? ' text-danger' : '');

        const netCf = kpis.netCashFlow || 0;
        const netEl = document.getElementById('kpi-val-netcashflow');
        netEl.textContent = Utils.formatCurrency(netCf);
        netEl.className = 'kpi-value' + (netCf >= 0 ? ' text-success' : ' text-danger');

        document.getElementById('kpi-val-forecast-min').textContent = Utils.formatCurrency(kpis.forecastedMinCash || 0);

        const riskEl = document.getElementById('kpi-val-risk');
        const risk = (kpis.riskStatus || 'LOW').toUpperCase();
        riskEl.textContent = risk;
        const riskClass = {
            'LOW': 'badge-success',
            'MODERATE': 'badge-warning',
            'HIGH': 'badge-danger',
            'CRITICAL': 'badge-danger',
        };
        riskEl.className = 'kpi-value ' + (riskClass[risk] || 'badge-info');

        // Sub-labels with crisp micro-copy
        document.getElementById('kpi-sub-current-cash').textContent = 'In-bank liquid balance';
        document.getElementById('kpi-sub-incoming').textContent = 'Pending collections due';
        document.getElementById('kpi-sub-expenses').textContent = 'Pending commitments due';
        document.getElementById('kpi-sub-receivables').textContent = `${kpis.pendingReceivablesCount || 0} invoice(s) pending`;
        document.getElementById('kpi-sub-overdue').textContent = `${kpis.overdueCount || 0} invoice(s) delayed`;
        document.getElementById('kpi-sub-netcashflow').textContent = netCf >= 0 ? 'Net positive liquidity' : 'Net negative deficit';
        document.getElementById('kpi-sub-forecast-min').textContent = `90-day cash floor threshold`;
        document.getElementById('kpi-sub-risk').textContent = `${kpis.warningCount || 0} active alert(s)`;
    },

    /**
     * Cash Dynamics Bar Chart (Direct alignment with Desktop DashboardView)
     */
    renderCashDynamicsChart(data) {
        const kpis = data || {};
        const c = Charts.getThemeColors();

        const labels = ['Current Cash', 'Expected Inflow', 'Upcoming Expenses', 'Net Cash Flow'];
        const values = [
            Math.max(0, kpis.currentCash || 0),
            Math.max(0, kpis.expectedIncoming || 0),
            Math.max(0, kpis.upcomingExpenses || 0),
            kpis.netCashFlow || 0
        ];

        Charts.create('chart-dynamics', 'bar', {
            labels,
            datasets: [{
                label: 'Rupees (₹)',
                data: values,
                backgroundColor: [
                    '#38bdf8cc', // Sky Blue (Current Cash)
                    '#10b981cc', // Emerald Green (Inflows)
                    '#f43f5ecc', // Crimson / Rose (Expenses)
                    '#8b5cf6cc'  // Indigo (Net)
                ],
            }],
        });
    },

    /**
     * Receivables Aging / Exposure Doughnut Chart (Direct alignment with Desktop DashboardView)
     */
    renderReceivablesRatioChart(data) {
        const kpis = data || {};
        const pending = Math.max(0, kpis.pendingReceivables || 0);
        const overdue = Math.max(0, kpis.overdueReceivables || 0);

        let labels = ['Pending On-Time', 'Overdue'];
        let values = [pending, overdue];
        let colors = ['#10b981', '#ef4444'];

        if (pending === 0 && overdue === 0) {
            labels = ['No Outstanding'];
            values = [100];
            colors = ['#64748b'];
        }

        Charts.create('chart-recv-ratio', 'doughnut', {
            labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
            }],
        });
    },

    /**
     * Historical Liquidity & Daily Inflow/Outflow Line Chart
     */
    renderCashFlowChart(data) {
        const history = data.cashFlowHistory || [];
        if (history.length === 0) return;

        const labels = history.map(h => Utils.formatDateShort(h.date || h.transaction_date));
        const balanceData = history.map(h => h.balance != null ? h.balance : (h.net_amount || 0));
        const incomeData = history.map(h => h.income || 0);
        const expenseData = history.map(h => h.expense || 0);

        Charts.create('chart-cashflow', 'line', {
            labels,
            datasets: [
                Charts.lineDataset('Cumulative Cash Balance', balanceData, 'chart1'),
                Charts.lineDataset('Daily Inflow', incomeData, 'success'),
                Charts.lineDataset('Daily Outflow', expenseData, 'danger'),
            ],
        });
    },

    renderUpcomingPayments(data) {
        const container = document.getElementById('upcoming-payments-list');
        const payments = data.upcomingPayments || [];
        if (payments.length === 0) {
            container.innerHTML = '<div class="empty-state">No upcoming payments scheduled</div>';
            return;
        }
        container.innerHTML = payments.slice(0, 6).map(p => `
            <div class="data-list-item">
                <div class="list-item-left">
                    <span class="list-item-title">${Utils.escapeHtml(p.description || p.category || 'Payment')}</span>
                    <span class="list-item-sub">Due: ${Utils.formatDate(p.due_date || p.dueDate)}</span>
                </div>
                <div class="list-item-right">
                    <span class="list-item-amount text-danger">-${Utils.formatCurrency(p.amount)}</span>
                    ${Utils.statusBadge(p.status)}
                </div>
            </div>
        `).join('');
    },

    renderWarnings(data) {
        const container = document.getElementById('dashboard-warnings-list');
        const warnings = data.warnings || [];
        if (warnings.length === 0) {
            container.innerHTML = '<div class="empty-state">System healthy — No active liquidity warnings</div>';
            return;
        }
        container.innerHTML = warnings.slice(0, 5).map(w => `
            <div class="data-list-item warning-item">
                <div class="list-item-left">
                    <div style="display:flex;align-items:center;gap:6px;margin-bottom:2px;">
                        ${Utils.statusBadge(w.severity)}
                        <span class="list-item-title">${Utils.escapeHtml(w.warning_type || w.warningType || 'Alert')}</span>
                    </div>
                    <span class="list-item-sub">${Utils.escapeHtml(w.message)}</span>
                </div>
                <div class="list-item-right">
                    <button class="btn btn-xs btn-outline-primary" onclick="App.navigateTo('communication')">Outreach</button>
                </div>
            </div>
        `).join('');
    },

    renderRecentChanges(data) {
        const container = document.getElementById('recent-changes-list');
        const changes = data.recentChanges || [];
        if (changes.length === 0) {
            container.innerHTML = '<div class="empty-state">No recent ledger modifications</div>';
            return;
        }
        container.innerHTML = changes.slice(0, 6).map(c => `
            <div class="data-list-item">
                <div class="list-item-left">
                    <div style="display:flex;align-items:center;gap:6px;">
                        ${Utils.statusBadge(c.change_type || c.changeType)}
                        <strong style="font-family:monospace;font-size:12px;">#${Utils.escapeHtml(c.transaction_id || c.transactionId)}</strong>
                    </div>
                    <span class="list-item-sub">${Utils.escapeHtml(c.field_name || c.fieldName || 'Record')}: ${Utils.escapeHtml(c.old_value || c.oldValue || '—')} &rarr; ${Utils.escapeHtml(c.new_value || c.newValue || '—')}</span>
                </div>
                <div class="list-item-right">
                    <span class="text-muted" style="font-size:11px;">${Utils.formatDate(c.changed_at || c.changedAt)}</span>
                </div>
            </div>
        `).join('');
    },
};
