import { useEffect, useState } from 'react'
import { BrowserRouter, Navigate, Route, Routes, useLocation } from 'react-router-dom'
import CosmosBackground from './components/CosmosBackground'
import LoginPage from './pages/LoginPage'
import DemoApp from './demo/DemoApp'
import AppShell from './app/AppShell'
import TodayPage from './app/TodayPage'
import StudyPage from './app/StudyPage'
import TestPage from './app/TestPage'
import StarsPage from './app/StarsPage'

function AppContent() {
  const location = useLocation()
  const isDemo = location.pathname.startsWith('/demo')
  const [motion, setMotion] = useState(() =>
    localStorage.getItem('starvocab_demo_motion') !== 'off' &&
    !window.matchMedia('(prefers-reduced-motion: reduce)').matches,
  )

  useEffect(() => {
    localStorage.setItem('starvocab_demo_motion', motion ? 'on' : 'off')
  }, [motion])
  useEffect(() => {
    document.body.dataset.demoScene = isDemo ? (location.pathname.split('/')[2] || 'login') : ''
  }, [isDemo, location.pathname])

  return (
    <>
      <CosmosBackground enabled={motion} />
      <div className="grain" />
      {isDemo ? <DemoApp motion={motion} setMotion={setMotion} /> : <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route element={<AppShell motion={motion} setMotion={setMotion} />}>
          <Route path="/today" element={<TodayPage />} />
          <Route path="/study" element={<StudyPage />} />
          <Route path="/test" element={<TestPage />} />
          <Route path="/stars" element={<StarsPage />} />
        </Route>
        <Route path="/home" element={<Navigate to="/today" replace />} />
        <Route path="/" element={<Navigate to="/today" replace />} />
        <Route path="*" element={<Navigate to="/today" replace />} />
      </Routes>}
    </>
  )
}

export default function App() {
  return <BrowserRouter><AppContent /></BrowserRouter>
}
