import { Link, useLocation } from 'react-router-dom';
import { Tooltip } from 'antd';

import { useAuth } from '../../context/AuthContext';
import './Sidebar.css';

const NAV_ITEMS = [
  {
    to: '/dashboard',
    label: 'Dashboard',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="3" y="3" width="7" height="7" rx="1.5" />
        <rect x="14" y="3" width="7" height="7" rx="1.5" />
        <rect x="14" y="14" width="7" height="7" rx="1.5" />
        <rect x="3" y="14" width="7" height="7" rx="1.5" />
      </svg>
    ),
  },
  {
    to: '/members',
    label: 'Members',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
        <circle cx="9" cy="7" r="4" />
        <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
        <path d="M16 3.13a4 4 0 0 1 0 7.75" />
      </svg>
    ),
  },
  {
    to: '/memberships',
    label: 'Memberships',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <rect x="2" y="5" width="20" height="14" rx="2" />
        <line x1="2" y1="10" x2="22" y2="10" />
      </svg>
    ),
  },
  {
    to: '/payments',
    label: 'Payments',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <circle cx="12" cy="12" r="9" />
        <path d="M12 7v10M9.5 9.5c0-1.1 1.1-2 2.5-2s2.5.9 2.5 2-1.1 1.6-2.5 2-2.5.9-2.5 2 1.1 2 2.5 2 2.5-.9 2.5-2" />
      </svg>
    ),
  },
  {
    to: '/memberships-type',
    label: 'Membership Type',
    icon: (
      <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
        <path d="M20.59 13.41 11 3.83A2 2 0 0 0 9.59 3.24L4 3a1 1 0 0 0-1 1l.24 5.59a2 2 0 0 0 .59 1.41l9.58 9.59a2 2 0 0 0 2.83 0l4.35-4.35a2 2 0 0 0 0-2.83Z" />
        <circle cx="7.5" cy="7.5" r="1.5" fill="currentColor" stroke="none" />
      </svg>
    ),
  },
];

function Sidebar() {
  const { pathname } = useLocation();
  const { logout } = useAuth();

  return (
    <aside className="fm-sidebar">
      <div className="fm-sidebar-mark">
        <span className="fm-sidebar-mark-badge">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M4 12c2-4 4-6 8-6s6 2 8 6c-2 4-4 6-8 6s-6-2-8-6Z" />
            <circle cx="12" cy="12" r="2" fill="currentColor" stroke="none" />
          </svg>
        </span>
        Fit<span className="fm-sidebar-mark-accent">Manager</span>
      </div>

      <nav className="fm-sidebar-nav">
        {NAV_ITEMS.map((item) => {
          const isActive = pathname === item.to || pathname.startsWith(`${item.to}/`);
          return (
            <Link
              key={item.to}
              to={item.to}
              className={`fm-sidebar-link${isActive ? ' active' : ''}`}
            >
              <span className="fm-sidebar-icon">{item.icon}</span>
              {item.label}
            </Link>
          );
        })}
      </nav>

      <div className="fm-sidebar-spacer" />

      <Tooltip placement="right">
        <button className="fm-sidebar-logout" onClick={logout}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
          Log out
        </button>
      </Tooltip>
    </aside>
  );
}

export default Sidebar;