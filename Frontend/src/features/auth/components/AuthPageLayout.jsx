import { ConfigProvider } from 'antd';
import heroAthlete from '../../../assets/hero-athlete.jpg';
import { authTheme } from './authTheme';
import { BrandIcon } from './AuthIcons';
import './AuthPage.css';

function AuthPageLayout({ children }) {
  return (
    <ConfigProvider theme={authTheme}>
      <div className="fm-shell">
        <div className="fm-form-side">
          <div className="fm-form-card">
            <div className="fm-form-mark">
              <span className="fm-form-mark-badge">
                <BrandIcon />
              </span>
              Fit<span>Manager</span>
            </div>

            {children}
          </div>
        </div>

        <div className="fm-image-side">
          <div className="fm-image-glow" />
          <div className="fm-image-frame">
            <img
              src={heroAthlete}
              alt="Athlete training"
              className="fm-image-photo"
            />
            <div className="fm-image-overlay" />
            <div className="fm-image-caption">
              <p className="fm-image-kicker">FitManager</p>
              <h2 className="fm-image-headline">Train harder.<br />Track smarter.</h2>
            </div>
          </div>
        </div>
      </div>
    </ConfigProvider>
  );
}

export default AuthPageLayout;
