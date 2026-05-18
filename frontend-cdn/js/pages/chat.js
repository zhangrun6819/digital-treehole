window.ChatPage = {
  name: 'ChatPage',
  data() {
    return {
      sessions: [],
      activeId: null,
      messages: [],
      loadingMsg: false,
      sending: false,
      input: '',
      emotion: '',
      shortNote: '',
      mode: 'TEXT', // TEXT or DOODLE
      doodleAsset: null,
      doodleFile: null,
      doodlePreview: '',
      newDialog: false,
      newForm: { companionStyle: 'PEER', title: '' },
      latestSupport: [],
      latestRisk: 'NONE',
      latestReframe: '',
      latestFollowUp: '',
      companionStyles: window.COMPANION_STYLES,
      emotionOptions: Object.entries(window.EMOTION_MAP).map(([v, m]) => ({ value: v, label: m.label, color: m.color })),
    };
  },
  async mounted() {
    try {
      await TreeholeApi.ensureVisitor();
      await this.loadSessions();
      if (this.sessions.length === 0) {
        this.newDialog = true;
      } else {
        this.selectSession(this.sessions[0].id);
      }
    } catch (err) {
      ElementPlus.ElMessage.error('初始化失败：' + err.message);
    }
  },
  methods: {
    async loadSessions() {
      const data = await TreeholeApi.listSessions(1, 30);
      this.sessions = data.records || [];
    },
    async selectSession(id) {
      this.activeId = id;
      this.loadingMsg = true;
      this.latestSupport = [];
      try {
        const data = await TreeholeApi.listMessages(id, 1, 100);
        this.messages = data.records || [];
        this.$nextTick(this.scrollBottom);
      } catch (err) {
        ElementPlus.ElMessage.error(err.message);
      } finally {
        this.loadingMsg = false;
      }
    },
    async createNewSession() {
      try {
        const session = await TreeholeApi.createSession({
          companionStyle: this.newForm.companionStyle,
          title: this.newForm.title || null,
        });
        this.newDialog = false;
        this.newForm.title = '';
        await this.loadSessions();
        this.selectSession(session.id);
      } catch (err) {
        ElementPlus.ElMessage.error(err.message);
      }
    },
    scrollBottom() {
      const el = this.$refs.scroll;
      if (el) el.scrollTop = el.scrollHeight;
    },
    onPickFile(e) {
      const f = e.target.files && e.target.files[0];
      if (!f) return;
      if (f.type !== 'image/png') {
        ElementPlus.ElMessage.error('涂鸦只支持 PNG 格式');
        return;
      }
      this.doodleFile = f;
      this.doodlePreview = URL.createObjectURL(f);
    },
    async uploadDoodle() {
      if (!this.doodleFile) return null;
      try {
        const data = await TreeholeApi.uploadDoodle(this.doodleFile);
        this.doodleAsset = data;
        ElementPlus.ElMessage.success('涂鸦上传成功');
        return data;
      } catch (err) {
        ElementPlus.ElMessage.error('涂鸦上传失败：' + err.message);
        return null;
      }
    },
    async send() {
      if (!this.activeId) {
        ElementPlus.ElMessage.warning('请先选择或创建一个会话');
        return;
      }
      if (this.mode === 'TEXT') {
        if (!this.input.trim()) { ElementPlus.ElMessage.warning('请输入内容'); return; }
      } else {
        if (!this.doodleAsset) {
          if (!this.doodleFile) { ElementPlus.ElMessage.warning('请选择 PNG 涂鸦'); return; }
          const a = await this.uploadDoodle();
          if (!a) return;
        }
        if (!this.emotion) { ElementPlus.ElMessage.warning('涂鸦消息必须选择情绪标签'); return; }
      }

      this.sending = true;
      try {
        const payload = {
          inputType: this.mode,
          content: this.mode === 'TEXT' ? this.input.trim() : null,
          shortNote: this.shortNote || null,
          emotionTag: this.emotion || null,
          doodleAssetId: this.mode === 'DOODLE' ? this.doodleAsset.doodleAssetId : null,
        };
        const resp = await TreeholeApi.sendMessage(this.activeId, payload);
        this.messages.push(resp.userMessage);
        this.messages.push(resp.assistantMessage);
        this.latestSupport = resp.supportResources || [];
        this.latestRisk = resp.riskLevel;
        this.latestReframe = resp.reframeText;
        this.latestFollowUp = resp.followUpQuestion;
        this.input = '';
        this.shortNote = '';
        this.doodleAsset = null;
        this.doodleFile = null;
        this.doodlePreview = '';
        if (this.$refs.fileInput) this.$refs.fileInput.value = '';
        await this.loadSessions();
        this.$nextTick(this.scrollBottom);
      } catch (err) {
        ElementPlus.ElMessage.error('发送失败：' + err.message);
      } finally {
        this.sending = false;
      }
    },
    emotionLabel(v) { return window.EMOTION_MAP[v] ? window.EMOTION_MAP[v].label : v; },
    emotionColor(v) { return window.EMOTION_MAP[v] ? window.EMOTION_MAP[v].color : '#888'; },
    formatTime(s) {
      if (!s) return '';
      const d = new Date(s);
      const pad = (n) => String(n).padStart(2, '0');
      return `${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
    },
  },
  template: `
    <div class="page" style="max-width:1200px;">
      <div class="chat-layout">
        <div class="chat-sidebar">
          <el-button class="new-btn" type="primary" @click="newDialog = true">+ 新会话</el-button>
          <div class="session-list">
            <div v-for="s in sessions" :key="s.id" class="session-item"
                 :class="{ active: s.id === activeId }" @click="selectSession(s.id)">
              <h4>{{ s.title || '新的树洞对话' }}</h4>
              <small>{{ formatTime(s.updatedAt) }}</small>
              <div v-if="s.latestEmotionTag">
                <el-tag size="small" :style="{ background: emotionColor(s.latestEmotionTag) + '33', borderColor: emotionColor(s.latestEmotionTag), color: emotionColor(s.latestEmotionTag) }">
                  {{ emotionLabel(s.latestEmotionTag) }}
                </el-tag>
              </div>
            </div>
            <p v-if="!sessions.length" style="color:#aab0d2;text-align:center;margin-top:20px;">还没有会话</p>
          </div>
        </div>

        <div class="chat-main">
          <div class="chat-messages" ref="scroll" v-loading="loadingMsg">
            <div v-if="!messages.length && !loadingMsg" style="text-align:center;color:#aab0d2;margin-top:40px;">
              开始你的第一句话吧 ✨
            </div>
            <div v-for="m in messages" :key="m.id" class="msg" :class="m.role === 'USER' ? 'user' : 'assistant'">
              <div>
                <div class="bubble">{{ m.content || (m.shortNote ? '【涂鸦】' + m.shortNote : '【涂鸦】') }}</div>
                <div class="meta">
                  <span v-if="m.emotionTag" :style="{ color: emotionColor(m.emotionTag) }">● {{ emotionLabel(m.emotionTag) }}</span>
                  <span> · {{ formatTime(m.createdAt) }}</span>
                  <span v-if="m.providerType"> · {{ m.providerType }}</span>
                  <span v-if="m.moderationStatus && m.moderationStatus !== 'PASS'"> · 审核：{{ m.moderationStatus }}</span>
                  <span v-if="m.riskLevel && m.riskLevel !== 'NONE'" style="color:#ff8f8f;"> · 风险：{{ m.riskLevel }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="latestSupport.length" class="support-tip">
            <h5>⚠️ 我们注意到你可能正在经历困难，可以联系：</h5>
            <ul>
              <li v-for="r in latestSupport" :key="r.id"><strong>{{ r.title }}</strong> · {{ r.contact }}<span v-if="r.description"> · {{ r.description }}</span></li>
            </ul>
          </div>

          <div class="chat-input">
            <div class="toolbar">
              <el-radio-group v-model="mode" size="small">
                <el-radio-button label="TEXT">📝 文字</el-radio-button>
                <el-radio-button label="DOODLE">🎨 涂鸦</el-radio-button>
              </el-radio-group>
              <el-select v-model="emotion" placeholder="情绪标签（涂鸦必选）" size="small" clearable style="width:160px;">
                <el-option v-for="o in emotionOptions" :key="o.value" :value="o.value" :label="o.label">
                  <span :style="{ color: o.color }">● {{ o.label }}</span>
                </el-option>
              </el-select>
              <el-input v-model="shortNote" size="small" placeholder="可选短说明（≤500字）" maxlength="500" style="flex:1;min-width:200px;" />
            </div>

            <div v-if="mode === 'TEXT'">
              <el-input
                v-model="input" type="textarea" :rows="3"
                placeholder="把你想说的告诉树洞…（Enter 发送，Shift+Enter 换行）" maxlength="2000" show-word-limit
                @keydown.enter.exact.prevent="send"
                @keydown.shift.enter.exact.stop
              />
            </div>
            <div v-else>
              <input ref="fileInput" type="file" accept="image/png" @change="onPickFile" />
              <div v-if="doodlePreview"><img :src="doodlePreview" style="max-width:200px;max-height:200px;margin-top:8px;border-radius:8px;" /></div>
              <p v-else style="color:#aab0d2;font-size:12px;">或者去 <el-link type="primary" :underline="false" href="javascript:;" @click="$root.onMenu('doodle')">涂鸦页</el-link> 画好后回来发送</p>
            </div>

            <div style="text-align:right;">
              <el-button type="primary" :loading="sending" @click="send">发送</el-button>
            </div>

            <div v-if="latestReframe || latestFollowUp" style="font-size:12px;color:#aab0d2;border-left:3px solid #7ec8e3;padding-left:10px;">
              <p v-if="latestReframe" style="margin:4px 0;">💡 {{ latestReframe }}</p>
              <p v-if="latestFollowUp" style="margin:4px 0;">❓ {{ latestFollowUp }}</p>
            </div>
          </div>
        </div>
      </div>

      <el-dialog v-model="newDialog" title="开启新的树洞对话" width="420px">
        <el-form label-width="90px">
          <el-form-item label="陪伴风格">
            <el-select v-model="newForm.companionStyle" style="width:100%;">
              <el-option v-for="o in companionStyles" :key="o.value" :value="o.value" :label="o.label" />
            </el-select>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="newForm.title" placeholder="可选，留空就叫'新的树洞对话'" maxlength="120" />
          </el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="newDialog = false">取消</el-button>
          <el-button type="primary" @click="createNewSession">创建</el-button>
        </template>
      </el-dialog>
    </div>
  `,
};
