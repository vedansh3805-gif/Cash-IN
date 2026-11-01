/* ==========================================================================
   CashFlow — Enterprise What-If Scenario Stress Testing Module
   ========================================================================== */

const WhatIfPage = {
    init() {
        const addBtn = document.getElementById('add-adjustment-btn');
        if (addBtn) {
            addBtn.onclick = (e) => {
                e.preventDefault();
                this.addAdjustmentRow();
            };
        }

        const resetBtn = document.getElementById('scenario-reset-btn');
        if (resetBtn) {
            resetBtn.onclick = (e) => {
                e.preventDefault();
                this.resetForm();
            };
        }
    },

    addAdjustmentRow(type = 'DELAY_PAYMENT', desc = '', amount = '', date = '') {
        const container = document.getElementById('adjustments-container');
        if (!container) return;
        const row = document.createElement('div');
        row.className = 'adjustment-row';
        row.innerHTML = `
            <select class="filter-select adj-type">
                <option value="DELAY_PAYMENT" ${type === 'DELAY_PAYMENT' ? 'selected' : ''}>Delay Customer Receivables</option>
                <option value="INCREASE_EXPENSE" ${type === 'INCREASE_EXPENSE' ? 'selected' : ''}>Increase Operating Expenses</option>
                <option value="DECREASE_INCOME" ${type === 'DECREASE_INCOME' ? 'selected' : ''}>Decrease Revenue Inflows</option>
                <option value="ADD_EXPENSE" ${type === 'ADD_EXPENSE' ? 'selected' : ''}>Inject Emergency Expense Shock</option>
            </select>
            <input type="text" class="form-input adj-desc" placeholder="Shock Label" value="${Utils.escapeHtml(desc)}">
            <input type="number" class="form-input adj-amount" placeholder="Amount (₹)" value="${amount}">
            <input type="date" class="form-input adj-date" value="${date}">
            <button type="button" class="btn btn-sm btn-outline-danger" onclick="this.parentElement.remove()" title="Remove Adjustment">✕</button>
        `;
        container.appendChild(row);
    },

    loadPreset(presetType) {
        const nameInput = document.getElementById('scenario-name');
        const descInput = document.getElementById('scenario-desc');
        const container = document.getElementById('adjustments-container');
        if (!nameInput || !descInput || !container) return;

        container.innerHTML = '';

        if (presetType === 'DEBTOR_DELAY') {
            nameInput.value = '30-Day Major Debtor Payment Stall';
            descInput.value = 'Simulate liquidity strain when key enterprise clients delay remittance by 30 days';
            this.addAdjustmentRow('DELAY_PAYMENT', 'Stalled Client Invoices', '60000', '');
        } else if (presetType === 'EMERGENCY_SHOCK') {
            nameInput.value = 'Emergency Equipment Capex Outflow';
            descInput.value = 'Immediate unexpected machinery repair and software infrastructure overhaul';
            this.addAdjustmentRow('ADD_EXPENSE', 'Emergency Facility Overhaul', '75000', '');
        } else if (presetType === 'REVENUE_DIP') {
            nameInput.value = '25% Macroeconomic Demand Contraction';
            descInput.value = 'Simulating an unexpected drop in Q3 customer billings and contracts';
            this.addAdjustmentRow('DECREASE_INCOME', 'Sales Inflow Drop', '50000', '');
        } else if (presetType === 'INFLATION_HIKE') {
            nameInput.value = 'Supply Chain & Operating Cost Surge';
            descInput.value = 'Raw material cost inflation and energy utility price hike';
            this.addAdjustmentRow('INCREASE_EXPENSE', 'Supplier Rate Hike', '40000', '');
        }

        Utils.showToast(`Loaded preset scenario: ${nameInput.value}`, 'info');
        this.runScenario();
    },

    resetForm() {
        const form = document.getElementById('scenario-form');
        if (form) form.reset();
        const container = document.getElementById('adjustments-container');
        if (container) {
            container.innerHTML = '';
            this.addAdjustmentRow('DELAY_PAYMENT', 'Key Client Delay', '30000', '');
        }
        const resultContainer = document.getElementById('scenario-result');
        if (resultContainer) {
            resultContainer.innerHTML = '<div class="empty-state">Select a preset or configure parameters and run simulation</div>';
        }
        const chartCard = document.getElementById('scenario-chart-card');
        if (chartCard) chartCard.style.display = 'none';
        Utils.showToast('Scenario parameters reset to defaults', 'info');
    },

    async runScenario(e) {
        if (e && e.preventDefault) e.preventDefault();

        const name = document.getElementById('scenario-name')?.value.trim();
        const desc = document.getElementById('scenario-desc')?.value.trim() || '';

        if (!name) {
            Utils.showToast('Please enter a scenario title', 'warning');
            return;
        }

        const adjustments = [];
        document.querySelectorAll('.adjustment-row').forEach(row => {
            const type = row.querySelector('.adj-type').value;
            const description = row.querySelector('.adj-desc').value;
            const amount = parseFloat(row.querySelector('.adj-amount').value) || 0;
            const targetDate = row.querySelector('.adj-date').value;
            if (amount !== 0) {
                adjustments.push({ adjustmentType: type, description, amount, targetDate });
            }
        });

        if (adjustments.length === 0) {
            Utils.showToast('Please specify at least one adjustment amount', 'warning');
            return;
        }

        const versionId = document.getElementById('filter-version')?.value || '';

        try {
            const scenario = await API.createScenario({ name, description: desc, adjustments, versionId });
            const scenarioId = scenario.scenarioId || scenario.id;
            const forecast = await API.getScenarioForecast(scenarioId);
            this.renderResult(forecast);
            Utils.showToast('Stress simulation generated successfully!', 'success');
        } catch (error) {
            console.error('Scenario error:', error);
            Utils.showToast('Failed to run simulation: ' + error.message, 'error');
        }
    },

    renderResult(forecast) {
        const container = document.getElementById('scenario-result');
        const chartCard = document.getElementById('scenario-chart-card');
        if (!container) return;

        if (!forecast) {
            container.innerHTML = '<div class="empty-state">No scenario results available</div>';
            if (chartCard) chartCard.style.display = 'none';
            return;
        }

        const baseline = forecast.baseline || [];
        const scenario = forecast.scenario || [];

        const baseMin = baseline.length > 0 ? Math.min(...baseline.map(e => e.closingCash || e.closing_cash || 0)) : 0;
        const scenMin = scenario.length > 0 ? Math.min(...scenario.map(e => e.closingCash || e.closing_cash || 0)) : 0;
        const baseEnd = baseline.length > 0 ? Number(baseline[baseline.length - 1].closingCash || baseline[baseline.length - 1].closing_cash || 0) : 0;
        const scenEnd = scenario.length > 0 ? Number(scenario[scenario.length - 1].closingCash || scenario[scenario.length - 1].closing_cash || 0) : 0;
        const delta = scenEnd - baseEnd;

        container.innerHTML = `
            <div class="scenario-comparison">
                <div class="scenario-col">
                    <h4>Baseline Liquidity</h4>
                    <div class="report-row"><span class="label">Minimum Cash Floor</span><span class="value font-bold">${Utils.formatCurrency(baseMin)}</span></div>
                    <div class="report-row"><span class="label">Closing Balance</span><span class="value font-bold">${Utils.formatCurrency(baseEnd)}</span></div>
                </div>
                <div class="scenario-col">
                    <h4>Stressed Scenario</h4>
                    <div class="report-row"><span class="label">Minimum Cash Floor</span><span class="value font-bold ${scenMin < baseMin ? 'text-danger' : 'text-success'}">${Utils.formatCurrency(scenMin)}</span></div>
                    <div class="report-row"><span class="label">Closing Balance</span><span class="value font-bold ${scenEnd < baseEnd ? 'text-danger' : 'text-success'}">${Utils.formatCurrency(scenEnd)}</span></div>
                    <div class="report-row"><span class="label">Net Impact Delta</span><span class="value font-bold ${delta < 0 ? 'text-danger' : 'text-success'}">${delta >= 0 ? '+' : ''}${Utils.formatCurrency(delta)}</span></div>
                </div>
            </div>
        `;

        if (baseline.length > 0 || scenario.length > 0) {
            if (chartCard) chartCard.style.display = 'block';
            const allEntries = baseline.length > 0 ? baseline : scenario;
            const labels = allEntries.map(e => Utils.formatDateShort(e.forecastDate || e.forecast_date || e.date));

            Charts.create('chart-scenario', 'line', {
                labels,
                datasets: [
                    Charts.lineDataset('Baseline Forecast', baseline.map(e => Number(e.closingCash || e.closing_cash || 0)), 'chart1'),
                    Charts.lineDataset('Stressed Simulation', scenario.map(e => Number(e.closingCash || e.closing_cash || 0)), 'danger'),
                ],
            });
        }
    },

    async load() {
        // Initial setup
    },
};
