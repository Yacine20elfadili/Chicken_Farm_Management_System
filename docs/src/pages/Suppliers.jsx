import './pages.css'

const sampleSuppliers = [
  { id: 1, name: 'Alimentation Plus', category: 'Nourriture', contact: 'Mohammed Ali', phone: '+212 661-234567', email: 'contact@alimentationplus.ma', ice: '001234567000089', active: true },
  { id: 2, name: 'Veterinex SARL', category: 'Médicaments', contact: 'Fatima Zahra', phone: '+212 662-345678', email: 'info@veterinex.ma', ice: '001234567000090', active: true },
  { id: 3, name: 'Matériel Agricole Fès', category: 'Équipement', contact: 'Ahmed Rami', phone: '+212 663-456789', email: 'ahmed@magricole.ma', ice: '001234567000091', active: true },
  { id: 4, name: 'EcoLitière Bio', category: 'Litière', contact: 'Youssef Bennani', phone: '+212 664-567890', email: 'youssef@ecoli.triere.ma', ice: '001234567000092', active: false },
  { id: 5, name: 'Poules & Co', category: 'Poussins', contact: 'Samira El Amrani', phone: '+212 665-678901', email: 'samira@poulesco.ma', ice: '001234567000093', active: true },
]

function Suppliers() {
  const handleEdit = (supplier) => {
    console.log('Edit supplier:', supplier)
  }

  const handleDelete = (supplier) => {
    console.log('Delete supplier:', supplier)
  }

  const handleExportPDF = () => {
    console.log('Export PDF')
  }

  const handleExportIndividual = (supplier) => {
    console.log('Export individual PDF:', supplier)
  }

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Gestion des Fournisseurs</h1>
          <p className="header-subtitle">Gérez vos fournisseurs et partenaires</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-save">+ Nouveau Fournisseur</button>
          <button className="btn btn-cancel" onClick={handleExportPDF}>Exporter PDF</button>
        </div>
      </header>

      <div className="content-area">
        <div className="table-card">
          <table className="suppliers-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Nom / Entreprise</th>
                <th>Catégorie</th>
                <th>Contact</th>
                <th>Téléphone</th>
                <th>Email</th>
                <th>ICE</th>
                <th>Statut</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {sampleSuppliers.map((supplier) => (
                <tr key={supplier.id} className={!supplier.active ? 'row-inactive' : ''}>
                  <td>{supplier.id}</td>
                  <td>{supplier.name}</td>
                  <td>{supplier.category}</td>
                  <td>{supplier.contact}</td>
                  <td>{supplier.phone}</td>
                  <td>{supplier.email}</td>
                  <td>{supplier.ice}</td>
                  <td>
                    <span className={`status-badge ${supplier.active ? 'status-active' : 'status-inactive'}`}>
                      {supplier.active ? 'Actif' : 'Inactif'}
                    </span>
                  </td>
                  <td>
                    <div className="action-buttons">
                      {supplier.active && (
                        <button 
                          className="btn btn-warning-small"
                          onClick={() => handleEdit(supplier)}
                        >
                          Modifier
                        </button>
                      )}
                      <button 
                        className="btn btn-pdf-small"
                        onClick={() => handleExportIndividual(supplier)}
                        title="Exporter Fiche"
                      >
                        📄
                      </button>
                      <button 
                        className={`btn ${supplier.active ? 'btn-danger-small' : 'btn-restore-small'}`}
                        onClick={() => handleDelete(supplier)}
                      >
                        {supplier.active ? 'Supprimer' : 'Restaurer'}
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

export default Suppliers
