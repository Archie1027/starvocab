import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { get } from '../api/request'

interface Profile {
  id: number
  nickname: string
  avatarUrl: string
  examDate: string
  dailyGoal: number
}

export default function HomePage() {
  const navigate = useNavigate()
  const [profile, setProfile] = useState<Profile | null>(null)

  useEffect(() => {
    const token = localStorage.getItem('starvocab_token')
    if (!token) {
      navigate('/login')
      return
    }
    get<Profile>('/users/profile')
      .then((r) => setProfile(r.data))
      .catch(() => {
        localStorage.clear()
        navigate('/login')
      })
  }, [navigate])

  const handleLogout = () => {
    localStorage.clear()
    navigate('/login')
  }

  return (
    <div className="page-width">
      <section className="hero">
        <div className="hero-copy">
          <div className="eyebrow">
            <span className="status-dot" />
            欢迎回来 <span className="eyebrow-divider" /> 星图已同步
          </div>
          <h1>
            你好，
            <span className="hero-word">{profile?.nickname ?? '星旅人'}</span>
          </h1>
          <p className="hero-description">
            今天的紫色星图已经为你展开。选一个方向，开始点亮属于你的星辰吧。
          </p>
          <div className="hero-buttons">
            <button className="button button-primary">开始今日学习</button>
            <button className="button button-text" onClick={handleLogout}>退出登录</button>
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 18, alignSelf: 'center' }}>
          <div style={{ padding: 24, background: 'rgba(20,18,32,.48)', border: '1px solid var(--line)', borderRadius: 7 }}>
            <div className="utility" style={{ color: '#a190b3', marginBottom: 8 }}>DAILY GOAL</div>
            <div style={{ fontFamily: 'var(--display-font)', fontSize: 34, color: '#d3c2e8', fontWeight: 400 }}>
              {profile?.dailyGoal ?? 50}
              <span style={{ fontSize: 14, color: 'var(--faint)', marginLeft: 6 }}>词 / 天</span>
            </div>
          </div>
          <div style={{ padding: 24, background: 'rgba(20,18,32,.48)', border: '1px solid var(--line)', borderRadius: 7 }}>
            <div className="utility" style={{ color: '#a190b3', marginBottom: 8 }}>EXAM DATE</div>
            <div style={{ fontFamily: 'var(--display-font)', fontSize: 28, color: '#d3c2e8', fontWeight: 400 }}>
              {profile?.examDate ?? '2026-12-14'}
            </div>
            <div style={{ fontSize: 13, color: 'var(--faint)', marginTop: 6 }}>距考试约 64 天</div>
          </div>
        </div>
      </section>
    </div>
  )
}
