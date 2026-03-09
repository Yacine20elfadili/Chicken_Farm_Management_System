import React from 'react'
import './pages.css'

const sampleCustomers = [
  { id: 1, name: 'Mohammed Tahiri', company: 'SuperMarché Atlas', type: 'Entreprise', contact: 'Mohammed Tahiri', phone: '+212 661-111111', ice: '001234567000001', balance: 12500.00, visits: 15, active: true },
  { id: 2, name: 'Fatima El Amrani', company: null, type: 'Particulier', contact: 'Fatima El Amrani', phone: '+212 662-222222', ice: null, balance: 0.00, visits: 3, active: true },
  { id: 3, name: 'Youssef Benali', company: 'Restaurant La Perle', type: 'Entreprise', contact: 'Youssef Benali', phone: '+212 663-333333', ice: '001234567000002', balance: 8200.50, visits: 28, active: true },
  { id: 4, name: 'Ahmed Khaldi', company: null, type: 'Particulier', contact: 'Ahmed Khaldi', phone: '+212 664-444444', ice: null, balance: 0.00, visits: 1, active: false },
  { id: 5, name: 'Sara Bennis', company: 'Hotel Royal', type: 'Entreprise', contact: 'Sara Bennis', phone: '+212 665-555555', ice: '001234567000003', balance: 45000.00, visits: 42, active: true },
]

function Customers() {
  const [filterType, setFilterType] = React.useState('Tous')

  const handleEdit = (customer) => {
    console.log('Edit customer:', customer)
  }

  const handleDelete = (customer) => {
    console.log('Delete customer:', customer)
  }

  const handleExportPDF = () => {
    console.log('Export PDF')
  }

  const handleExportIndividual = (customer) => {
    console.log('Export individual PDF:', customer)
  }

  const filteredCustomers = filterType === 'Tous' 
    ? sampleCustomers 
    : sampleCustomers.filter(c => c.type === filterType)

  const getTypeBadgeClass = (type) => {
    return type === 'Entreprise' ? 'type-badge-company' : 'type-badge-individual'
  }

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Gestion des Clients</h1>
          <p className="header-subtitle">Gérez vos clients et partenaires commerciaux</p>
        </div>
        <div className="header-actions">
          <select 
            className="filter-select"
            value={filterType}
            onChange={(e) => setFilterType(e.target.value)}
          >
            <option value="Tous">Tous</option>
            <option value="Entreprise">Entreprise</option>
            <option value="Particulier">Particulier</option>
          </select>
          <button className="btn btn-save">+ Nouveau Client</button>
          <button className="btn btn-cancel" onClick={handleExportPDF}>📄 Exporter PDF</button>
        </div>
      </header>

      <div className="content-area">
        <div className="table-card">
          <table className="customers-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nom / Entreprise</th>
                <th>Type</th>
                <th>Contact</th>
                <th>Téléphone</th>
                <th>ICE</th>
                <th>Solde Impayé</th>
                <th>Visites</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredCustomers.map((customer) => (
                <tr key={customer.id} className={!customer.active ? 'row-inactive' : ''}>
                  <td>{customer.id}</td>
                  <td>
                    {customer.company 
                      ? `${customer.company} (${customer.name})` 
                      : customer.name}
                  </td>
                  <td>
                    <span className={`type-badge ${getTypeBadgeClass(customer.type)}`}>
                      {customer.type}
                    </span>
                  </td>
                  <td>{customer.contact}</td>
                  <td>{customer.phone}</td>
                  <td>{customer.ice || '-'}</td>
                  <td className={customer.balance > 0 ? 'balance-owing' : ''}>
                    {customer.balance.toLocaleString('fr-MA', { style: 'currency', currency: 'MAD' })}
                  </td>
                  <td>{customer.visits}</td>
                  <td>
                    <span className={`status-badge ${customer.active ? 'status-active' : 'status-inactive'}`}>
                      {customer.active ? 'Actif' : 'Inactif'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      {customer.active && (
                        <button 
                          className="btn btn-warning-small"
                          onClick={() => handleEdit(customer)}
                        >
                          Modifier
                        </button>
                      )}
                      <button 
                        className="btn btn-pdf-small"
                        onClick={() => handleExportIndividual(customer)}
                        title="Exporter Fiche"
                      >
                        📄
                      </button>
                      <button 
                        className={`btn ${customer.active ? 'btn-danger-small' : 'btn-restore-small'}`}
                        onClick={() => handleDelete(customer)}
                      >
                        {customer.active ? 'Supprimer' : 'Restaurer'}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </>
  )
}

export default Customers
