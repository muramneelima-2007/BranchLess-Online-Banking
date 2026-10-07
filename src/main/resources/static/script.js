/**
 * BranchLess – Online Banking System
 * Frontend Controller Script (Vanilla JavaScript)
 * Interacts directly with Spring Boot REST APIs
 */

(function () {
    "use strict";

    // Application State
    const state = {
        currentUser: null,       // { customerId, name, email, phone }
        accounts: [],            // Array of customer accounts
        selectedAccountNumber: null,
        currentAccount: null     // Detailed account object { accountNumber, accountType, balance, status }
    };

    // DOM Elements Cache
    const DOM = {
        // Notification
        notificationContainer: document.getElementById("notification-container"),

        // Header
        headerUserActions: document.getElementById("header-user-actions"),
        headerUserName: document.getElementById("header-user-name"),
        btnLogout: document.getElementById("btn-logout"),

        // Auth Section
        authSection: document.getElementById("auth-section"),
        tabLogin: document.getElementById("tab-login"),
        tabRegister: document.getElementById("tab-register"),
        authAlert: document.getElementById("auth-alert"),
        formLogin: document.getElementById("form-login"),
        loginEmail: document.getElementById("login-email"),
        loginPassword: document.getElementById("login-password"),
        btnLoginSubmit: document.getElementById("btn-login-submit"),
        linkGoToRegister: document.getElementById("link-go-to-register"),

        formRegister: document.getElementById("form-register"),
        regName: document.getElementById("reg-name"),
        regEmail: document.getElementById("reg-email"),
        regPassword: document.getElementById("reg-password"),
        regPhone: document.getElementById("reg-phone"),
        btnRegisterSubmit: document.getElementById("btn-register-submit"),
        linkGoToLogin: document.getElementById("link-go-to-login"),

        // Dashboard Section
        dashboardSection: document.getElementById("dashboard-section"),
        dashCustomerName: document.getElementById("dash-customer-name"),
        dashCustomerEmail: document.getElementById("dash-customer-email"),
        dashCustomerPhone: document.getElementById("dash-customer-phone"),
        accountSelect: document.getElementById("account-select"),
        btnOpenAccountModal: document.getElementById("btn-open-account-modal"),

        // Account Display
        noAccountCard: document.getElementById("no-account-card"),
        btnCreateFirstAccount: document.getElementById("btn-create-first-account"),
        activeAccountCard: document.getElementById("active-account-card"),
        dashAccountNumber: document.getElementById("dash-account-number"),
        dashAccountType: document.getElementById("dash-account-type"),
        dashAccountStatus: document.getElementById("dash-account-status"),
        dashAccountBalance: document.getElementById("dash-account-balance"),
        btnRefreshDashboard: document.getElementById("btn-refresh-dashboard"),

        // Operations
        operationsSection: document.getElementById("operations-section"),
        formDeposit: document.getElementById("form-deposit"),
        depositAmount: document.getElementById("deposit-amount"),
        btnDepositSubmit: document.getElementById("btn-deposit-submit"),

        formWithdraw: document.getElementById("form-withdraw"),
        withdrawAmount: document.getElementById("withdraw-amount"),
        btnWithdrawSubmit: document.getElementById("btn-withdraw-submit"),

        formTransfer: document.getElementById("form-transfer"),
        transferDest: document.getElementById("transfer-dest"),
        transferAmount: document.getElementById("transfer-amount"),
        btnTransferSubmit: document.getElementById("btn-transfer-submit"),

        // Transactions
        transactionsSection: document.getElementById("transactions-section"),
        transCount: document.getElementById("trans-count"),
        btnRefreshTransactions: document.getElementById("btn-refresh-transactions"),
        transactionsTbody: document.getElementById("transactions-tbody"),

        // Modal
        modalOpenAccount: document.getElementById("modal-open-account"),
        formOpenAccount: document.getElementById("form-open-account"),
        newAccountType: document.getElementById("new-account-type"),
        btnModalClose: document.getElementById("btn-modal-close"),
        btnModalCancel: document.getElementById("btn-modal-cancel")
    };

    // ==================== INITIALIZATION ====================
    function init() {
        bindEvents();
        checkExistingSession();
    }

    function checkExistingSession() {
        const storedUser = localStorage.getItem("branchless_user");
        if (storedUser) {
            try {
                const user = JSON.parse(storedUser);
                if (user && user.customerId) {
                    state.currentUser = user;
                    showDashboard();
                    loadCustomerDashboard(user.customerId);
                    return;
                }
            } catch (e) {
                localStorage.removeItem("branchless_user");
            }
        }
        showAuth("login");
    }

    // ==================== EVENT LISTENERS ====================
    function bindEvents() {
        // Auth Tab switching
        DOM.tabLogin.addEventListener("click", () => showAuth("login"));
        DOM.tabRegister.addEventListener("click", () => showAuth("register"));
        DOM.linkGoToRegister.addEventListener("click", () => showAuth("register"));
        DOM.linkGoToLogin.addEventListener("click", () => showAuth("login"));

        // Forms
        DOM.formLogin.addEventListener("submit", handleLogin);
        DOM.formRegister.addEventListener("submit", handleRegister);

        // Header Actions
        DOM.btnLogout.addEventListener("click", handleLogout);

        // Account Selector
        DOM.accountSelect.addEventListener("change", (e) => {
            const selectedNum = e.target.value;
            if (selectedNum) {
                switchActiveAccount(selectedNum);
            }
        });

        // Open Account Modal
        DOM.btnOpenAccountModal.addEventListener("click", () => openAccountModal());
        DOM.btnCreateFirstAccount.addEventListener("click", () => handleCreateAccount("SAVINGS"));
        DOM.btnModalClose.addEventListener("click", () => closeAccountModal());
        DOM.btnModalCancel.addEventListener("click", () => closeAccountModal());
        DOM.formOpenAccount.addEventListener("submit", (e) => {
            e.preventDefault();
            const type = DOM.newAccountType.value;
            handleCreateAccount(type);
            closeAccountModal();
        });

        // Dashboard Refresh
        DOM.btnRefreshDashboard.addEventListener("click", refreshCurrentAccountData);
        DOM.btnRefreshTransactions.addEventListener("click", refreshCurrentAccountData);

        // Banking Operations
        DOM.formDeposit.addEventListener("submit", handleDeposit);
        DOM.formWithdraw.addEventListener("submit", handleWithdraw);
        DOM.formTransfer.addEventListener("submit", handleTransfer);
    }

    // ==================== NOTIFICATIONS / ALERTS ====================
    function showNotification(message, type = "info") {
        const toast = document.createElement("div");
        toast.className = `toast toast-${type}`;

        const msgSpan = document.createElement("span");
        msgSpan.textContent = message;

        const closeBtn = document.createElement("button");
        closeBtn.className = "toast-close";
        closeBtn.innerHTML = "&times;";
        closeBtn.onclick = () => toast.remove();

        toast.appendChild(msgSpan);
        toast.appendChild(closeBtn);

        DOM.notificationContainer.appendChild(toast);

        setTimeout(() => {
            if (toast.parentElement) {
                toast.style.opacity = "0";
                toast.style.transition = "opacity 0.4s ease";
                setTimeout(() => toast.remove(), 400);
            }
        }, 4000);
    }

    function showAuthAlert(message, type = "error") {
        DOM.authAlert.className = `alert-box alert-${type}`;
        DOM.authAlert.textContent = message;
        DOM.authAlert.classList.remove("hidden");
    }

    function clearAuthAlert() {
        DOM.authAlert.className = "alert-box hidden";
        DOM.authAlert.textContent = "";
    }

    // ==================== VIEW SWITCHING ====================
    function showAuth(mode = "login") {
        clearAuthAlert();
        DOM.authSection.classList.remove("hidden");
        DOM.dashboardSection.classList.add("hidden");
        DOM.headerUserActions.classList.add("hidden");

        if (mode === "login") {
            DOM.tabLogin.classList.add("active");
            DOM.tabRegister.classList.remove("active");
            DOM.formLogin.classList.remove("hidden");
            DOM.formRegister.classList.add("hidden");
        } else {
            DOM.tabRegister.classList.add("active");
            DOM.tabLogin.classList.remove("active");
            DOM.formRegister.classList.remove("hidden");
            DOM.formLogin.classList.add("hidden");
        }
    }

    function showDashboard() {
        DOM.authSection.classList.add("hidden");
        DOM.dashboardSection.classList.remove("hidden");
        DOM.headerUserActions.classList.remove("hidden");
    }

    function openAccountModal() {
        DOM.modalOpenAccount.classList.remove("hidden");
    }

    function closeAccountModal() {
        DOM.modalOpenAccount.classList.add("hidden");
    }

    // ==================== AUTHENTICATION LOGIC ====================
    async function handleLogin(e) {
        e.preventDefault();
        clearAuthAlert();

        const email = DOM.loginEmail.value.trim();
        const password = DOM.loginPassword.value;

        // Simple validation
        if (!email || !password) {
            showAuthAlert("Please enter both email and password.");
            return;
        }

        DOM.btnLoginSubmit.disabled = true;
        DOM.btnLoginSubmit.textContent = "Signing In...";

        try {
            const response = await fetch("/customers/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ email, password })
            });

            const data = await response.json();

            if (!response.ok) {
                const errorMsg = data.message || "Invalid email or password.";
                showAuthAlert(errorMsg, "error");
                showNotification(errorMsg, "error");
                return;
            }

            // Successful login
            state.currentUser = {
                customerId: data.customerId,
                name: data.name,
                email: data.email,
                phone: data.phone
            };

            // Store non-sensitive user info
            localStorage.setItem("branchless_user", JSON.stringify(state.currentUser));

            DOM.formLogin.reset();
            showNotification(`Welcome back, ${data.name}!`, "success");

            showDashboard();
            await loadCustomerDashboard(data.customerId);

        } catch (err) {
            console.error("Login error:", err);
            showAuthAlert("Could not connect to the server. Please verify the backend is running.", "error");
        } finally {
            DOM.btnLoginSubmit.disabled = false;
            DOM.btnLoginSubmit.textContent = "Sign In to Account";
        }
    }

    async function handleRegister(e) {
        e.preventDefault();
        clearAuthAlert();

        const name = DOM.regName.value.trim();
        const email = DOM.regEmail.value.trim();
        const password = DOM.regPassword.value;
        const phone = DOM.regPhone.value.trim();

        if (!name || !email || !password || !phone) {
            showAuthAlert("Please fill in all registration fields.");
            return;
        }

        if (password.length < 4) {
            showAuthAlert("Password should be at least 4 characters long.");
            return;
        }

        DOM.btnRegisterSubmit.disabled = true;
        DOM.btnRegisterSubmit.textContent = "Creating Account...";

        try {
            const response = await fetch("/customers/register", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ name, email, password, phone })
            });

            const data = await response.json();

            if (!response.ok) {
                const errorMsg = data.message || "Registration failed. Please check your inputs.";
                showAuthAlert(errorMsg, "error");
                showNotification(errorMsg, "error");
                return;
            }

            // Registration succeeded!
            showNotification("Registration successful! You may now sign in.", "success");
            DOM.formRegister.reset();

            // Auto-fill login email and switch to login tab
            DOM.loginEmail.value = email;
            DOM.loginPassword.value = "";
            showAuth("login");
            showAuthAlert("Account created successfully! Please enter your password to log in.", "success");

        } catch (err) {
            console.error("Registration error:", err);
            showAuthAlert("Could not complete registration. Check backend connection.", "error");
        } finally {
            DOM.btnRegisterSubmit.disabled = false;
            DOM.btnRegisterSubmit.textContent = "Register Customer";
        }
    }

    function handleLogout() {
        state.currentUser = null;
        state.accounts = [];
        state.selectedAccountNumber = null;
        state.currentAccount = null;

        localStorage.removeItem("branchless_user");

        showNotification("You have been signed out.", "info");
        showAuth("login");
    }

    // ==================== DASHBOARD DATA LOADING ====================
    async function loadCustomerDashboard(customerId) {
        try {
            // 1. Fetch latest customer profile
            const profileRes = await fetch(`/customers/${customerId}`);
            if (profileRes.ok) {
                const customer = await profileRes.json();
                state.currentUser = {
                    customerId: customer.id,
                    name: customer.name,
                    email: customer.email,
                    phone: customer.phone
                };
            }

            // Update header and profile cards
            DOM.headerUserName.textContent = state.currentUser.name;
            DOM.dashCustomerName.textContent = state.currentUser.name;
            DOM.dashCustomerEmail.textContent = state.currentUser.email;
            DOM.dashCustomerPhone.textContent = state.currentUser.phone;

            // 2. Fetch customer's bank accounts
            const accRes = await fetch(`/accounts/customer/${customerId}`);
            if (!accRes.ok) {
                throw new Error("Failed to load accounts.");
            }

            const accounts = await accRes.json();
            state.accounts = accounts;

            if (!accounts || accounts.length === 0) {
                // Show empty account prompt
                DOM.noAccountCard.classList.remove("hidden");
                DOM.activeAccountCard.classList.add("hidden");
                DOM.operationsSection.classList.add("hidden");
                DOM.transactionsSection.classList.add("hidden");
                DOM.accountSelect.innerHTML = '<option value="">No Accounts</option>';
                return;
            }

            // Has accounts
            DOM.noAccountCard.classList.add("hidden");
            DOM.activeAccountCard.classList.remove("hidden");
            DOM.operationsSection.classList.remove("hidden");
            DOM.transactionsSection.classList.remove("hidden");

            // Populate account selector
            populateAccountSelector(accounts);

            // Determine which account to show
            const targetAccountNumber = state.selectedAccountNumber || accounts[0].accountNumber;
            await switchActiveAccount(targetAccountNumber);

        } catch (err) {
            console.error("Dashboard load error:", err);
            showNotification("Failed to load dashboard data: " + err.message, "error");
        }
    }

    function populateAccountSelector(accounts) {
        DOM.accountSelect.innerHTML = "";
        accounts.forEach(acc => {
            const opt = document.createElement("option");
            opt.value = acc.accountNumber;
            opt.textContent = `${acc.accountType} - ${acc.accountNumber} ($${formatNumber(acc.balance)})`;
            DOM.accountSelect.appendChild(opt);
        });
    }

    async function switchActiveAccount(accountNumber) {
        state.selectedAccountNumber = accountNumber;
        DOM.accountSelect.value = accountNumber;

        try {
            // 1. Get latest account details
            const accRes = await fetch(`/accounts/${accountNumber}`);
            if (!accRes.ok) {
                const errData = await accRes.json();
                throw new Error(errData.message || "Failed to load account details.");
            }

            const account = await accRes.json();
            state.currentAccount = account;

            // Update UI
            renderAccountDetails(account);

            // 2. Load transactions for this account
            await loadTransactions(accountNumber);

        } catch (err) {
            console.error("Error switching account:", err);
            showNotification(err.message, "error");
        }
    }

    function renderAccountDetails(account) {
        DOM.dashAccountNumber.textContent = account.accountNumber;
        DOM.dashAccountType.textContent = account.accountType || "SAVINGS";
        
        // Status Badge
        const status = account.status || "ACTIVE";
        DOM.dashAccountStatus.textContent = status;
        if (status.toUpperCase() === "ACTIVE") {
            DOM.dashAccountStatus.className = "badge badge-status badge-active";
        } else {
            DOM.dashAccountStatus.className = "badge badge-status badge-blocked";
        }

        // Balance
        DOM.dashAccountBalance.textContent = formatNumber(account.balance);
    }

    async function loadTransactions(accountNumber) {
        DOM.transactionsTbody.innerHTML = `<tr><td colspan="6" class="table-empty">Loading transactions...</td></tr>`;

        try {
            const res = await fetch(`/transactions/account/${accountNumber}`);
            if (!res.ok) {
                throw new Error("Could not load transactions.");
            }

            const transactions = await res.json();
            DOM.transCount.textContent = `${transactions.length} Records`;

            if (!transactions || transactions.length === 0) {
                DOM.transactionsTbody.innerHTML = `<tr><td colspan="6" class="table-empty">No transactions found for this account.</td></tr>`;
                return;
            }

            DOM.transactionsTbody.innerHTML = "";
            transactions.forEach(tx => {
                const tr = document.createElement("tr");

                // Type Badge
                const type = tx.type || "UNKNOWN";
                let typeBadgeClass = "trans-type-badge";
                if (type === "DEPOSIT") typeBadgeClass += " trans-deposit";
                else if (type === "WITHDRAW") typeBadgeClass += " trans-withdraw";
                else if (type === "TRANSFER") typeBadgeClass += " trans-transfer";

                // Amount Sign & Color
                const isIncoming = (type === "DEPOSIT") || (type === "TRANSFER" && tx.destinationAccount === accountNumber);
                const amountClass = isIncoming ? "amount-positive" : "amount-negative";
                const amountPrefix = isIncoming ? "+" : "-";

                tr.innerHTML = `
                    <td><span class="${typeBadgeClass}">${type}</span></td>
                    <td><span class="trans-amount ${amountClass}">${amountPrefix}$${formatNumber(tx.amount)}</span></td>
                    <td><code>${tx.sourceAccount || "-"}</code></td>
                    <td><code>${tx.destinationAccount || "-"}</code></td>
                    <td>${formatDateTime(tx.date)}</td>
                    <td>${escapeHtml(tx.description || "")}</td>
                `;

                DOM.transactionsTbody.appendChild(tr);
            });

        } catch (err) {
            console.error("Load transactions error:", err);
            DOM.transactionsTbody.innerHTML = `<tr><td colspan="6" class="table-empty">Failed to load transaction history.</td></tr>`;
        }
    }

    async function refreshCurrentAccountData() {
        if (!state.selectedAccountNumber || !state.currentUser) return;
        showNotification("Refreshing data...", "info");
        await loadCustomerDashboard(state.currentUser.customerId);
        showNotification("Data up to date.", "success");
    }

    // ==================== ACCOUNT CREATION ====================
    async function handleCreateAccount(accountType = "SAVINGS") {
        if (!state.currentUser) return;

        try {
            const res = await fetch("/accounts", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    customerId: state.currentUser.customerId,
                    accountType: accountType
                })
            });

            const data = await res.json();

            if (!res.ok) {
                const errMsg = data.message || "Failed to create account.";
                showNotification(errMsg, "error");
                return;
            }

            showNotification(`New ${accountType} account created! (A/C: ${data.accountNumber})`, "success");
            state.selectedAccountNumber = data.accountNumber;
            await loadCustomerDashboard(state.currentUser.customerId);

        } catch (err) {
            console.error("Account creation error:", err);
            showNotification("Could not create account: " + err.message, "error");
        }
    }

    // ==================== BANKING OPERATIONS ====================
    async function handleDeposit(e) {
        e.preventDefault();
        const accountNumber = state.selectedAccountNumber;
        if (!accountNumber) {
            showNotification("Please select an active account first.", "error");
            return;
        }

        const amountVal = parseFloat(DOM.depositAmount.value);
        if (isNaN(amountVal) || amountVal <= 0) {
            showNotification("Please enter a valid deposit amount greater than 0.", "error");
            return;
        }

        DOM.btnDepositSubmit.disabled = true;
        DOM.btnDepositSubmit.textContent = "Processing...";

        try {
            const res = await fetch(`/accounts/${accountNumber}/deposit`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ amount: amountVal })
            });

            const data = await res.json();

            if (!res.ok) {
                const msg = data.message || "Deposit failed.";
                showNotification(msg, "error");
                return;
            }

            showNotification(`Successfully deposited $${formatNumber(amountVal)} to account ${accountNumber}`, "success");
            DOM.formDeposit.reset();

            // Refresh account data and transactions
            await loadCustomerDashboard(state.currentUser.customerId);

        } catch (err) {
            console.error("Deposit error:", err);
            showNotification("Deposit operation failed: " + err.message, "error");
        } finally {
            DOM.btnDepositSubmit.disabled = false;
            DOM.btnDepositSubmit.textContent = "Deposit Money";
        }
    }

    async function handleWithdraw(e) {
        e.preventDefault();
        const accountNumber = state.selectedAccountNumber;
        if (!accountNumber) {
            showNotification("Please select an active account first.", "error");
            return;
        }

        const amountVal = parseFloat(DOM.withdrawAmount.value);
        if (isNaN(amountVal) || amountVal <= 0) {
            showNotification("Please enter a valid withdrawal amount greater than 0.", "error");
            return;
        }

        DOM.btnWithdrawSubmit.disabled = true;
        DOM.btnWithdrawSubmit.textContent = "Processing...";

        try {
            const res = await fetch(`/accounts/${accountNumber}/withdraw`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ amount: amountVal })
            });

            const data = await res.json();

            if (!res.ok) {
                const msg = data.message || "Withdrawal failed.";
                showNotification(msg, "error");
                return;
            }

            showNotification(`Successfully withdrew $${formatNumber(amountVal)} from account ${accountNumber}`, "success");
            DOM.formWithdraw.reset();

            // Refresh account data and transactions
            await loadCustomerDashboard(state.currentUser.customerId);

        } catch (err) {
            console.error("Withdraw error:", err);
            showNotification("Withdrawal operation failed: " + err.message, "error");
        } finally {
            DOM.btnWithdrawSubmit.disabled = false;
            DOM.btnWithdrawSubmit.textContent = "Withdraw Money";
        }
    }

    async function handleTransfer(e) {
        e.preventDefault();
        const sourceAccountNumber = state.selectedAccountNumber;
        if (!sourceAccountNumber) {
            showNotification("Please select an active account first.", "error");
            return;
        }

        const destinationAccount = DOM.transferDest.value.trim();
        const amountVal = parseFloat(DOM.transferAmount.value);

        if (!destinationAccount) {
            showNotification("Please enter a destination account number.", "error");
            return;
        }

        if (destinationAccount === sourceAccountNumber) {
            showNotification("Source and destination accounts cannot be the same.", "error");
            return;
        }

        if (isNaN(amountVal) || amountVal <= 0) {
            showNotification("Please enter a transfer amount greater than 0.", "error");
            return;
        }

        DOM.btnTransferSubmit.disabled = true;
        DOM.btnTransferSubmit.textContent = "Processing...";

        try {
            const res = await fetch(`/accounts/${sourceAccountNumber}/transfer`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    destinationAccount: destinationAccount,
                    amount: amountVal
                })
            });

            const data = await res.json();

            if (!res.ok) {
                let msg = data.message || "Transfer failed.";
                if (msg.includes("Account not found with account number")) {
                    msg = `Destination account "${destinationAccount}" was not found. Please verify the account number.`;
                }
                showNotification(msg, "error");
                return;
            }

            showNotification(`Successfully transferred $${formatNumber(amountVal)} to A/C ${destinationAccount}`, "success");
            DOM.formTransfer.reset();

            // Refresh account data and transactions
            await loadCustomerDashboard(state.currentUser.customerId);

        } catch (err) {
            console.error("Transfer error:", err);
            showNotification("Transfer operation failed: " + err.message, "error");
        } finally {
            DOM.btnTransferSubmit.disabled = false;
            DOM.btnTransferSubmit.textContent = "Transfer Funds";
        }
    }

    // ==================== FORMATTING & HELPERS ====================
    function formatNumber(num) {
        if (num === null || num === undefined) return "0.00";
        const val = Number(num);
        if (isNaN(val)) return "0.00";
        return val.toLocaleString("en-US", {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        });
    }

    function formatDateTime(isoString) {
        if (!isoString) return "-";
        try {
            const date = new Date(isoString);
            if (isNaN(date.getTime())) return isoString;
            return date.toLocaleString("en-US", {
                year: "numeric",
                month: "short",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit"
            });
        } catch (e) {
            return isoString;
        }
    }

    function escapeHtml(str) {
        if (!str) return "";
        return str
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    // Initialize application when DOM is ready
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", init);
    } else {
        init();
    }

})();
