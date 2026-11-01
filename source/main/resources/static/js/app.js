/* ============================================
   CashFlow — Main Application Controller
   ============================================ */

const App = {
    currentPage: 'dashboard',

    init() {
        // Load saved theme
        SettingsPage.loadSavedTheme();

        // Initialize Authentication & Session (Component 3)
        if (typeof Auth !== 'undefined') {
            Auth.init();
        }

        // Navigation
        document.querySelectorAll('.nav-item').forEach(item => {
            item.addEventListener('click', (e) => {
                e.preventDefault();
                this.navigateTo(item.dataset.page);
            });
        });

        // Sidebar toggle
        document.getElementById('sidebar-toggle').addEventListener('click', () => {
            document.getElementById('sidebar').classList.toggle('collapsed');
        });

        // CSV Upload Modal
        this.initUpload();

        // Initialize page modules
        TransactionsPage.init();
        ReceivablesPage.init();
        CommunicationPage.init();
        ExpensesPage.init();
        ForecastPage.init();
        WarningsPage.init();
        VersionsPage.init();
        WhatIfPage.init();
        ReportsPage.init();
        SettingsPage.init();

        // Global filter handlers
        document.getElementById('filter-date-preset').addEventListener('change', (e) => {
            const custom = e.target.value === 'custom';
            document.getElementById('filter-date-from').classList.toggle('hidden', !custom);
            document.getElementById('filter-date-to').classList.toggle('hidden', !custom);
            if (!custom) this.refreshCurrentPage();
        });

        document.getElementById('filter-version').addEventListener('change', () => this.refreshCurrentPage());

        // Close modals on backdrop click
        document.querySelectorAll('.modal').forEach(modal => {
            modal.addEventListener('click', (e) => {
                if (e.target === modal) modal.classList.remove('active');
            });
        });

        document.getElementById('txn-detail-close').addEventListener('click', () => {
            document.getElementById('txn-detail-modal').classList.remove('active');
        });

        // Load dashboard
        this.navigateTo('dashboard');

        // Load version list for filters
        this.loadVersionFilter();
    },

    navigateTo(page) {
        // Toggle dashboard body class for zero-scroll layout
        document.body.classList.toggle('on-dashboard', page === 'dashboard');

        // Update nav
        document.querySelectorAll('.nav-item').forEach(item => {
            item.classList.toggle('active', item.dataset.page === page);
        });

        // Update page
        document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
        const pageEl = document.getElementById('page-' + page);
        if (pageEl) pageEl.classList.add('active');

        // Update title
        const titles = {
            dashboard: 'Executive Cockpit & Dashboard',
            transactions: 'Transactions Ledger',
            receivables: 'Receivables & Aging Schedule',
            communication: 'Customer Communication Hub',
            expenses: 'Operating Expenses & Spend',
            forecast: 'Cash Runway Forecast',
            warnings: 'Early Warnings & Anomaly Radar',
            versions: 'Dataset Versions & Audit Lineage',
            whatif: 'What-If Stress Simulator',
            reports: 'Financial Reports & Statements',
            settings: 'Settings & Configuration',
        };
        document.getElementById('page-title').textContent = titles[page] || page;
        this.currentPage = page;

        // Load page data
        this.loadPageData(page);

        // Close mobile sidebar
        document.getElementById('sidebar').classList.remove('mobile-open');
    },

    loadPageData(page) {
        const loaders = {
            dashboard: () => DashboardPage.load(),
            transactions: () => TransactionsPage.load(),
            receivables: () => ReceivablesPage.load(),
            communication: () => CommunicationPage.load(),
            expenses: () => ExpensesPage.load(),
            forecast: () => ForecastPage.load(),
            warnings: () => WarningsPage.load(),
            versions: () => VersionsPage.load(),
            timeline: () => TimelinePage.load(),
            whatif: () => WhatIfPage.load(),
            reports: () => ReportsPage.load(),
            settings: () => SettingsPage.load(),
        };
        if (loaders[page]) loaders[page]();
    },

    refreshCurrentPage() {
        this.loadPageData(this.currentPage);
    },

    onVersionChange(val) {
        const filter = document.getElementById('filter-version');
        if (filter && filter.value !== val) filter.value = val;
        this.refreshCurrentPage();
    },

    onDatePresetChange(val) {
        const filter = document.getElementById('filter-date-preset');
        if (filter && filter.value !== val) filter.value = val;
        if (typeof ForecastPage !== 'undefined' && ForecastPage.setPeriod && ['7', '30', '60', '90'].includes(val)) {
            ForecastPage.setPeriod(parseInt(val, 10));
        }
        this.refreshCurrentPage();
    },

    async loadVersionFilter() {
        try {
            const versions = await API.getVersions();
            if (versions && versions.length > 0) {
                const latest = versions[versions.length - 1] || versions[0];
                const badge = document.getElementById('current-version-badge');
                if (badge) badge.textContent = 'V' + (latest.versionNumber || '1');

                const globalFilter = document.getElementById('filter-version');
                if (globalFilter) {
                    const currentVal = globalFilter.value;
                    globalFilter.innerHTML = '<option value="">All Versions</option>';
                    versions.forEach(v => {
                        const opt = document.createElement('option');
                        opt.value = v.versionId;
                        opt.textContent = `V${v.versionNumber} (${v.fileName || 'data.csv'})`;
                        globalFilter.appendChild(opt);
                    });
                    if (currentVal) globalFilter.value = currentVal;
                }
            }
        } catch (e) {
            console.error('Failed to load versions filter:', e);
        }
    },

    // ---- CSV Upload ----
    initUpload() {
        const modal = document.getElementById('upload-modal');
        const zone = document.getElementById('upload-zone');
        const fileInput = document.getElementById('csv-file-input');

        document.getElementById('csv-upload-btn').addEventListener('click', () => {
            this.resetUploadModal();
            modal.classList.add('active');
        });

        document.getElementById('upload-modal-close').addEventListener('click', () => {
            modal.classList.remove('active');
        });

        zone.addEventListener('click', () => fileInput.click());

        zone.addEventListener('dragover', (e) => {
            e.preventDefault();
            zone.classList.add('drag-over');
        });

        zone.addEventListener('dragleave', () => {
            zone.classList.remove('drag-over');
        });

        zone.addEventListener('drop', (e) => {
            e.preventDefault();
            zone.classList.remove('drag-over');
            const file = e.dataTransfer.files[0];
            if (file) this.uploadFile(file);
        });

        fileInput.addEventListener('change', () => {
            if (fileInput.files[0]) this.uploadFile(fileInput.files[0]);
        });
    },

    resetUploadModal() {
        document.getElementById('upload-zone').style.display = '';
        document.getElementById('upload-progress').classList.add('hidden');
        document.getElementById('upload-result').classList.add('hidden');
        document.getElementById('csv-file-input').value = '';
    },

    async uploadFile(file) {
        // Validate
        if (!file.name.toLowerCase().endsWith('.csv')) {
            Utils.showToast('Please upload a CSV file', 'error');
            return;
        }

        if (file.size > 10 * 1024 * 1024) {
            Utils.showToast('File size exceeds 10MB limit', 'error');
            return;
        }

        // Show progress
        document.getElementById('upload-zone').style.display = 'none';
        const progress = document.getElementById('upload-progress');
        const progressFill = document.getElementById('upload-progress-fill');
        const statusText = document.getElementById('upload-status');
        progress.classList.remove('hidden');

        progressFill.style.width = '30%';
        statusText.textContent = `Uploading ${file.name}...`;

        try {
            progressFill.style.width = '60%';
            statusText.textContent = 'Processing and validating...';

            const result = await API.uploadCsv(file);

            progressFill.style.width = '100%';
            statusText.textContent = 'Complete!';

            // Show result
            const resultDiv = document.getElementById('upload-result');
            resultDiv.classList.remove('hidden');
            resultDiv.innerHTML = `
                <div class="result-success">
                    <strong>✓ CSV Imported Successfully</strong>
                    <br><br>
                    Total rows: ${result.totalRows || 0}<br>
                    Valid rows: ${result.validRows || 0}<br>
                    Invalid rows: ${result.invalidRows || 0}<br>
                    <br>
                    Added: ${result.added || 0}<br>
                    Modified: ${result.modified || 0}<br>
                    Removed: ${result.removed || 0}<br>
                    Unchanged: ${result.unchanged || 0}<br>
                    ${result.versionNumber ? `<br>Version: V${result.versionNumber}` : ''}
                </div>
                ${result.errors && result.errors.length > 0 ? `
                    <div class="result-error" style="margin-top:12px;">
                        <strong>Validation Issues:</strong><br>
                        ${result.errors.slice(0, 10).map(e => `• ${Utils.escapeHtml(e)}`).join('<br>')}
                        ${result.errors.length > 10 ? `<br>... and ${result.errors.length - 10} more` : ''}
                    </div>
                ` : ''}
            `;

            Utils.showToast('CSV imported successfully!', 'success');

            // Refresh data
            this.loadVersionFilter();
            setTimeout(() => this.refreshCurrentPage(), 500);

        } catch (error) {
            progressFill.style.width = '100%';
            progressFill.style.background = 'var(--danger)';
            statusText.textContent = 'Upload failed';

            const resultDiv = document.getElementById('upload-result');
            resultDiv.classList.remove('hidden');
            resultDiv.innerHTML = `<div class="result-error"><strong>✕ Upload Failed</strong><br><br>${Utils.escapeHtml(error.message)}</div>`;

            Utils.showToast('Upload failed: ' + error.message, 'error');
        }
    },

    async loadDemoDataset(demoName) {
        if (!demoName) return;
        try {
            Utils.showToast(`Loading demo scenario: ${demoName}...`, 'info');
            const result = await API.loadDemo(demoName);
            Utils.showToast(`Loaded ${result.validRows || 0} transactions (V${result.versionNumber || ''})`, 'success');
            await this.loadVersionFilter();
            if (result.versionId) {
                const filterEl = document.getElementById('filter-version');
                if (filterEl) filterEl.value = result.versionId;
            }
            this.refreshCurrentPage();
            const demoSel = document.getElementById('quick-demo-dataset');
            if (demoSel) demoSel.value = '';
        } catch (err) {
            console.error('Demo load error:', err);
            Utils.showToast('Failed to load demo scenario: ' + err.message, 'error');
        }
    },
};

// Start the application
document.addEventListener('DOMContentLoaded', () => App.init());
