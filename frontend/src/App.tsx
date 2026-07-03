import { BrowserRouter, Routes, Route } from 'react-router-dom'
import Layout from './components/Layout'
import Dashboard from './pages/Dashboard'
import Payments from './pages/Payments'
import PaymentDetail from './pages/PaymentDetail'
import NewPayment from './pages/NewPayment'
import Accounts from './pages/Accounts'
import Fraud from './pages/Fraud'

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Dashboard />} />
          <Route path="/payments" element={<Payments />} />
          <Route path="/payments/new" element={<NewPayment />} />
          <Route path="/payments/:id" element={<PaymentDetail />} />
          <Route path="/accounts" element={<Accounts />} />
          <Route path="/fraud" element={<Fraud />} />
        </Route>
      </Routes>
    </BrowserRouter>
  )
}
