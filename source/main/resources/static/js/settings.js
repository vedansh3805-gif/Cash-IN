/* ==========================================================================
   CashFlow — Enterprise Settings & Theming Page Module
   ========================================================================== */

const SettingsPage = {
    init() {
        // Theme selection clicks
        document.querySelectorAll('.theme-card').forEach(card => {
            card.onclick = (e) => {
                e.preventDefault();
                const theme = card.dataset.theme;
                if (theme) this.selectTheme(theme);
            };
        });

        // Settings form submission
        const form = document.getElementById('business-form');
        if (form) {
            form.onsubmit = (e) => this.saveSettings(e);
        }
    },

    selectTheme(themeName) {
        if (!themeName) return;

        // 1. Cleanly update theme classes on document.body while preserving other state classes
        const classes = document.body.className
            .split(' ')
            .filter(c => !c.startsWith('theme-') && c.trim().length > 0);
        classes.push(themeName);
        document.body.className = classes.join(' ');

        // 2. Update active highlight on all theme cards
        document.querySelectorAll('.theme-card').forEach(card => {
            const isTarget = card.dataset.theme === themeName;
            card.classList.toggle('active', isTarget);
        });

        // 3. Persist to localStorage
        localStorage.setItem('cashflow-theme', themeName);

        // 4. Persist to backend database via API
        API.updateSettings({ theme: themeName }).catch(err => {
            console.warn('Backend theme save warning:', err);
        });

        // 5. Update charts if any active
        if (window.Charts && Charts.updateAllThemes) {
            setTimeout(() => Charts.updateAllThemes(), 60);
        }

        const themeLabels = {
            'theme-arctic-ledger': 'Arctic Ledger (Navy & Ice Blue)',
            'theme-forest-ledger': 'Forest Ledger (Deep Green & Emerald)',
            'theme-copper-desk': 'Copper Desk (Warm Charcoal & Terracotta)',
            'theme-ocean-glass': 'Ocean Glass (Deep Ocean & Teal)',
            'theme-paper-ink': 'Paper & Ink (High-Contrast Clean Cream)'
        };
        Utils.showToast(`Applied theme: ${themeLabels[themeName] || themeName}`, 'info');
    },

    loadSavedTheme() {
        const saved = localStorage.getItem('cashflow-theme');
        if (saved) {
            this.selectTheme(saved);
        }
    },

    async load() {
        try {
            const settings = await API.getSettings();
            this.populateForm(settings);
        } catch (error) {
            console.error('Settings load error:', error);
        }
    },

    populateForm(settings) {
        if (!settings) return;
        if (settings.businessName) {
            const el = document.getElementById('setting-business-name');
            if (el) el.value = settings.businessName;
        }
        if (settings.currency) {
            const el = document.getElementById('setting-currency');
            if (el) el.value = settings.currency;
            Utils.setCurrency(settings.currency);
        }
        if (settings.minimumCashThreshold != null) {
            const el = document.getElementById('setting-threshold');
            if (el) el.value = settings.minimumCashThreshold;
        }
        if (settings.forecastDays) {
            const el = document.getElementById('setting-forecast-days');
            if (el) el.value = settings.forecastDays;
        }
        if (settings.shopkeeperPhone || settings.phone) {
            const phoneEl = document.getElementById('setting-phone');
            if (phoneEl) phoneEl.value = settings.shopkeeperPhone || settings.phone;
        }
        if (settings.shopkeeperEmail || settings.email) {
            const emailEl = document.getElementById('setting-email');
            if (emailEl) emailEl.value = settings.shopkeeperEmail || settings.email;
        }

        // Apply theme: prefer localStorage if user previously chose one, otherwise backend setting
        const localTheme = localStorage.getItem('cashflow-theme');
        const themeToApply = localTheme || settings.theme || 'theme-arctic-ledger';
        this.selectTheme(themeToApply);
    },

    async saveSettings(e) {
        if (e) e.preventDefault();
        try {
            const currentTheme = document.body.className.split(' ').find(c => c.startsWith('theme-')) || 'theme-arctic-ledger';
            const data = {
                businessName: document.getElementById('setting-business-name')?.value || 'Acme Enterprise Pvt Ltd',
                currency: document.getElementById('setting-currency')?.value || 'INR',
                minimumCashThreshold: parseFloat(document.getElementById('setting-threshold')?.value) || 50000,
                forecastDays: parseInt(document.getElementById('setting-forecast-days')?.value) || 90,
                shopkeeperPhone: document.getElementById('setting-phone')?.value.trim() || '+91 98765 00000',
                shopkeeperEmail: document.getElementById('setting-email')?.value.trim() || 'shopkeeper@cashflow.com',
                theme: currentTheme,
            };

            await API.updateSettings(data);
            Utils.setCurrency(data.currency);
            Utils.showToast('Business configuration and theme saved successfully', 'success');
        } catch (error) {
            console.error('Settings save error:', error);
            Utils.showToast('Failed to save settings: ' + error.message, 'error');
        }
        return false;
    },
};
