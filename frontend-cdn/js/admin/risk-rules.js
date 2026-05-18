window.AdminRiskRules = {
  name: 'AdminRiskRules',
  data() {
    return {
      list: [],
      loading: false,
      dialog: false,
      editId: null,
      form: { keyword: '', riskLevel: 'HIGH', enabled: true },
      saving: false,
      riskLevels: [
        { value: 'HIGH',   label: '高危 HIGH' },
        { value: 'MEDIUM', label: '中危 MEDIUM' },
        { value: 'LOW',    label: '低危 LOW' },
      ],
    };
  },
  mounted() { this.load(); },
  methods: {
    async load() {
      this.loading = true;
      try {
        this.list = await TreeholeApi.adminListRiskRules();
      } catch (e) {
        ElementPlus.ElMessage.error(e.message);
      } finally {
        this.loading = false;
      }
    },
    openCreate() {
      this.editId = null;
      this.form = { keyword: '', riskLevel: 'HIGH', enabled: true };
      this.dialog = true;
    },
    openEdit(row) {
      this.editId = row.id;
      this.form = { keyword: row.keyword, riskLevel: row.riskLevel, enabled: row.enabled };
      this.dialog = true;
    },
    async save() {
      if (!this.form.keyword.trim()) { ElementPlus.ElMessage.warning('关键词不能为空'); return; }
      this.saving = true;
      try {
        if (this.editId) {
          await TreeholeApi.adminUpdateRiskRule(this.editId, this.form);
        } else {
          await TreeholeApi.adminCreateRiskRule(this.form);
        }
        this.dialog = false;
        await this.load();
        ElementPlus.ElMessage.success('保存成功');
      } catch (e) {
        ElementPlus.ElMessage.error(e.message);
      } finally {
        this.saving = false;
      }
    },
    riskColor(level) {
      return { HIGH: '#ff6b6b', MEDIUM: '#f6c177', LOW: '#7bd389' }[level] || '#aab0d2';
    },
  },
  template: `
    <div class="admin-page">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
        <h2 style="margin:0;">危机预警规则</h2>
        <div style="display:flex;gap:8px;">
          <el-button @click="load" :loading="loading">刷新</el-button>
          <el-button type="primary" @click="openCreate">+ 新建</el-button>
        </div>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="keyword" label="关键词" />
        <el-table-column prop="riskLevel" label="风险等级" width="120">
          <template #default="{row}">
            <el-tag :style="{ background: riskColor(row.riskLevel)+'33', borderColor: riskColor(row.riskLevel), color: riskColor(row.riskLevel) }">
              {{ row.riskLevel }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enabled" label="启用" width="80">
          <template #default="{row}">
            <el-tag :type="row.enabled ? 'success' : 'info'" size="small">{{ row.enabled ? '是' : '否' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{row}">
            <el-button size="small" @click="openEdit(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialog" :title="editId ? '编辑预警规则' : '新建预警规则'" width="420px">
        <el-form :model="form" label-width="80px">
          <el-form-item label="关键词">
            <el-input v-model="form.keyword" maxlength="200" />
          </el-form-item>
          <el-form-item label="风险等级">
            <el-select v-model="form.riskLevel" style="width:100%;">
              <el-option v-for="o in riskLevels" :key="o.value" :value="o.value" :label="o.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="启用">
            <el-switch v-model="form.enabled" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialog = false">取消</el-button>
          <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        </template>
      </el-dialog>
    </div>
  `,
};
