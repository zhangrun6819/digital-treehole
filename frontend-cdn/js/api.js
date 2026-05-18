// 统一接口封装：根据 window.__TREEHOLE_API__ 决定后端地址，默认 http://localhost:8080
window.TreeholeApi = (function () {
  const baseURL = (window.__TREEHOLE_API__ || 'http://localhost:8080') + '/api/v1';
  const VISITOR_KEY = 'treehole.visitor';
  const ADMIN_KEY = 'treehole.admin';

  const http = axios.create({ baseURL, timeout: 15000 });

  http.interceptors.request.use((config) => {
    // 管理后台单独区分；否则用访客 token
    const isAdmin = (config.url || '').startsWith('/admin');
    const stored = isAdmin ? getAdmin() : getVisitor();
    if (stored && stored.token) {
      config.headers = config.headers || {};
      config.headers.Authorization = 'Bearer ' + stored.token;
    }
    return config;
  });

  http.interceptors.response.use(
    (resp) => {
      const body = resp.data;
      if (body && typeof body === 'object' && 'code' in body) {
        if (body.code !== 0) {
          const err = new Error(body.message || '请求失败');
          err.payload = body;
          return Promise.reject(err);
        }
        return body.data;
      }
      return body;
    },
    (err) => {
      if (err.response && err.response.data && err.response.data.message) {
        err.message = err.response.data.message;
      }
      return Promise.reject(err);
    }
  );

  // 访客 token 存取
  function getVisitor() {
    try { return JSON.parse(localStorage.getItem(VISITOR_KEY) || 'null'); } catch (e) { return null; }
  }
  function setVisitor(v) { localStorage.setItem(VISITOR_KEY, JSON.stringify(v)); }
  function clearVisitor() { localStorage.removeItem(VISITOR_KEY); }
  function getAdmin() {
    try { return JSON.parse(localStorage.getItem(ADMIN_KEY) || 'null'); } catch (e) { return null; }
  }
  function setAdmin(v) { localStorage.setItem(ADMIN_KEY, JSON.stringify(v)); }
  function clearAdmin() { localStorage.removeItem(ADMIN_KEY); }

  // 确保有访客 token；若无则创建
  async function ensureVisitor() {
    let v = getVisitor();
    if (v && v.token) return v;
    const data = await http.post('/visitors/session');
    setVisitor(data);
    return data;
  }

  return {
    http,
    getVisitor, setVisitor, clearVisitor,
    getAdmin, setAdmin, clearAdmin,
    ensureVisitor,

    // visitor
    createVisitor: () => http.post('/visitors/session'),
    refreshVisitor: () => http.post('/visitors/session/refresh'),

    // chat
    createSession: (payload) => http.post('/chat/sessions', payload),
    listSessions: (pageNo = 1, pageSize = 20) => http.get('/chat/sessions', { params: { pageNo, pageSize } }),
    getSession: (id) => http.get(`/chat/sessions/${id}`),
    listMessages: (id, pageNo = 1, pageSize = 50) => http.get(`/chat/sessions/${id}/messages`, { params: { pageNo, pageSize } }),
    sendMessage: (id, payload) => http.post(`/chat/sessions/${id}/messages`, payload),
    starMap: (days = 7) => http.get('/chat/stats/star-map', { params: { days } }),

    // doodle
    uploadDoodle: (file) => {
      const fd = new FormData();
      fd.append('file', file);
      return http.post('/doodles', fd, { headers: { 'Content-Type': 'multipart/form-data' } });
    },

    // admin
    adminLogin: (payload) => http.post('/admin/auth/login', payload),
    adminListRiskEvents: () => http.get('/admin/risk-events'),
    adminListContentRules: () => http.get('/admin/content-rules'),
    adminCreateContentRule: (payload) => http.post('/admin/content-rules', payload),
    adminUpdateContentRule: (id, payload) => http.put(`/admin/content-rules/${id}`, payload),
    adminListRiskRules: () => http.get('/admin/risk-rules'),
    adminCreateRiskRule: (payload) => http.post('/admin/risk-rules', payload),
    adminUpdateRiskRule: (id, payload) => http.put(`/admin/risk-rules/${id}`, payload),
    adminListSupport: () => http.get('/admin/support-resources'),
    adminCreateSupport: (payload) => http.post('/admin/support-resources', payload),
    adminUpdateSupport: (id, payload) => http.put(`/admin/support-resources/${id}`, payload),
  };
})();

// 情绪标签映射（与后端 EmotionTag 枚举保持一致）
window.EMOTION_MAP = {
  CALM:    { label: '平静',    color: '#7EC8E3' },
  SAD:     { label: '难过',    color: '#6C7BFF' },
  ANXIOUS: { label: '焦虑',    color: '#F6C177' },
  ANGRY:   { label: '生气',    color: '#FF6B6B' },
  HOPEFUL: { label: '有希望',  color: '#7BD389' },
};
window.COMPANION_STYLES = [
  { value: 'GENTLE_ELDER', label: '温柔长辈' },
  { value: 'PEER',         label: '同龄朋友' },
  { value: 'HUMOROUS',     label: '幽默陪伴' },
];
