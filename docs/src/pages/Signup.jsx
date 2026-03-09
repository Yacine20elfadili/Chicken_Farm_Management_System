import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import './Auth.css'

function Signup() {
  const navigate = useNavigate()
  const [formData, setFormData] = useState({
    companyName: '',
    legalForm: 'SARL',
    capitalSocial: '',
    ice: '',
    rc: '',
    fiscalId: '',
    patente: '',
    cnss: '',
    onssa: '',
    address: '',
    city: '',
    postalCode: '',
    bankRIB: '',
    bankName: '',
    phoneNumber: '',
    email: '',
    website: '',
    password: '',
    confirmPassword: '',
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState(false)

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  const handleSignup = (e) => {
    e.preventDefault()
    
    if (formData.password !== formData.confirmPassword) {
      setError('Les mots de passe ne correspondent pas')
      return
    }

    if (!formData.companyName || !formData.ice || !formData.email || !formData.password) {
      setError('Veuillez remplir tous les champs obligatoires')
      return
    }

    // Mock signup - in real app would call API
    setSuccess(true)
    setError('')
    
    // Redirect to login after 2 seconds
    setTimeout(() => {
      navigate('/login')
    }, 2000)
  }

  const handleBackToLogin = () => {
    navigate('/login')
  }

  return (
    <div className="auth-container signup-container">
      <div className="auth-left-panel">
        <div className="brand-content">
          <div className="brand-logo">F</div>
          <h1 className="brand-title">Poultry Farm</h1>
          <p className="brand-subtitle">Management</p>
          <p className="brand-tagline">Inscription Entreprise Marocaine</p>
        </div>
      </div>

      <div className="auth-right-panel signup-panel">
        <div className="signup-scroll">
          <div className="auth-form-container">
            <div className="auth-header">
              <h2 className="auth-title">Créer un Compte Entreprise</h2>
              <p className="auth-subtitle">Inscrivez votre entreprise avicole</p>
            </div>

            <form className="auth-form" onSubmit={handleSignup}>
              {/* Company Information */}
              <div className="form-section">
                <h3 className="section-title">📋 Informations de l'Entreprise</h3>
                
                <div className="input-group">
                  <label className="input-label">Nom de Société *</label>
                  <input 
                    type="text" 
                    name="companyName"
                    className="text-field"
                    placeholder="Ex: Ferme Avicole Al Amal SARL"
                    value={formData.companyName}
                    onChange={handleChange}
                  />
                </div>

                <div className="form-row">
                  <div className="input-group">
                    <label className="input-label">Forme Juridique *</label>
                    <select name="legalForm" className="combo-box" value={formData.legalForm} onChange={handleChange}>
                      <option value="SARL">SARL</option>
                      <option value="SA">SA</option>
                      <option value="SNC">SNC</option>
                      <option value="Auto-entrepreneur">Auto-entrepreneur</option>
                    </select>
                  </div>
                  <div className="input-group">
                    <label className="input-label">Capital Social (MAD) *</label>
                    <input 
                      type="text" 
                      name="capitalSocial"
                      className="text-field"
                      placeholder="Ex: 100000"
                      value={formData.capitalSocial}
                      onChange={handleChange}
                    />
                  </div>
                </div>
              </div>

              {/* Legal Identifiers */}
              <div className="form-section">
                <h3 className="section-title">⚖ Identifiants Légaux (Maroc)</h3>
                
                <div className="input-group">
                  <label className="input-label-critical">⚠ ICE (Identifiant Commun Entreprise) *</label>
                  <input 
                    type="text" 
                    name="ice"
                    className="text-field text-field-critical"
                    placeholder="15 chiffres - Ex: 002532678000045"
                    value={formData.ice}
                    onChange={handleChange}
                  />
                  <span className="input-hint-critical">OBLIGATOIRE sur toutes les factures - 15 chiffres exactement</span>
                </div>

                <div className="form-row">
                  <div className="input-group">
                    <label className="input-label">Registre de Commerce (RC) *</label>
                    <input 
                      type="text" 
                      name="rc"
                      className="text-field"
                      placeholder="Ex: RC 12345 Casablanca"
                      value={formData.rc}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="input-group">
                    <label className="input-label">Identifiant Fiscal (IF) *</label>
                    <input 
                      type="text" 
                      name="fiscalId"
                      className="text-field"
                      placeholder="7-8 chiffres"
                      value={formData.fiscalId}
                      onChange={handleChange}
                    />
                  </div>
                </div>

                <div className="form-row">
                  <div className="input-group">
                    <label className="input-label">Patente *</label>
                    <input 
                      type="text" 
                      name="patente"
                      className="text-field"
                      placeholder="Numéro de patente"
                      value={formData.patente}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="input-group">
                    <label className="input-label">CNSS (si employés)</label>
                    <input 
                      type="text" 
                      name="cnss"
                      className="text-field"
                      placeholder="7-9 chiffres (optionnel)"
                      value={formData.cnss}
                      onChange={handleChange}
                    />
                  </div>
                </div>

                <div className="input-group">
                  <label className="input-label-critical">⚠ Autorisation ONSSA *</label>
                  <input 
                    type="text" 
                    name="onssa"
                    className="text-field text-field-critical"
                    placeholder="Ex: ONSSA-AV-2024-0123"
                    value={formData.onssa}
                    onChange={handleChange}
                  />
                  <span className="input-hint-critical">OBLIGATOIRE pour les fermes avicoles</span>
                </div>
              </div>

              {/* Address */}
              <div className="form-section">
                <h3 className="section-title">📍 Adresse du Siège Social</h3>
                
                <div className="input-group">
                  <label className="input-label">Adresse Complète *</label>
                  <input 
                    type="text" 
                    name="address"
                    className="text-field"
                    placeholder="Ex: 123 Avenue Mohammed V, Quartier Industriel"
                    value={formData.address}
                    onChange={handleChange}
                  />
                </div>

                <div className="form-row">
                  <div className="input-group">
                    <label className="input-label">Ville *</label>
                    <input 
                      type="text" 
                      name="city"
                      className="text-field"
                      placeholder="Ex: Casablanca"
                      value={formData.city}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="input-group">
                    <label className="input-label">Code Postal *</label>
                    <input 
                      type="text" 
                      name="postalCode"
                      className="text-field"
                      placeholder="5 chiffres"
                      value={formData.postalCode}
                      onChange={handleChange}
                    />
                  </div>
                </div>
              </div>

              {/* Banking */}
              <div className="form-section">
                <h3 className="section-title">🏦 Informations Bancaires</h3>
                
                <div className="input-group">
                  <label className="input-label-critical">⚠ RIB Bancaire *</label>
                  <input 
                    type="text" 
                    name="bankRIB"
                    className="text-field text-field-critical"
                    placeholder="24 chiffres - Ex: 230780000012345678901234"
                    value={formData.bankRIB}
                    onChange={handleChange}
                  />
                  <span className="input-hint-critical">CRITIQUE pour les paiements - 24 chiffres exactement</span>
                </div>

                <div className="input-group">
                  <label className="input-label">Nom de la Banque *</label>
                  <input 
                    type="text" 
                    name="bankName"
                    className="text-field"
                    placeholder="Ex: Attijariwafa Bank, BMCE, Banque Populaire"
                    value={formData.bankName}
                    onChange={handleChange}
                  />
                </div>
              </div>

              {/* Contact */}
              <div className="form-section">
                <h3 className="section-title">📞 Contact</h3>
                
                <div className="form-row">
                  <div className="input-group">
                    <label className="input-label">Téléphone *</label>
                    <input 
                      type="text" 
                      name="phoneNumber"
                      className="text-field"
                      placeholder="+212 522-123456"
                      value={formData.phoneNumber}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="input-group">
                    <label className="input-label">Email *</label>
                    <input 
                      type="email" 
                      name="email"
                      className="text-field"
                      placeholder="contact@ferme.ma"
                      value={formData.email}
                      onChange={handleChange}
                    />
                  </div>
                </div>

                <div className="input-group">
                  <label className="input-label">Site Web (optionnel)</label>
                  <input 
                    type="text" 
                    name="website"
                    className="text-field"
                    placeholder="https://www.ferme-avicole.ma"
                    value={formData.website}
                    onChange={handleChange}
                  />
                </div>
              </div>

              {/* Account Credentials */}
              <div className="form-section">
                <h3 className="section-title">🔐 Informations de Connexion</h3>
                
                <div className="form-row">
                  <div className="input-group">
                    <label className="input-label">Mot de Passe *</label>
                    <input 
                      type="password" 
                      name="password"
                      className="password-field"
                      placeholder="Minimum 6 caractères"
                      value={formData.password}
                      onChange={handleChange}
                    />
                  </div>
                  <div className="input-group">
                    <label className="input-label">Confirmer *</label>
                    <input 
                      type="password" 
                      name="confirmPassword"
                      className="password-field"
                      placeholder="Retapez le mot de passe"
                      value={formData.confirmPassword}
                      onChange={handleChange}
                    />
                  </div>
                </div>
              </div>

              <div className="auth-actions">
                <button type="submit" className="auth-button signup-button">
                  Créer le Compte
                </button>
                <button type="button" className="secondary-button" onClick={handleBackToLogin}>
                  ← Retour à la Connexion
                </button>
              </div>
            </form>

            {success && <div className="success-message">Inscription réussie! Redirection...</div>}
            {error && <div className="error-message">{error}</div>}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Signup
