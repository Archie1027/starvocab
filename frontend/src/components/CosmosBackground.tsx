import { useEffect, useRef } from 'react'

/**
 * 紫色星云宇宙背景 —— 完全复刻兽星 ANIMA cosmos.js
 * 噪声生成的星云云团 + 视差漂浮 + 星辰闪烁
 */
export default function CosmosBackground({ enabled = true }: { enabled?: boolean }) {
  const canvasRef = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = canvasRef.current
    if (!canvas) return
    const ctx = canvas.getContext('2d', { alpha: false })
    if (!ctx) return

    let width = 0, height = 0, frame = 0, last = 0, elapsed = 0
    let pointerX = 0, pointerY = 0
    let seed = 8273
    const random = () => { seed = (seed * 16807) % 2147483647; return (seed - 1) / 2147483646 }
    const stars = Array.from({ length: 235 }, () => ({
      x: random(), y: random(), radius: .35 + random() * 1.05,
      alpha: .18 + random() * .62, phase: random() * Math.PI * 2, depth: random(),
    }))

    // 预生成星云云团（值噪声 + 山脊 + 边缘衰减）
    const cloud = document.createElement('canvas')
    cloud.width = 540; cloud.height = 360
    const cloudCtx = cloud.getContext('2d')!
    function hash(x: number, y: number) { const n = Math.sin(x * 127.1 + y * 311.7) * 43758.5453; return n - Math.floor(n) }
    function noise(x: number, y: number) {
      const ix = Math.floor(x), iy = Math.floor(y), fx = x - ix, fy = y - iy
      const u = fx * fx * (3 - 2 * fx), v = fy * fy * (3 - 2 * fy)
      return ((1 - u) * hash(ix, iy) + u * hash(ix + 1, iy)) * (1 - v) + ((1 - u) * hash(ix, iy + 1) + u * hash(ix + 1, iy + 1)) * v
    }
    const pixels = cloudCtx.createImageData(540, 360)
    for (let y = 0; y < 360; y++) {
      for (let x = 0; x < 540; x++) {
        const nx = x / 540, ny = y / 360
        const n = noise(nx * 5 + 2, ny * 5) * .52 + noise(nx * 12, ny * 12 + 9) * .29 + noise(nx * 29, ny * 29) * .13 + noise(nx * 75, ny * 75) * .06
        const ridge = Math.exp(-Math.pow((ny - .68 + nx * .36 + Math.sin(nx * 8) * .065) / .18, 2))
        const edge = Math.sin(nx * Math.PI) * Math.sin(ny * Math.PI)
        const density = Math.pow(n, 2.4) * ridge * edge
        const i = (y * 540 + x) * 4
        pixels.data[i] = 94 + n * 53
        pixels.data[i + 1] = 71 + nx * 38
        pixels.data[i + 2] = 150 + n * 42
        pixels.data[i + 3] = density * 140
      }
    }
    cloudCtx.putImageData(pixels, 0, 0)

    function paint() {
      ctx!.fillStyle = '#090a13'; ctx!.fillRect(0, 0, width, height)
      const t = elapsed / 1000
      const driftX = Math.sin(t * .016) * 45, driftY = Math.cos(t * .012) * 22
      ctx!.save(); ctx!.globalAlpha = .62
      ctx!.drawImage(cloud, -width * .16 + driftX, -height * .05 + driftY, width * 1.4, height * 1.25)
      ctx!.globalAlpha = .28
      ctx!.translate(width * .48, height * .45); ctx!.rotate(-.5 + Math.sin(t * .006) * .028)
      ctx!.drawImage(cloud, -width * .65, -height * .6, width * 1.5, height * 1.5); ctx!.restore()
      for (const s of stars) {
        const x = ((s.x + t * .000008 * (s.depth + .2)) % 1) * width + pointerX * s.depth * 5
        const y = ((s.y - t * .000006 * (s.depth + .2) + 10) % 1) * height + pointerY * s.depth * 5
        const alpha = s.alpha * (.82 + Math.sin(t * .45 + s.phase) * .18)
        ctx!.fillStyle = `rgba(217,220,240,${alpha})`; ctx!.beginPath(); ctx!.arc(x, y, s.radius, 0, Math.PI * 2); ctx!.fill()
        if (s.radius > 1.22) {
          ctx!.strokeStyle = `rgba(196,186,230,${alpha * .23})`; ctx!.lineWidth = .5
          ctx!.beginPath(); ctx!.moveTo(x - 3.5, y); ctx!.lineTo(x + 3.5, y); ctx!.moveTo(x, y - 3.5); ctx!.lineTo(x, y + 3.5); ctx!.stroke()
        }
      }
    }
    function animate(time: number) {
      if (document.hidden) { frame = 0; return }
      if (time - last >= 1000 / 30) { elapsed += last ? Math.min(time - last, 80) : 0; last = time; paint() }
      frame = requestAnimationFrame(animate)
    }
    function resize() {
      width = window.innerWidth; height = window.innerHeight
      const dpr = Math.min(window.devicePixelRatio || 1, 1.5)
      canvas!.width = Math.round(width * dpr); canvas!.height = Math.round(height * dpr)
      ctx!.setTransform(dpr, 0, 0, dpr, 0, 0); paint()
    }
    const onPointerMove = (e: PointerEvent) => {
      pointerX = e.clientX / width - .5; pointerY = e.clientY / height - .5
    }
    const onVisibilityChange = () => {
      cancelAnimationFrame(frame); frame = 0; last = 0
      if (!document.hidden && enabled) frame = requestAnimationFrame(animate)
    }
    window.addEventListener('resize', resize, { passive: true })
    window.addEventListener('pointermove', onPointerMove, { passive: true })
    document.addEventListener('visibilitychange', onVisibilityChange)
    resize()
    if (enabled) frame = requestAnimationFrame(animate)
    return () => {
      cancelAnimationFrame(frame)
      window.removeEventListener('resize', resize)
      window.removeEventListener('pointermove', onPointerMove)
      document.removeEventListener('visibilitychange', onVisibilityChange)
    }
  }, [enabled])

  return <canvas id="cosmos" ref={canvasRef} />
}
