import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { login } from '../api/auth'
import PlanetCanvas from '../components/PlanetCanvas'
import './LoginPage.css'

/**
 * 登录页 —— 完全复刻兽星 ANIMA hero 两栏布局
 * 左栏：eyebrow + 宋体大标题 + 登录表单（仿 question-panel）
 * 右栏：orbital-map + 气态巨行星 + 卫星 + 坐标标注
 */
export default function LoginPage() {
  const navigate = useNavigate()
  const [nickname, setNickname] = useState('星旅人')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    if (!nickname.trim() || !password) {
      setError('昵称和密码不能为空')
      return
    }
    setLoading(true)
    try {
      const res = await login(nickname.trim(), password)
      localStorage.setItem('starvocab_token', res.data.accessToken)
      localStorage.setItem('starvocab_user', JSON.stringify(res.data))
      navigate('/home')
    } catch (err) {
      setError(err instanceof Error ? err.message : '登录失败')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="page-width">
      <section className="hero login-hero">
        <div className="hero-copy">
          <div className="eyebrow">
            <span className="status-dot" />
            六级备考 <span className="eyebrow-divider" /> 紫色星图 <span className="eyebrow-divider" /> 2026.12
          </div>
          <h1>
            让每一个单词
            <br />
            都成为<span className="hero-word">星辰</span>
          </h1>
          <p className="hero-description">
            在紫色星云中开启你的六级备考旅程。每记住一个词，就在宇宙里点亮一颗星，
            直到属于你的那片星座，完整浮现。
          </p>

          <form className="login-panel" onSubmit={handleSubmit}>
            <div className="login-field">
              <span className="login-label">昵称</span>
              <input
                type="text"
                value={nickname}
                onChange={(e) => setNickname(e.target.value)}
                placeholder="输入你的昵称"
                autoComplete="username"
              />
            </div>
            <div className="login-field">
              <span className="login-label">密码</span>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="输入密码"
                autoComplete="current-password"
              />
            </div>
            {error && <div className="login-error">{error}</div>}
            <div className="login-actions">
              <button type="submit" className="button button-primary" disabled={loading}>
                {loading ? '星轨连接中…' : '登录'}
              </button>
              <button type="button" className="button button-text">
                忘记密码？
              </button>
            </div>
          </form>
        </div>

        {/* 右侧星球场景 */}
        <div className="planet-scene">
          <svg className="orbital-map" viewBox="0 0 620 580" aria-hidden="true">
            <ellipse cx="310" cy="290" rx="300" ry="80" />
            <ellipse cx="310" cy="290" rx="240" ry="64" />
            <ellipse cx="310" cy="290" rx="180" ry="48" />
            <circle cx="100" cy="290" r="3" />
            <circle cx="520" cy="290" r="3" />
            <circle cx="310" cy="50" r="2" />
            <circle cx="310" cy="530" r="2" />
          </svg>

          <PlanetCanvas />

          <div className="small-moon moon-one" />
          <div className="small-moon moon-two" />
          <span className="orbit-cross cross-one">✦</span>
          <span className="orbit-cross cross-two">✦</span>

          <div className="scene-coordinates">
            VOCAB-06
            <span>LON 116.4° · LAT 39.9°</span>
          </div>

          <div className="planet-caption">
            <span className="caption-line" />
            <div>
              <span className="utility">PLANET · CET-6</span>
              <p>5500 颗星辰，等待被点亮</p>
            </div>
          </div>

          <div className="scroll-cue">
            <span className="scroll-line" />
            向下滚动
          </div>
        </div>
      </section>
    </div>
  )
}
