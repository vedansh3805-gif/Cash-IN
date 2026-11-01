/* ==========================================================================
   CashFlow — Enterprise Multi-Channel Customer Communication Hub
   ========================================================================== */

const CommunicationPage = {
    customers: [],
    filteredCustomers: [],
    logs: [],
    filteredLogs: [],
    activeTab: 'directory',
    currentSearch: '',
    currentStatusFilter: 'ALL',
    currentChannelFilter: 'ALL',

    init() {
        // Tab switching
        document.querySelectorAll('.comm-tab-btn').forEach(btn => {
            btn.onclick = (e) => {
                e.preventDefault();
                const target = btn.dataset.tab;
                this.switchTab(target);
            };
        });

        // Search & Filter
        const searchInput = document.getElementById('comm-search-input');
        if (searchInput) {
            searchInput.oninput = (e) => this.setSearch(e.target.value);
        }

        const statusSelect = document.getElementById('comm-status-filter');
        if (statusSelect) {
            statusSelect.onchange = (e) => this.setStatusFilter(e.target.value);
        }

        const channelSelect = document.getElementById('comm-channel-filter');
        if (channelSelect) {
            channelSelect.onchange = (e) => this.setChannelFilter(e.target.value);
        }

        // Modal triggers
        this.bindModalEvents();
    },

    setSearch(val) {
        this.currentSearch = (val || '').toLowerCase().trim();
        this.applyFilters();
    },

    setStatusFilter(val) {
        this.currentStatusFilter = val || 'ALL';
        const el = document.getElementById('comm-status-filter');
        if (el && el.value !== val) el.value = val;
        this.applyFilters();
    },

    setChannelFilter(val) {
        this.currentChannelFilter = val || 'ALL';
        const el = document.getElementById('comm-channel-filter');
        if (el && el.value !== val) el.value = val;
        this.applyFilters();
    },

    bindModalEvents() {
        // Call Modal
        const callClose = document.getElementById('comm-call-modal-close');
        if (callClose) callClose.onclick = () => this.closeCallModal();
        const callCancel = document.getElementById('comm-call-cancel-btn');
        if (callCancel) callCancel.onclick = () => this.closeCallModal();
        const callForm = document.getElementById('comm-call-form');
        if (callForm) callForm.onsubmit = (e) => this.handleCallSubmit(e);

        // SMS Modal
        const smsClose = document.getElementById('comm-sms-modal-close');
        if (smsClose) smsClose.onclick = () => this.closeSmsModal();
        const smsCancel = document.getElementById('comm-sms-cancel-btn');
        if (smsCancel) smsCancel.onclick = () => this.closeSmsModal();
        const smsForm = document.getElementById('comm-sms-form');
        if (smsForm) smsForm.onsubmit = (e) => this.handleSmsSubmit(e);
        const smsText = document.getElementById('sms-modal-body');
        if (smsText) {
            smsText.oninput = (e) => {
                const charCount = document.getElementById('sms-modal-char-count');
                if (charCount) charCount.textContent = e.target.value.length;
            };
        }

        // Email Modal
        const emailClose = document.getElementById('comm-email-modal-close');
        if (emailClose) emailClose.onclick = () => this.closeEmailModal();
        const emailCancel = document.getElementById('comm-email-cancel-btn');
        if (emailCancel) emailCancel.onclick = () => this.closeEmailModal();
        const emailForm = document.getElementById('comm-email-form');
        if (emailForm) emailForm.onsubmit = (e) => this.handleEmailSubmit(e);
    },

    async load() {
        try {
            const versionId = document.getElementById('filter-version')?.value || '';
            const [receivablesData, logsData] = await Promise.all([
                API.getReceivables(versionId),
                API.getCommunicationLogs(),
            ]);

            this.logs = Array.isArray(logsData) ? logsData : [];
            this.processCustomerDirectory(receivablesData);
            this.renderKPIs(receivablesData);
            this.applyFilters();
        } catch (error) {
            console.error('Communication hub load error:', error);
            Utils.showToast('Failed to load communication hub: ' + error.message, 'error');
        }
    },

    processCustomerDirectory(data) {
        const rawRecv = (data && (data.receivables || data)) || [];
        const customerMap = {};

        if (Array.isArray(rawRecv)) {
            rawRecv.forEach(r => {
                const name = r.customer || r.customerName || 'Valued Buyer';
                const phone = r.customerPhone || r.customer_phone || r.phone || '';
                const email = r.customerEmail || r.customer_email || r.email || '';
                const amount = parseFloat(r.amount) || 0;
                const status = (r.status || 'PENDING').toUpperCase();
                const txnId = r.transactionId || r.transaction_id || '';
                const dueDate = r.dueDate || r.due_date || '';

                if (!customerMap[name]) {
                    customerMap[name] = {
                        name,
                        phone: phone,
                        email: email,
                        totalOutstanding: 0,
                        overdueAmount: 0,
                        invoices: [],
                        status: 'PENDING',
                        daysOverdue: 0,
                    };
                }

                if (phone && (!customerMap[name].phone || customerMap[name].phone === '—')) {
                    customerMap[name].phone = phone;
                }
                if (email && (!customerMap[name].email || customerMap[name].email === '—')) {
                    customerMap[name].email = email;
                }

                customerMap[name].totalOutstanding += amount;
                if (status === 'OVERDUE') {
                    customerMap[name].overdueAmount += amount;
                    customerMap[name].status = 'OVERDUE';
                }

                customerMap[name].invoices.push({
                    txnId,
                    amount,
                    dueDate,
                    status,
                });
            });
        }

        // Cross-reference customer delays table
        const customerDelays = (data && data.customerDelays) || [];
        if (Array.isArray(customerDelays)) {
            customerDelays.forEach(cd => {
                const cname = cd.customerName || cd.customer;
                if (cname) {
                    if (!customerMap[cname]) {
                        customerMap[cname] = {
                            name: cname,
                            phone: cd.phone || '',
                            email: cd.email || '',
                            totalOutstanding: parseFloat(cd.totalOutstanding) || 0,
                            overdueAmount: (parseFloat(cd.totalOutstanding) || 0) > 0 ? parseFloat(cd.totalOutstanding) : 0,
                            invoices: [],
                            status: (parseFloat(cd.totalOutstanding) || 0) > 0 ? 'OVERDUE' : 'PENDING',
                            daysOverdue: Math.round(cd.averageDelay || 0),
                        };
                    } else {
                        if (cd.phone && !customerMap[cname].phone) customerMap[cname].phone = cd.phone;
                        if (cd.email && !customerMap[cname].email) customerMap[cname].email = cd.email;
                    }
                }
            });
        }

        this.customers = Object.values(customerMap);
    },

    renderKPIs(data) {
        const totalOut = (data && data.totalOutstanding) || 0;
        const overdue = (data && data.overdueAmount) || 0;
        const overdueCount = this.customers.filter(c => c.status === 'OVERDUE').length;

        let calls = 0, sms = 0, emails = 0;
        this.logs.forEach(l => {
            const ch = (l.channel || '').toUpperCase();
            if (ch.includes('CALL')) calls++;
            if (ch.includes('SMS')) sms++;
            if (ch.includes('EMAIL')) emails++;
            if (ch.includes('BOTH')) { sms++; emails++; }
        });

        const setVal = (id, val) => {
            const el = document.getElementById(id);
            if (el) el.textContent = val;
        };

        setVal('comm-kpi-defaulters', overdueCount);
        setVal('comm-kpi-total-overdue', Utils.formatCurrency(overdue));
        setVal('comm-kpi-active-debtors', this.customers.length);
        setVal('comm-kpi-calls', calls);
        setVal('comm-kpi-sms', sms);
        setVal('comm-kpi-emails', emails);
    },

    switchTab(tabName) {
        this.activeTab = tabName;
        document.querySelectorAll('.comm-tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.tab === tabName);
        });

        const dirPanel = document.getElementById('comm-panel-directory');
        const logsPanel = document.getElementById('comm-panel-logs');

        if (tabName === 'directory') {
            if (dirPanel) dirPanel.style.display = 'block';
            if (logsPanel) logsPanel.style.display = 'none';
        } else {
            if (dirPanel) dirPanel.style.display = 'none';
            if (logsPanel) logsPanel.style.display = 'block';
        }

        this.applyFilters();
    },

    applyFilters() {
        if (this.activeTab === 'directory') {
            this.filterAndRenderDirectory();
        } else {
            this.filterAndRenderLogs();
        }
    },

    filterAndRenderDirectory() {
        const tbody = document.getElementById('comm-directory-tbody');
        if (!tbody) return;

        let list = this.customers.slice();

        // Status Filter
        if (this.currentStatusFilter === 'OVERDUE') {
            list = list.filter(c => c.status === 'OVERDUE' || c.overdueAmount > 0);
        } else if (this.currentStatusFilter === 'PENDING') {
            list = list.filter(c => c.status === 'PENDING' && c.overdueAmount === 0);
        }

        // Search Filter (customer name, phone, email, OR invoice ID)
        if (this.currentSearch) {
            const q = this.currentSearch;
            list = list.filter(c => {
                const nameMatch = c.name.toLowerCase().includes(q);
                const phoneMatch = c.phone && c.phone.toLowerCase().includes(q);
                const emailMatch = c.email && c.email.toLowerCase().includes(q);
                const invoiceMatch = c.invoices.some(inv => (inv.txnId || '').toLowerCase().includes(q));
                return nameMatch || phoneMatch || emailMatch || invoiceMatch;
            });
        }

        this.filteredCustomers = list;

        if (list.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="7"><div class="empty-state">No matching customer accounts found</div></td></tr>';
            return;
        }

        tbody.innerHTML = list.map((c, idx) => {
            const phone = c.phone || '—';
            const email = c.email || '—';
            const cleanPhone = phone.replace(/[^0-9+]/g, '');

            return `
                <tr>
                    <td>
                        <div style="font-weight:600;font-size:13px;">${Utils.escapeHtml(c.name)}</div>
                        <small class="text-muted">${c.invoices.length} active invoice(s)</small>
                    </td>
                    <td>
                        ${phone !== '—' ? `
                            <div class="contact-pill phone-pill">
                                <span class="pill-icon">${Utils.icons.phone}</span>
                                <a href="tel:${Utils.escapeHtml(cleanPhone)}" class="contact-link" title="Click to call customer">${Utils.escapeHtml(phone)}</a>
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
                    <td class="amount-col font-bold">${Utils.formatCurrency(c.totalOutstanding)}</td>
                    <td class="amount-col font-bold ${c.overdueAmount > 0 ? 'text-danger' : 'text-muted'}">${c.overdueAmount > 0 ? Utils.formatCurrency(c.overdueAmount) : '₹0'}</td>
                    <td>${Utils.statusBadge(c.status)}</td>
                    <td>
                        <div class="table-actions">
                            <button class="btn btn-xs btn-outline-primary" title="Direct Phone Call" onclick="CommunicationPage.openCallModalByIndex(${idx})">
                                ${Utils.icons.phone} Call
                            </button>
                            <button class="btn btn-xs btn-outline-warning" title="Dispatch SMS Reminder" onclick="CommunicationPage.openSmsModalByIndex(${idx})">
                                ${Utils.icons.message} SMS
                            </button>
                            <button class="btn btn-xs btn-outline-info" title="Dispatch Email Statement" onclick="CommunicationPage.openEmailModalByIndex(${idx})">
                                ${Utils.icons.mail} Email
                            </button>
                            <button class="btn btn-xs btn-primary" title="Simultaneous Dual-Party Notice" onclick="CommunicationPage.notifyBothPartiesByIndex(${idx})">
                                ${Utils.icons.sync} Both
                            </button>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    },

    filterAndRenderLogs() {
        const tbody = document.getElementById('comm-logs-tbody');
        if (!tbody) return;

        let list = this.logs.slice().reverse();

        // Channel filter
        if (this.currentChannelFilter !== 'ALL') {
            list = list.filter(l => (l.channel || '').toUpperCase().includes(this.currentChannelFilter));
        }

        // Status filter
        if (this.currentStatusFilter === 'OVERDUE') {
            list = list.filter(l => (l.message_body || l.message || '').toUpperCase().includes('OVERDUE'));
        }

        // Search filter
        if (this.currentSearch) {
            const q = this.currentSearch;
            list = list.filter(l => {
                const cust = (l.customer_name || l.customerName || '').toLowerCase();
                const contact = (l.recipient_contact || l.recipientContact || '').toLowerCase();
                const msg = (l.message_body || l.message || '').toLowerCase();
                const txn = (l.transaction_id || l.transactionId || '').toLowerCase();
                return cust.includes(q) || contact.includes(q) || msg.includes(q) || txn.includes(q);
            });
        }

        this.filteredLogs = list;

        if (list.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="6"><div class="empty-state">No matching communication log entries found</div></td></tr>';
            return;
        }

        // Cap to latest 100 entries for super fast rendering
        const displayList = list.slice(0, 100);

        tbody.innerHTML = displayList.map(l => {
            const ch = (l.channel || 'CALL').toUpperCase();
            let channelBadge = '<span class="badge badge-primary">PHONE CALL</span>';
            if (ch === 'SMS') channelBadge = '<span class="badge badge-warning">SMS NOTICE</span>';
            else if (ch === 'EMAIL') channelBadge = '<span class="badge badge-info">EMAIL STATEMENT</span>';
            else if (ch === 'BOTH') channelBadge = '<span class="badge badge-success">DUAL-PARTY SYNC</span>';

            const timeStr = l.sent_at || l.sentAt || l.timestamp;
            const txnStr = l.transaction_id || l.transactionId ? ` <span class="badge badge-outline">#${Utils.escapeHtml(l.transaction_id || l.transactionId)}</span>` : '';

            return `
                <tr>
                    <td>${Utils.formatDate(timeStr)}</td>
                    <td><strong>${Utils.escapeHtml(l.customer_name || l.customerName || 'Customer')}</strong>${txnStr}</td>
                    <td><span class="text-muted font-mono" style="font-size:12px;">${Utils.escapeHtml(l.recipient_contact || l.recipientContact || '—')}</span></td>
                    <td>${channelBadge}</td>
                    <td>${Utils.statusBadge(l.delivery_status || l.status || 'SENT')}</td>
                    <td><small style="color:var(--text-secondary);display:block;max-width:380px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;" title="${Utils.escapeHtml(l.message_body || l.message || '')}">${Utils.escapeHtml(l.message_body || l.message || '—')}</small></td>
                </tr>
            `;
        }).join('');
    },

    /* ---- Index-Based Modal Helpers (100% immune to quote escaping bugs) ---- */
    getCustomerByIndex(idx) {
        return this.filteredCustomers[idx] || this.customers[idx] || null;
    },

    openCallModalByIndex(idx) {
        const c = this.getCustomerByIndex(idx);
        if (!c) return;
        const firstTxn = c.invoices[0] || {};
        this.openCallModal(firstTxn.txnId || '', c.name, c.phone, c.totalOutstanding, firstTxn.dueDate || '', c.status);
    },

    openSmsModalByIndex(idx) {
        const c = this.getCustomerByIndex(idx);
        if (!c) return;
        const firstTxn = c.invoices[0] || {};
        this.openSmsModal(firstTxn.txnId || '', c.name, c.phone, c.totalOutstanding, firstTxn.dueDate || '', c.status);
    },

    openEmailModalByIndex(idx) {
        const c = this.getCustomerByIndex(idx);
        if (!c) return;
        const firstTxn = c.invoices[0] || {};
        this.openEmailModal(firstTxn.txnId || '', c.name, c.email, c.totalOutstanding, firstTxn.dueDate || '', c.status);
    },

    notifyBothPartiesByIndex(idx) {
        const c = this.getCustomerByIndex(idx);
        if (!c) return;
        const firstTxn = c.invoices[0] || {};
        this.notifyBothParties(firstTxn.txnId || '', c.name);
    },

    /* ---- Modals: Call, SMS, Email, Both ---- */
    openCallModal(txnId, customer, phone, amount, dueDate, status) {
        document.getElementById('call-modal-txnid').value = txnId;
        document.getElementById('call-modal-customer').value = customer;
        document.getElementById('call-modal-phone').value = phone;
        document.getElementById('call-modal-amount').value = amount;
        document.getElementById('call-modal-display-customer').textContent = customer;
        document.getElementById('call-modal-display-phone').textContent = phone || '—';
        document.getElementById('call-modal-display-amount').textContent = Utils.formatCurrency(amount);

        const cleanPhone = (phone || '').replace(/[^0-9+]/g, '');
        const telLink = document.getElementById('call-modal-tel-link');
        if (telLink) telLink.href = cleanPhone ? `tel:${cleanPhone}` : '#';

        document.getElementById('comm-call-modal').classList.add('active');
    },

    closeCallModal() {
        document.getElementById('comm-call-modal').classList.remove('active');
    },

    async handleCallSubmit(e) {
        e.preventDefault();
        const txnId = document.getElementById('call-modal-txnid').value;
        const notes = document.getElementById('call-modal-notes').value.trim();

        try {
            await API.initiateCall(txnId, notes);
            Utils.showToast('Voice call logged in audit trail', 'success');
            this.closeCallModal();
            this.load();
        } catch (error) {
            Utils.showToast('Failed to record call: ' + error.message, 'error');
        }
    },

    openSmsModal(txnId, customer, phone, amount, dueDate, status) {
        document.getElementById('sms-modal-txnid').value = txnId;
        document.getElementById('sms-modal-customer').value = customer;
        document.getElementById('sms-modal-phone').value = phone;
        document.getElementById('sms-modal-amount').value = amount;

        const defaultMsg = `Dear ${customer}, your payment of ${Utils.formatCurrency(amount)} (Invoice #${txnId}) is pending. Please clear immediately via secure link: https://pay.cashflow.in/${txnId}`;
        const textarea = document.getElementById('sms-modal-body');
        textarea.value = defaultMsg;

        const charCount = document.getElementById('sms-modal-char-count');
        if (charCount) charCount.textContent = defaultMsg.length;

        document.getElementById('comm-sms-modal').classList.add('active');
    },

    closeSmsModal() {
        document.getElementById('comm-sms-modal').classList.remove('active');
    },

    async handleSmsSubmit(e) {
        e.preventDefault();
        const txnId = document.getElementById('sms-modal-txnid').value;
        const msg = document.getElementById('sms-modal-body').value.trim();

        try {
            await API.sendSms(txnId, msg);
            Utils.showToast('SMS payment notice dispatched and verified', 'success');
            this.closeSmsModal();
            this.load();
        } catch (error) {
            Utils.showToast('Failed to send SMS: ' + error.message, 'error');
        }
    },

    openEmailModal(txnId, customer, email, amount, dueDate, status) {
        document.getElementById('email-modal-txnid').value = txnId;
        document.getElementById('email-modal-customer').value = customer;
        document.getElementById('email-modal-email').value = email;
        document.getElementById('email-modal-amount').value = amount;

        document.getElementById('email-modal-subject').value = `Formal Account Statement: Outstanding Balance ${Utils.formatCurrency(amount)} (Ref #${txnId})`;
        document.getElementById('email-modal-body').value =
            `Dear Accounts Team at ${customer},\n\n` +
            `This is an official statement regarding invoice #${txnId} amounting to ${Utils.formatCurrency(amount)}.\n` +
            `As per your credit arrangement, this invoice is due for immediate remittance.\n\n` +
            `Please transfer funds to our primary account:\n` +
            `Bank: State Bank of India\n` +
            `A/C: 109283746501\n` +
            `IFSC: SBIN0004521\n\n` +
            `Thank you for your prompt cooperation.\n` +
            `Finance & Accounts Team`;

        document.getElementById('comm-email-modal').classList.add('active');
    },

    closeEmailModal() {
        document.getElementById('comm-email-modal').classList.remove('active');
    },

    async handleEmailSubmit(e) {
        e.preventDefault();
        const txnId = document.getElementById('email-modal-txnid').value;
        const subject = document.getElementById('email-modal-subject').value.trim();
        const body = document.getElementById('email-modal-body').value.trim();

        try {
            await API.sendEmail(txnId, subject, body);
            Utils.showToast('Billing statement emailed to buyer with shopkeeper audit copy', 'success');
            this.closeEmailModal();
            this.load();
        } catch (error) {
            Utils.showToast('Failed to dispatch email: ' + error.message, 'error');
        }
    },

    async notifyBothParties(txnId, customer) {
        if (!confirm(`Dispatch synchronized dual-party notification for ${customer} (Invoice #${txnId})? Buyer and Shopkeeper will both receive verification alerts.`)) {
            return;
        }

        try {
            await API.notifyBoth(txnId, `Synchronized collection alert dispatched for Invoice #${txnId}`);
            Utils.showToast(`Dual-party notices delivered to ${customer} and Shopkeeper`, 'success');
            this.load();
        } catch (error) {
            Utils.showToast('Dual-party dispatch failed: ' + error.message, 'error');
        }
    },

    async bulkAlertDefaulters() {
        const overdueCount = this.customers.filter(c => c.status === 'OVERDUE' || c.overdueAmount > 0).length;
        if (overdueCount === 0) {
            Utils.showToast('No overdue defaulters detected in active ledger', 'info');
            return;
        }

        if (!confirm(`Execute portfolio recovery alert to ${overdueCount} overdue defaulter(s)? This will send multi-channel notices and record audit entries.`)) {
            return;
        }

        try {
            const res = await API.bulkNotifyDefaulters();
            const count = res.dispatchedCount || overdueCount;
            Utils.showToast(`Bulk recovery alerts dispatched to ${count} overdue accounts`, 'success');
            this.load();
        } catch (error) {
            Utils.showToast('Bulk alert failed: ' + error.message, 'error');
        }
    },
};
