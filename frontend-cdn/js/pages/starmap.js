window.StarMapPage = {
  name: 'StarMapPage',
  data() {
    return {
      days: 7,
      data: null,
      loading: false,
      emotionMap: window.EMOTION_MAP,
    };
  },
  async mounted() {
    try {
      await TreeholeApi.ensureVisitor();
      await this.load();
    } catch (e) {
      ElementPlus.ElMessage.error(e.message);
    }
  },
  methods: {
    async load() {
      this.loading = true;
      try {
        this.data = await TreeholeApi.starMap(this.days);
      } catch (e) {
        ElementPlus.ElMessage.error('加载失败：' + e.message);
      } finally {
        this.loading = false;
      }
    },
    dominantLabel() {
      if (!this.data) return '';
      const tag = this.data.dominantEmotion;
      return this.emotionMap[tag] ? this.emotionMap[tag].label : tag;
    },
    dominantColor() {
      if (!this.data) return '#7EC8E3';
      const tag = this.data.dominantEmotion;
      return this.emotionMap[tag] ? this.emotionMap[tag].color : '#7EC8E3';
    },
    uniqueEmotions() {
      if (!this.data || !this.data.points) return [];
      const seen = new Set();
      const result = [];
      for (const p of this.data.points) {
        if (!seen.has(p.emotionTag)) {
          seen.add(p.emotionTag);
          result.push({ tag: p.emotionTag, color: p.color, label: p.label });
        }
      }
      return result;
    },
    // Sparkle effect: SVG twinkle animation offset per star
    twinkleDelay(i) {
      return (i * 0.37) % 3;
    },
  },
  template: `
    <div class="page">
      <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:16px;flex-wrap:wrap;gap:12px;">
        <div>
          <h2 style="margin:0;">情感星空图</h2>
          <p style="color:#aab0d2;margin:4px 0 0;">每一颗星代表你说出的一次感受，星越大代表这类情绪越频繁。</p>
        </div>
        <div style="display:flex;gap:8px;align-items:center;">
          <span style="color:#aab0d2;">最近</span>
          <el-select v-model="days" style="width:100px;" @change="load">
            <el-option :value="3" label="3 天" />
            <el-option :value="7" label="7 天" />
            <el-option :value="14" label="14 天" />
            <el-option :value="30" label="30 天" />
          </el-select>
          <el-button @click="load" :loading="loading">刷新</el-button>
        </div>
      </div>

      <div class="star-map-board" v-loading="loading">
        <template v-if="data && data.points && data.points.length">
          <div style="display:flex;align-items:center;gap:16px;margin-bottom:12px;flex-wrap:wrap;">
            <div>
              <span style="color:#aab0d2;font-size:13px;">主导情绪</span>
              <el-tag :style="{ background: dominantColor()+'33', borderColor: dominantColor(), color: dominantColor(), marginLeft:'8px' }">
                {{ dominantLabel() }}
              </el-tag>
            </div>
            <div style="color:#aab0d2;font-size:12px;">{{ data.periodStart }} ~ {{ data.periodEnd }}</div>
          </div>

          <p class="star-summary">{{ data.summaryText }}</p>

          <svg class="star-map-svg" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
            <!-- Background stars (decorative) -->
            <rect width="100" height="100" fill="transparent" />
            <circle v-for="(p, i) in data.points" :key="p.emotionTag + i"
              :cx="p.x" :cy="p.y" :r="p.radius"
              :fill="p.color"
              :opacity="0.7 + p.intensity * 0.3"
              :style="{ filter: 'drop-shadow(0 0 ' + (p.radius * 1.5) + 'px ' + p.color + '99)' }"
            >
              <animate attributeName="opacity"
                :values="(0.6 + p.intensity * 0.3) + ';' + (0.9 + p.intensity * 0.1) + ';' + (0.6 + p.intensity * 0.3)"
                dur="3s" :begin="twinkleDelay(i) + 's'" repeatCount="indefinite" />
            </circle>
            <!-- Labels on hover via title -->
            <circle v-for="(p, i) in data.points" :key="'hit' + i"
              :cx="p.x" :cy="p.y" :r="p.radius + 2"
              fill="transparent" style="cursor:pointer;">
              <title>{{ p.label }} · {{ p.sourceDate }} · 强度 {{ Math.round(p.intensity * 100) }}%</title>
            </circle>
          </svg>

          <div class="star-legend">
            <div v-for="e in uniqueEmotions()" :key="e.tag" class="item">
              <div class="dot" :style="{ background: e.color, boxShadow: '0 0 6px ' + e.color + '88' }"></div>
              <span style="font-size:13px;color:#ccc;">{{ e.label }}</span>
            </div>
          </div>
        </template>

        <div v-else-if="data && (!data.points || !data.points.length)" style="text-align:center;padding:60px 0;color:#aab0d2;">
          <div style="font-size:48px;margin-bottom:16px;">🌌</div>
          <p>{{ data.summaryText || '最近还没有情绪记录，去聊几句吧～' }}</p>
        </div>

        <div v-else-if="!loading" style="text-align:center;padding:60px 0;color:#aab0d2;">
          <div style="font-size:48px;margin-bottom:16px;">✨</div>
          <p>点击刷新加载你的星空图</p>
        </div>
      </div>
    </div>
  `,
};
