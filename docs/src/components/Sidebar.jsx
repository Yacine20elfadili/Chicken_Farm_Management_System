import { useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import './Sidebar.css'

const navItems = [
  { path: '/dashboard', icon: '📊', label: 'Tableau De Bord' },
  { path: '/chicken-bay', icon: '🐔', label: 'Poulailler' },
  { path: '/eggs', icon: '🥚', label: 'Œufs' },
  { path: '/suppliers', icon: '📦', label: 'Fournisseurs' },
  { path: '/customers', icon: '👤', label: 'Clients' },
  { path: '/storage', icon: '📦', label: 'Réserve' },
  { path: '/documents', icon: '📄', label: 'Documents' },
  { path: '/finances', icon: '💰', label: 'Finances' },
  { path: '/reports', icon: '📊', label: 'Rapports' },
  { path: '/tasks', icon: '✓', label: 'Tâches' },
  { path: '/personnel', icon: '👥', label: 'Personnel' },
]

function Sidebar() {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()

  const handleLogout = () => {
    navigate('/login')
  }

  const toggleSidebar = () => {
    setCollapsed(!collapsed)
  }

  return (
    <aside className={`sidebar ${collapsed ? 'collapsed' : ''}`}>
      <div className="sidebar-header">
        <div className="sidebar-logo">F</div>
        {!collapsed && (
          <div className="sidebar-title">
            Farm<br />Management
          </div>
        )}
      </div>

      <nav className="sidebar-nav">
        {navItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
          >
            <span className="nav-icon">{item.icon}</span>
            {!collapsed && <span className="nav-label">{item.label}</span>}
          </NavLink>
        ))}
      </nav>

      <button className="menu-toggle" onClick={toggleSidebar}>
        {collapsed ? '>' : '<'}
      </button>

      <div className="sidebar-bottom">
        <NavLink
          to="/settings"
          className={({ isActive }) => `nav-item ${isActive ? 'active' : ''}`}
        >
          <span className="nav-icon">⚙</span>
          {!collapsed && <span className="nav-label">Paramètres</span>}
        </NavLink>
        <div className="nav-item logout" onClick={handleLogout}>
          <span className="nav-icon">🚪</span>
          {!collapsed && <span className="nav-label">Déconnexion</span>}
        </div>
      </div>
    </aside>
  )
}

export default Sidebar
