window.HomePage = {
  name: 'HomePage',
  emits: ['go'],
  template: `
    <div class="page">
      <div class="hero">
        <h1>欢迎来到数字树洞</h1>
        <p>这里是一个匿名、安全的情绪栖息地。你可以打字诉说，也可以涂鸦表达；AI 树洞会用温柔的方式陪你聊一聊，<br/>
        同时把你的情绪汇成一片专属的"星空图"。</p>
        <div class="actions">
          <el-button type="primary" size="large" @click="$emit('go','chat')">开始聊天</el-button>
          <el-button size="large" @click="$emit('go','doodle')">心情涂鸦</el-button>
          <el-button size="large" @click="$emit('go','starmap')">查看星空图</el-button>
        </div>
      </div>
      <div class="feature-grid">
        <div class="feature-card" @click="$emit('go','chat')">
          <h3>📖 真多轮对话</h3>
          <p>AI 树洞每次回复会带上最近 6 条历史，听得懂上下文，不会答非所问。</p>
        </div>
        <div class="feature-card" @click="$emit('go','doodle')">
          <h3>🎨 引导式涂鸦</h3>
          <p>不擅长打字时，画一画就好。配上情绪标签和一句短说明，AI 也能给你共情回应。</p>
        </div>
        <div class="feature-card" @click="$emit('go','starmap')">
          <h3>✨ 情感星空图</h3>
          <p>最近 7 天的情绪都会变成一颗颗星，越频繁的感受星星越大；一眼看见自己的状态。</p>
        </div>
        <div class="feature-card">
          <h3>🛡️ 内容审核 & 危机预警</h3>
          <p>不良内容会被拦截或标记；一旦发现极端高风险表达，立刻附上心理援助热线。</p>
        </div>
      </div>
    </div>
  `,
};
