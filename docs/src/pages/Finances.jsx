import React, { useState } from 'react'
import './pages.css'

const sampleTransactions = [
  { id: 1, date: '2026-03-01', type: 'Income', category: 'Vente Œufs', entity: 'SuperMarché Atlas', amount: 24500.00, description: 'Vente œufs taille M' },
  { id: 2, date: '2026-03-01', type: 'Expense', category: 'Nourriture', entity: 'Alimentation Plus', amount: 12500.00, description: 'Poussins Starter 50 sacs' },
  { id: 3, date: '2026-02-28', type: 'Income', category: 'Vente Œufs', entity: 'Hotel Royal', amount: 18200.00, description: 'Vente œufs taille L' },
  { id: 4, date: '2026-02-28', type: 'Expense', category: 'Médicaments', entity: 'Veterinex SARL', amount: 4500.00, description: 'Vitamines et antibiotiques' },
  { id: 5, date: '2026-02-27', type: 'Income', category: 'Vente Poussins', entity: 'Particulier', amount: 3200.00, description: 'Poussins jour 500 unités' },
  { id: 6, date: '2026-02-27', type: 'Expense', category: 'Salaires', entity: 'Personnel', amount: 35000.00, description: 'Masse salariale février' },
  { id: 7, date: '2026-02-26', type: 'Income', category: 'Vente Fumier', entity: 'Agriculteur', amount: 1200.00, description: 'Fumier composté 10 tonnes' },
  { id: 8, date: '2026-02-25', type: 'Expense', category: 'Équipement', entity: 'Matériel Agricole', amount: 8900.00, description: 'Abreuvoirs automatiques' },
]

function Finances() {
  const [typeFilter, setTypeFilter] = useState('All')
  const [entityFilter, setEntityFilter] = useState('All')

  const summary = {
    totalIncome: 47100.00,
    totalExpense: 65400.00,
    netBalance: -18300.00,
    outstanding: 45620.00,
  }

  const filteredTransactions = sampleTransactions.filter(t => {
    if (typeFilter !== 'All' && t.type !== typeFilter) return false
    if (entityFilter !== 'All' && t.entity !== entityFilter) return false
    return true
  })

  const getTypeClass = (type) => type === 'Income' ? 'type-income' : 'type-expense'

  const handleExport = () => {
    console.log('Export all to PDF')
  }

  return (
    <>
      <header className="header">
        <div className="header-left">
          <h1 className="header-title">Suivi Financier</h1>
          <p className="header-subtitle">Gestion et suivi des transactions financières</p>
        </div>
        <div className="header-actions">
          <button className="btn btn-cancel" onClick={handleExport}>📥 Exporter Tout (PDF)</button>
        </div>
      </header>

      <div className="content-area">
        {/* Summary Cards */}
        <div className="finances-summary">
          <div className="summary-card income-card">
            <span className="summary-icon">📈</span>
            <span className="summary-label">Recettes</span>
            <span className="summary-value income">{summary.totalIncome.toLocaleString('fr-MA', { style: 'currency', currency: 'MAD' })}</span>
          </div>
          <div className="summary-card expense-card">
            <span className="summary-icon">📉</span>
            <span className="summary-label">Dépenses</span>
            <span className="summary-value expense">{summary.totalExpense.toLocaleString('fr-MA', { style: 'currency', currency: 'MAD' })}</span>
          </div>
          <div className="summary-card balance-card">
            <span className="summary-icon">💵</span>
            <span className="summary-label">Solde Net</span>
            <span className={`summary-value ${summary.netBalance >= 0 ? 'income' : 'expense'}`}>
              {summary.netBalance.toLocaleString('fr-MA', { style: 'currency', currency: 'MAD' })}
            </span>
          </div>
          <div className="summary-card outstanding-card">
            <span className="summary-icon">⏳</span>
            <span className="summary-label">Impayés Clients</span>
            <span className="summary-value outstanding">{summary.outstanding.toLocaleString('fr-MA', { style: 'currency', currency: 'MAD' })}</span>
          </div>
        </div>

        {/* Filter Bar */}
        <div className="finances-filter">
          <span className="filter-label">Filtrer:</span>
          <select value={typeFilter} onChange={(e) => setTypeFilter(e.target.value)}>
            <option value="All">Tous les types</option>
            <option value="Income">Recettes</option>
            <option value="Expense">Dépenses</option>
          </select>
          <select value={entityFilter} onChange={(e) => setEntityFilter(e.target.value)}>
            <option value="All">Tous les tiers</option>
            <option value="SuperMarché Atlas">SuperMarché Atlas</option>
            <option value="Hotel Royal">Hotel Royal</option>
            <option value="Alimentation Plus">Alimentation Plus</option>
            <option value="Veterinex SARL">Veterinex SARL</option>
          </select>
        </div>

        {/* Transactions Table */}
        <div className="finances-table-card">
          <table className="finances-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Date</th>
                <th>Type</th>
                <th>Catégorie</th>
                <th>Tiers</th>
                <th>Montant</th>
                <th>Description</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredTransactions.map((t) => (
                <tr key={t.id}>
                  <td>{t.id}</td>
                  <td>{t.date}</td>
                  <td><span className={`type-badge ${getTypeClass(t.type)}`}>{t.type === 'Income' ? 'Recette' : 'Dépense'}</span></td>
                  <td>{t.category}</td>
                  <td>{t.entity}</td>
                  <td className={t.type === 'Income' ? 'amount-income' : 'amount-expense'}>
                    {t.amount.toLocaleString('fr-MA', { style: 'currency', currency: 'MAD' })}
                  </td>
                  <td className="description-cell">{t.description}</td>
                  <td>
                    <div className="action-buttons">
                      <button className="action-btn" title="Modifier">✏</button>
                      <button className="action-btn action-delete" title="Supprimer">🗑</button>
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

export default Finances
