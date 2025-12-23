import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './Navbar.css';

function Navbar() {
  const { user, logout, isAdmin } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const isActive = (path) => location.pathname === path;

  return (
    <nav className="navbar">
      <div className="container navbar-content">
        <Link to="/dashboard" className="navbar-brand">
          <svg className="navbar-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect>
            <line x1="1" y1="10" x2="23" y2="10"></line>
          </svg>
          <span>WalletPro</span>
        </Link>

        <div className="navbar-links">
          <Link 
            to="/dashboard" 
            className={`navbar-link ${isActive('/dashboard') ? 'active' : ''}`}
          >
            Dashboard
          </Link>
          <Link 
            to="/transfer" 
            className={`navbar-link ${isActive('/transfer') ? 'active' : ''}`}
          >
            Transfer
          </Link>
          <Link 
            to="/history" 
            className={`navbar-link ${isActive('/history') ? 'active' : ''}`}
          >
            History
          </Link>
          {isAdmin && (
            <Link 
              to="/admin" 
              className={`navbar-link admin-link ${isActive('/admin') ? 'active' : ''}`}
            >
              Admin
            </Link>
          )}
        </div>

        <div className="navbar-user">
          <span className="navbar-greeting">
            Hello, <strong>{user?.firstName}</strong>
          </span>
          <button onClick={handleLogout} className="btn btn-secondary navbar-logout">
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
}

export default Navbar;
