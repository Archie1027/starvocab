import { useEffect, useState } from 'react'
import { Link, useOutletContext } from 'react-router-dom'
import { NebulaOverview, TodayQueue, nebulaOverview, nebulaConstellation, todayQueue } from '../api/modules'
import OrbitScene from '../demo/OrbitScene'

/** 今日任务：真实队列 + 星云概览 */
export default function TodayPage() {
  const { motion } = useOutletContext<{ motion: boolean }>()
  const [queue, setQueue] = useState<TodayQueue | null>(null)
  const [overview, setOverview] = useState<NebulaOverview | null>(null)
  const [lit, setLit] = useState(0)
  const [error, setError] = useState('')

  useEffect(() => {
    document.title = '今日任务 · 星词 StarVocab'
    todayQueue().then(r => setQueue(r.data)).catch(e => setError(e.message))
    nebulaOverview().then(r => setOverview(r.data)).catch(() => {})
    nebulaConstellation().then(r => setLit(r.data.litStars)).catch(() => {})
  }, [])

  const goal = queue ? queue.queue.length : 0

  return <main id="main" className="demo-main demo-today" tabIndex={-1}>
    <div className="demo-page-heading"><div>
      <div className="demo-overline"><span className="demo-dot" /> TODAY'S ORBIT / 今日星轨</div>
      <h1>你好，<span>星旅人</span></h1>
      <p>先复习，再学新词。今天的路线已为你排好。</p>
    </div><div className="demo-date"><span>连续学习</span><strong>{overview ? `${overview.streak} 天` : '—'}</strong>
      <small>{overview ? `距考试还有 ${overview.daysToExam} 天` : ''}</small></div></div>

    {error && <p className="demo-error" role="alert">{error}</p>}

    <div className="demo-today-grid"><div className="demo-today-left">
      <section className="demo-panel demo-today-route"><div className="demo-panel-title">
        <span>今日任务 / YOUR ROUTE</span>
        <span className="demo-live-dot">{queue && queue.reviewCount > 0 ? '有到期复习卡' : '星轨就绪'}</span>
      </div><div className="demo-route-line" />
        <div className="demo-task-row"><span className="demo-task-icon">↺</span>
          <div><small>01 / PRIORITY</small><h2>复习到期单词</h2>
            <p>{queue ? `${queue.reviewCount} 个词 · 根据记忆轨道到期` : '加载中…'}</p></div>
          <Link to="/study?mode=review" aria-label="开始复习">开始复习 ↗</Link></div>
        <div className="demo-task-row"><span className="demo-task-icon">✦</span>
          <div><small>02 / NEW STARS</small><h2>点亮今日新词</h2>
            <p>{queue ? `${queue.newCount} 个词 · 六级高频优先` : ''}</p></div>
          <Link to="/study" aria-label="开始新词学习">开始学习 ↗</Link></div>
        <div className="demo-task-row"><span className="demo-task-icon">◇</span>
          <div><small>03 / CHECKPOINT</small><h2>完成随堂小测</h2>
            <p>5 道题 · 检查精确释义与拼写</p></div>
          <Link to="/test" aria-label="进入随堂测试">去测试 ↗</Link></div>
      </section>

      <div className="demo-metric-grid">
        <div className="demo-panel demo-metric"><span>今日队列</span>
          <strong>{queue ? queue.queue.length : '—'}<small> 张卡</small></strong>
          <div className="demo-meter"><i style={{ width: goal ? '12%' : '0' }} /></div></div>
        <div className="demo-panel demo-metric"><span>已学词数</span>
          <strong>{overview ? overview.totalLearned : '—'}<small> / {overview ? overview.totalLearned + overview.remainingNew : '—'}</small></strong>
          <small>已掌握 {overview ? overview.totalMastered : '—'}</small></div>
        <div className="demo-panel demo-metric"><span>近 7 天准确率</span>
          <strong>{overview && overview.accuracy7d != null ? Math.round(overview.accuracy7d * 100) : '—'}<small>%</small></strong>
          <small>今日已学 {overview ? overview.todayLearned : 0} · 复习 {overview ? overview.todayReviewed : 0}</small></div>
      </div>
    </div>

    <div className="demo-today-right">
      <section className="demo-today-planet"><OrbitScene motion={motion} />
        <div className="demo-planet-label"><span>YOUR VOCABULARY UNIVERSE</span>
          <strong>{overview ? `已学 ${overview.totalLearned} / 待学 ${overview.remainingNew}` : '星图加载中'}</strong></div></section>
      <div className="demo-constellation" aria-label="词汇星图进度">
        <svg viewBox="0 0 100 85" aria-hidden="true">
          <path d="M12 65L21 35 32 52 43 23 51 59 65 31 76 49 88 19M51 59L93 68" />
          {[0, 1, 2, 3, 4, 5, 6, 7, 8].map(i =>
            <circle key={i} cx={[12, 21, 32, 43, 51, 65, 76, 88, 93][i]} cy={[65, 35, 52, 23, 59, 31, 49, 19, 68][i]}
              r={i < 6 ? 1.7 : 1} className={i < Math.min(9, lit) ? 'lit' : ''} />)}
        </svg>
        <div><span>词汇星图</span><strong>已点亮 {lit} 颗</strong><small>掌握越深，星辰越亮</small></div>
      </div>
    </div></div>
    <div className="demo-data-note">学习数据实时来自后端 /orbits/today 与 /nebula 接口。</div>
  </main>
}
