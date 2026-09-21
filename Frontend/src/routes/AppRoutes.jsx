import { BrowserRouter, Routes, Route } from 'react-router-dom';

import LoginPage from '../pages/LoginPage';
import OAuthCallbackPage from '../pages/OAuthCallbackPage';
import CreateMembershipTypePage from '../pages/CreateMembershipTypePage';
import DashboardPage from '../pages/DashboardPage';
import MembersPage from '../pages/MembersPage';
import CreateMemberPage from '../pages/CreateMemberPage';
import MemberDetailsPage from '../pages/MemberDetailsPage';
import CreateMembershipPage from '../pages/CreateMembershipPage';
import MembershipsPage from '../pages/MembershipsPage';
import PaymentsPage from '../pages/PaymentsPage';
import MembershipsTypePage from '../pages/MembershipsTypePage';
import MembershipDetailsPage from '../pages/MembershipDetailsPage';
import PaymentDetailsPage from '../pages/PaymentDetailsPage';
import CreatePaymentPage from '../pages/CreatePaymentPage';

import AppLayout from '../layouts/AppLayout';
import ProtectedRoute from '../components/auth/ProtectedRoute';

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>

        <Route
          path="/login"
          element={<LoginPage />}
        />

        <Route
          path="/oauth2/callback"
          element={<OAuthCallbackPage />}
        />

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
              path="/members/create"
              element={<CreateMemberPage />}
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
              path="/memberships/create"
              element={<CreateMembershipPage />}
            />

            <Route
              path="/memberships/:id"
              element={<MembershipDetailsPage />}
            />

            <Route
              path="/payments"
              element={<PaymentsPage />}
            />

            <Route
              path="/payments/create"
              element={<CreatePaymentPage />}
            />

            <Route
              path="/payments/:id"
              element={<PaymentDetailsPage />}
            />

            <Route
              path="/memberships-type"
              element={<MembershipsTypePage />}
            />
            <Route
              path="/membership-types/create"
              element={<CreateMembershipTypePage />}
            />

          </Route>

        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default AppRoutes;
