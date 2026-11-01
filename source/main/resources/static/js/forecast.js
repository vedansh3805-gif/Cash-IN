/* ==========================================================================
   CashFlow — Enterprise Cash Forecast Page Module
   ========================================================================== */

const ForecastPage = {
    currentDays: 30,

    init() {
        // Buttons handled cleanly
    },

    setPeriod(days) {
        this.currentDays = parseInt(days) || 30;
        document.querySelectorAll('.forecast-period').forEach(b => {
            const d = parseInt(b.dataset.days || b.getAttribute('data-days'));
            b.classList.toggle('active', d === this.currentDays);
        });
        const horizonEl = document.getElementById('forecast-summary-horizon');
        if (horizonEl) horizonEl.textContent = `${this.currentDays} Days`;
        this.load();
    },

    async load() {
        try {
            const versionId = document.getElementById('filter-version')?.value || '';
            const data = await API.getForecast(this.currentDays, versionId);
            const entries = data.entries || (Array.isArray(data) ? data : []);
            const threshold = data.threshold || 50000;

            this.renderSummaryMetrics(entries, threshold);
            this.renderChart(entries, threshold);
            this.renderTable(entries);
        } catch (error) {
            console.error('Forecast load error:', error);
            Utils.showToast('Failed to load forecast: ' + error.message, 'error');
        }
    },

    renderSummaryMetrics(entries, threshold) {
        const horizonEl = document.getElementById('forecast-summary-horizon');
        if (horizonEl) horizonEl.textContent = `${this.currentDays} Days`;

        const minEl = document.getElementById('forecast-summary-min');
        const closingEl = document.getElementById('forecast-summary-closing');
        const bufferEl = document.getElementById('forecast-summary-buffer');

        if (!entries || entries.length === 0) {
            if (minEl) { minEl.textContent = Utils.formatCurrency(0); minEl.className = 'kpi-value text-muted'; }
            if (closingEl) { closingEl.textContent = Utils.formatCurrency(0); }
            if (bufferEl) { bufferEl.textContent = Utils.formatCurrency(0); bufferEl.className = 'kpi-value text-muted'; }
            return;
        }

        let minVal = Infinity;
        let maxVal = -Infinity;
        entries.forEach(e => {
            const c = Number(e.closingCash || e.closing_cash || 0);
            if (c < minVal) minVal = c;
            if (c > maxVal) maxVal = c;
        });

        const closingFinal = Number(entries[entries.length - 1].closingCash || entries[entries.length - 1].closing_cash || 0);
        const buffer = minVal - threshold;

        if (minEl) {
            minEl.textContent = Utils.formatCurrency(minVal);
            minEl.className = 'kpi-value ' + (minVal < threshold ? 'text-danger' : 'text-success');
        }

        if (closingEl) closingEl.textContent = Utils.formatCurrency(closingFinal);

        if (bufferEl) {
            bufferEl.textContent = (buffer >= 0 ? '+' : '') + Utils.formatCurrency(buffer);
            bufferEl.className = 'kpi-value ' + (buffer < 0 ? 'text-danger' : 'text-info');
        }
    },

    renderChart(entries, threshold) {
        if (!entries || entries.length === 0) {
            Charts.create('chart-forecast', 'line', { labels: [], datasets: [] });
            return;
        }

        const labels = entries.map(e => Utils.formatDateShort(e.forecastDate || e.forecast_date || e.date));
        const closingCash = entries.map(e => Number(e.closingCash || e.closing_cash || 0));

        Charts.create('chart-forecast', 'line', {
            labels,
            datasets: [
                Charts.lineDataset('Projected Closing Cash', closingCash, 'chart1'),
                {
                    label: 'Safe Cash Threshold',
                    data: Array(entries.length).fill(threshold),
                    borderColor: '#ef4444',
                    borderWidth: 2,
                    borderDash: [6, 4],
                    fill: false,
                    pointRadius: 0,
                },
            ],
        });
    },

    renderTable(entries) {
        const tbody = document.getElementById('forecast-tbody');
        if (!tbody) return;

        if (!entries || entries.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="5"><div class="empty-state">No forecast entries available</div></td></tr>';
            return;
        }

        tbody.innerHTML = entries.map(e => {
            const closing = Number(e.closingCash || e.closing_cash || 0);
            const opening = Number(e.openingCash || e.opening_cash || 0);
            const inflow = Number(e.expectedInflow || e.expected_inflow || 0);
            const outflow = Number(e.expectedOutflow || e.expected_outflow || 0);
            const dateStr = e.forecastDate || e.forecast_date || e.date;

            const isNegative = closing < 0;
            const closingClass = isNegative ? 'text-danger font-bold' : 'font-bold';

            return `
                <tr>
                    <td><strong>${Utils.formatDate(dateStr)}</strong></td>
                    <td class="amount-col">${Utils.formatCurrency(opening)}</td>
                    <td class="amount-col text-success">${inflow > 0 ? '+' + Utils.formatCurrency(inflow) : '—'}</td>
                    <td class="amount-col text-danger">${outflow > 0 ? '-' + Utils.formatCurrency(outflow) : '—'}</td>
                    <td class="amount-col ${closingClass}">${Utils.formatCurrency(closing)}</td>
                </tr>
            `;
        }).join('');
    },
};
