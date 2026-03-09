import React from 'react'
import './pages.css'

const sampleTasks = [
  { id: 1, title: 'Vaccination Bâtiment A1', description: 'Vaccin HB1 pour poussins', dueDate: '2026-03-05', status: 'pending', priority: 'high' },
  { id: 2, title: 'Nettoyage hangar principal', description: 'Nettoyage et désinfection', dueDate: '2026-03-03', status: 'done', priority: 'medium' },
  { id: 3, title: 'Commande nourriture', description: 'Commander 100 sacs Poussins Starter', dueDate: '2026-03-10', status: 'pending', priority: 'low' },
  { id: 4, title: 'Contrôle température', description: 'Vérifier capteurs température bâtiments', dueDate: '2026-03-02', status: 'missed', priority: 'high' },
  { id: 5, title: 'Réparation abreuvoirs', description: 'Remplacer abreuvoirs défectueux B3', dueDate: '2026-03-08', status: 'pending', priority: 'medium' },
  { id: 6, title: 'Formation personnel', description: 'Session formation hygiene', dueDate: '2026-03-15', status: 'pending', priority: 'low' },
]

const stats = {
  total: 6,
  done: 1,
  pending: 4,
  missed: 1,
}

function Tasks() {
  const getStatusBadge = (status) => {
    switch (status) {
      case 'done': return 'status-done'
      case 'pending': return 'status-pending'
      case 'missed': return 'status-missed'
      default: return ''
    }
  }

  const getStatusText = (status) => {
    switch (status) {
      case 'done': return 'Terminée'
      case 'pending': return 'En Attente'
      case 'missed': return 'Manquée'
      default: return status
    }
  }

  const getPriorityClass = (priority) => {
    switch (priority) {
      case 'high': return 'priority-high'
      case 'medium': return 'priority-medium'
      case 'low': return 'priority-low'
      default: return ''
    }
  }

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Gestion des Tâches</h1>
          <p className="header-subtitle">Suivez et gérez toutes les tâches quotidiennes de la ferme</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-sell">➕ Nouvelle Tâche</button>
          <button className="btn btn-save">🔄 Actualiser</button>
        </div>
      </header>

      <div className="content-area">
        {/* Statistics Cards */}
        <div className="tasks-stats">
          <div className="task-stat-card">
            <div className="task-stat-icon">📋</div>
            <div className="task-stat-title">Tâches Totales</div>
            <div className="task-stat-value">{stats.total}</div>
            <div className="task-stat-desc">tâches planifiées</div>
          </div>
          <div className="task-stat-card done-card">
            <div className="task-stat-icon">✅</div>
            <div className="task-stat-title">Terminées</div>
            <div className="task-stat-value">{stats.done}</div>
            <div className="task-stat-desc">réalisées</div>
          </div>
          <div className="task-stat-card pending-card">
            <div className="task-stat-icon">⏳</div>
            <div className="task-stat-title">En Attente</div>
            <div className="task-stat-value">{stats.pending}</div>
            <div className="task-stat-desc">à faire</div>
          </div>
          <div className="task-stat-card missed-card">
            <div className="task-stat-icon">⚠️</div>
            <div className="task-stat-title">Manquées</div>
            <div className="task-stat-value">{stats.missed}</div>
            <div className="task-stat-desc">en retard</div>
          </div>
        </div>

        {/* Task List */}
        <div className="tasks-list-card">
          <h2 className="card-title">Liste des Tâches</h2>
          <div className="task-list">
            {sampleTasks.map((task) => (
              <div key={task.id} className={`task-item ${task.status}`}>
                <div className="task-checkbox">
                  <input type="checkbox" checked={task.status === 'done'} readOnly />
                </div>
                <div className="task-content">
                  <div className="task-header">
                    <span className="task-title">{task.title}</span>
                    <span className={`priority-badge ${getPriorityClass(task.priority)}`}>
                      {task.priority === 'high' ? 'Haute' : task.priority === 'medium' ? 'Moyenne' : 'Basse'}
                    </span>
                  </div>
                  <div className="task-description">{task.description}</div>
                  <div className="task-footer">
                    <span className="task-date">📅 {task.dueDate}</span>
                    <span className={`status-badge ${getStatusBadge(task.status)}`}>
                      {getStatusText(task.status)}
                    </span>
                  </div>
                </div>
                <div className="task-actions">
                  <button className="action-btn" title="Modifier">✏</button>
                  <button className="action-btn action-delete" title="Supprimer">🗑</button>
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>
    </>
  )
}

export default Tasks
