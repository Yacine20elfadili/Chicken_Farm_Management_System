import './pages.css'

const sampleHouses = {
  dayOld: [
    { id: 'A1', name: 'House A1', chickens: 4850, age: '3 days', actionLabel: 'Distribute', actionDate: '15/03/2026', status: 'good', capacity: 5000 },
    { id: 'A2', name: 'House A2', chickens: 4920, age: '2 days', actionLabel: 'Distribute', actionDate: '16/03/2026', status: 'good', capacity: 5000 },
  ],
  eggLayer: [
    { id: 'B1', name: 'House B1', chickens: 2850, age: '180 days', actionLabel: 'Transfer', actionDate: '-', status: 'good', capacity: 3000 },
    { id: 'B2', name: 'House B2', chickens: 2780, age: '175 days', actionLabel: 'Transfer', actionDate: '-', status: 'fair', capacity: 3000 },
    { id: 'B3', name: 'House B3', chickens: 2920, age: '168 days', actionLabel: 'Transfer', actionDate: '-', status: 'good', capacity: 3000 },
  ],
  femaleMeat: [
    { id: 'C1', name: 'House C1', chickens: 3780, age: '42 days', actionLabel: 'Sell', actionDate: '15/04/2026', status: 'good', capacity: 4000 },
    { id: 'C2', name: 'House C2', chickens: 3650, age: '35 days', actionLabel: 'Sell', actionDate: '22/04/2026', status: 'good', capacity: 4000 },
  ],
  maleMeat: [
    { id: 'D1', name: 'House D1', chickens: 3920, age: '52 days', actionLabel: 'Sell', actionDate: '05/04/2026', status: 'poor', capacity: 4500 },
    { id: 'D2', name: 'House D2', chickens: 4280, age: '28 days', actionLabel: 'Sell', actionDate: '29/04/2026', status: 'good', capacity: 4500 },
    { id: 'D3', name: 'House D3', chickens: 4350, age: '21 days', actionLabel: 'Sell', actionDate: '06/05/2026', status: 'good', capacity: 4500 },
  ],
}

const mortalityStats = {
  today: 12,
  thisWeek: 47,
  thisMonth: 189,
  total: 892,
}

function HouseCard({ house }) {
  const getStatusColor = (status) => {
    switch (status) {
      case 'good': return '#27ae60'
      case 'fair': return '#f39c12'
      case 'poor': return '#e74c3c'
      default: return '#666'
    }
  }

  const getButtonStyle = (actionLabel) => {
    switch (actionLabel) {
      case 'Distribute': return 'btn-distribute'
      case 'Transfer': return 'btn-transfer'
      case 'Sell': return 'btn-sell-action'
      default: return 'btn-primary'
    }
  }

  return (
    <div className="house-card">
      <div className="house-card-header">
        <span className="house-name">{house.name}</span>
        <span 
          className="house-status" 
          style={{ backgroundColor: getStatusColor(house.status) }}
        >
          {house.status === 'good' ? 'Healthy' : house.status === 'fair' ? 'Observation' : 'Attention'}
        </span>
      </div>
      <div className="house-details">
        <div className="house-detail">
          <span className="house-detail-label">Capacity</span>
          <span className="house-detail-value">{house.capacity}</span>
        </div>
        <div className="house-detail">
          <span className="house-detail-label">Chickens</span>
          <span className="house-detail-value">{house.chickens.toLocaleString()}</span>
        </div>
        <div className="house-detail">
          <span className="house-detail-label">Age</span>
          <span className="house-detail-value" style={{ color: getStatusColor(house.status) }}>{house.age}</span>
        </div>
        <div className="house-detail">
          <span className="house-detail-label">{house.actionLabel}</span>
          <span className="house-detail-value">{house.actionDate}</span>
        </div>
      </div>
      <div className="house-actions">
        <button className={`btn btn-small ${getButtonStyle(house.actionLabel)}`}>
          {house.actionLabel}
        </button>
      </div>
    </div>
  )
}

function HouseSection({ title, icon, houses, bgColor, titleColor, showImport = false }) {
  return (
    <div className="house-section">
      <div className="house-section-header" style={{ backgroundColor: bgColor }}>
        <span className="house-section-title" style={{ color: titleColor }}>
          {icon} {title}
        </span>
        {showImport && (
          <button className="btn btn-import">
            📥 Import
          </button>
        )}
      </div>
      <div className="houses-grid">
        {houses.map((house) => (
          <HouseCard key={house.id} house={house} />
        ))}
      </div>
    </div>
  )
}

function ChickenBay() {
  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Chicken Bay</h1>
          <p className="header-subtitle">Manage chicken houses and lifecycle</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-save">⚙ Config Houses</button>
          <button className="btn btn-death">⚠ Record Death</button>
        </div>
      </header>

      <div className="content-area">
        {/* Mortality Stats Row */}
        <div className="mortality-stats">
          <div className="mortality-stat">
            <div className="mortality-label">Today</div>
            <div className="mortality-value today">{mortalityStats.today}</div>
          </div>
          <div className="mortality-stat">
            <div className="mortality-label">This Week</div>
            <div className="mortality-value week">{mortalityStats.thisWeek}</div>
          </div>
          <div className="mortality-stat">
            <div className="mortality-label">This Month</div>
            <div className="mortality-value month">{mortalityStats.thisMonth}</div>
          </div>
          <div className="mortality-stat">
            <div className="mortality-label">Total</div>
            <div className="mortality-value total">{mortalityStats.total}</div>
          </div>
        </div>

        {/* DayOld Houses Section */}
        <HouseSection
          title="DayOld-House(s)"
          icon="🐣"
          houses={sampleHouses.dayOld}
          bgColor="#e3f2fd"
          titleColor="#1565c0"
          showImport={true}
        />

        {/* FemaleEggLayer Houses Section */}
        <HouseSection
          title="FemaleEggLayer-House(s)"
          icon="🥚"
          houses={sampleHouses.eggLayer}
          bgColor="#fce4ec"
          titleColor="#c2185b"
        />

        {/* FemaleMeat Houses Section */}
        <HouseSection
          title="FemaleMeat-House(s)"
          icon="🐓"
          houses={sampleHouses.femaleMeat}
          bgColor="#f3e5f5"
          titleColor="#7b1fa2"
        />

        {/* MaleMeat Houses Section */}
        <HouseSection
          title="MaleMeat-House(s)"
          icon="🐔"
          houses={sampleHouses.maleMeat}
          bgColor="#fff3e0"
          titleColor="#e65100"
        />
      </div>
    </>
  )
}

export default ChickenBay
