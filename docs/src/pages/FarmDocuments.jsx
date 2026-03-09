import React, { useState } from 'react'
import './pages.css'

const sampleSuppliers = ['Alimentation Plus', 'Veterinex SARL', 'Matériel Agricole Fès']
const sampleCustomers = ['SuperMarché Atlas', 'Restaurant La Perle', 'Hotel Royal']
const sampleEmployees = ['Mohammed Tahiri', 'Fatima El Amrani', 'Youssef Khaldi']

function FarmDocuments() {
  const [activeSection, setActiveSection] = useState('commercial')
  const [expandedPanel, setExpandedPanel] = useState(null)

  const commercialDocs = [
    { id: 'bc', title: '📋 Bon de Commande', description: 'Créer un bon de commande fournisseur' },
    { id: 'devis', title: '💰 Devis', description: 'Générer un devis pour client' },
    { id: 'bl', title: '🚚 Bon de Livraison', description: 'Créer un bon de livraison' },
    { id: 'facture', title: '🧾 Facture', description: 'Générer une facture' },
    { id: 'avoir', title: '↩️ Avoir', description: 'Créer une note de crédit' },
    { id: 'recu', title: '🧾 Reçu', description: 'Imprimer un reçu' },
  ]

  const hrDocs = [
    { id: 'bulletin', title: '💵 Bulletin de Paie', description: 'Générer un bulletin de salaire' },
    { id: 'contrat', title: '📝 Contrat de Travail', description: 'Créer un contrat de travail' },
    { id: 'attestation', title: '📋 Attestation de Travail', description: 'Générer une attestation' },
  ]

  const togglePanel = (panelId) => {
    setExpandedPanel(expandedPanel === panelId ? null : panelId)
  }

  const DocumentPanel = ({ doc }) => (
    <div className={`doc-panel ${expandedPanel === doc.id ? 'expanded' : ''}`}>
      <div className="doc-panel-header" onClick={() => togglePanel(doc.id)}>
        <span className="doc-panel-title">{doc.title}</span>
        <span className="doc-panel-toggle">{expandedPanel === doc.id ? '▼' : '▶'}</span>
      </div>
      {expandedPanel === doc.id && (
        <div className="doc-panel-content">
          <p className="doc-description">{doc.description}</p>
          <DocForm docId={doc.id} />
        </div>
      )}
    </div>
  )

  const DocForm = ({ docId }) => {
    switch (docId) {
      case 'bc':
        return (
          <div className="doc-form">
            <div className="form-group">
              <label>Type:</label>
              <div className="toggle-group">
                <button className="btn btn-sell active">Initial</button>
                <button className="btn btn-primary">Final</button>
              </div>
            </div>
            <div className="form-group">
              <label>Fournisseur:</label>
              <select><option>Sélectionner un fournisseur</option>{sampleSuppliers.map(s => <option key={s}>{s}</option>)}</select>
            </div>
            <div className="form-group">
              <label>Articles commandés:</label>
              <table className="items-table">
                <thead><tr><th>Réf</th><th>Description</th><th>Quantité</th><th>Unité</th></tr></thead>
                <tbody><tr><td colSpan="4" className="empty-row">Aucun article</td></tr></tbody>
              </table>
              <div className="form-buttons">
                <button className="btn btn-save">+ Ajouter</button>
                <button className="btn btn-death">- Supprimer</button>
              </div>
            </div>
            <div className="form-group">
              <label>Date de livraison souhaitée:</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Notes / Conditions:</label>
              <textarea placeholder="Conditions particulières..."></textarea>
            </div>
            <button className="btn btn-cancel generate-btn">📄 Générer PDF</button>
          </div>
        )
      case 'devis':
        return (
          <div className="doc-form">
            <div className="form-group">
              <label>Client:</label>
              <select><option>Sélectionner un client</option>{sampleCustomers.map(c => <option key={c}>{c}</option>)}</select>
            </div>
            <div className="form-group">
              <label>Valide jusqu'au:</label>
              <input type="date" />
            </div>
            <div className="form-group">
              <label>Produits / Services:</label>
              <table className="items-table">
                <thead><tr><th>Réf</th><th>Description</th><th>Qté</th><th>Prix HT</th><th>Total HT</th></tr></thead>
                <tbody><tr><td colSpan="5" className="empty-row">Aucun article</td></tr></tbody>
              </table>
              <div className="form-buttons">
                <button className="btn btn-save">+ Ajouter</button>
                <button className="btn btn-death">- Supprimer</button>
              </div>
            </div>
            <div className="totals-section">
              <div className="total-row"><span>Total HT:</span><span>0.00 DH</span></div>
              <div className="total-row"><span>TVA (20%):</span><span>0.00 DH</span></div>
              <div className="total-row total-ttc"><span>Total TTC:</span><span>0.00 DH</span></div>
            </div>
            <button className="btn btn-cancel generate-btn">📄 Générer PDF</button>
          </div>
        )
      case 'bulletin':
        return (
          <div className="doc-form">
            <div className="form-row">
              <div className="form-group">
                <label>Employé:</label>
                <select><option>Sélectionner un employé</option>{sampleEmployees.map(e => <option key={e}>{e}</option>)}</select>
              </div>
              <div className="form-group">
                <label>Mois:</label>
                <select><option>Mars 2026</option></select>
              </div>
              <div className="form-group">
                <label>Année:</label>
                <input type="text" defaultValue="2026" />
              </div>
            </div>
            <p className="info-text">Détails calculés automatiquement depuis les données RH</p>
            <button className="btn btn-cancel generate-btn">📄 Générer Bulletin</button>
          </div>
        )
      default:
        return (
          <div className="doc-form">
            <p className="info-text">Formulaire pour {docId}</p>
            <button className="btn btn-cancel generate-btn">📄 Générer Document</button>
          </div>
        )
    }
  }

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Documents de la Ferme</h1>
          <p className="header-subtitle">Générez et gérez vos documents commerciaux et RH</p>
        </div>
      </header>

      <div className="content-area">
        <div className="doc-sections">
          <div className="doc-section">
            <h2 className="section-title">Documents Commerciaux</h2>
            <div className="doc-panels">
              {commercialDocs.map(doc => <DocumentPanel key={doc.id} doc={doc} />)}
            </div>
          </div>

          <div className="doc-section">
            <h2 className="section-title">Documents RH</h2>
            <div className="doc-panels">
              {hrDocs.map(doc => <DocumentPanel key={doc.id} doc={doc} />)}
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default FarmDocuments
