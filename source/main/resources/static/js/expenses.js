/* ==========================================================================
   CashFlow — Enterprise Expenses & Obligations Module
   ========================================================================== */

const ExpensesPage = {
    init() {
        const addBtn = document.getElementById('add-recurring-btn');
        if (addBtn) addBtn.addEventListener('click', () => this.openRecurringModal());
        const form = document.getElementById('recurring-form');
        if (form) form.addEventListener('submit', (e) => this.saveRecurring(e));
        const cancelBtn = document.getElementById('recurring-cancel-btn');
        if (cancelBtn) cancelBtn.addEventListener('click', () => this.closeRecurringModal());
        const closeBtn = document.getElementById('recurring-modal-close');
        if (closeBtn) closeBtn.addEventListener('click', () => this.closeRecurringModal());
    },

    async load() {
        try {
            const versionId = document.getElementById('filter-version')?.value || '';
            const [expenseData, recurringData] = await Promise.all([
                API.getExpenses(versionId),
                API.getRecurringPayments(),
            ]);
            this.renderCategoryChart(expenseData);
            this.renderMonthlyChart(expenseData);
            this.renderExpenseTable(expenseData.expenses || expenseData || []);
            this.renderRecurringTable(recurringData || []);
        } catch (error) {
            console.error('Expenses load error:', error);
            Utils.showToast('Failed to load expenses: ' + error.message, 'error');
        }
    },

    renderCategoryChart(data) {
        const categories = (data && data.byCategory) || {};
        const labels = Object.keys(categories);
        const values = Object.values(categories).map(v => Number(v) || 0);
        if (labels.length === 0) {
            Charts.create('chart-expense-category', 'doughnut', { labels: [], datasets: [] });
            return;
        }

        const colors = [
            '#38bdf8', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316'
        ];

        Charts.create('chart-expense-category', 'doughnut', {
            labels,
            datasets: [{
                data: values,
                backgroundColor: colors,
            }],
        });
    },

    renderMonthlyChart(data) {
        const monthly = (data && data.monthly) || [];
        if (monthly.length === 0) {
            Charts.create('chart-monthly-expense', 'bar', { labels: [], datasets: [] });
            return;
        }

        const labels = monthly.map(m => m.month || 'Month');
        const values = monthly.map(m => Number(m.total || 0));

        Charts.create('chart-monthly-expense', 'bar', {
            labels,
            datasets: [{
                label: 'Expenses (₹)',
                data: values,
                backgroundColor: '#f43f5ecc',
            }],
        });
    },

    renderExpenseTable(expenses) {
        const tbody = document.getElementById('expenses-tbody');
        if (!tbody) return;

        if (!expenses || expenses.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="6"><div class="empty-state">No expense records found</div></td></tr>';
            return;
        }

        tbody.innerHTML = expenses.map(e => `
            <tr>
                <td>${Utils.formatDate(e.transactionDate || e.transaction_date || e.date)}</td>
                <td><span class="category-badge">${Utils.escapeHtml(e.category || 'General')}</span></td>
                <td><strong>${Utils.escapeHtml(e.description || 'Expense')}</strong></td>
                <td class="amount-col text-danger font-bold">-${Utils.formatCurrency(e.amount)}</td>
                <td>${Utils.formatDate(e.dueDate || e.due_date)}</td>
                <td>${Utils.statusBadge(e.status)}</td>
            </tr>
        `).join('');
    },

    renderRecurringTable(recurring) {
        const tbody = document.getElementById('recurring-tbody');
        if (!tbody) return;

        if (!recurring || recurring.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="7"><div class="empty-state">No recurring payment commitments defined</div></td></tr>';
            return;
        }

        tbody.innerHTML = recurring.map(r => `
            <tr>
                <td><strong>${Utils.escapeHtml(r.description || '')}</strong></td>
                <td><span class="category-badge">${Utils.escapeHtml(r.category || '')}</span></td>
                <td class="amount-col font-bold">${Utils.formatCurrency(r.amount)}</td>
                <td><span class="badge badge-info">${Utils.escapeHtml(r.frequency || 'MONTHLY')}</span></td>
                <td>${Utils.formatDate(r.nextDueDate || r.next_due_date)}</td>
                <td>${Utils.statusBadge(r.active ? 'ACTIVE' : 'INACTIVE')}</td>
                <td>
                    <div class="table-actions">
                        <button class="btn btn-xs btn-outline-primary" onclick="ExpensesPage.editRecurring('${r.recurringId || r.recurring_id || r.id}')">Edit</button>
                        <button class="btn btn-xs btn-outline-danger" onclick="ExpensesPage.deleteRecurring('${r.recurringId || r.recurring_id || r.id}')">Delete</button>
                    </div>
                </td>
            </tr>
        `).join('');
    },

    openRecurringModal(data = null) {
        const modal = document.getElementById('recurring-modal');
        const title = document.getElementById('recurring-modal-title');
        const form = document.getElementById('recurring-form');

        form.reset();
        document.getElementById('recurring-edit-id').value = '';

        if (data) {
            title.textContent = 'Edit Recurring Obligation';
            document.getElementById('recurring-edit-id').value = data.recurringId || data.recurring_id || data.id;
            document.getElementById('recurring-desc').value = data.description;
            document.getElementById('recurring-category').value = data.category;
            document.getElementById('recurring-amount').value = data.amount;
            document.getElementById('recurring-frequency').value = data.frequency;
            document.getElementById('recurring-next-due').value = Utils.formatDateISO(data.nextDueDate || data.next_due_date);
        } else {
            title.textContent = 'Add Recurring Obligation';
        }

        modal.classList.add('active');
    },

    closeRecurringModal() {
        document.getElementById('recurring-modal').classList.remove('active');
    },

    async saveRecurring(e) {
        e.preventDefault();
        try {
            const id = document.getElementById('recurring-edit-id').value;
            const data = {
                description: document.getElementById('recurring-desc').value,
                category: document.getElementById('recurring-category').value,
                amount: parseFloat(document.getElementById('recurring-amount').value),
                frequency: document.getElementById('recurring-frequency').value,
                nextDueDate: document.getElementById('recurring-next-due').value,
                active: true,
            };

            if (id) {
                await API.updateRecurringPayment(id, data);
                Utils.showToast('Recurring commitment updated', 'success');
            } else {
                await API.addRecurringPayment(data);
                Utils.showToast('Recurring commitment added', 'success');
            }

            this.closeRecurringModal();
            this.load();
        } catch (error) {
            Utils.showToast('Failed to save commitment: ' + error.message, 'error');
        }
    },

    async editRecurring(id) {
        try {
            const recurring = await API.getRecurringPayments();
            const item = (recurring || []).find(r => (r.recurringId || r.recurring_id || r.id) === id);
            if (item) this.openRecurringModal(item);
        } catch (error) {
            Utils.showToast('Failed to load commitment details', 'error');
        }
    },

    async deleteRecurring(id) {
        if (!confirm('Are you sure you want to delete this recurring payment?')) return;
        try {
            await API.deleteRecurringPayment(id);
            Utils.showToast('Recurring payment deleted', 'success');
            this.load();
        } catch (error) {
            Utils.showToast('Failed to delete: ' + error.message, 'error');
        }
    },
};
