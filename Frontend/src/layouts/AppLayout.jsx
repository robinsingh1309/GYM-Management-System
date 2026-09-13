import { Outlet } from 'react-router-dom';
import { Button } from 'antd';

import Sidebar from '../components/common/Sidebar';
import { useAuth } from '../context/AuthContext';

function AppLayout() {
  const { logout } = useAuth();

  return (
    <div className="app-layout">

      <header className="app-header">
        <h1>FitManager</h1>

        <Button
          danger
          onClick={logout}
        >
          Logout
        </Button>
      </header>

      <div className="app-body">
        <Sidebar />

        <main className="page-content">
          <Outlet />
        </main>
      </div>

    </div>
  );
}

export default AppLayout;