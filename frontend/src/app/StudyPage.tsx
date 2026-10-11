import { useEffect, useState } from 'react'
import { Link, useOutletContext, useSearchParams } from 'react-router-dom'
import { QueueCard, StarDetail, TodayQueue, starDetail, submitReview, todayQueue } from '../api/modules'
import OrbitScene from '../demo/OrbitScene'

interface DetailCache { [wordId: number]: StarDetail }

/** 单词学习：今日队列逐卡揭示 + SM-2 评分 */
export default function StudyPage() {
  const { motion } = useOutletContext<{ motion: boolean }>()
  const [params] = useSearchParams()
  const reviewMode = params.get('mode') === 'review'
  const [queue, setQueue] = useState<TodayQueue | null>(null)
  const [cards, setCards] = useState<QueueCard[]>([])
  const [index, setIndex] = useState(0)
  const [stage, setStage] = useState(0)
  const [done, setDone] = useState(0)
  const [completed, setCompleted] = useState(false)
  const [error, setError] = useState('')
  const [details, setDetails] = useState<DetailCache>({})
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    document.title = '单词学习 · 星词 StarVocab'
    todayQueue().then(r => {
      setQueue(r.data)
      setCards(r.data.queue.filter(c => reviewMode ? c.type === 'review' : true))
    }).catch(e => setError(e.message))
  }, [reviewMode])

  const card = cards[index]
  const detail = card ? details[card.wordId] : undefined

  useEffect(() => {
    if (card && !details[card.wordId] && stage >= 3) {
      starDetail(card.wordId).then(r =>
        setDetails(prev => ({ ...prev, [card.wordId]: r.data }))).catch(() => {})
    }
  }, [card, stage, details])

  function speak(text: string) {
    if (!('speechSynthesis' in window)) return
    window.speechSynthesis.cancel()
    const u = new SpeechSynthesisUtterance(text)
    u.lang = 'en-US'
    u.rate = .82
    window.speechSynthesis.speak(u)
  }

  async function rate(rating: number) {
    if (!card || saving) return
    setSaving(true)
    try {
      await submitReview(card.wordId, rating, card.type === 'review' ? 'review' : 'learn')
      setDone(d => d + 1)
      if (index === cards.length - 1) setCompleted(true)
      else { setIndex(i => i + 1); setStage(0) }
    } catch (e) {
      setError(e instanceof Error ? e.message : '提交失败')
    } finally {
      setSaving(false)
    }
  }

  const ratings: [string, number][] = [['忘记', 0], ['费力', 1], ['迟疑', 2], ['轻松', 3]]

  return <main id="main" className="demo-main demo-study" tabIndex={-1}>
    <div className="demo-page-heading"><div>
      <div className="demo-overline"><span className="demo-dot" /> ORBIT / 单词学习</div>
      <h1>每记住一个词，<span>点亮一颗星。</span></h1>
      <p>先回想，再逐层揭示。按真实掌握程度给自己评分，SM-2 会安排下次复习。</p>
    </div><div className="demo-study-count">
      <strong>{completed ? cards.length : index + 1}<small> / {cards.length}</small></strong>
      <span>{reviewMode ? '到期复习' : '今日队列'}</span></div></div>

    {error && <p className="demo-error" role="alert">{error}</p>}

    <div className="demo-study-grid">
      <aside className="demo-study-sidebar demo-panel">
        <span className="demo-panel-title">学习轨道</span>
        <div className="demo-sidebar-progress">
          {cards.map((c, i) => <span key={c.wordId} className={i < index || completed ? 'done' : i === index ? 'active' : ''} />)}
        </div>
        <strong>{done} / {cards.length}</strong>
        <p>本次已评分词数</p>
        <div className="demo-sidebar-rule" />
        <span className="demo-side-kicker">本组词汇</span>
        {cards.slice(0, 12).map((c, i) =>
          <div className={`demo-word-list-row ${i === index && !completed ? 'current' : ''}`} key={c.wordId}>
            <span>{String(i + 1).padStart(2, '0')}</span>{c.word}{i < index || completed ? <b>✓</b> : null}
          </div>)}
        {cards.length > 12 && <p>…等 {cards.length} 张卡</p>}
      </aside>

      <section className="demo-study-center">
        {completed ? <div className="demo-word-card demo-complete-card">
          <span className="demo-card-index">THIS ORBIT IS COMPLETE</span>
          <div className="demo-complete-star">✦</div>
          <h2>这一组星辰已经点亮。</h2>
          <p>本组 {cards.length} 张卡已完成评分。刚学过的词将按 SM-2 轨道安排复习；现在可以用小测检查记忆。</p>
          <Link className="demo-primary" to="/test">进入随堂小测 ↗</Link>
          <button type="button" onClick={() => { setIndex(0); setStage(0); setCompleted(false); setDone(0) }}>再学一组 ↺</button>
        </div> : !card ? <div className="demo-word-card"><h2>{queue ? '本组没有待学的卡片' : '星轨加载中…'}</h2>
          <p>{queue ? '今日队列为空，去词库逛逛或先做一组小测。' : ''}</p>
          <Link className="demo-primary" to="/stars">前往词库 ↗</Link>
        </div> : <>
          <div className="demo-word-card">
            <div className="demo-card-top"><span>STAR / {String(index + 1).padStart(4, '0')}</span>
              <span>{card.type === 'review' ? `REVIEW · EF ${card.easeFactor.toFixed(2)}` : 'NEW STAR'}</span></div>
            <div className="demo-word-main"><div className="demo-word-english">{card.word}</div>
              <button type="button" className="demo-speak" onClick={() => speak(card.word)} aria-label={`朗读 ${card.word}`}>◖)) <span>发音</span></button></div>
            {stage >= 1 ? <div className="demo-reveal-row"><small>PRONUNCIATION</small><span>{card.phoneticUs}</span></div>
              : <div className="demo-hidden-row">先试着读出这个词，再揭示音标</div>}
            {stage >= 2 && <div className="demo-reveal-row"><small>MEANING</small><span>{card.definition}</span></div>}
            {stage >= 3 && <div className="demo-example"><small>IN CONTEXT / 例句</small>
              <p>{detail ? detail.exampleEn : '例句加载中…'}</p>
              <span>{detail ? detail.exampleCn : ''}</span></div>}
            {stage < 3 && <button type="button" className="demo-reveal-button" onClick={() => setStage(stage + 1)}>揭示下一层 <span>{stage + 1} / 3 ↗</span></button>}
          </div>
          <div className="demo-rating">
            <span>{stage < 2 ? '先揭示释义，再判断掌握程度' : reviewMode ? '这次回忆有多轻松？' : '你对这个词熟悉吗？'}</span>
            <div>{ratings.map(([label, rating]) =>
              <button type="button" key={rating} disabled={stage < 2 || saving} onClick={() => rate(rating)}>
                {label}<small>{rating}</small></button>)}</div>
          </div>
        </>}
      </section>

      <aside className="demo-study-insight">
        <div className="demo-study-orbit"><OrbitScene motion={motion} compact /></div>
        <div className="demo-panel demo-insight-card"><span>记忆提示 / MEMORY NOTE</span>
          <strong>先回忆，再看答案</strong>
          <p>评分 0-3 对应忘记 / 费力 / 迟疑 / 轻松。忘记会重置记忆间隔，轻松则拉长下次复习周期。</p>
          <div className="demo-sidebar-rule" />
          <small>{card ? `当前轨道：第 ${card.repetitions} 次复习 · 间隔 ${card.intervalDays} 天` : '下一次复习：按 SM-2 轨道安排'}</small>
        </div>
      </aside>
    </div>
    <div className="demo-data-note">学习结果实时写入后端 /orbits/review，刷新页面不会丢失。</div>
  </main>
}
