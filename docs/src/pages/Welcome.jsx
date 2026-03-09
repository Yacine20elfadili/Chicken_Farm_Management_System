import { Link } from 'react-router-dom'
import './pages.css'

function Welcome() {
  return (
    <>
      <header className="header">
        <h1 className="header-title">Bienvenue</h1>
        <p className="header-subtitle">Chicken Farm Management System</p>
      </header>

      <div className="content-area">
        <div className="welcome-page">
          <div className="welcome-card">
            <h1>Welcome to CFMS!!</h1>
            <p>Chicken Farm Management System</p>
            <div className="welcome-subtitle">
              Select an option from the sidebar to get started
            </div>
            <div className="welcome-actions">
              <Link to="/dashboard" className="btn btn-primary">
                Go to Dashboard
              </Link>
            </div>
          </div>
        </div>
      </div>
    </>
  )
}

export default Welcome
