import { FormEvent, useState } from 'react'
import { Link, NavLink, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import OrbitScene from './OrbitScene'
import { demoQuestions, demoWords } from './data'
import './demo.css'

interface DemoProps {
  motion: boolean
  setMotion: (value: boolean) => void
}

function DemoHeader({ motion, setMotion }: DemoProps) {
  const links = [
    ['/demo/login', '登录'], ['/demo/today', '今日任务'],
    ['/demo/study', '单词学习'], ['/demo/test', '测试与复盘'],
  ]
  return <header className="demo-header">
    <Link to="/demo/login" className="demo-brand" aria-label="星词演示首页">
      <span className="demo-brand-mark">✦</span><span>星词 <small>STARVOCAB</small></span>
    </Link>
    <nav className="demo-nav" aria-label="四个演示场景">
      {links.map(([to, label]) => <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'current' : ''}>{label}</NavLink>)}
    </nav>
    <div className="demo-header-tools"><span className="demo-badge">交互原型</span><button className="demo-motion-toggle" onClick={() => setMotion(!motion)} aria-pressed={!motion} type="button">{motion ? '关闭动效' : '开启动效'}</button></div>
  </header>
}

function DemoLogin({ motion }: { motion: boolean }) {
  const navigate = useNavigate()
  const [nickname, setNickname] = useState('星旅人')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    if (!nickname.trim() || !password.trim()) { setError('请输入昵称和密码。'); return }
    sessionStorage.setItem('starvocab_demo_nickname', nickname.trim())
    setError('')
    navigate('/demo/today')
  }
  return <main id="main" className="demo-main demo-login-layout" tabIndex={-1}>
    <section className="demo-login-copy"><div className="demo-overline"><span className="demo-dot" /> CET–6 / 2026 DECEMBER / PERSONAL STUDY</div>
      <h1>让每一个单词<br />都成为<span>星辰。</span></h1>
      <p className="demo-lede">从今天开始，重新唤醒熟悉的词汇，补上六级高频词。学一张卡、测一道题，让备考进度在星图上慢慢亮起来。</p>
      <form className="demo-login-form" onSubmit={submit}>
        <div className="demo-form-head"><strong>进入你的学习宇宙</strong><span>仅需昵称与密码</span></div>
        <label>昵称<input value={nickname} onChange={e => setNickname(e.target.value)} autoComplete="username" placeholder="你的昵称" /></label>
        <label>密码<input type="password" value={password} onChange={e => setPassword(e.target.value)} autoComplete="current-password" placeholder="输入密码体验" /></label>
        {error && <p className="demo-error" role="alert">{error}</p>}
        <button type="submit" className="demo-primary">进入星图 <span>↗</span></button>
        <p className="demo-form-note">个人使用，无需注册。此页为交互演示，输入不会发送到服务器。</p>
      </form>
    </section>
    <section className="demo-login-art" aria-label="慢速星球环绕"><OrbitScene motion={motion} /><div className="demo-planet-note"><span>01 / THE FIRST STAR</span><strong>你的六级旅程，从一颗星开始。</strong></div></section>
  </main>
}

function MiniConstellation() {
  const stars = [[12,65],[21,35],[32,52],[43,23],[51,59],[65,31],[76,49],[88,19],[93,68]]
  return <div className="demo-constellation" aria-label="词汇星图进度示意">
    <svg viewBox="0 0 100 85" aria-hidden="true"><path d="M12 65L21 35 32 52 43 23 51 59 65 31 76 49 88 19M51 59L93 68" />{stars.map(([x,y], i) => <circle key={i} cx={x} cy={y} r={i < 6 ? 1.7 : 1} className={i < 6 ? 'lit' : ''} />)}</svg>
    <div><span>词汇星图</span><strong>已点亮 168 颗</strong><small>演示数据 · 持续积累中</small></div>
  </div>
}

function DemoToday({ motion, learned }: { motion: boolean; learned: number }) {
  const nickname = sessionStorage.getItem('starvocab_demo_nickname') || '星旅人'
  const now = new Date()
  const chinaToday = new Date(now.toLocaleString('en-US', { timeZone: 'Asia/Shanghai' }))
  const days = Math.max(0, Math.ceil((new Date(2026, 11, 14).getTime() - new Date(chinaToday.getFullYear(), chinaToday.getMonth(), chinaToday.getDate()).getTime()) / 86400000))
  return <main id="main" className="demo-main demo-today" tabIndex={-1}>
    <div className="demo-page-heading"><div><div className="demo-overline"><span className="demo-dot" /> TODAY'S ORBIT / 今日星轨</div><h1>你好，<span>{nickname}</span></h1><p>先复习，再学新词。今天的路线已为你排好。</p></div><div className="demo-date"><span>目标日期</span><strong>2026.12.14</strong><small>距设定日期还有 {days} 天</small></div></div>
    <div className="demo-today-grid"><div className="demo-today-left">
      <section className="demo-panel demo-today-route"><div className="demo-panel-title"><span>今日任务 / YOUR ROUTE</span><span className="demo-live-dot">学习进行中</span></div><div className="demo-route-line" />
        <div className="demo-task-row"><span className="demo-task-icon">↺</span><div><small>01 / PRIORITY</small><h2>复习到期单词</h2><p>18 个词 · 根据记忆轨道到期</p></div><Link to="/demo/study?mode=review" aria-label="开始复习">开始复习 ↗</Link></div>
        <div className="demo-task-row"><span className="demo-task-icon">✦</span><div><small>02 / NEW STARS</small><h2>点亮今日新词</h2><p>30 个词 · 六级高频优先</p></div><Link to="/demo/study" aria-label="开始新词学习">开始学习 ↗</Link></div>
        <div className="demo-task-row"><span className="demo-task-icon">◇</span><div><small>03 / CHECKPOINT</small><h2>完成随堂小测</h2><p>5 道演示题 · 检查精确释义与拼写</p></div><Link to="/demo/test" aria-label="进入随堂测试">去测试 ↗</Link></div>
      </section>
      <div className="demo-metric-grid"><div className="demo-panel demo-metric"><span>今日已学</span><strong>{learned}<small> / 30</small></strong><div className="demo-meter"><i style={{ width: `${Math.min(100, learned / 30 * 100)}%` }} /></div></div><div className="demo-panel demo-metric"><span>连续学习</span><strong>7<small> 天</small></strong><small>每天一点，星图更亮</small></div><div className="demo-panel demo-metric"><span>近 7 天复习准确率</span><strong>82<small>%</small></strong><small>演示数据</small></div></div>
    </div><div className="demo-today-right"><section className="demo-today-planet"><OrbitScene motion={motion} /><div className="demo-planet-label"><span>YOUR VOCABULARY UNIVERSE</span><strong>已学 430 / 目标词库 2380</strong></div></section><MiniConstellation /></div></div>
    <div className="demo-data-note">示例学习数据用于页面体验；目标日期以个人资料设置为准。</div>
  </main>
}

function DemoStudy({ motion, learned, onLearned }: { motion: boolean; learned: number; onLearned: () => void }) {
  const review = new URLSearchParams(window.location.search).get('mode') === 'review'
  const [index, setIndex] = useState(0)
  const [stage, setStage] = useState(0)
  const [completed, setCompleted] = useState(false)
  const word = demoWords[index]
  function speak() {
    if (!('speechSynthesis' in window)) return
    window.speechSynthesis.cancel()
    const utterance = new SpeechSynthesisUtterance(word.word)
    utterance.lang = 'en-US'
    utterance.rate = .82
    window.speechSynthesis.speak(utterance)
  }
  function rate() {
    onLearned()
    if (index === demoWords.length - 1) setCompleted(true)
    else { setIndex(index + 1); setStage(0) }
  }
  return <main id="main" className="demo-main demo-study" tabIndex={-1}>
    <div className="demo-page-heading"><div><div className="demo-overline"><span className="demo-dot" /> ORBIT / 单词学习</div><h1>每记住一个词，<span>点亮一颗星。</span></h1><p>先回想，再逐层揭示。按真实掌握程度给自己评分。</p></div><div className="demo-study-count"><strong>{completed ? 4 : index + 1}<small> / 4</small></strong><span>{review ? '到期复习演示' : '新词学习演示'}</span></div></div>
    <div className="demo-study-grid"><aside className="demo-study-sidebar demo-panel"><span className="demo-panel-title">学习轨道</span><div className="demo-sidebar-progress">{demoWords.map((w, i) => <span key={w.word} className={i < index || completed ? 'done' : i === index ? 'active' : ''} />)}</div><strong>{Math.min(4, learned)} / 30</strong><p>今日已学习词数</p><div className="demo-sidebar-rule" /><span className="demo-side-kicker">本组词汇</span>{demoWords.map((w, i) => <div className={`demo-word-list-row ${i === index && !completed ? 'current' : ''}`} key={w.word}><span>{String(i+1).padStart(2,'0')}</span>{w.word}{i < index || completed ? <b>✓</b> : null}</div>)}</aside>
      <section className="demo-study-center">{completed ? <div className="demo-word-card demo-complete-card"><span className="demo-card-index">THIS ORBIT IS COMPLETE</span><div className="demo-complete-star">✦</div><h2>四颗新星，已经点亮。</h2><p>本组演示学习完成。刚学过的词将安排在次日复习；现在可以用小测检查记忆。</p><Link className="demo-primary" to="/demo/test">进入随堂小测 ↗</Link><button type="button" onClick={() => {setIndex(0);setStage(0);setCompleted(false)}}>再看一遍单词</button></div> : <>
        <div className="demo-word-card"><div className="demo-card-top"><span>STAR / {String(index+1).padStart(4,'0')}</span><span>演示词频 {word.cet6Count}</span></div><div className="demo-word-main"><div className="demo-word-english">{word.word}</div><button type="button" className="demo-speak" onClick={speak} aria-label={`朗读 ${word.word}`}>◖)) <span>发音</span></button></div>
          {stage >= 1 ? <div className="demo-reveal-row"><small>PRONUNCIATION</small><span>{word.phonetic}</span></div> : <div className="demo-hidden-row">先试着读出这个词，再揭示音标</div>}
          {stage >= 2 && <div className="demo-reveal-row"><small>MEANING</small><span>{word.pos} {word.meaning}</span></div>}
          {stage >= 3 && <div className="demo-example"><small>IN CONTEXT / 例句</small><p>{word.example}</p><span>{word.exampleCn}</span></div>}
          {stage < 3 && <button type="button" className="demo-reveal-button" onClick={() => setStage(stage + 1)}>揭示下一层 <span>{stage + 1} / 3 ↗</span></button>}
        </div><div className="demo-rating"><span>{stage < 2 ? '先揭示释义，再判断掌握程度' : review ? '这次回忆有多轻松？' : '你对这个词熟悉吗？'}</span><div>{review ? [ ['忘记',0],['费力',1],['迟疑',2],['轻松',3] ].map(([label, rating]) => <button type="button" key={rating} disabled={stage < 2} onClick={rate}>{label}<small>{rating}</small></button>) : ['不熟','模糊','认识'].map(label => <button type="button" key={label} disabled={stage < 2} onClick={rate}>{label}<small>↗</small></button>)}</div></div>
      </>}</section><aside className="demo-study-insight"><div className="demo-study-orbit"><OrbitScene motion={motion} compact /></div><div className="demo-panel demo-insight-card"><span>记忆提示 / MEMORY NOTE</span><strong>先回忆，再看答案</strong><p>不确定时继续揭示。评分只记录你的真实感觉，不需要为了“全对”勉强选择认识。</p><div className="demo-sidebar-rule" /><small>下一次复习：次日</small></div></aside></div>
    <div className="demo-data-note">演示数据只存于当前浏览器；正式学习记录由 /orbits/review 保存。</div>
  </main>
}

function DemoTest({ motion }: { motion: boolean }) {
  const [index, setIndex] = useState(0)
  const [answers, setAnswers] = useState<string[]>(Array(demoQuestions.length).fill(''))
  const [submitted, setSubmitted] = useState(false)
  const q = demoQuestions[index]
  const correct = demoQuestions.reduce((n, question, i) => n + (answers[i].trim().toLowerCase() === question.answer.toLowerCase() ? 1 : 0), 0)
  function choose(value: string) { setAnswers(prev => prev.map((answer, i) => i === index ? value : answer)) }
  function restart() { setIndex(0); setAnswers(Array(demoQuestions.length).fill('')); setSubmitted(false) }
  return <main id="main" className="demo-main demo-test" tabIndex={-1}>
    <div className="demo-page-heading"><div><div className="demo-overline"><span className="demo-dot" /> CHECKPOINT / 测试与复盘</div><h1>{submitted ? '这一站的星图，' : '记住了多少，'}<span>{submitted ? '已经绘好。' : '测一测。'}</span></h1><p>检验精确释义与拼写。交卷后再查看正确答案和错题解析。</p></div><div className="demo-test-status"><span>随堂小测 · 演示</span><strong>{submitted ? `${correct} / ${demoQuestions.length}` : `${index + 1} / ${demoQuestions.length}`}</strong><small>{submitted ? '答对题数' : '当前题目'}</small></div></div>
    <div className="demo-test-grid"><section className="demo-test-primary">{submitted ? <div className="demo-panel demo-result"><div className="demo-result-top"><div><span>VOCABULARY CHECK / RESULT</span><h2>{correct === demoQuestions.length ? '五题全对，继续前进。' : '定位模糊词，明天再巩固。'}</h2><p>这只是本次词汇小测得分，不能预测六级总分。</p></div><strong>{Math.round(correct / demoQuestions.length * 100)}<small>分</small></strong></div><div className="demo-result-review"><h3>逐题复盘</h3>{demoQuestions.map((question, i) => <div className="demo-review-row" key={question.id}><span className={answers[i].trim().toLowerCase() === question.answer.toLowerCase() ? 'right' : 'wrong'}>{answers[i].trim().toLowerCase() === question.answer.toLowerCase() ? '✓' : '×'}</span><div><strong>{question.word}</strong><small>{question.explanation}</small>{answers[i].trim().toLowerCase() !== question.answer.toLowerCase() && <em>你的答案：{answers[i] || '未作答'} · 正确答案：{question.answer}</em>}</div></div>)}</div><div className="demo-result-actions"><button type="button" className="demo-primary" onClick={restart}>重新测一组 ↺</button><Link to="/demo/today">返回今日任务 ↗</Link></div></div> : <div className="demo-panel demo-question"><div className="demo-question-meta"><span>QUESTION {String(index + 1).padStart(2,'0')} / {String(demoQuestions.length).padStart(2,'0')}</span><span>{q.type === 'choice' ? '选择题' : '拼写题'}</span></div><div className="demo-question-progress"><i style={{ width: `${(index + 1) / demoQuestions.length * 100}%` }} /></div><h2>{q.prompt}</h2><p>选择你认为最准确的一项。交卷前可以返回修改。</p>{q.type === 'choice' ? <div className="demo-options">{q.options?.map((option, i) => <button type="button" key={option} className={answers[index] === option ? 'selected' : ''} onClick={() => choose(option)}><span>{String.fromCharCode(65+i)}</span>{option}<b>{answers[index] === option ? '●' : '○'}</b></button>)}</div> : <label className="demo-spelling">英文拼写<input type="text" value={answers[index]} onChange={e => choose(e.target.value)} placeholder="在这里输入英文单词" autoComplete="off" spellCheck={false} /></label>}<div className="demo-question-actions"><button type="button" onClick={() => setIndex(Math.max(0,index-1))} disabled={index===0}>← 上一题</button><button type="button" className="demo-primary" onClick={() => index === demoQuestions.length - 1 ? setSubmitted(true) : setIndex(index+1)} disabled={!answers[index].trim()}>{index === demoQuestions.length - 1 ? '提交并查看结果 ↗' : '下一题 →'}</button></div></div>}</section><aside className="demo-test-aside"><div className="demo-panel demo-test-map"><span>CHECKPOINT / 词汇测绘</span><div className="demo-test-orbit"><OrbitScene motion={motion} compact /></div><div className="demo-test-pips">{demoQuestions.map((item, i) => <button type="button" key={item.id} aria-label={`第 ${i+1} 题`} onClick={() => !submitted && setIndex(i)} className={i===index&&!submitted ? 'current' : answers[i] ? 'answered' : ''}>{i+1}</button>)}</div><p>已作答 {answers.filter(Boolean).length} / {demoQuestions.length} 题</p></div><div className="demo-panel demo-test-note"><span>作答原则</span><p>先独立回忆，再选择。提交前页面不会显示正确答案；完成后集中查看错题。</p></div></aside></div>
    <div className="demo-data-note">此页在浏览器内模拟判分；正式 API 由后端保管答案并返回结果。</div>
  </main>
}

export default function DemoApp({ motion, setMotion }: DemoProps) {
  const [learned, setLearned] = useState(() => Number(localStorage.getItem('starvocab_demo_learned') || 0))
  function onLearned() { setLearned(current => { const next = current + 1; localStorage.setItem('starvocab_demo_learned', String(next)); return next }) }
  return <div className="demo-app"><DemoHeader motion={motion} setMotion={setMotion} />
    <Routes>
      <Route path="/demo" element={<Navigate to="/demo/login" replace />} />
      <Route path="/demo/login" element={<DemoLogin motion={motion} />} />
      <Route path="/demo/today" element={<DemoToday motion={motion} learned={learned} />} />
      <Route path="/demo/study" element={<DemoStudy motion={motion} learned={learned} onLearned={onLearned} />} />
      <Route path="/demo/test" element={<DemoTest motion={motion} />} />
      <Route path="*" element={<Navigate to="/demo/login" replace />} />
    </Routes>
    <footer className="demo-footer"><span>星词 STARVOCAB · 六级备考视觉原型</span><span>四个场景 / 三幅后端设计图 / 示例数据</span></footer>
  </div>
}
