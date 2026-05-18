window.AdminSupportResources = {
  name: 'AdminSupportResources',
  data() {
    return {
      list: [],
      loading: false,
      dialog: false,
      editId: null,
      form: { title: '', contact: '', description: '', enabled: true },
      saving: false,
    };
  },
  mounted() { this.load(); },
  methods: {
    async load() {
      this.loading = true;
      try {
        this.list = await TreeholeApi.adminListSupport();
      } catch (e) {
        ElementPlus.ElMessage.error(e.message);
      } finally {
        this.loading = false;
      }
    },
    openCreate() {
      this.editId = null;
      this.form = { title: '', contact: '', description: '', enabled: true };
      this.dialog = true;
    },
    openEdit(row) {
      this.editId = row.id;
      this.form = { title: row.title, contact: row.contact, description: row.description || '', enabled: row.enabled };
      this.dialog = true;
    },
    async save() {
      if (!this.form.title.trim() || !this.form.contact.trim()) {
        ElementPlus.ElMessage.warning('标题和联系方式不能为空');
        return;
      }
      this.saving = true;
      try {
        if (this.editId) {
          await TreeholeApi.adminUpdateSupport(this.editId, this.form);
        } else {
          await TreeholeApi.adminCreateSupport(this.form);
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
        <h2 style="margin:0;">心理援助资源</h2>
        <div style="display:flex;gap:8px;">
          <el-button @click="load" :loading="loading">刷新</el-button>
          <el-button type="primary" @click="openCreate">+ 新建</el-button>
        </div>
      </div>
      <p style="color:#aab0d2;font-size:13px;margin-top:0;">当用户触发中/高危风险规则时，系统会自动在回复中附上已启用的援助资源联系方式。</p>
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="title" label="名称" />
        <el-table-column prop="contact" label="联系方式" />
        <el-table-column prop="description" label="说明" show-overflow-tooltip />
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

      <el-dialog v-model="dialog" :title="editId ? '编辑援助资源' : '新建援助资源'" width="460px">
        <el-form :model="form" label-width="90px">
          <el-form-item label="名称">
            <el-input v-model="form.title" maxlength="100" />
          </el-form-item>
          <el-form-item label="联系方式">
            <el-input v-model="form.contact" placeholder="电话 / 网址 / 公众号" maxlength="200" />
          </el-form-item>
          <el-form-item label="说明">
            <el-input v-model="form.description" type="textarea" :rows="2" maxlength="500" />
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
