const { createApp, ref } = Vue;

const app = createApp({
  components: {
    HomePage: window.HomePage,
    ChatPage: window.ChatPage,
    DoodlePage: window.DoodlePage,
    StarMapPage: window.StarMapPage,
  },
  setup() {
    const route = ref('home');
    const visitor = ref({});

    function onMenu(key) {
      route.value = key;
    }

    // 初始化访客 session
    (async () => {
      try {
        const v = await TreeholeApi.ensureVisitor();
        visitor.value = v;
      } catch (e) {
        console.error('访客初始化失败', e);
      }
    })();

    return { route, visitor, onMenu };
  },
});

app.use(ElementPlus, { locale: ElementPlusLocaleZhCn });
app.mount('#app');
