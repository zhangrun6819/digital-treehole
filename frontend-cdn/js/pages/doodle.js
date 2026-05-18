window.DoodlePage = {
  name: 'DoodlePage',
  data() {
    return {
      color: '#FF6B6B',
      lineWidth: 4,
      drawing: false,
      lastX: 0, lastY: 0,
      ctx: null,
      colors: ['#FF6B6B', '#F6C177', '#7BD389', '#7EC8E3', '#6C7BFF', '#000000'],
      emotion: '',
      shortNote: '',
      uploaded: null,
      uploading: false,
      sessionId: null,
      sessions: [],
      sending: false,
      emotionOptions: Object.entries(window.EMOTION_MAP).map(([v, m]) => ({ value: v, label: m.label, color: m.color })),
    };
  },
  async mounted() {
    try {
      await TreeholeApi.ensureVisitor();
      const data = await TreeholeApi.listSessions(1, 30);
      this.sessions = data.records || [];
      if (this.sessions[0]) this.sessionId = this.sessions[0].id;
    } catch (e) {
      ElementPlus.ElMessage.error(e.message);
    }
    const cv = this.$refs.cv;
    cv.width = 800; cv.height = 600;
    this.ctx = cv.getContext('2d');
    this.ctx.lineCap = 'round';
    this.ctx.lineJoin = 'round';
    this.fill('#ffffff');
    cv.addEventListener('pointerdown', this.start);
    cv.addEventListener('pointermove', this.move);
    cv.addEventListener('pointerup', this.end);
    cv.addEventListener('pointerleave', this.end);
  },
  beforeUnmount() {
    const cv = this.$refs.cv;
    if (cv) {
      cv.removeEventListener('pointerdown', this.start);
      cv.removeEventListener('pointermove', this.move);
      cv.removeEventListener('pointerup', this.end);
      cv.removeEventListener('pointerleave', this.end);
    }
  },
  methods: {
    fill(color) {
      const cv = this.$refs.cv;
      this.ctx.fillStyle = color;
      this.ctx.fillRect(0, 0, cv.width, cv.height);
    },
    pos(e) {
      const cv = this.$refs.cv;
      const rect = cv.getBoundingClientRect();
      const sx = cv.width / rect.width;
      const sy = cv.height / rect.height;
      return { x: (e.clientX - rect.left) * sx, y: (e.clientY - rect.top) * sy };
    },
    start(e) {
      this.drawing = true;
      const p = this.pos(e); this.lastX = p.x; this.lastY = p.y;
    },
    move(e) {
      if (!this.drawing) return;
      const p = this.pos(e);
      this.ctx.strokeStyle = this.color;
      this.ctx.lineWidth = this.lineWidth;
      this.ctx.beginPath();
      this.ctx.moveTo(this.lastX, this.lastY);
      this.ctx.lineTo(p.x, p.y);
      this.ctx.stroke();
      this.lastX = p.x; this.lastY = p.y;
    },
    end() { this.drawing = false; },
    clear() { this.fill('#ffffff'); this.uploaded = null; },
    async upload() {
      const cv = this.$refs.cv;
      this.uploading = true;
      try {
        const blob = await new Promise((resolve) => cv.toBlob(resolve, 'image/png'));
        const file = new File([blob], 'doodle.png', { type: 'image/png' });
        if (file.size > 2 * 1024 * 1024) throw new Error('涂鸦超过 2MB');
        const data = await TreeholeApi.uploadDoodle(file);
        this.uploaded = data;
        ElementPlus.ElMessage.success('涂鸦上传成功，可提交到对话');
      } catch (e) {
        ElementPlus.ElMessage.error('上传失败：' + e.message);
      } finally {
        this.uploading = false;
      }
    },
    async sendToSession() {
      if (!this.uploaded) { ElementPlus.ElMessage.warning('请先上传涂鸦'); return; }
      if (!this.emotion) { ElementPlus.ElMessage.warning('请选择情绪标签'); return; }
      if (!this.sessionId) { ElementPlus.ElMessage.warning('请选择要发送到的会话'); return; }
      this.sending = true;
      try {
        await TreeholeApi.sendMessage(this.sessionId, {
          inputType: 'DOODLE',
          shortNote: this.shortNote || null,
          emotionTag: this.emotion,
          doodleAssetId: this.uploaded.doodleAssetId,
        });
        ElementPlus.ElMessage.success('已发送到会话，可去聊天页查看回复');
        this.uploaded = null;
        this.clear();
      } catch (e) {
        ElementPlus.ElMessage.error('发送失败：' + e.message);
      } finally {
        this.sending = false;
      }
    },
    async createSessionFor() {
      try {
        const s = await TreeholeApi.createSession({ companionStyle: 'PEER', title: '心情涂鸦' });
        const data = await TreeholeApi.listSessions(1, 30);
        this.sessions = data.records || [];
        this.sessionId = s.id;
        ElementPlus.ElMessage.success('已为涂鸦创建新会话');
      } catch (e) { ElementPlus.ElMessage.error(e.message); }
    },
  },
  template: `
    <div class="page">
      <h2>心情涂鸦</h2>
      <p style="color:#aab0d2;">不擅长打字时画一笔。涂鸦只支持 PNG，体积 ≤ 2MB，分辨率 ≤ 1024×1024。</p>

      <div class="doodle-canvas-wrap">
        <canvas ref="cv" style="aspect-ratio:4/3;"></canvas>
        <div class="doodle-toolbar">
          <span style="color:#aab0d2;">颜色：</span>
          <span v-for="c in colors" :key="c"
                class="swatch" :class="{ active: c === color }"
                :style="{ background: c }" @click="color = c"></span>
          <el-divider direction="vertical" />
          <span style="color:#aab0d2;">粗细：</span>
          <el-slider v-model="lineWidth" :min="1" :max="20" style="width:160px;" />
          <el-divider direction="vertical" />
          <el-button @click="clear">清空</el-button>
          <el-button type="primary" :loading="uploading" @click="upload">上传</el-button>
        </div>

        <el-divider />

        <h4>提交到对话</h4>
        <el-form label-width="100px" style="max-width:600px;">
          <el-form-item label="情绪标签">
            <el-select v-model="emotion" placeholder="必选" style="width:100%;">
              <el-option v-for="o in emotionOptions" :key="o.value" :value="o.value" :label="o.label">
                <span :style="{color:o.color}">● {{ o.label }}</span>
              </el-option>
            </el-select>
          </el-form-item>
          <el-form-item label="短说明">
            <el-input v-model="shortNote" placeholder="可选，≤500字" maxlength="500" />
          </el-form-item>
          <el-form-item label="目标会话">
            <div style="display:flex;gap:8px;width:100%;">
              <el-select v-model="sessionId" placeholder="选择会话" style="flex:1;">
                <el-option v-for="s in sessions" :key="s.id" :value="s.id" :label="s.title || ('会话 #' + s.id)" />
              </el-select>
              <el-button @click="createSessionFor">+ 新建会话</el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="sending" @click="sendToSession">发送涂鸦消息</el-button>
            <span v-if="uploaded" style="margin-left:12px;color:#7BD389;">✔ 已上传 · {{ Math.round(uploaded.fileSize / 1024) }}KB · {{ uploaded.width }}×{{ uploaded.height }}</span>
          </el-form-item>
        </el-form>
      </div>
    </div>
  `,
};
