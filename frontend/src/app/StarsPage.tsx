import { useEffect, useState } from 'react'
import { PageResult, StarItem, starsPage, starsSearch } from '../api/modules'

const LETTERS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'.split('')

function speak(text: string) {
  if (!('speechSynthesis' in window)) return
  window.speechSynthesis.cancel()
  const u = new SpeechSynthesisUtterance(text)
  u.lang = 'en-US'
  u.rate = .85
  window.speechSynthesis.speak(u)
}

function meaningText(d: StarItem['definition']) {
  return d.map(e => `${e.pos} ${e.meanings.join('；')}`).join('  ')
}

/** 词库星图：分页浏览 + 搜索 + 字母筛选 */
export default function StarsPage() {
  const [page, setPage] = useState(1)
  const [letter, setLetter] = useState('')
  const [keyword, setKeyword] = useState('')
  const [data, setData] = useState<PageResult<StarItem> | null>(null)
  const [searchHits, setSearchHits] = useState<StarItem[] | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  useEffect(() => { document.title = '词库星图 · 星词 StarVocab' }, [])

  useEffect(() => {
    setLoading(true)
    setError('')
    starsPage(page, 24, letter || undefined).then(r => setData(r.data))
      .catch(e => setError(e.message)).finally(() => setLoading(false))
  }, [page, letter])

  function doSearch(e: React.FormEvent) {
    e.preventDefault()
    const q = keyword.trim()
    if (!q) { setSearchHits(null); return }
    setLoading(true)
    starsSearch(q).then(r => setSearchHits(r.data)).catch(err => setError(err.message))
      .finally(() => setLoading(false))
  }

  const list = searchHits ?? data?.list ?? []
  const total = searchHits ? searchHits.length : data?.total ?? 0

  return <main id="main" className="demo-main" tabIndex={-1}>
    <div className="demo-page-heading"><div>
      <div className="demo-overline"><span className="demo-dot" /> STAR CATALOG / 词库星图</div>
      <h1>{total} 颗星辰，<span>等待点名。</span></h1>
      <p>六级核心词按词频排列。点击喇叭听发音，先记住高频星。</p>
    </div></div>

    <section className="demo-panel" style={{ padding: '18px 22px', display: 'flex', gap: 14, flexWrap: 'wrap', alignItems: 'center' }}>
      <form onSubmit={doSearch} style={{ display: 'flex', gap: 10, flex: '1 1 320px' }}>
        <input value={keyword} onChange={e => setKeyword(e.target.value)} placeholder="搜索单词或释义，如 abundant / 丰富"
          style={{ flex: 1, height: 43, padding: '0 13px', color: '#f1eaf8', background: '#100f1b', border: '1px solid #5a476b', borderRadius: 5, outline: 'none' }} />
        <button type="submit" className="demo-primary" style={{ minHeight: 43 }}>搜索</button>
      </form>
      {searchHits && <button type="button" className="demo-motion-toggle" onClick={() => { setSearchHits(null); setKeyword('') }}>清除搜索 ×</button>}
    </section>

    {!searchHits && <div style={{ display: 'flex', gap: 6, flexWrap: 'wrap', margin: '18px 0 6px' }}>
      <button type="button" className="demo-motion-toggle" style={!letter ? { background: '#4a3560', color: '#efe4fb' } : undefined}
        onClick={() => { setLetter(''); setPage(1) }}>全部</button>
      {LETTERS.map(l =>
        <button type="button" key={l} className="demo-motion-toggle" style={letter === l ? { background: '#4a3560', color: '#efe4fb' } : undefined}
          onClick={() => { setLetter(l); setPage(1) }}>{l}</button>)}
    </div>}

    {error && <p className="demo-error" role="alert">{error}</p>}
    {loading && <p className="demo-data-note">星域加载中…</p>}

    <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(255px, 1fr))', gap: 14, margin: '18px 0 8px' }}>
      {list.map(w => <article key={w.id} className="demo-panel" style={{ padding: '16px 18px' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gap: 10 }}>
          <strong style={{ fontFamily: 'var(--display-font)', fontWeight: 400, fontSize: 21, color: '#e9dcf7' }}>{w.word}</strong>
          <button type="button" className="demo-speak" onClick={() => speak(w.word)} aria-label={`朗读 ${w.word}`}>◖))</button>
        </div>
        <div style={{ color: '#9d8fb0', fontSize: 12, margin: '3px 0 8px' }}>{w.phoneticUs || w.phoneticUk}</div>
        <div style={{ color: '#c9b9dd', fontSize: 13.5, lineHeight: 1.65 }}>{meaningText(w.definition)}</div>
        <div style={{ display: 'flex', gap: 12, marginTop: 10, fontSize: 11, color: '#847695' }}>
          <span>词频 {w.frequency}</span><span>六级考频 {w.cet6Count}</span><span>难度 {w.difficulty}</span>
        </div>
      </article>)}
    </div>

    {!searchHits && data && data.totalPages > 1 && <div style={{ display: 'flex', gap: 10, justifyContent: 'center', alignItems: 'center', margin: '22px 0' }}>
      <button type="button" className="demo-motion-toggle" disabled={page <= 1} onClick={() => setPage(p => p - 1)}>← 上一页</button>
      <span style={{ color: '#a99eb9', fontSize: 13 }}>第 {data.page} / {data.totalPages} 页 · 共 {data.total} 词</span>
      <button type="button" className="demo-motion-toggle" disabled={page >= data.totalPages} onClick={() => setPage(p => p + 1)}>下一页 →</button>
    </div>}
  </main>
}
