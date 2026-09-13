import { Link } from 'react-router-dom';

function Sidebar() {
  return (
    <aside className="sidebar">
      <h2>FitManager</h2>

      <nav>
        <Link to="/dashboard">Dashboard</Link>
        <Link to="/members">Members</Link>
        <Link to="/memberships">Memberships</Link>
        <Link to="/payments">Payments</Link>
        <Link to="/pricing">Pricing</Link>
      </nav>
    </aside>
  );
}

export default Sidebar;