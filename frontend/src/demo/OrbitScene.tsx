import { useEffect, useRef } from 'react'

/** Slow 52-second orbit. Back satellites are drawn before the planet, front satellites after it. */
export default function OrbitScene({ motion, compact = false }: { motion: boolean; compact?: boolean }) {
  const ref = useRef<HTMLCanvasElement>(null)
  useEffect(() => {
    const canvas = ref.current
    if (!canvas) return
    const ctx = canvas.getContext('2d')
    if (!ctx) return
    const planet = new Image()
    planet.src = '/starvocab-planet.png'
    let frame = 0
    let elapsed = 0
    let last = 0
    let width = 0
    let height = 0
    const dpr = Math.min(window.devicePixelRatio || 1, 2)

    function position(angle: number, radiusX: number, radiusY: number, centerX: number, centerY: number) {
      const x = Math.cos(angle) * radiusX
      const y = Math.sin(angle) * radiusY
      const tilt = -.34
      return { x: centerX + x * Math.cos(tilt) - y * Math.sin(tilt), y: centerY + x * Math.sin(tilt) + y * Math.cos(tilt), depth: Math.sin(angle) }
    }
    function paint() {
      if (!ctx || !planet.complete || !planet.naturalWidth || !width || !height) return
      ctx.clearRect(0, 0, width, height)
      const cx = width * .53
      const cy = height * .49
      const radius = Math.min(width * .29, height * .34)
      const rx = width * .43
      const ry = height * .23
      const glow = ctx.createRadialGradient(cx, cy, radius * .5, cx, cy, radius * 1.8)
      glow.addColorStop(0, 'rgba(157,102,230,.16)')
      glow.addColorStop(1, 'rgba(157,102,230,0)')
      ctx.fillStyle = glow
      ctx.fillRect(0, 0, width, height)
      const drawRing = (front: boolean) => {
        ctx.save()
        ctx.translate(cx, cy)
        ctx.rotate(-.34)
        for (let i = 0; i < 3; i++) {
          ctx.strokeStyle = front ? `rgba(198,177,230,${.13 + i * .035})` : `rgba(167,128,220,${.06 + i * .015})`
          ctx.lineWidth = i === 1 ? 1.2 : .7
          ctx.beginPath()
          ctx.ellipse(0, 0, rx + i * 7, ry + i * 3, 0, front ? 0 : Math.PI, front ? Math.PI : Math.PI * 2)
          ctx.stroke()
        }
        ctx.restore()
      }
      const satellites = [0, Math.PI * 1.13].map((offset, index) => {
        const angle = elapsed * Math.PI * 2 / (index ? 68 : 52) + offset + .7
        return { ...position(angle, rx, ry, cx, cy), index }
      })
      const drawSatellite = ({ x, y, depth, index }: typeof satellites[number]) => {
        const r = (index ? 8 : 14) * (.72 + (depth + 1) * .19) * (compact ? .7 : 1)
        ctx.save()
        ctx.shadowColor = '#c2a7ec'
        ctx.shadowBlur = 14
        const light = ctx.createRadialGradient(x - r * .3, y - r * .35, 1, x, y, r)
        light.addColorStop(0, '#ebd8ff')
        light.addColorStop(.42, '#aa80e1')
        light.addColorStop(1, '#302149')
        ctx.fillStyle = light
        ctx.beginPath()
        ctx.arc(x, y, r, 0, Math.PI * 2)
        ctx.fill()
        ctx.restore()
      }
      drawRing(false)
      satellites.filter(s => s.depth < 0).forEach(drawSatellite)
      ctx.save()
      ctx.filter = 'saturate(.78) brightness(.82)'
      const size = radius * 2 / .83
      ctx.drawImage(planet, cx - size / 2, cy - size / 2, size, size)
      ctx.restore()
      drawRing(true)
      satellites.filter(s => s.depth >= 0).forEach(drawSatellite)
      if (!compact) {
        ctx.fillStyle = 'rgba(205,185,239,.5)'
        ctx.font = '11px Courier New'
        ctx.fillText('CET–6  /  VOCABULARY ORBIT', width * .44, height * .92)
      }
    }
    function resize() {
      if (!canvas || !ctx) return
      const rect = canvas.getBoundingClientRect()
      width = rect.width
      height = rect.height
      canvas.width = Math.round(width * dpr)
      canvas.height = Math.round(height * dpr)
      ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
      paint()
    }
    function tick(now: number) {
      if (!document.hidden) {
        if (last && motion) elapsed += Math.min(now - last, 100) / 1000
        last = now
        paint()
      } else last = 0
      if (motion) frame = requestAnimationFrame(tick)
    }
    const observer = new ResizeObserver(resize)
    observer.observe(canvas)
    planet.onload = paint
    resize()
    if (motion) frame = requestAnimationFrame(tick)
    return () => { cancelAnimationFrame(frame); observer.disconnect(); planet.onload = null }
  }, [motion, compact])
  return <canvas className={`demo-orbit ${compact ? 'demo-orbit--compact' : ''}`} ref={ref} role="img" aria-label="两颗卫星缓慢围绕紫色星球运行" />
}
