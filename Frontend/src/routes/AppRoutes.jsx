import { BrowserRouter, Routes, Route } from 'react-router-dom';

import SignUpPage from '../features/auth/pages/SignUpPage';
import LoginPage from '../features/auth/pages/LoginPage';
import OAuthCallbackPage from '../features/auth/pages/OAuthCallbackPage';
import DashboardPage from '../features/dashboard/pages/DashboardPage';
import MembersPage from '../features/members/pages/MembersPage';
import CreateMemberPage from '../features/members/pages/CreateMemberPage';
import MemberDetailsPage from '../features/members/pages/MemberDetailsPage';
import CreateMembershipPage from '../features/memberships/pages/CreateMembershipPage';
import MembershipsPage from '../features/memberships/pages/MembershipsPage';
import MembershipDetailsPage from '../features/memberships/pages/MembershipDetailsPage';
import CreateMembershipTypePage from '../features/membershipTypes/pages/CreateMembershipTypePage';
import MembershipsTypePage from '../features/membershipTypes/pages/MembershipsTypePage';
import PaymentsPage from '../features/payments/pages/PaymentsPage';
import PaymentDetailsPage from '../features/payments/pages/PaymentDetailsPage';
import CreatePaymentPage from '../features/payments/pages/CreatePaymentPage';

import AppLayout from '../layouts/AppLayout';
import ProtectedRoute from '../features/auth/components/ProtectedRoute';

function AppRoutes() {
  return (
    <BrowserRouter>
      <Routes>

        <Route
          path="/signup"
          element={<SignUpPage />}
        />
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
