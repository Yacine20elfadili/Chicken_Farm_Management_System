import './pages.css'

function Dashboard() {
  const chartData = [
    { day: '24/02', value: 7200 },
    { day: '25/02', value: 8000 },
    { day: '26/02', value: 7100 },
    { day: '27/02', value: 8800 },
    { day: '28/02', value: 7600 },
    { day: '01/03', value: 9200 },
    { day: '02/03', value: 8600 },
  ]

  const maxValue = Math.max(...chartData.map(d => d.value))

  return (
    <>
      <header className="header">
        <h1 className="header-title">Tableau de bord</h1>
        <p className="header-subtitle">Bienvenue ! Voici un aperçu de votre ferme.</p>
      </header>

      <div className="content-area">
        <div className="metrics-row">
          <div className="metric-card">
            <div className="metric-card-inner">
              <div className="metric-info">
                <span className="label-cards">Effectif total</span>
                <span className="label-cards-stats">12,450</span>
              </div>
              <div className="card-icon chickens">🐔</div>
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-card-inner">
              <div className="metric-info">
                <span className="label-cards">Œufs du jour</span>
                <span className="label-cards-stats">8,320</span>
              </div>
              <div className="card-icon eggs">🥚</div>
            </div>
          </div>

          <div className="metric-card">
            <div className="metric-card-inner">
              <div className="metric-info">
                <span className="label-cards">Finances (Global)</span>
                <span className="finance-income">Rec: 245,600.00 DH</span>
                <span className="finance-expense">Dép: 128,400.00 DH</span>
              </div>
              <div className="card-icon finance">💰</div>
            </div>
          </div>
        </div>

        <div className="chart-card">
          <div className="cards-title">Production d'Œufs (7 jours)</div>
          
          <div className="chart-container">
            <div className="y-axis">
              <span>10,000</span>
              <span>7,500</span>
              <span>5,000</span>
              <span>2,500</span>
              <span>0</span>
            </div>
            
            <div className="chart-area">
              {chartData.map((data, index) => (
                <div key={index} className="chart-bar-group">
                  <div 
                    className="chart-bar" 
                    style={{ height: `${(data.value / maxValue) * 250}px` }}
                    data-value={data.value.toLocaleString()}
                  ></div>
                  <span className="chart-label">{data.day}</span>
                </div>
              ))}
            </div>
          </div>

          <div className="chart-legend">
            <div className="legend-item">
              <div 
                className="legend-color" 
                style={{ background: 'linear-gradient(to right, #4e73df, #6f85df)' }}
              ></div>
              <span>Œufs collectés</span>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Dashboard
