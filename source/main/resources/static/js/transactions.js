/* ============================================
   CashFlow — Transactions Page Module
   ============================================ */

const TransactionsPage = {
    currentPage: 0,
    pageSize: 25,
    sortField: 'transaction_date',
    sortDir: 'desc',
    totalPages: 1,

    init() {
        // Sorting
        document.querySelectorAll('#transactions-table th.sortable').forEach(th => {
            th.addEventListener('click', () => {
                const field = th.dataset.sort;
                if (this.sortField === field) {
                    this.sortDir = this.sortDir === 'asc' ? 'desc' : 'asc';
                } else {
                    this.sortField = field;
                    this.sortDir = 'asc';
                }
                // Update UI
                document.querySelectorAll('#transactions-table th').forEach(h => {
                    h.classList.remove('sorted-asc', 'sorted-desc');
                });
                th.classList.add(this.sortDir === 'asc' ? 'sorted-asc' : 'sorted-desc');
                this.loadTransactions();
            });
        });
    },

    async load() {
        await this.loadTransactions();
    },

    onSearchInput(val) {
        if (this._debounceTimer) clearTimeout(this._debounceTimer);
        this._debounceTimer = setTimeout(() => {
            this.currentPage = 0;
            this.loadTransactions();
        }, 300);
    },

    onFilterChange() {
        this.currentPage = 0;
        this.loadTransactions();
    },

    prevPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            this.loadTransactions();
        }
    },

    nextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            this.loadTransactions();
        }
    },

    async loadTransactions() {
        try {
            const params = {
                page: this.currentPage,
                size: this.pageSize,
                sort: this.sortField,
                dir: this.sortDir,
            };

            const search = document.getElementById('txn-search').value.trim();
            if (search) params.search = search;

            const type = document.getElementById('txn-filter-type').value;
            if (type) params.type = type;

            const category = document.getElementById('txn-filter-category').value;
            if (category) params.category = category;

            const status = document.getElementById('txn-filter-status').value;
            if (status) params.status = status;

            // Global filters
            const version = document.getElementById('filter-version').value;
            if (version) params.versionId = version;

            const data = await API.getTransactions(params);

            this.renderTable(data.content || data.transactions || data || []);
            this.totalPages = data.totalPages || 1;
            this.updatePagination(data);
            this.updateCategories(data.categories || []);
        } catch (error) {
            console.error('Transactions load error:', error);
        }
    },

    renderTable(transactions) {
        const tbody = document.getElementById('transactions-tbody');
        if (!transactions || transactions.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="10"><div class="empty-state">No transactions found. Upload a CSV to get started.</div></td></tr>';
            return;
        }

        tbody.innerHTML = transactions.map(t => {
            const txnId = t.transactionId || t.transaction_id || '';
            const txnDate = t.transactionDate || t.transaction_date || t.date || '';
            const txnType = (t.transactionType || t.transaction_type || t.type || 'EXPENSE').toUpperCase();
            const category = t.category || 'General';
            const description = t.description || '—';
            const amount = parseFloat(t.amount) || 0;
            const counterparty = t.customer || t.supplier || '—';
            const dueDate = t.dueDate || t.due_date || '';
            const status = (t.status || 'PENDING').toUpperCase();
            const versionStr = t.versionNumber ? `V${t.versionNumber}` : (t.versionId || t.version_id ? 'V' + (t.versionId || t.version_id).substring(0, 4) : '—');

            return `
                <tr data-txn-id="${Utils.escapeHtml(txnId)}">
                    <td><strong>${Utils.formatDate(txnDate)}</strong></td>
                    <td>${Utils.statusBadge(txnType)}</td>
                    <td><span class="category-badge">${Utils.escapeHtml(category)}</span></td>
                    <td title="${Utils.escapeHtml(description)}">${Utils.truncate(description, 35)}</td>
                    <td class="amount-col font-bold" style="color:${txnType === 'INCOME' ? 'var(--success)' : 'var(--danger)'}">
                        ${txnType === 'INCOME' ? '+' : '-'}${Utils.formatCurrency(amount)}
                    </td>
                    <td>${Utils.escapeHtml(counterparty)}</td>
                    <td>${Utils.formatDate(dueDate)}</td>
                    <td>${Utils.statusBadge(status)}</td>
                    <td><span class="badge badge-info">${versionStr}</span></td>
                    <td><button class="expand-btn" onclick="TransactionsPage.showDetail('${Utils.escapeHtml(txnId)}')">Details</button></td>
                </tr>
            `;
        }).join('');
    },

    updatePagination(data) {
        const total = data.totalPages || 1;
        const page = data.number || this.currentPage;
        document.getElementById('txn-page-info').textContent = `Page ${page + 1} of ${total}`;
        document.getElementById('txn-prev').disabled = page <= 0;
        document.getElementById('txn-next').disabled = page >= total - 1;
    },

    updateCategories(categories) {
        const select = document.getElementById('txn-filter-category');
        const current = select.value;
        if (categories.length > 0 && select.options.length <= 1) {
            categories.forEach(cat => {
                const opt = document.createElement('option');
                opt.value = cat;
                opt.textContent = cat;
                select.appendChild(opt);
            });
            if (current) select.value = current;
        }
    },

    async showDetail(txnId) {
        try {
            const raw = await API.getTransactionDetail(txnId);
            const detail = raw.transaction || raw;
            const changes = raw.changes || raw.history || detail.changes || [];
            const modal = document.getElementById('txn-detail-modal');
            const body = document.getElementById('txn-detail-body');

            const id = detail.transaction_id || detail.transactionId || txnId;
            const date = detail.transaction_date || detail.transactionDate || detail.date;
            const type = (detail.transaction_type || detail.transactionType || detail.type || 'EXPENSE').toUpperCase();
            const cat = detail.category || 'General';
            const desc = detail.description || '—';
            const amt = parseFloat(detail.amount) || 0;
            const cust = detail.customer || '';
            const supp = detail.supplier || '';
            const phone = detail.customer_phone || detail.customerPhone || '';
            const email = detail.customer_email || detail.customerEmail || '';
            const due = detail.due_date || detail.dueDate;
            const paid = detail.payment_date || detail.paymentDate;
            const status = (detail.status || 'PENDING').toUpperCase();
            const cleanPhone = phone.replace(/[^0-9+]/g, '');

            body.innerHTML = `
                <div class="txn-detail-content">
                    <h3>Transaction: #${Utils.escapeHtml(id)}</h3>
                    <div class="report-section">
                        <h4>Current Record Attributes</h4>
                        <div class="report-row"><span class="label">Date</span><span class="value">${Utils.formatDate(date)}</span></div>
                        <div class="report-row"><span class="label">Type</span><span class="value">${Utils.statusBadge(type)}</span></div>
                        <div class="report-row"><span class="label">Category</span><span class="value font-bold">${Utils.escapeHtml(cat)}</span></div>
                        <div class="report-row"><span class="label">Description</span><span class="value">${Utils.escapeHtml(desc)}</span></div>
                        <div class="report-row"><span class="label">Amount</span><span class="value font-bold ${type === 'INCOME' ? 'text-success' : 'text-danger'}">${type === 'INCOME' ? '+' : '-'}${Utils.formatCurrency(amt)}</span></div>
                        ${cust ? `<div class="report-row"><span class="label">Customer / Debtor</span><span class="value font-bold">${Utils.escapeHtml(cust)}</span></div>` : ''}
                        ${supp ? `<div class="report-row"><span class="label">Vendor / Supplier</span><span class="value font-bold">${Utils.escapeHtml(supp)}</span></div>` : ''}
                        ${phone ? `<div class="report-row"><span class="label">Customer Mobile</span><span class="value"><a href="tel:${Utils.escapeHtml(cleanPhone)}" class="contact-link">${Utils.icons.phone} ${Utils.escapeHtml(phone)}</a></span></div>` : ''}
                        ${email ? `<div class="report-row"><span class="label">Billing Email</span><span class="value"><a href="mailto:${Utils.escapeHtml(email)}" class="contact-link">${Utils.icons.mail} ${Utils.escapeHtml(email)}</a></span></div>` : ''}
                        <div class="report-row"><span class="label">Due Date</span><span class="value">${Utils.formatDate(due)}</span></div>
                        <div class="report-row"><span class="label">Settlement Date</span><span class="value">${paid ? Utils.formatDate(paid) : '<span class="text-muted">Unsettled</span>'}</span></div>
                        <div class="report-row"><span class="label">Status</span><span class="value">${Utils.statusBadge(status)}</span></div>
                    </div>
                    ${changes && changes.length > 0 ? `
                        <div class="report-section">
                            <h4>Ledger Audit Trail &amp; Modifications (${changes.length})</h4>
                            ${changes.map(c => `
                                <div class="report-row">
                                    <span class="label">${Utils.escapeHtml(c.field_name || c.fieldName || 'Field')} (${Utils.escapeHtml(c.change_type || c.changeType || 'UPDATE')})</span>
                                    <span class="value">${Utils.escapeHtml(c.old_value || c.oldValue || '—')} &rarr; <strong>${Utils.escapeHtml(c.new_value || c.newValue || '—')}</strong></span>
                                </div>
                            `).join('')}
                        </div>
                    ` : ''}
                </div>
            `;

            modal.classList.add('active');
        } catch (error) {
            console.error('Show detail error:', error);
            Utils.showToast('Failed to load transaction details: ' + error.message, 'error');
        }
    },
};
