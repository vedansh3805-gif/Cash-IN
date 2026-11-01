/* ==========================================================================
   CashFlow — Enterprise Receivables & Credit Operations Module
   ========================================================================== */

const ReceivablesPage = {
    records: [],
    filteredRecords: [],
    currentSearch: '',
    currentStatus: 'ALL',

    init() {
        const searchInput = document.getElementById('recv-search-input');
        if (searchInput) {
            searchInput.oninput = (e) => this.setSearch(e.target.value);
        }

        const statusSelect = document.getElementById('recv-status-filter');
        if (statusSelect) {
            statusSelect.onchange = (e) => this.setStatusFilter(e.target.value);
        }
    },

    setSearch(val) {
        this.currentSearch = (val || '').toLowerCase().trim();
        this.applyFilters();
    },

    setStatusFilter(val) {
        this.currentStatus = val || 'ALL';
        const el = document.getElementById('recv-status-filter');
        if (el && el.value !== val) el.value = val;
        this.applyFilters();
    },

    async load() {
        try {
            const versionId = document.getElementById('filter-version')?.value || '';
            const data = await API.getReceivables(versionId);
            this.records = (data && (data.receivables || data)) || [];
            if (!Array.isArray(this.records)) this.records = [];

            this.renderSummary(data);
            this.applyFilters();
            this.renderAgingChart(data);
            this.renderDelayChart(data);
        } catch (error) {
            console.error('Receivables load error:', error);
            Utils.showToast('Failed to load receivables: ' + error.message, 'error');
        }
    },

    applyFilters() {
        let list = this.records.slice();

        // Status Filter: Overdue vs Pending
        if (this.currentStatus === 'OVERDUE') {
            list = list.filter(r => {
                const st = (r.status || '').toUpperCase();
                const days = r.daysOverdue != null ? Number(r.daysOverdue) : 0;
                return st === 'OVERDUE' || days > 0;
            });
        } else if (this.currentStatus === 'PENDING') {
            list = list.filter(r => {
                const st = (r.status || '').toUpperCase();
                const days = r.daysOverdue != null ? Number(r.daysOverdue) : 0;
                return st === 'PENDING' && days <= 0;
            });
        }

        // Search Filter: Customer, Invoice ID, Phone, Email
        if (this.currentSearch) {
            const q = this.currentSearch;
            list = list.filter(r => {
                const cust = (r.customer || r.customerName || '').toLowerCase();
                const txn = (r.transactionId || r.transaction_id || '').toLowerCase();
                const phone = (r.customerPhone || r.customer_phone || r.phone || '').toLowerCase();
                const email = (r.customerEmail || r.customer_email || r.email || '').toLowerCase();
                return cust.includes(q) || txn.includes(q) || phone.includes(q) || email.includes(q);
            });
        }

        this.filteredRecords = list;

        // Update count indicator if present
        const countBadge = document.getElementById('recv-count-badge');
        if (countBadge) {
            countBadge.textContent = `Showing ${list.length} of ${this.records.length} invoices`;
        }

        this.renderTable(list);
    },

    renderSummary(data) {
        document.getElementById('recv-total').textContent = Utils.formatCurrency(data.totalOutstanding || 0);
        document.getElementById('recv-overdue').textContent = Utils.formatCurrency(data.overdueAmount || 0);
        const avg = data.averagePaymentDelay != null ? Math.round(Number(data.averagePaymentDelay)) : 0;
        document.getElementById('recv-avg-delay').textContent = `${avg} days`;
    },

    renderTable(receivables) {
        const tbody = document.getElementById('receivables-tbody');
        if (!tbody) return;

        if (!receivables || receivables.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="8"><div class="empty-state">No matching receivables found for selected filter</div></td></tr>';
            return;
        }

        tbody.innerHTML = receivables.map((r, idx) => {
            const customer = r.customer || r.customerName || 'Valued Buyer';
            const phone = r.customerPhone || r.customer_phone || r.phone || '—';
            const email = r.customerEmail || r.customer_email || r.email || '—';
            const txnId = r.transactionId || r.transaction_id || '—';
            const amount = parseFloat(r.amount) || 0;
            const dueDate = r.dueDate || r.due_date || '';
            const status = (r.status || 'PENDING').toUpperCase();
            const daysOver = r.daysOverdue != null ? Number(r.daysOverdue) : (status === 'OVERDUE' ? 14 : 0);
            const isOverdue = status === 'OVERDUE' || daysOver > 0;

            const phoneClean = phone.replace(/[^0-9+]/g, '');

            return `
                <tr>
                    <td><strong style="font-family:monospace;font-size:12px;">#${Utils.escapeHtml(txnId)}</strong></td>
                    <td><strong>${Utils.escapeHtml(customer)}</strong></td>
                    <td>
                        ${phone !== '—' ? `
                            <div class="contact-pill phone-pill">
                                <span class="pill-icon">${Utils.icons.phone}</span>
                                <a href="tel:${Utils.escapeHtml(phoneClean)}" class="contact-link" title="Click to call customer">${Utils.escapeHtml(phone)}</a>
                            </div>
                        ` : '<span class="text-muted">—</span>'}
                    </td>
                    <td>
                        ${email !== '—' ? `
                            <div class="contact-pill email-pill">
                                <span class="pill-icon">${Utils.icons.mail}</span>
                                <a href="mailto:${Utils.escapeHtml(email)}" class="contact-link" title="Click to email customer">${Utils.escapeHtml(email)}</a>
                            </div>
                        ` : '<span class="text-muted">—</span>'}
                    </td>
                    <td class="amount-col font-bold">${Utils.formatCurrency(amount)}</td>
                    <td>
                        <div>${Utils.formatDate(dueDate)}</div>
                        ${isOverdue ? `<small class="text-danger font-bold">${daysOver}d overdue</small>` : '<small class="text-muted">On Schedule</small>'}
                    </td>
                    <td>${Utils.statusBadge(isOverdue ? 'OVERDUE' : status)}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-xs btn-outline-primary" title="Direct Phone Call" onclick="ReceivablesPage.openCallModal(${idx})">
                                ${Utils.icons.phone} Call
                            </button>
                            <button class="btn btn-xs btn-outline-warning" title="Dispatch SMS Reminder" onclick="ReceivablesPage.openSmsModal(${idx})">
                                ${Utils.icons.message} SMS
                            </button>
                            <button class="btn btn-xs btn-outline-info" title="Dispatch Email Statement" onclick="ReceivablesPage.openEmailModal(${idx})">
                                ${Utils.icons.mail} Email
                            </button>
                            <button class="btn btn-xs btn-primary" title="Simultaneous Dual-Party Notice" onclick="ReceivablesPage.notifyBothParties(${idx})">
                                ${Utils.icons.sync} Both
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    openCallModal(idx) {
        const r = this.filteredRecords[idx] || this.records[idx];
        if (!r) return;
        const customer = r.customer || r.customerName || 'Customer';
        const phone = r.customerPhone || r.customer_phone || r.phone || '';
        const txnId = r.transactionId || r.transaction_id || '';
        const amount = parseFloat(r.amount) || 0;
        const dueDate = r.dueDate || r.due_date || '';
        const status = (r.status || 'PENDING').toUpperCase();
        CommunicationPage.openCallModal(txnId, customer, phone, amount, dueDate, status);
    },

    openSmsModal(idx) {
        const r = this.filteredRecords[idx] || this.records[idx];
        if (!r) return;
        const customer = r.customer || r.customerName || 'Customer';
        const phone = r.customerPhone || r.customer_phone || r.phone || '';
        const txnId = r.transactionId || r.transaction_id || '';
        const amount = parseFloat(r.amount) || 0;
        const dueDate = r.dueDate || r.due_date || '';
        const status = (r.status || 'PENDING').toUpperCase();
        CommunicationPage.openSmsModal(txnId, customer, phone, amount, dueDate, status);
    },

    openEmailModal(idx) {
        const r = this.filteredRecords[idx] || this.records[idx];
        if (!r) return;
        const customer = r.customer || r.customerName || 'Customer';
        const email = r.customerEmail || r.customer_email || r.email || '';
        const txnId = r.transactionId || r.transaction_id || '';
        const amount = parseFloat(r.amount) || 0;
        const dueDate = r.dueDate || r.due_date || '';
        const status = (r.status || 'PENDING').toUpperCase();
        CommunicationPage.openEmailModal(txnId, customer, email, amount, dueDate, status);
    },

    notifyBothParties(idx) {
        const r = this.filteredRecords[idx] || this.records[idx];
        if (!r) return;
        const customer = r.customer || r.customerName || 'Customer';
        const txnId = r.transactionId || r.transaction_id || '';
        CommunicationPage.notifyBothParties(txnId, customer);
    },

    renderAgingChart(data) {
        const aging = (data && data.aging) || { current: 0, days30: 0, days60: 0, days90: 0, over90: 0 };

        Charts.create('chart-recv-aging', 'bar', {
            labels: ['Current', '1–30 Days', '31–60 Days', '61–90 Days', '90+ Days'],
            datasets: [{
                label: 'Aging Balance (₹)',
                data: [
                    aging.current || 0,
                    aging.days30 || 0,
                    aging.days60 || 0,
                    aging.days90 || 0,
                    aging.over90 || 0
                ],
                backgroundColor: [
                    '#10b981cc', // Current (Emerald)
                    '#38bdf8cc', // 1-30 (Sky Blue)
                    '#f59e0bcc', // 31-60 (Amber)
                    '#f97316cc', // 61-90 (Orange)
                    '#ef4444cc'  // 90+ (Red)
                ],
            }],
        });
    },

    renderDelayChart(data) {
        const customers = (data && data.customerDelays) || [];
        if (!Array.isArray(customers) || customers.length === 0) {
            Charts.create('chart-payment-delay', 'bar', { labels: [], datasets: [] });
            return;
        }

        Charts.create('chart-payment-delay', 'bar', {
            labels: customers.map(c => c.customerName || c.customer || 'Unknown'),
            datasets: [{
                label: 'Average Delay (days)',
                data: customers.map(c => Math.round(Number(c.averageDelay || c.averagePaymentDelay || 0))),
                backgroundColor: '#f59e0bcc',
            }],
        }, {
            indexAxis: 'y',
        });
    },
};
