import './pages.css'

const sampleData = {
  feeds: [
    { id: 1, name: 'Poussins Starter', quantity: 2500, unit: 'kg', type: 'Starters', lowStock: false },
    { id: 2, name: 'Croissance', quantity: 4200, unit: 'kg', type: 'Croissance', lowStock: false },
    { id: 3, name: 'Pondeuses', quantity: 1800, unit: 'kg', type: 'Production', lowStock: true },
    { id: 4, name: 'Viande Finition', quantity: 3500, unit: 'kg', type: 'Finition', lowStock: false },
  ],
  medications: [
    { id: 1, name: 'Vitamine ADEK', quantity: 50, unit: 'flacons', type: 'Vitamines', expiry: '2026-06-15', supplier: 'Veterinex SARL', expired: false, lowStock: false },
    { id: 2, name: 'Antibiotique XL', quantity: 25, unit: 'flacons', type: 'Antibiotiques', expiry: '2026-03-01', supplier: 'PharmaVet', expired: false, lowStock: true },
    { id: 3, name: 'Vaccin HB1', quantity: 100, unit: 'doses', type: 'Vaccins', expiry: '2025-12-01', supplier: 'Veterinex SARL', expired: true, lowStock: false },
    { id: 4, name: 'Anti-parasitaire', quantity: 40, unit: 'litres', type: 'Anti-parasitaires', expiry: '2027-01-20', supplier: 'AgriMed', expired: false, lowStock: false },
  ],
  equipment: [
    { id: 1, name: 'Mangeoires', category: 'Équipement d\'élevage', count: 150, location: 'Hangar A', notes: '' },
    { id: 2, name: 'Abreuvoirs', category: 'Équipement d\'élevage', count: 120, location: 'Hangar A', notes: '' },
    { id: 3, name: 'Chauffages', category: 'Climatisation', count: 25, location: 'Stock Principal', notes: '5 en réparation' },
    { id: 4, name: 'Ventilateurs', category: 'Climatisation', count: 40, location: 'Stock Principal', notes: '' },
    { id: 5, name: 'Balais', category: 'Nettoyage', count: 30, location: 'Local Ménage', notes: '' },
  ],
}

function FeedItem({ feed }) {
  return (
    <div className={`storage-list-item ${feed.lowStock ? 'low-stock' : ''}`}>
      <div className="storage-item-info">
        <span className="storage-item-name">{feed.name}</span>
        <span className="storage-item-details">{feed.quantity} {feed.unit} ({feed.type})</span>
      </div>
      {feed.lowStock && <span className="stock-warning">⚠️ STOCK BAS</span>}
    </div>
  )
}

function MedicationItem({ medication }) {
  const getStatusClass = () => {
    if (medication.expired) return 'expired'
    if (medication.lowStock) return 'low-stock'
    return 'normal'
  }

  return (
    <div className={`storage-list-item ${getStatusClass()}`}>
      <div className="storage-item-info">
        <span className="storage-item-name">{medication.name}</span>
        <span className="storage-item-details">
          {medication.quantity} {medication.unit} ({medication.type})
        </span>
        <span className="storage-item-expiry">
          Expire: {medication.expiry} | Fournisseur: {medication.supplier}
        </span>
      </div>
      {medication.expired && <span className="stock-warning expired-badge">🚫 EXPIRÉ</span>}
      {medication.lowStock && !medication.expired && <span className="stock-warning">⚠️ STOCK BAS</span>}
    </div>
  )
}

function EquipmentCard({ equipment }) {
  return (
    <div className="equipment-category-card">
      <div className="equipment-card-header">
        <span className="equipment-card-name">{equipment.name}</span>
        <span className="equipment-card-category">{equipment.category}</span>
      </div>
      <div className="equipment-card-details">
        <div className="equipment-detail">
          <span className="equipment-label">Quantité:</span>
          <span className="equipment-value">{equipment.count}</span>
        </div>
        <div className="equipment-detail">
          <span className="equipment-label">📍</span>
          <span className="equipment-value">{equipment.location}</span>
        </div>
        {equipment.notes && (
          <div className="equipment-detail">
            <span className="equipment-label">Note:</span>
            <span className="equipment-value notes">{equipment.notes}</span>
          </div>
        )}
      </div>
      <button className="btn btn-primary-small">Modifier Catégorie</button>
    </div>
  )
}

function Storage() {
  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Gestion du Stockage</h1>
          <p className="header-subtitle">Gérez vos stocks d'aliments, médicaments et équipements</p>
        </div>
      </header>

      <div className="content-area">
        <div className="storage-sections">
          {/* Feed and Medications Row */}
          <div className="storage-row">
            {/* Feed Card */}
            <div className="storage-card-section">
              <div className="storage-section-header">
                <div className="storage-section-icon">🌾</div>
                <h3 className="storage-section-title">Stock Aliments</h3>
                <div className="storage-section-count">
                  <span className="count-label">Types</span>
                  <span className="count-value">{sampleData.feeds.length}</span>
                </div>
              </div>
              <div className="storage-list">
                {sampleData.feeds.map((feed) => (
                  <FeedItem key={feed.id} feed={feed} />
                ))}
              </div>
              <div className="storage-buttons">
                <button className="btn btn-primary">Ajouter Aliment</button>
                <button className="btn btn-secondary">Utiliser</button>
              </div>
            </div>

            {/* Medications Card */}
            <div className="storage-card-section">
              <div className="storage-section-header">
                <div className="storage-section-icon">💊</div>
                <h3 className="storage-section-title">Médicaments</h3>
                <div className="storage-section-count">
                  <span className="count-label">Total</span>
                  <span className="count-value">{sampleData.medications.length}</span>
                </div>
              </div>
              <div className="storage-list">
                {sampleData.medications.map((medication) => (
                  <MedicationItem key={medication.id} medication={medication} />
                ))}
              </div>
              <div className="storage-buttons">
                <button className="btn btn-primary">Ajouter Médicament</button>
                <button className="btn btn-secondary">Utiliser</button>
              </div>
            </div>
          </div>

          {/* Equipment Section */}
          <div className="storage-card-section equipment-section">
            <div className="storage-section-header">
              <div className="storage-section-icon">🔧</div>
              <h3 className="storage-section-title">Équipements</h3>
            </div>
            <div className="equipment-grid">
              {sampleData.equipment.map((item) => (
                <EquipmentCard key={item.id} equipment={item} />
              ))}
            </div>
            <div className="storage-buttons">
              <button className="btn btn-primary">Ajouter Équipement</button>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Storage
