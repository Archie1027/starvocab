import { useEffect } from 'react'
import { Link, Navigate, NavLink, Outlet, useLocation } from 'react-router-dom'
import '../demo/demo.css'

interface ShellProps {
  motion: boolean
  setMotion: (value: boolean) => void
}

/** 正式页导航外壳：登录守卫 + 星词导航条 */
export default function AppShell({ motion, setMotion }: ShellProps) {
  const location = useLocation()
  const token = localStorage.getItem('starvocab_token')
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('starvocab_user') || '{}') } catch { return {} }
  })()

  useEffect(() => {
    document.body.dataset.demoScene =
      location.pathname.startsWith('/study') ? 'study'
        : location.pathname.startsWith('/test') ? 'test' : 'today'
  }, [location.pathname])

  if (!token) return <Navigate to="/login" replace />

  const links = [
    ['/today', '今日任务'], ['/study', '单词学习'],
    ['/test', '测试与复盘'], ['/stars', '词库星图'],
  ]
  return <div className="demo-app">
    <header className="demo-header">
      <Link to="/today" className="demo-brand" aria-label="星词首页">
        <span className="demo-brand-mark">✦</span><span>星词 <small>STARVOCAB</small></span>
      </Link>
      <nav className="demo-nav" aria-label="主导航">
        {links.map(([to, label]) =>
          <NavLink key={to} to={to} className={({ isActive }) => isActive ? 'current' : ''}>{label}</NavLink>)}
      </nav>
      <div className="demo-header-tools">
        <span className="demo-badge">{user.nickname || '星旅人'}</span>
        <button className="demo-motion-toggle" onClick={() => setMotion(!motion)}
          aria-pressed={!motion} type="button">{motion ? '关闭动效' : '开启动效'}</button>
      </div>
    </header>
    <Outlet context={{ motion }} />
    <footer className="demo-footer">
      <span>星词 STARVOCAB · 六级备考</span>
      <span>学习记录已同步至星图后端</span>
    </footer>
  </div>
}
