import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import './Auth.css'

function Login() {
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const handleLogin = (e) => {
    e.preventDefault()
    if (!email || !password) {
      setError('Veuillez remplir tous les champs')
      return
    }
    // Mock login - in real app would call API
    if (email === 'admin@farm.ma' && password === 'admin123') {
      navigate('/welcome')
    } else {
      setError('E-mail ou mot de passe incorrect.')
    }
  }

  const handleSignUpLink = () => {
    navigate('/signup')
  }

  return (
    <div className="auth-container">
      <div className="auth-left-panel">
        <div className="brand-content">
          <div className="brand-logo">F</div>
          <h1 className="brand-title">Poultry Farm</h1>
          <p className="brand-subtitle">Management</p>
          <p className="brand-tagline">Gérez votre ferme avicole avec efficacité</p>
        </div>
      </div>

      <div className="auth-right-panel">
        <div className="auth-form-container login-form-center">
          <div className="auth-header">
            <h2 className="auth-title">Connexion</h2>
            <p className="auth-subtitle">Accédez à votre tableau de bord</p>
          </div>

          <form className="auth-form" onSubmit={handleLogin}>
            <div className="input-group">
              <label className="input-label">E-Mail</label>
              <input 
                type="email" 
                className="text-field"
                placeholder="Entrez votre e-mail"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="input-group">
              <label className="input-label">Mot de passe</label>
              <input 
                type="password" 
                className="password-field"
                placeholder="Entrez votre mot de passe"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
              />
            </div>

            <button type="submit" className="auth-button">
              Se Connecter
            </button>

            <div className="signup-link-container">
              <span className="signup-link-text">Vous n'avez pas de compte?</span>
              <button type="button" className="link-button" onClick={handleSignUpLink}>
                S'inscrire
              </button>
            </div>
          </form>

          {error && <div className="error-message">{error}</div>}
        </div>
      </div>
    </div>
  )
}

export default Login
