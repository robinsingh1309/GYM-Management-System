import { Outlet } from 'react-router-dom';

import Sidebar from '../components/common/Sidebar';
import './AppLayout.css';

function AppLayout() {
  return (
    <div className="app-layout">
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