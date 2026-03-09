import React from 'react'
import './pages.css'

function Settings() {
  const handleSave = () => {
    console.log('Save settings')
  }

  const handleDelete = () => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer toutes les données? Cette action est irréversible.')) {
      console.log('Delete account')
    }
  }

  return (
    <>
      <header className="header">
        <h1 className="header-title">Paramètres de la Ferme</h1>
      </header>

      <div className="content-area">
        <div className="settings-container">
          {/* General Settings */}
          <div className="settings-card">
            <h2 className="settings-section-title">Informations Générales</h2>
            
            <div className="settings-form">
              <div className="form-row">
                <div className="form-group">
                  <label>Nom de la Société</label>
                  <input type="text" placeholder="Nom de la société" defaultValue="Ferme Avicole Atlas" />
                </div>
                <div className="form-group">
                  <label>Téléphone</label>
                  <input type="text" placeholder="+212..." defaultValue="+212 661-234567" />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label>Adresse</label>
                  <input type="text" placeholder="Adresse complète" defaultValue="Route de Meknès, Km 12" />
                </div>
                <div className="form-group">
                  <label>Ville</label>
                  <input type="text" placeholder="Ville" defaultValue="Fès" />
                </div>
              </div>

              <div className="form-group">
                <label>Email</label>
                <input type="email" placeholder="Email" defaultValue="contact@fermeatlas.ma" />
              </div>

              <button className="btn btn-primary" onClick={handleSave}>Enregistrer les modifications</button>
            </div>
          </div>

          {/* Danger Zone */}
          <div className="settings-card danger-zone">
            <h2 className="danger-title">Zone de Danger</h2>
            
            <div className="danger-content">
              <div className="danger-info">
                <h3>Suppression du Compte</h3>
                <p>Cette action supprimera toutes les données de la base de données (animaux, finances, fournisseurs, etc.). Cette action est irréversible.</p>
              </div>
              <button className="btn btn-danger" onClick={handleDelete}>
                Supprimer mon compte et toutes les données
              </button>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Settings
