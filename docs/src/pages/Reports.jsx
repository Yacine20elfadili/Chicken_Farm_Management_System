import React, { useState } from 'react'
import './pages.css'

const sampleHistory = [
  { id: 1, title: 'Rap. Summary (2026-03-01)', type: 'Summary', period: '2026-02-01 - 2026-03-01', date: '2026-03-01' },
  { id: 2, title: 'Rap. Production (2026-02-15)', type: 'Production', period: '2026-02-01 - 2026-02-15', date: '2026-02-15' },
  { id: 3, title: 'Rap. Financial (2026-02-01)', type: 'Financial', period: '2026-01-01 - 2026-02-01', date: '2026-02-01' },
  { id: 4, title: 'Rap. Summary (2026-01-15)', type: 'Summary', period: '2025-12-15 - 2026-01-15', date: '2026-01-15' },
]

const sampleData = {
  production: [
    { metric: 'Total Oeufs Bons', value: '456,200', unit: 'oeufs', change: '' },
    { metric: 'Total Oeufs Fêlés', value: '12,450', unit: 'oeufs', change: '' },
    { metric: 'Moyenne Journalière', value: '15,207', unit: 'oeufs/jour', change: '+5%' },
  ],
  financial: [
    { metric: 'Total Recettes', value: '1,245,600.00', unit: 'DH', change: '' },
    { metric: 'Total Dépenses', value: '892,400.00', unit: 'DH', change: '' },
    { metric: 'Bénéfice Net', value: '353,200.00', unit: 'DH', change: '+' },
  ],
  inventory: [
    { metric: 'Effectif Total Poulets', value: '12,450', unit: 'poulets', change: '' },
    { metric: 'Nombre de Bâtiments', value: '8', unit: 'bâtiments', change: '' },
  ],
  personnel: [
    { metric: 'Total Personnel', value: '12', unit: 'personnes', change: '' },
    { metric: 'Administration', value: '3', unit: 'personnes', change: '45,000 DH/mois' },
    { metric: 'Ferme', value: '9', unit: 'personnes', change: '67,500 DH/mois' },
    { metric: 'Masse Salariale Totale', value: '112,500.00', unit: 'DH/mois', change: '' },
  ],
  summary: [
    { metric: 'Total Oeufs Bons', value: '456,200', unit: 'oeufs', change: '' },
    { metric: 'Total Oeufs Fêlés', value: '12,450', unit: 'oeufs', change: '' },
    { metric: 'Moyenne Journalière', value: '15,207', unit: 'oeufs/jour', change: '+5%' },
    { metric: 'Total Recettes', value: '1,245,600.00', unit: 'DH', change: '' },
    { metric: 'Total Dépenses', value: '892,400.00', unit: 'DH', change: '' },
    { metric: 'Effectif Total', value: '12', unit: 'personnes', change: '' },
    { metric: 'Effectif Total Poulets', value: '12,450', unit: 'poulets', change: '' },
    { metric: 'Nombre de Bâtiments', value: '8', unit: 'bâtiments', change: '' },
  ],
}

function Reports() {
  const [reportType, setReportType] = useState('Summary')
  const [activeTab, setActiveTab] = useState('dashboard')

  const getReportData = () => {
    switch (reportType) {
      case 'Production': return sampleData.production
      case 'Financial': return sampleData.financial
      case 'Inventory': return sampleData.inventory
      case 'Personnel': return sampleData.personnel
      default: return sampleData.summary
    }
  }

  const handleGenerate = () => {
    console.log('Generate report:', reportType)
  }

  const handleExport = () => {
    console.log('Export PDF')
  }

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Rapports & Analyses</h1>
          <p className="header-subtitle">Analysez les performances et générez des rapports détaillés</p>
        </div>
      </header>

      <div className="content-area">
        {/* Filter Bar */}
        <div className="reports-filter-bar">
          <select 
            className="filter-select"
            value={reportType}
            onChange={(e) => setReportType(e.target.value)}
          >
            <option value="Production">Production</option>
            <option value="Financial">Financial</option>
            <option value="Inventory">Inventory</option>
            <option value="Personnel">Personnel</option>
            <option value="Summary">Summary</option>
          </select>
          <input type="date" className="date-input" defaultValue="2026-02-01" />
          <span className="date-separator">à</span>
          <input type="date" className="date-input" defaultValue="2026-03-02" />
          <button className="btn btn-save" onClick={handleGenerate}>Générer Analysis</button>
          <div className="flex-spacer"></div>
          <button className="btn btn-cancel" onClick={handleExport}>Exporter PDF</button>
        </div>

        {/* Tabs */}
        <div className="reports-tabs">
          <button 
            className={`tab-btn ${activeTab === 'dashboard' ? 'active' : ''}`}
            onClick={() => setActiveTab('dashboard')}
          >
            Tableau de Bord
          </button>
          <button 
            className={`tab-btn ${activeTab === 'history' ? 'active' : ''}`}
            onClick={() => setActiveTab('history')}
          >
            Historique des Rapports
          </button>
        </div>

        {/* Tab Content */}
        {activeTab === 'dashboard' && (
          <div className="reports-dashboard">
            {/* Charts Row */}
            <div className="charts-row">
              <div className="chart-card">
                <h3 className="card-title">Tendances</h3>
                <div className="bar-chart">
                  <div className="bar-group">
                    <div className="bar" style={{ height: '70%' }}></div>
                    <span className="bar-label">Jan</span>
                  </div>
                  <div className="bar-group">
                    <div className="bar" style={{ height: '85%' }}></div>
                    <span className="bar-label">Fév</span>
                  </div>
                  <div className="bar-group">
                    <div className="bar" style={{ height: '65%' }}></div>
                    <span className="bar-label">Mar</span>
                  </div>
                  <div className="bar-group">
                    <div className="bar" style={{ height: '90%' }}></div>
                    <span className="bar-label">Avr</span>
                  </div>
                </div>
              </div>
              <div className="chart-card">
                <h3 className="card-title">Répartition</h3>
                <div className="pie-chart">
                  <div className="pie-segment" style={{ '--percent': '70%', backgroundColor: '#27ae60' }}></div>
                  <div className="pie-segment" style={{ '--percent': '30%', backgroundColor: '#e74c3c' }}></div>
                </div>
                <div className="chart-legend">
                  <div className="legend-item"><span className="legend-color" style={{ backgroundColor: '#27ae60' }}></span> Bons</div>
                  <div className="legend-item"><span className="legend-color" style={{ backgroundColor: '#e74c3c' }}></span> Fêlés</div>
                </div>
              </div>
            </div>

            {/* Data Table */}
            <div className="data-table-card">
              <h3 className="card-title">Détails Chiffrés</h3>
              <table className="report-table">
                <thead>
                  <tr>
                    <th>Métrique</th>
                    <th>Valeur</th>
                    <th>Unité</th>
                    <th>Var.</th>
                  </tr>
                </thead>
                <tbody>
                  {getReportData().map((row, index) => (
                    <tr key={index}>
                      <td>{row.metric}</td>
                      <td className={row.change === '+' ? 'positive' : row.change === '-' ? 'negative' : ''}>
                        {row.value}
                      </td>
                      <td>{row.unit}</td>
                      <td className={row.change && row.change.startsWith('+') ? 'positive' : ''}>
                        {row.change}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}

        {activeTab === 'history' && (
          <div className="history-card">
            <table className="report-table">
              <thead>
                <tr>
                  <th>#</th>
                  <th>Titre</th>
                  <th>Type</th>
                  <th>Période</th>
                  <th>Généré le</th>
                </tr>
              </thead>
              <tbody>
                {sampleHistory.map((item) => (
                  <tr key={item.id}>
                    <td>{item.id}</td>
                    <td>{item.title}</td>
                    <td><span className="type-badge">{item.type}</span></td>
                    <td>{item.period}</td>
                    <td>{item.date}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </>
  )
}

export default Reports
