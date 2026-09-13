import { BrowserRouter, Routes, Route } from 'react-router-dom';

import LoginPage from '../pages/LoginPage';
import DashboardPage from '../pages/DashboardPage';
import MembersPage from '../pages/MembersPage';
import MembershipsPage from '../pages/MembershipsPage';
import PaymentsPage from '../pages/PaymentsPage';
import MembershipPricingPage from '../pages/MembershipPricingPage';
import MemberDetailsPage from '../pages/MemberDetailsPage';

import AppLayout from '../layouts/AppLayout';
import ProtectedRoute from '../components/auth/ProtectedRoute';

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>

        <Route path="/login" element={<LoginPage />} />

        <Route element={<ProtectedRoute />}>

          <Route element={<AppLayout />}>

            <Route
              path="/dashboard"
              element={<DashboardPage />}
            />

            <Route
              path="/members"
              element={<MembersPage />}
            />

            <Route
              path="/members/:id"
              element={<MemberDetailsPage />}
            />

            <Route
              path="/memberships"
              element={<MembershipsPage />}
            />

            <Route
              path="/payments"
              element={<PaymentsPage />}
            />

            <Route
              path="/pricing"
              element={<MembershipPricingPage />}
            />

          </Route>

        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;