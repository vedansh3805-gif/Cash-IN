/* ============================================
   CashFlow — Timeline Page Module
   ============================================ */

const TimelinePage = {
    async load() {
        try {
            const data = await API.getTimeline();
            this.render(data || []);
        } catch (error) {
            console.error('Timeline load error:', error);
            Utils.showToast('Failed to load timeline: ' + error.message, 'error');
        }
    },

    render(events) {
        const container = document.getElementById('timeline-container');
        if (!events || events.length === 0) {
            container.innerHTML = '<div class="empty-state">No timeline data. Upload CSV files to build a history.</div>';
            return;
        }

        container.innerHTML = events.map(e => `
            <div class="timeline-item">
                <div class="timeline-date">${Utils.formatDate(e.date || e.uploadDate)}</div>
                <div class="timeline-title">${Utils.escapeHtml(e.title || 'Version ' + (e.versionNumber || ''))}</div>
                <div class="timeline-desc">
                    ${Utils.escapeHtml(e.description || '')}
                    ${e.recordCount ? `<br>Records: ${e.recordCount}` : ''}
                    ${e.changeSummary ? `<br>Changes: +${e.changeSummary.added || 0} added, ${e.changeSummary.modified || 0} modified, -${e.changeSummary.removed || 0} removed` : ''}
                </div>
            </div>
        `).join('') + `
            <div class="timeline-item">
                <div class="timeline-date">CURRENT</div>
                <div class="timeline-title">Latest Financial State</div>
                <div class="timeline-desc">This represents your current data.</div>
            </div>
        `;
    },
};
