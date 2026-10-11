import { useCallback, useEffect, useRef, useState } from 'react'
import { Link, useOutletContext } from 'react-router-dom'
import { SubmitResult, TestQuestion, TestSessionVO, createTest, submitTest } from '../api/modules'
import OrbitScene from '../demo/OrbitScene'

/** 随堂小测：后端组卷保管答案，交卷后复盘错题 */
export default function TestPage() {
  const { motion } = useOutletContext<{ motion: boolean }>()
  const [session, setSession] = useState<TestSessionVO | null>(null)
  const [answers, setAnswers] = useState<string[]>([])
  const [index, setIndex] = useState(0)
  const [result, setResult] = useState<SubmitResult | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const startedAt = useRef(Date.now())

  const start = useCallback(() => {
    setLoading(true)
    setError('')
    setResult(null)
    createTest('quiz', 5).then(r => {
      setSession(r.data)
      setAnswers(Array(r.data.questions.length).fill(''))
      setIndex(0)
      startedAt.current = Date.now()
    }).catch(e => setError(e.message)).finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    document.title = '测试与复盘 · 星词 StarVocab'
    start()
  }, [start])

  const questions: TestQuestion[] = session?.questions ?? []
  const q = questions[index]
  const answered = answers.filter(a => a.trim()).length

  function choose(value: string) {
    setAnswers(prev => prev.map((a, i) => i === index ? value : a))
  }

  async function submit() {
    if (!session) return
    setLoading(true)
    try {
      const payload = questions.map((question, i) => ({
        questionId: question.questionId, answer: answers[i] ?? '', costSec: 0,
      }))
      const r = await submitTest(session.sessionId, payload,
        Math.round((Date.now() - startedAt.current) / 1000))
      setResult(r.data)
    } catch (e) {
      setError(e instanceof Error ? e.message : '交卷失败')
    } finally {
      setLoading(false)
    }
  }

  return <main id="main" className="demo-main demo-test" tabIndex={-1}>
    <div className="demo-page-heading"><div>
      <div className="demo-overline"><span className="demo-dot" /> CHECKPOINT / 测试与复盘</div>
      <h1>{result ? '这一站的星图，' : '记住了多少，'}<span>{result ? '已经绘好。' : '测一测。'}</span></h1>
      <p>检验精确释义与拼写。答案由后端保管，交卷后再查看解析。</p>
    </div><div className="demo-test-status"><span>随堂小测 · 5 题</span>
      <strong>{result ? `${result.correct} / ${result.total}` : `${index + 1} / ${questions.length || 5}`}</strong>
      <small>{result ? '答对题数' : '当前题目'}</small></div></div>

    {error && <p className="demo-error" role="alert">{error}</p>}

    <div className="demo-test-grid"><section className="demo-test-primary">
      {result ? <div className="demo-panel demo-result">
        <div className="demo-result-top"><div>
          <span>VOCABULARY CHECK / RESULT</span>
          <h2>{result.correct === result.total ? '全部答对，星图又亮了一片。' : '定位模糊词，明天再巩固。'}</h2>
          <p>{result.message} · 用时 {result.durationSec} 秒</p></div>
          <strong>{result.score}<small>分</small></strong></div>
        <div className="demo-result-review"><h3>逐题复盘</h3>
          {questions.map((question) => {
            const wrong = result.wrongQuestions.find(w => w.questionId === question.questionId)
            return <div className="demo-review-row" key={question.questionId}>
              <span className={wrong ? 'wrong' : 'right'}>{wrong ? '×' : '✓'}</span>
              <div><strong>{question.type === 'en2cn' ? question.stem : (wrong?.correctAnswer || question.stem)}</strong>
                <small>{question.stem}</small>
                {wrong && <em>你的答案：{wrong.yourAnswer || '未作答'} · 正确答案：{wrong.correctAnswer}</em>}
              </div></div>
          })}</div>
        <div className="demo-result-actions">
          <button type="button" className="demo-primary" onClick={start} disabled={loading}>再测一组 ↺</button>
          <Link to="/today">返回今日任务 ↗</Link></div>
      </div>
        : loading || !q ? <div className="demo-panel demo-question"><h2>{loading ? '正在从星域选题组卷…' : '暂无可用的词汇'}</h2>
          <p>{!loading && '先去学习页点亮几颗新星，再来测试。'}</p></div>
        : <div className="demo-panel demo-question">
          <div className="demo-question-meta">
            <span>QUESTION {String(index + 1).padStart(2, '0')} / {String(questions.length).padStart(2, '0')}</span>
            <span>{q.type === 'en2cn' ? '英译中' : q.type === 'cn2en' ? '中译英' : '拼写题'}</span></div>
          <div className="demo-question-progress"><i style={{ width: `${(index + 1) / questions.length * 100}%` }} /></div>
          <h2>{q.type === 'en2cn' ? `${q.stem} 最准确的意思是？` : q.type === 'cn2en' ? `“${q.stem}”对应哪个词？` : `请拼写：${q.stem}`}</h2>
          <p>{q.type === 'spelling' ? '输入英文单词，注意精确拼写。' : '选择你认为最准确的一项。'}</p>
          {q.type !== 'spelling'
            ? <div className="demo-options">{q.options.map((option, i) =>
              <button type="button" key={option} className={answers[index] === option ? 'selected' : ''} onClick={() => choose(option)}>
                <span>{String.fromCharCode(65 + i)}</span>{option}<b>{answers[index] === option ? '●' : '○'}</b>
              </button>)}</div>
            : <label className="demo-spelling">英文拼写
              <input type="text" value={answers[index] ?? ''} onChange={e => choose(e.target.value)}
                placeholder="在这里输入英文单词" autoComplete="off" spellCheck={false} /></label>}
          <div className="demo-question-actions">
            <button type="button" onClick={() => setIndex(Math.max(0, index - 1))} disabled={index === 0}>← 上一题</button>
            {index === questions.length - 1
              ? <button type="button" className="demo-primary" onClick={submit} disabled={loading || !answered}>提交并查看结果 ↗</button>
              : <button type="button" className="demo-primary" onClick={() => setIndex(index + 1)}>下一题 →</button>}
          </div>
        </div>}
    </section>

      <aside className="demo-test-aside">
        <div className="demo-panel demo-test-map"><span>CHECKPOINT / 词汇测绘</span>
          <div className="demo-test-orbit"><OrbitScene motion={motion} compact /></div>
          <div className="demo-test-pips">{questions.map((question, i) =>
            <button type="button" key={question.questionId} aria-label={`第 ${i + 1} 题`} onClick={() => setIndex(i)}
              className={i === index ? 'current' : answers[i]?.trim() ? 'answered' : ''}>{i + 1}</button>)}</div>
          <p>已作答 {answered} / {questions.length} 题</p></div>
        <div className="demo-panel demo-test-note"><span>作答原则</span>
          <p>先独立回忆，再选择。页面不会提前显示对错；交卷后集中查看错题解析。</p></div>
      </aside></div>
    <div className="demo-data-note">组卷来自 /tests 接口，答案仅保存在后端，交卷后返回得分与错题。</div>
  </main>
}
