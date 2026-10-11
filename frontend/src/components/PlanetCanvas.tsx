import { useEffect, useRef } from 'react'

/**
 * 气态巨行星 + 光环 —— 完全复刻兽星 ANIMA art.js planet()
 * 值噪声生成星球表面条带 + 漫反射光照 + 边缘高光 + 前后光环
 */
export default function PlanetCanvas() {
  const ref = useRef<HTMLCanvasElement>(null)

  useEffect(() => {
    const canvas = ref.current
    if (!canvas || canvas.dataset.painted) return
    canvas.dataset.painted = 'true'
    canvas.width = 780; canvas.height = 660
    const ctx = canvas.getContext('2d')!
    const cx = 390, cy = 330, radius = 168

    const glow = ctx.createRadialGradient(cx - 65, cy - 45, 90, cx, cy, 310)
    glow.addColorStop(0, 'rgba(129,104,190,.13)')
    glow.addColorStop(1, 'rgba(81,65,133,0)')
    ctx.fillStyle = glow; ctx.fillRect(0, 0, 780, 660)

    function hash(a: number, b: number) { const n = Math.sin(a * 127.1 + b * 311.7) * 43758.5453; return n - Math.floor(n) }
    function noise(x: number, y: number) {
      const ix = Math.floor(x), iy = Math.floor(y), fx = x - ix, fy = y - iy
      const u = fx * fx * (3 - 2 * fx), v = fy * fy * (3 - 2 * fy)
      const a = hash(ix, iy), b = hash(ix + 1, iy), c = hash(ix, iy + 1), d = hash(ix + 1, iy + 1)
      return (a * (1 - u) + b * u) * (1 - v) + (c * (1 - u) + d * u) * v
    }

    function rings(front: boolean) {
      ctx.save(); ctx.translate(cx, cy); ctx.rotate(-.36)
      for (let r = 230; r <= 354; r += .8) {
        const density = .11 + noise(r * .11, 4) * .34
        const gap = (r > 271 && r < 282) || (r > 313 && r < 319)
        ctx.strokeStyle = `rgba(${front ? '163,147,192' : '124,108,159'},${gap ? .035 : density * (front ? .9 : .5)})`
        ctx.lineWidth = .65
        ctx.beginPath(); ctx.ellipse(0, 0, r, r * .265, 0, front ? 0 : Math.PI, front ? Math.PI : Math.PI * 2); ctx.stroke()
      }
      ctx.restore()
    }
    rings(false)

    const surface = document.createElement('canvas'); surface.width = surface.height = radius * 2
    const surfaceCtx = surface.getContext('2d')!
    const img = surfaceCtx.createImageData(radius * 2, radius * 2)
    for (let y = 0; y < radius * 2; y++) {
      for (let x = 0; x < radius * 2; x++) {
        const nx = (x - radius) / radius, ny = (y - radius) / radius
        const z2 = 1 - nx * nx - ny * ny
        if (z2 <= 0) continue
        const z = Math.sqrt(z2), lon = Math.atan2(nx, z), lat = Math.asin(ny)
        const n = noise(lon * 4 + 11, lat * 10 + 20) * .58 + noise(lon * 17, lat * 28) * .27 + noise(lon * 50, lat * 90) * .15
        const band = Math.sin(lat * 33 + n * 5) * .5 + .5
        const fine = noise(lon * 150 + 5, lat * 220 + 7)
        const diffuse = Math.max(0, nx * -.62 + ny * -.38 + z * .64)
        const light = .045 + diffuse * .88
        const rim = Math.pow(1 - z, 4) * diffuse * .3
        const p = (y * radius * 2 + x) * 4
        img.data[p] = (84 + n * 54 + band * 19 + fine * 11) * light + rim * 90
        img.data[p + 1] = (72 + n * 42 + band * 14 + fine * 10) * light + rim * 73
        img.data[p + 2] = (119 + n * 58 + band * 25 + fine * 13) * light + rim * 135
        img.data[p + 3] = Math.min(255, z2 * radius * 255)
      }
    }
    surfaceCtx.putImageData(img, 0, 0); ctx.drawImage(surface, cx - radius, cy - radius)
    rings(true)
  }, [])

  return <canvas ref={ref} className="hero-planet" />
}
