/**
 * 站点页头 —— 复刻兽星 ANIMA site-header
 * 品牌符号（轨道星球 SVG）+ 宋体标题 + Courier 英文副标题
 */
export default function SiteHeader() {
  return (
    <header className="site-header">
      <a className="brand" href="#home" aria-label="星词 StarVocab 首页">
        <svg className="brand-symbol" viewBox="0 0 48 48" aria-hidden="true">
          <ellipse cx="24" cy="24" rx="23" ry="8" transform="rotate(-35 24 24)" />
          <circle cx="24" cy="24" r="12" />
          <path d="M24 16v16M16 24h16" />
          <circle cx="41" cy="13" r="2" fill="currentColor" />
        </svg>
        <span className="brand-name">
          星词<span className="brand-en">STARVOCAB</span>
        </span>
      </a>
      <nav className="main-nav" aria-label="主导航">
        <a href="#home" data-nav="home">紫色星图</a>
        <a href="#atlas" data-nav="atlas">词库</a>
        <a href="#about" data-nav="about">关于</a>
      </nav>
      <div className="header-actions">
        <button className="header-start" type="button">
          开启探索 <span aria-hidden="true">↗</span>
        </button>
      </div>
    </header>
  )
}
