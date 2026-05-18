window.AdminContentRules = {
  name: 'AdminContentRules',
  data() {
    return {
      list: [],
      loading: false,
      dialog: false,
      editId: null,
      form: { keyword: '', category: '', action: 'BLOCK', enabled: true },
      saving: false,
      categoryOptions: ['辱骂', '色情', '暴力', '骚扰', '其他'],
      actionOptions: [{ value: 'BLOCK', label: '拦截 BLOCK' }, { value: 'MARK', label: '标记 MARK' }],
    };
  },
  mounted() { this.load(); },
  methods: {
    async load() {
      this.loading = true;
      try {
        this.list = await TreeholeApi.adminListContentRules();
      } catch (e) {
        ElementPlus.ElMessage.error(e.message);
      } finally {
        this.loading = false;
      }
    },
    openCreate() {
      this.editId = null;
      this.form = { keyword: '', category: '辱骂', action: 'BLOCK', enabled: true };
      this.dialog = true;
    },
    openEdit(row) {
      this.editId = row.id;
      this.form = { keyword: row.keyword, category: row.category, action: row.action, enabled: row.enabled };
      this.dialog = true;
    },
    async save() {
      if (!this.form.keyword.trim()) { ElementPlus.ElMessage.warning('关键词不能为空'); return; }
      this.saving = true;
      try {
        if (this.editId) {
          await TreeholeApi.adminUpdateContentRule(this.editId, this.form);
        } else {
          await TreeholeApi.adminCreateContentRule(this.form);
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
  },
  template: `
    <div class="admin-page">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;">
        <h2 style="margin:0;">内容审核规则</h2>
        <div style="display:flex;gap:8px;">
          <el-button @click="load" :loading="loading">刷新</el-button>
          <el-button type="primary" @click="openCreate">+ 新建</el-button>
        </div>
      </div>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="keyword" label="关键词" />
        <el-table-column prop="category" label="分类" width="100" />
        <el-table-column prop="action" label="动作" width="100">
          <template #default="{row}">
            <el-tag :type="row.action === 'BLOCK' ? 'danger' : 'warning'" size="small">{{ row.action }}</el-tag>
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

      <el-dialog v-model="dialog" :title="editId ? '编辑内容规则' : '新建内容规则'" width="420px">
        <el-form :model="form" label-width="80px">
          <el-form-item label="关键词">
            <el-input v-model="form.keyword" maxlength="200" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category" style="width:100%;" allow-create filterable>
              <el-option v-for="c in categoryOptions" :key="c" :value="c" :label="c" />
            </el-select>
          </el-form-item>
          <el-form-item label="动作">
            <el-select v-model="form.action" style="width:100%;">
              <el-option v-for="o in actionOptions" :key="o.value" :value="o.value" :label="o.label" />
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
