import './pages.css'

const sampleData = {
  houses: [
    { id: 2, name: 'Bâtiment 2', type: 'Pondeuses', eggsCollected: 850, cracked: 12, date: '02/03/2026' },
    { id: 3, name: 'Bâtiment 3', type: 'Femelles à Viande', eggsCollected: 620, cracked: 8, date: '02/03/2026' },
  ],
  storage: {
    totalEggs: 45620,
    status: 'ok',
    percentage: 45.6,
  },
  production: {
    today: 8320,
    week: 58240,
    month: 249600,
  },
  cracked: {
    today: 145,
    week: 892,
    month: 3680,
  },
}

function HouseEggCard({ house }) {
  return (
    <div className="house-egg-card">
      <div className="house-egg-card-header">
        <span className="house-egg-card-title">{house.name} - {house.type}</span>
      </div>
      <div className="house-egg-card-details">
        <div className="house-egg-detail">
          <span className="house-egg-label">🥚 Œufs du jour :</span>
          <span className="house-egg-value production">{house.eggsCollected.toLocaleString()}</span>
        </div>
        <div className="house-egg-detail">
          <span className="house-egg-label">⚠ Cracked :</span>
          <span className="house-egg-value cracked">{house.cracked}</span>
        </div>
        <div className="house-egg-detail">
          <span className="house-egg-label">📅 Date :</span>
          <span className="house-egg-value date">{house.date}</span>
        </div>
      </div>
    </div>
  )
}

function StorageCard({ storage }) {
  const getStatusClass = (status) => {
    switch (status) {
      case 'ok': return 'storage-status-ok'
      case 'warning': return 'storage-status-warning'
      case 'critical': return 'storage-status-critical'
      default: return 'storage-status-ok'
    }
  }

  const getStatusText = (status, percentage) => {
    switch (status) {
      case 'ok': return `Stock OK (${percentage.toFixed(1)}%)`
      case 'warning': return `Stock Remplissage (${percentage.toFixed(1)}%)`
      case 'critical': return `Stock Complet (${percentage.toFixed(1)}%)`
      default: return `Stock OK (${percentage.toFixed(1)}%)`
    }
  }

  return (
    <div className="storage-egg-card">
      <div className="storage-egg-header">
        <span className="storage-egg-title">Stock d'œufs</span>
        <span className={`storage-egg-status ${getStatusClass(storage.status)}`}>
          {getStatusText(storage.status, storage.percentage)}
        </span>
        <button className="btn btn-sell">💰 Vendre</button>
      </div>
      <div className="storage-egg-content">
        <span className="storage-egg-count">{storage.totalEggs.toLocaleString()}</span>
        <span className="storage-egg-icon">🥚</span>
      </div>
    </div>
  )
}

function StatCard({ title, data, type }) {
  return (
    <div className={`stat-card ${type === 'production' ? 'production-stats-card' : 'cracked-stats-card'}`}>
      <div className="stat-card-title">{title}</div>
      <div className="stat-card-content">
        <div className="stat-item">
          <span className="stat-label">📅 Aujourd'hui</span>
          <span className={`stat-value ${type}`}>{data.today.toLocaleString()}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">📊 Cette Semaine</span>
          <span className={`stat-value ${type}`}>{data.week.toLocaleString()}</span>
        </div>
        <div className="stat-item">
          <span className="stat-label">🗓 Ce Mois</span>
          <span className={`stat-value ${type}`}>{data.month.toLocaleString()}</span>
        </div>
      </div>
    </div>
  )
}

function EggsBay() {
  const hasEggHouses = sampleData.houses.length > 0

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Espace Œufs</h1>
          <p className="header-subtitle">Gérez la collecte, le stockage et la qualité des œufs</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-save">📝 Enregistrer Collecte</button>
        </div>
      </header>

      <div className="content-area">
        {hasEggHouses ? (
          <>
            <div className="house-egg-cards">
              {sampleData.houses.map((house) => (
                <HouseEggCard key={house.id} house={house} />
              ))}
            </div>
          </>
        ) : (
          <div className="no-egg-houses-card">
            <div className="no-egg-houses-content">
              <span className="no-egg-houses-icon">🚫</span>
              <span className="no-egg-houses-title">Aucune maison pondeuse configurée</span>
              <span className="no-egg-houses-subtitle">
                Configurez des bâtiments de type 'Pondeuses' ou 'Femelles à Viande' pour commencer la collecte d'œufs
              </span>
            </div>
          </div>
        )}

        <StorageCard storage={sampleData.storage} />

        <div className="stats-row">
          <StatCard title="Œufs Produits" data={sampleData.production} type="production" />
          <StatCard title="Œufs Cassés" data={sampleData.cracked} type="cracked" />
        </div>
      </div>
    </>
  )
}

export default EggsBay
