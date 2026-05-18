window.AdminRiskEvents = {
  name: 'AdminRiskEvents',
  data() {
    return {
      list: [],
      loading: false,
    };
  },
  mounted() { this.load(); },
  methods: {
    async load() {
      this.loading = true;
      try {
        this.list = await TreeholeApi.adminListRiskEvents();
      } catch (e) {
        ElementPlus.ElMessage.error(e.message);
      } finally {
        this.loading = false;
      }
    },
    riskColor(level) {
      return { HIGH: '#ff6b6b', MEDIUM: '#f6c177', LOW: '#7bd389', NONE: '#aab0d2' }[level] || '#aab0d2';
    },
    formatTime(s) {
      if (!s) return '';
      const d = new Date(s);
      const pad = n => String(n).padStart(2, '0');
      return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
    },
  },
  template: `
    <div class="admin-page">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
        <h2 style="margin:0;">风险事件</h2>
        <el-button @click="load" :loading="loading">刷新</el-button>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="visitorId" label="访客 ID" width="90" />
        <el-table-column prop="sessionId" label="会话 ID" width="90" />
        <el-table-column prop="messageId" label="消息 ID" width="90" />
        <el-table-column prop="riskLevel" label="风险等级" width="110">
          <template #default="{row}">
            <el-tag :style="{ background: riskColor(row.riskLevel)+'33', borderColor: riskColor(row.riskLevel), color: riskColor(row.riskLevel) }">
              {{ row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="matchedKeyword" label="触发关键词" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="{row}">
            <el-tag :type="row.status === 'OPEN' ? 'danger' : 'success'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="触发时间" width="160">
          <template #default="{row}">{{ formatTime(row.createdAt) }}</template>
        </el-table-column>
      </el-table>
      <div v-if="!loading && !list.length" style="text-align:center;padding:40px;color:#aab0d2;">暂无风险事件</div>
    </div>
  `,
};
