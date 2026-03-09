import React from 'react'
import './pages.css'

const sampleData = {
  stats: {
    admin: 3,
    veterinary: '1/2',
    inventory: '1/1',
    farmhand: '1/5',
  },
  administration: [
    { id: 1, name: 'Mohammed Tahiri', role: 'Propriétaire', phone: '+212 661-111111', email: 'mohammed@farm.ma', isMissing: false },
    { id: 2, name: 'Fatima El Amrani', role: 'Caissier', phone: '+212 662-222222', email: 'fatima@farm.ma', isMissing: false },
    { id: 3, name: 'Youssef Khaldi', role: 'Admin Staff', phone: '+212 663-333333', email: 'youssef@farm.ma', positions: ['Comptabilité', 'Relations Clients'], isMissing: false },
  ],
  veterinary: [
    { id: 4, name: 'Dr. Ahmed Rami', role: 'Vétérinaire (Superviseur)', phone: '+212 664-444444', email: 'ahmed.rami@vet.ma', subordinates: 1, isMissing: false },
    { id: 5, name: 'Samira Bennis', role: 'Vétérinaire', phone: '+212 665-555555', email: 'samira@vet.ma', isMissing: false },
  ],
  inventory: [
    { id: 6, name: 'Ali El Fassi', role: 'Inventaire (Superviseur)', phone: '+212 666-666666', email: 'ali@farm.ma', subordinates: 0, isMissing: false },
  ],
  farmhand: [
    { id: 7, name: 'Mohamed Ouali', role: 'Ouvrier (Superviseur)', phone: '+212 667-777777', email: 'mohamed.ouali@farm.ma', subordinates: 4, isMissing: false },
    { id: 8, name: 'Rachid Amrani', role: 'Ouvrier Agricole', phone: '+212 668-888888', email: 'rachid@farm.ma', isMissing: false },
    { id: 9, name: 'Hamza Tahiri', role: 'Ouvrier Agricole', phone: '+212 669-999999', email: 'hamza@farm.ma', isMissing: false },
    { id: 10, name: 'Karim Bennani', role: 'Ouvrier Agricole', phone: '+212 670-101010', email: 'karim@farm.ma', isMissing: false },
    { id: 11, name: 'Issam El Amrani', role: 'Ouvrier Agricole', phone: '+212 671-111111', email: 'issam@farm.ma', isMissing: false },
  ],
}

function StatCard({ title, value, color }) {
  return (
    <div className="personnel-stat-card" style={{ borderColor: color }}>
      <div className="stat-card-title" style={{ color }}>{title}</div>
      <div className="stat-card-value" style={{ color }}>{value}</div>
      <div className="stat-card-subtitle">superviseur/total</div>
    </div>
  )
}

function PersonnelCard({ person, color, showSubordinates = false }) {
  const getActionButtons = () => (
    <div className="personnel-actions">
      <button className="action-btn" title="Voir">👁</button>
      <button className="action-btn" title="Modifier">✏</button>
      <button className="action-btn" title="Payer Salaire">💵</button>
      <button className="action-btn action-delete" title={person.subordinates > 0 ? 'Supprimer' : 'Supprimer'}>🗑</button>
    </div>
  )

  if (person.isMissing) {
    return (
      <div className="personnel-card missing-card">
        <div className="missing-icon">👤</div>
        <div className="missing-title">{person.role}</div>
        <div className="missing-subtitle">Cliquez sur + Ajouter pour créer</div>
      </div>
    )
  }

  return (
    <div className="personnel-card" style={{ borderColor: color }}>
      <div className="personnel-card-header">
        <span className="role-badge" style={{ backgroundColor: color }}>{person.role}</span>
        {person.subordinates > 0 && <span className="supervisor-icon">⭐</span>}
      </div>
      <div className="personnel-name">{person.name}</div>
      <div className="personnel-info">
        <div className="info-item">👤 {person.age || 35} ans</div>
        <div className="info-item">📞 {person.phone}</div>
        <div className="info-item">✉️ {person.email}</div>
        {person.positions && (
          <div className="info-item positions">
            Positions: {person.positions.map((pos, i) => (
              <span key={i} className="position-badge">{pos}</span>
            ))}
          </div>
        )}
        {showSubordinates && person.subordinates > 0 && (
          <div className="info-item subordinates">👥 {person.subordinates} subordonné(s)</div>
        )}
      </div>
      {getActionButtons()}
    </div>
  )
}

function Personnel() {
  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Gestion du Personnel</h1>
          <p className="header-subtitle">Administration et Personnel de Ferme</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-sell">➕ Ajouter Personnel</button>
          <button className="btn btn-sell">➕ Generer carte</button>
          <button className="btn btn-save">🔄 Actualiser</button>
        </div>
      </header>

      <div className="content-area">
        {/* Statistics Cards */}
        <div className="personnel-stats-row">
          <StatCard title="Admin" value={sampleData.stats.admin} color="#007bff" />
          <StatCard title="Veterinaire" value={sampleData.stats.veterinary} color="#17a2b8" />
          <StatCard title="Inventaire" value={sampleData.stats.inventory} color="#6f42c1" />
          <StatCard title="Ouvriers" value={sampleData.stats.farmhand} color="#fd7e14" />
        </div>

        {/* Administration Section */}
        <div className="personnel-section">
          <h2 className="section-title" style={{ color: '#007bff' }}>Administration</h2>
          <div className="personnel-cards">
            {sampleData.administration.map((person) => (
              <PersonnelCard key={person.id} person={person} color="#007bff" />
            ))}
          </div>
        </div>

        {/* Farm Section */}
        <div className="personnel-section">
          {/* Veterinary Team */}
          <div className="personnel-subsection" style={{ backgroundColor: '#e8f4f8' }}>
            <h2 className="section-title" style={{ color: '#17a2b8' }}>Equipe Veterinaire</h2>
            <div className="personnel-cards">
              {sampleData.veterinary.map((person) => (
                <PersonnelCard key={person.id} person={person} color="#17a2b8" showSubordinates={true} />
              ))}
            </div>
          </div>

          {/* Inventory Team */}
          <div className="personnel-subsection" style={{ backgroundColor: '#f3eefa' }}>
            <h2 className="section-title" style={{ color: '#6f42c1' }}>Equipe Inventaire</h2>
            <div className="personnel-cards">
              {sampleData.inventory.map((person) => (
                <PersonnelCard key={person.id} person={person} color="#6f42c1" showSubordinates={true} />
              ))}
            </div>
          </div>

          {/* Farmhand Team */}
          <div className="personnel-subsection" style={{ backgroundColor: '#fff3e6' }}>
            <h2 className="section-title" style={{ color: '#fd7e14' }}>Equipe Ouvriers Agricoles</h2>
            <div className="personnel-cards">
              {sampleData.farmhand.map((person) => (
                <PersonnelCard key={person.id} person={person} color="#fd7e14" showSubordinates={true} />
              ))}
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Personnel
