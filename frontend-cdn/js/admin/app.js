const { createApp, ref } = Vue;

const adminApp = createApp({
  components: {
    AdminLogin: window.AdminLogin,
    AdminRiskEvents: window.AdminRiskEvents,
    AdminContentRules: window.AdminContentRules,
    AdminRiskRules: window.AdminRiskRules,
    AdminSupportResources: window.AdminSupportResources,
  },
  setup() {
    const stored = TreeholeApi.getAdmin();
    const loggedIn = ref(!!(stored && stored.token));
    const admin = ref(stored || {});
    const tab = ref('risk');

    function onLogin(data) {
      admin.value = data;
      loggedIn.value = true;
    }

    function logout() {
      TreeholeApi.clearAdmin();
      loggedIn.value = false;
      admin.value = {};
    }

    return { loggedIn, admin, tab, onLogin, logout };
  },
});

adminApp.use(ElementPlus, { locale: ElementPlusLocaleZhCn });
adminApp.mount('#admin');
