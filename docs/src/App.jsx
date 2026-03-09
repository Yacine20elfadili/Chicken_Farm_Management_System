import { HashRouter as Router, Routes, Route, Outlet } from 'react-router-dom'
import Sidebar from './components/Sidebar'
import Dashboard from './pages/Dashboard'
import ChickenBay from './pages/ChickenBay'
import EggsBay from './pages/EggsBay'
import Suppliers from './pages/Suppliers'
import Customers from './pages/Customers'
import Storage from './pages/Storage'
import FarmDocuments from './pages/FarmDocuments'
import Finances from './pages/Finances'
import Reports from './pages/Reports'
import Tasks from './pages/Tasks'
import Personnel from './pages/Personnel'
import Settings from './pages/Settings'
import Welcome from './pages/Welcome'
import Login from './pages/Login'
import Signup from './pages/Signup'

function AppLayout() {
  return (
    <div className="app-container">
      <Sidebar />
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  )
}

function AuthLayout() {
  return <Outlet />
}

function App() {
  return (
    <Router>
      <Routes>
        {/* Auth Routes - No Sidebar */}
        <Route element={<AuthLayout />}>
          <Route path="/" element={<Login />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
        </Route>

        {/* App Routes - With Sidebar */}
        <Route element={<AppLayout />}>
          <Route path="/welcome" element={<Welcome />} />
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/chicken-bay" element={<ChickenBay />} />
          <Route path="/eggs" element={<EggsBay />} />
          <Route path="/suppliers" element={<Suppliers />} />
          <Route path="/customers" element={<Customers />} />
          <Route path="/storage" element={<Storage />} />
          <Route path="/documents" element={<FarmDocuments />} />
          <Route path="/finances" element={<Finances />} />
          <Route path="/reports" element={<Reports />} />
          <Route path="/tasks" element={<Tasks />} />
          <Route path="/personnel" element={<Personnel />} />
          <Route path="/settings" element={<Settings />} />
        </Route>
      </Routes>
    </Router>
  )
}

export default App
