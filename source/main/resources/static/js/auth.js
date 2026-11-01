/* ==========================================================================
   CashFlow — Authentication & Login Module (Component 3)
   ========================================================================== */

const Auth = {
    currentUser: null,

    init() {
        const savedUser = localStorage.getItem('cashflow_auth_user');
        const overlay = document.getElementById('auth-overlay');

        if (savedUser) {
            this.currentUser = savedUser;
            if (overlay) overlay.classList.add('hidden');
            this.updateUserUI(savedUser);
        } else {
            // First time or logged out: show login screen
            if (overlay) overlay.classList.remove('hidden');
        }

        // Form submit
        const form = document.getElementById('login-form');
        if (form) {
            form.onsubmit = (e) => {
                e.preventDefault();
                this.handleLogin();
                return false;
            };
        }
    },

    quickFill(role = 'admin') {
        const uInput = document.getElementById('login-username');
        const pInput = document.getElementById('login-password');
        if (!uInput || !pInput) return;

        if (role === 'admin') {
            uInput.value = 'admin@cashflow.com';
            pInput.value = 'password123';
            Utils.showToast('Demo Admin credentials filled', 'info');
        } else if (role === 'auditor') {
            uInput.value = 'auditor@cashflow.com';
            pInput.value = 'audit2026';
            Utils.showToast('Finance Auditor credentials filled', 'info');
        } else if (role === 'shopkeeper') {
            uInput.value = 'shopkeeper@cashflow.com';
            pInput.value = 'owner123';
            Utils.showToast('Shopkeeper credentials filled', 'info');
        }
    },

    handleLogin() {
        const usernameEl = document.getElementById('login-username');
        const passwordEl = document.getElementById('login-password');
        const username = usernameEl ? usernameEl.value.trim() : '';
        const password = passwordEl ? passwordEl.value.trim() : '';

        // Validation
        if (!username) {
            Utils.showToast('Please enter your email or username', 'warning');
            if (usernameEl) usernameEl.focus();
            return;
        }

        if (!username.includes('@') && username.length < 3) {
            Utils.showToast('Please enter a valid email address or username', 'warning');
            if (usernameEl) usernameEl.focus();
            return;
        }

        if (!password || password.length < 4) {
            Utils.showToast('Password must be at least 4 characters long', 'warning');
            if (passwordEl) passwordEl.focus();
            return;
        }

        // Determine user display label
        let displayName = 'Finance Admin';
        if (username.includes('auditor')) displayName = 'Finance Auditor';
        else if (username.includes('shopkeeper')) displayName = 'Business Owner';
        else if (username.split('@')[0]) {
            const raw = username.split('@')[0];
            displayName = raw.charAt(0).toUpperCase() + raw.slice(1);
        }

        // Save session
        this.currentUser = displayName;
        localStorage.setItem('cashflow_auth_user', displayName);

        // Hide login overlay
        const overlay = document.getElementById('auth-overlay');
        if (overlay) overlay.classList.add('hidden');

        this.updateUserUI(displayName);
        Utils.showToast(`Welcome back, ${displayName}!`, 'success');

        // Refresh active dashboard
        if (typeof App !== 'undefined' && App.refreshCurrentPage) {
            App.refreshCurrentPage();
        }
    },

    logout() {
        this.currentUser = null;
        localStorage.removeItem('cashflow_auth_user');

        const overlay = document.getElementById('auth-overlay');
        if (overlay) {
            overlay.classList.remove('hidden');
        }

        Utils.showToast('Logged out successfully', 'info');
    },

    updateUserUI(name) {
        const el = document.getElementById('user-display-name');
        if (el) el.textContent = name || 'Admin User';
    }
};
