window.AdminLogin = {
  name: 'AdminLogin',
  emits: ['login'],
  data() {
    return {
      form: { username: 'admin', password: '' },
      loading: false,
    };
  },
  methods: {
    async submit() {
      if (!this.form.username || !this.form.password) {
        ElementPlus.ElMessage.warning('请输入账号和密码');
        return;
      }
      this.loading = true;
      try {
        const data = await TreeholeApi.adminLogin(this.form);
        TreeholeApi.setAdmin(data);
        this.$emit('login', data);
      } catch (e) {
        ElementPlus.ElMessage.error('登录失败：' + e.message);
      } finally {
        this.loading = false;
      }
    },
  },
  template: `
    <div class="login-box">
      <div class="login-card">
        <div class="brand" style="margin-bottom:24px;">
          <div class="brand-dot"></div>
          <span>数字树洞 · 管理后台</span>
        </div>
        <h2 style="margin:0 0 24px;">管理员登录</h2>
        <el-form @submit.prevent="submit">
          <el-form-item>
            <el-input v-model="form.username" placeholder="账号" prefix-icon="User" />
          </el-form-item>
          <el-form-item>
            <el-input v-model="form.password" type="password" placeholder="密码" prefix-icon="Lock"
              @keydown.enter="submit" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" style="width:100%;" :loading="loading" @click="submit">登录</el-button>
          </el-form-item>
        </el-form>
        <p style="color:#aab0d2;font-size:12px;text-align:center;margin:0;">默认账号 admin / admin123456</p>
      </div>
    </div>
  `,
};
