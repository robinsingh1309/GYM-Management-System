import { useEffect, useRef, useState } from 'react';
import { Link, useLocation } from 'react-router-dom';

import { useAuth } from '../../context/AuthContext';
import './Sidebar.css';

const MEMBERSHIP_GROUP_KEY = 'fitmanager_memberships_expanded';

const Icon = ({ children }) => (
  <svg aria-hidden="true" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.8" strokeLinecap="round" strokeLinejoin="round">
    {children}
  </svg>
);

const NAV_ITEMS = [
  {
    to: '/dashboard',
    label: 'Dashboard',
    icon: <Icon><rect x="3" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="3" width="7" height="7" rx="1.5" /><rect x="14" y="14" width="7" height="7" rx="1.5" /><rect x="3" y="14" width="7" height="7" rx="1.5" /></Icon>,
  },
  {
    to: '/members',
    label: 'Members',
    icon: <Icon><path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" /><circle cx="9" cy="7" r="4" /><path d="M22 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75" /></Icon>,
  },
];

const PAYMENT_ITEM = {
  to: '/payments',
  label: 'Payments',
  icon: <Icon><circle cx="12" cy="12" r="9" /><path d="M8 7h8M8 10h8M10 7c3.5 0 5 1.2 5 3s-1.5 3-5 3h-1l6 5" /></Icon>,
};

const MEMBERSHIP_ITEMS = [
  { to: '/memberships', label: 'Active Memberships' },
  { to: '/memberships-type', label: 'Plans', activePaths: ['/memberships-type', '/membership-types'] },
];

const SMART_TOOLS = [
  {
    to: '/personal-assistant',
    label: 'Personal Assistant',
    icon: <Icon><path d="M21 15a4 4 0 0 1-4 4H8l-5 3V7a4 4 0 0 1 4-4h10a4 4 0 0 1 4 4Z" /><path d="M8 9h8M8 13h5" /></Icon>,
  },
  {
    to: '/exercise-recommendations',
    label: 'Recommendations',
    icon: <Icon><path d="M6.5 6.5 17.5 17.5M3 8l5-5M16 21l5-5M2 12l10-10M12 22l10-10" /></Icon>,
  },
];

const isTypingTarget = (target) => target instanceof HTMLElement
  && (['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName) || target.isContentEditable);

function SidebarLink({ item, pathname, onSelect, child = false }) {
  const isActive = (item.activePaths || [item.to]).some(
    (path) => pathname === path || pathname.startsWith(`${path}/`)
  );

  return (
    <Link
      to={item.to}
      aria-current={isActive ? 'page' : undefined}
      className={`fm-sidebar-link${child ? ' fm-sidebar-child-link' : ''}${isActive ? ' active' : ''}`}
      onClick={onSelect}
    >
      {item.icon && <span className="fm-sidebar-icon">{item.icon}</span>}
      <span className="fm-sidebar-label">{item.label}</span>
    </Link>
  );
}

function Sidebar() {
  const { pathname } = useLocation();
  const { logout, role, user } = useAuth();
  const profileRef = useRef(null);
  const [isDrawerOpen, setIsDrawerOpen] = useState(false);
  const [isProfileOpen, setIsProfileOpen] = useState(false);
  const [isMembershipsExpanded, setIsMembershipsExpanded] = useState(
    () => localStorage.getItem(MEMBERSHIP_GROUP_KEY) === 'true'
  );

  const isMembershipRoute = pathname === '/memberships'
    || pathname.startsWith('/memberships/')
    || pathname === '/memberships-type'
    || pathname.startsWith('/membership-types/');
  const showMembershipChildren = isMembershipRoute || isMembershipsExpanded;
  const username = user?.sub?.split('@')[0] || 'User';
  const displayName = username.charAt(0).toUpperCase() + username.slice(1);

  const closeDrawer = () => setIsDrawerOpen(false);
  const handleSearch = () => undefined;

  const toggleMemberships = () => {
    const nextValue = !isMembershipsExpanded;
    setIsMembershipsExpanded(nextValue);
    localStorage.setItem(MEMBERSHIP_GROUP_KEY, String(nextValue));
  };

  useEffect(() => {
    const handleKeyDown = (event) => {
      if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k' && !isTypingTarget(event.target)) {
        event.preventDefault();
        handleSearch();
      }

      if (event.key === 'Escape') {
        setIsDrawerOpen(false);
        setIsProfileOpen(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  useEffect(() => {
    if (!isDrawerOpen) return undefined;

    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';
    return () => {
      document.body.style.overflow = previousOverflow;
    };
  }, [isDrawerOpen]);

  useEffect(() => {
    if (!isProfileOpen) return undefined;

    const closeOnOutsideClick = (event) => {
      if (!profileRef.current?.contains(event.target)) setIsProfileOpen(false);
    };

    document.addEventListener('pointerdown', closeOnOutsideClick);
    return () => document.removeEventListener('pointerdown', closeOnOutsideClick);
  }, [isProfileOpen]);

  return (
    <>
      <button type="button" className="fm-sidebar-menu-button" aria-label="Open navigation" aria-expanded={isDrawerOpen} aria-controls="fitmanager-sidebar" onClick={() => setIsDrawerOpen(true)}>
        <Icon><path d="M4 7h16M4 12h16M4 17h16" /></Icon>
      </button>

      <button type="button" className={`fm-sidebar-overlay${isDrawerOpen ? ' visible' : ''}`} aria-label="Close navigation" onClick={closeDrawer} />

      <aside id="fitmanager-sidebar" className={`fm-sidebar${isDrawerOpen ? ' open' : ''}`}>
        <div className="fm-sidebar-mark">
          <span className="fm-sidebar-mark-badge" aria-hidden="true" />
          <span className="fm-sidebar-brand-name">Fit<span className="fm-sidebar-mark-accent">Manager</span></span>
          <button type="button" className="fm-sidebar-close" aria-label="Close navigation" onClick={closeDrawer}>
            <Icon><path d="m6 6 12 12M18 6 6 18" /></Icon>
          </button>
        </div>

        <button type="button" className="fm-sidebar-search" onClick={handleSearch} aria-label="Search">
          <span className="fm-sidebar-search-label"><span className="fm-sidebar-icon"><Icon><circle cx="11" cy="11" r="7" /><path d="m20 20-4-4" /></Icon></span>Search</span>
          <kbd>Ctrl K</kbd>
        </button>

        <nav className="fm-sidebar-nav" aria-label="Main">
          {NAV_ITEMS.map((item) => <SidebarLink key={item.to} item={item} pathname={pathname} onSelect={closeDrawer} />)}

          <div className="fm-sidebar-divider" />

          <button type="button" className={`fm-sidebar-group-toggle${showMembershipChildren ? ' highlighted' : ''}`} aria-expanded={showMembershipChildren} aria-controls="membership-navigation" onClick={toggleMemberships}>
            <span className="fm-sidebar-icon"><Icon><rect x="3" y="5" width="18" height="14" rx="2" /><path d="M3 10h18" /></Icon></span>
            <span className="fm-sidebar-label">Memberships</span>
            <span className={`fm-sidebar-chevron${showMembershipChildren ? ' expanded' : ''}`}><Icon><path d="m9 18 6-6-6-6" /></Icon></span>
          </button>

          <div id="membership-navigation" className={`fm-sidebar-children${showMembershipChildren ? ' expanded' : ''}`}>
            <div className="fm-sidebar-children-inner">
              {MEMBERSHIP_ITEMS.map((item) => <SidebarLink key={item.to} item={item} pathname={pathname} onSelect={closeDrawer} child />)}
            </div>
          </div>

          <div className="fm-sidebar-divider" />
          <SidebarLink item={PAYMENT_ITEM} pathname={pathname} onSelect={closeDrawer} />
          <div className="fm-sidebar-divider" />

          <span className="fm-sidebar-section-label">Smart Tools</span>
          {SMART_TOOLS.map((item) => <SidebarLink key={item.to} item={item} pathname={pathname} onSelect={closeDrawer} />)}
        </nav>

        <div className="fm-sidebar-spacer" />

        <div className="fm-sidebar-profile-wrapper" ref={profileRef}>
          {isProfileOpen && (
            <div className="fm-sidebar-profile-menu" role="menu">
              <button type="button" role="menuitem" disabled>Profile</button>
              <button type="button" role="menuitem" onClick={logout}>Log out</button>
            </div>
          )}
          <button type="button" className="fm-sidebar-profile" aria-expanded={isProfileOpen} aria-haspopup="menu" onClick={() => setIsProfileOpen((isOpen) => !isOpen)}>
            <span className="fm-sidebar-avatar" aria-hidden="true">{displayName.charAt(0)}</span>
            <span className="fm-sidebar-profile-copy"><strong>{displayName}</strong><small>{role || 'User'}</small></span>
            <span className={`fm-sidebar-profile-chevron${isProfileOpen ? ' expanded' : ''}`}><Icon><path d="m8 14 4 4 4-4M8 10l4-4 4 4" /></Icon></span>
          </button>
        </div>
      </aside>
    </>
  );
}

export default Sidebar;
