/* ==========================================================================
   CashFlow — Enterprise Dataset Versions & Audit Lineage Module
   ========================================================================== */

const VersionsPage = {
    selectedVersion: null,

    init() {
        // Wired cleanly
    },

    async load() {
        try {
            const versions = await API.getVersions();
            this.renderVersionsList(versions || []);
            this.populateCompareDropdowns(versions || []);

            if (typeof TimelinePage !== 'undefined' && TimelinePage.load) {
                TimelinePage.load();
            }

            if (versions && versions.length > 0 && !this.selectedVersion) {
                this.selectVersion(versions[0].versionId);
            }
        } catch (error) {
            console.error('Versions load error:', error);
            Utils.showToast('Failed to load dataset versions: ' + error.message, 'error');
        }
    },

    renderVersionsList(versions) {
        const container = document.getElementById('versions-list');
        if (!container) return;

        if (!versions || versions.length === 0) {
            container.innerHTML = '<div class="empty-state">No dataset versions available. Upload a CSV to initialize versioning.</div>';
            return;
        }

        container.innerHTML = versions.map(v => `
            <div class="version-item ${this.selectedVersion === v.versionId ? 'active' : ''}"
                 data-id="${v.versionId}" onclick="VersionsPage.selectVersion('${v.versionId}')">
                <div class="version-item-header">
                    <span class="version-badge">V${v.versionNumber}</span>
                    <span class="version-date">${Utils.formatDate(v.uploadDate)}</span>
                </div>
                <div class="version-file">${Utils.escapeHtml(v.fileName || 'data.csv')}</div>
                <div class="version-count">${v.recordCount || 0} transaction records</div>
            </div>
        `).join('');
    },

    populateCompareDropdowns(versions) {
        ['compare-v1', 'compare-v2'].forEach((id, idx) => {
            const select = document.getElementById(id);
            if (!select) return;
            const current = select.value;
            select.innerHTML = '<option value="">Select Version</option>';
            versions.forEach((v, vIdx) => {
                const opt = document.createElement('option');
                opt.value = v.versionId;
                opt.textContent = `V${v.versionNumber} — ${Utils.escapeHtml(v.fileName || '')} (${Utils.formatDate(v.uploadDate)})`;
                select.appendChild(opt);
            });

            // Default: compare base version (earliest) with target version (latest)
            if (current) {
                select.value = current;
            } else if (versions.length >= 2) {
                if (idx === 0) select.value = versions[versions.length - 1].versionId;
                if (idx === 1) select.value = versions[0].versionId;
            } else if (versions.length === 1) {
                select.value = versions[0].versionId;
            }
        });

        // Automatically trigger comparison if versions are selected and not yet compared
        if (versions.length >= 2) {
            setTimeout(() => {
                const v1 = document.getElementById('compare-v1')?.value;
                const v2 = document.getElementById('compare-v2')?.value;
                if (v1 && v2 && v1 !== v2) {
                    this.compareVersions();
                }
            }, 100);
        }

        // Global filter
        const globalFilter = document.getElementById('filter-version');
        if (globalFilter) {
            const gCurrent = globalFilter.value;
            globalFilter.innerHTML = '<option value="">All Versions</option>';
            versions.forEach(v => {
                const opt = document.createElement('option');
                opt.value = v.versionId;
                opt.textContent = `V${v.versionNumber}`;
                globalFilter.appendChild(opt);
            });
            if (gCurrent) globalFilter.value = gCurrent;
        }
    },

    async selectVersion(id) {
        this.selectedVersion = id;

        // Highlight active card
        document.querySelectorAll('.version-item').forEach(item => {
            item.classList.toggle('active', item.dataset.id === id);
        });

        try {
            const [detail, changes] = await Promise.all([
                API.getVersionDetail(id),
                API.getVersionChanges(id),
            ]);

            this.renderVersionDetail(detail);
            this.renderChanges(changes || []);
            this.renderChangesChart(changes || []);
        } catch (error) {
            console.error('Select version error:', error);
            Utils.showToast('Failed to load version details', 'error');
        }
    },

    renderVersionDetail(detail) {
        const container = document.getElementById('version-detail');
        if (!container) return;

        if (!detail) {
            container.innerHTML = '<div class="empty-state">No version details available</div>';
            return;
        }

        const summary = detail.changeSummary || {};

        container.innerHTML = `
            <div class="report-section">
                <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:12px;">
                    <h4 style="margin:0;">Dataset Version V${detail.versionNumber}</h4>
                    <span class="badge badge-primary">${detail.recordCount || 0} Records</span>
                </div>
                <div class="report-row"><span class="label">Source Filename</span><span class="value font-bold">${Utils.escapeHtml(detail.fileName || '—')}</span></div>
                <div class="report-row"><span class="label">Ingestion Timestamp</span><span class="value">${Utils.formatDate(detail.uploadDate)}</span></div>
                <div class="report-row"><span class="label">Total Paid Income</span><span class="value text-success font-bold">${Utils.formatCurrency(detail.totalIncome)}</span></div>
                <div class="report-row"><span class="label">Total Paid Expenses</span><span class="value text-danger font-bold">${Utils.formatCurrency(detail.totalExpenses)}</span></div>
                
                <div style="margin-top:14px;padding-top:10px;border-top:1px solid var(--border-color);">
                    <div style="font-weight:600;font-size:12px;margin-bottom:8px;text-transform:uppercase;color:var(--text-muted);">Delta Changes vs Prior Baseline</div>
                    <div class="report-row"><span class="label">Added Records</span><span class="value text-success font-bold">+${summary.added || 0}</span></div>
                    <div class="report-row"><span class="label">Modified Records</span><span class="value text-info font-bold">${summary.modified || 0}</span></div>
                    <div class="report-row"><span class="label">Removed Records</span><span class="value text-danger font-bold">-${summary.removed || 0}</span></div>
                    <div class="report-row"><span class="label">Unchanged Records</span><span class="value text-muted font-bold">${summary.unchanged != null ? summary.unchanged : '—'}</span></div>
                </div>
            </div>
        `;
    },

    renderChanges(changes) {
        const tbody = document.getElementById('changes-tbody');
        if (!tbody) return;

        if (!changes || changes.length === 0) {
            tbody.innerHTML = '<tr class="empty-row"><td colspan="6"><div class="empty-state">No delta changes detected in this dataset version</div></td></tr>';
            return;
        }

        tbody.innerHTML = changes.map(c => `
            <tr>
                <td><strong style="font-family:monospace;font-size:12px;">#${Utils.escapeHtml(c.transaction_id || c.transactionId)}</strong></td>
                <td>${Utils.statusBadge(c.change_type || c.changeType)}</td>
                <td><strong>${Utils.escapeHtml(c.field_name || c.fieldName || 'Record')}</strong></td>
                <td><span class="text-muted">${Utils.escapeHtml(c.old_value || c.oldValue || '—')}</span></td>
                <td><strong class="text-primary">${Utils.escapeHtml(c.new_value || c.newValue || '—')}</strong></td>
                <td>${Utils.formatDate(c.changed_at || c.changedAt)}</td>
            </tr>
        `).join('');
    },

    renderChangesChart(changes) {
        const canvas = document.getElementById('chart-changes');
        if (!canvas) return;

        const counts = { ADDED: 0, MODIFIED: 0, REMOVED: 0 };
        (changes || []).forEach(c => {
            const type = (c.change_type || c.changeType || '').toUpperCase();
            if (counts[type] !== undefined) counts[type]++;
        });

        Charts.create('chart-changes', 'bar', {
            labels: ['Added Records', 'Modified Records', 'Removed Records'],
            datasets: [{
                label: 'Records',
                data: [counts.ADDED, counts.MODIFIED, counts.REMOVED],
                backgroundColor: [
                    '#10b981cc', // Added (Emerald)
                    '#38bdf8cc', // Modified (Sky Blue)
                    '#ef4444cc'  // Removed (Red)
                ],
            }],
        });
    },

    async compareVersions() {
        const v1 = document.getElementById('compare-v1').value;
        const v2 = document.getElementById('compare-v2').value;

        if (!v1 || !v2) {
            Utils.showToast('Please select two versions to compare', 'warning');
            return;
        }

        if (v1 === v2) {
            Utils.showToast('Please select two distinct versions to calculate differences', 'warning');
            return;
        }

        try {
            const result = await API.compareVersions(v1, v2);
            this.renderCompareResult(result);
        } catch (error) {
            console.error('Compare error:', error);
            Utils.showToast('Failed to compare versions: ' + error.message, 'error');
        }
    },

    renderCompareResult(result) {
        const container = document.getElementById('compare-result');
        if (!container) return;

        if (!result) {
            container.innerHTML = '<div class="empty-state">No comparison differential calculated</div>';
            return;
        }

        const summary = result.changeSummary || result.summary || {};
        const added = result.added != null ? result.added : (summary.added || 0);
        const modified = result.modified != null ? result.modified : (summary.modified || 0);
        const removed = result.removed != null ? result.removed : (summary.removed || 0);
        const unchanged = result.unchanged != null ? result.unchanged : (summary.unchanged || 0);

        container.innerHTML = `
            <div class="report-section" style="margin-bottom:16px;">
                <h4 style="margin-bottom:12px;">Differential Comparison Summary</h4>
                <div class="report-row"><span class="label">Added in Target Version</span><span class="value text-success font-bold">+${added}</span></div>
                <div class="report-row"><span class="label">Modified Attributes</span><span class="value text-info font-bold">${modified}</span></div>
                <div class="report-row"><span class="label">Removed Records</span><span class="value text-danger font-bold">-${removed}</span></div>
                <div class="report-row"><span class="label">Unchanged Records</span><span class="value text-muted font-bold">${unchanged}</span></div>
            </div>
            ${result.changes && result.changes.length > 0 ? `
                <div class="report-section">
                    <h4 style="margin-bottom:10px;">Itemized Differences (${result.changes.length})</h4>
                    <div style="max-height:240px;overflow-y:auto;display:flex;flex-direction:column;gap:6px;">
                        ${result.changes.slice(0, 30).map(c => `
                            <div class="report-row" style="padding:6px 0;">
                                <span class="label">
                                    ${Utils.statusBadge(c.changeType || c.change_type)}
                                    <strong style="margin-left:6px;font-family:monospace;">#${Utils.escapeHtml(c.transactionId || c.transaction_id)}</strong>
                                </span>
                                <span class="value">
                                    ${Utils.escapeHtml(c.fieldName || c.field_name || '')}: 
                                    <span class="text-muted">${Utils.escapeHtml(c.oldValue || c.old_value || '—')}</span> &rarr; 
                                    <strong class="text-primary">${Utils.escapeHtml(c.newValue || c.new_value || '—')}</strong>
                                </span>
                            </div>
                        `).join('')}
                    </div>
                </div>
            ` : '<div class="empty-state" style="padding:16px;">Both selected versions contain identical transaction records</div>'}
        `;
    },
};
