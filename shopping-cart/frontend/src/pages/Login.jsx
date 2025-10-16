import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Login.css';

const Login = () => {
    const [formData, setFormData] = useState({
        name: '',
        password: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const navigate = useNavigate();

    // 使用 session 检查是否已登录
    useEffect(() => {
        // add body class so we can adjust global layout for login page
        document.body.classList.add('login-page');

        const checkSession = async () => {
            try {
                const res = await fetch('/api/customers/session', { credentials: 'include' });
                if (!res.ok) return;
                const body = await res.json();
                if (body.code === 200 && body.data) {
                    console.log('Session valid, redirecting to profile');
                    navigate('/profile');
                }
            } catch (e) {
                console.log('Session check failed:', e.message);
            }
        };

        checkSession();

        return () => {
            document.body.classList.remove('login-page');
        };
    }, [navigate]);

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setError('');
    };

    // Cookie 操作函数
    const setCookie = (name, value, days = 7) => {
        const expires = new Date();
        expires.setTime(expires.getTime() + days * 24 * 60 * 60 * 1000);
        document.cookie = `${name}=${value};expires=${expires.toUTCString()};path=/`;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            // For improved security: fetch salt for this username, compute hash(salt+password) client-side,
            // then send salt and hash to server.
            const saltResp = await fetch(`/api/customers/salt/${encodeURIComponent(formData.name)}`, { credentials: 'include' });
            if (!saltResp.ok) {
                throw new Error('Failed to retrieve salt for user');
            }
            const saltBody = await saltResp.json();
            const salt = (saltBody && saltBody.data && saltBody.data.salt) || saltBody.salt || '';

            // compute SHA-256(salt + password)
            const hash = await (async (password, salt) => {
                const enc = new TextEncoder();
                const data = enc.encode(salt + password);
                const buf = await crypto.subtle.digest('SHA-256', data);
                const arr = Array.from(new Uint8Array(buf));
                return arr.map(b => b.toString(16).padStart(2, '0')).join('');
            })(formData.password, salt);

            const payload = {
                name: formData.name,
                passwordHash: hash,
                passwordSalt: salt
            };

            const response = await fetch('/api/customers/customerlogin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify(payload)
            });
            console.log('Login request sent:', formData);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            
            console.log('Login response:', result);

            if (result.code === 200 && result.data) {
                // 使用 session 进行登录状态管理；前端无需写 cookie
                localStorage.setItem('user', JSON.stringify(result.data));
                console.log('Login successful (session), redirecting to profile');
                navigate(window.location.href = 'http://localhost:8080/catalogue');
            } else {
                setError(result.message || 'Invalid username or password');
            }
        } catch (error) {
            console.error('Login error:', error);
            if (error.message.includes('Failed to fetch')) {
                setError('Cannot connect to server. Please make sure the backend is running.');
            } else {
                setError(error.message || 'Network error. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="login-container">
          <button className="back-button" onClick={() => { window.location.href = 'http://localhost:8080/catalogue'; }} aria-label="Go back">
              ← Back
          </button>
            <div className="login-description">This page is used to sign in to your account.</div>
            <div className="login-card">
                <div className="login-header">
                    <h1>Welcome Back</h1>
                    <p>Sign in to your account</p>
                </div>

                {/* ...existing code... */}

                <form onSubmit={handleSubmit} className="login-form">
                    {error && (
                        <div className="error-message">
                            <i className="error-icon">⚠️</i>
                            {error}
                        </div>
                    )}

                    <div className="form-group">
                        <label htmlFor="name">Username</label>
                        <input
                            type="text"
                            id="name"
                            name="name"
                            value={formData.name}
                            onChange={handleInputChange}
                            placeholder="Enter your username"
                            autoComplete="username"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            placeholder="Enter your password"
                            autoComplete="current-password"
                            required
                        />
                    </div>

                    <button 
                        type="submit" 
                        className="login-button"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <span className="spinner"></span>
                                Signing in...
                            </>
                        ) : (
                            'Sign In'
                        )}
                    </button>
                </form>

                {/* Place Google sign-in directly under the Sign In button (full-width, same sizing) */}
                <div className="google-row">
                    <a href="/api/customers/oauth2/authorize/google" className="google-button" aria-label="Sign in with Google">
                        Sign in with Google
                    </a>
                </div>

                <div className="login-footer">
                    <div className="footer-left">
                        <Link to="/forgot-password" className="link-button">
                            Forgot Password?
                        </Link>
                    </div>
                    <div className="footer-right">
                        <Link to="/register" className="link-button" aria-label="Sign up">
                            Sign Up
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;

// Global callback used by Google platform library
window.onSignIn = async function(googleUser) {
    try {
        const id_token = googleUser.getAuthResponse().id_token;
        // send id_token to backend for verification and session creation
        const resp = await fetch('/api/customers/google/token', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify({ id_token })
        });
        if (!resp.ok) {
            console.error('Backend returned non-OK status for id_token');
            return;
        }
        const result = await resp.json();
        if (result.code === 200 && result.data) {
            localStorage.setItem('user', JSON.stringify(result.data));
            window.location.href = '/profile';
        } else {
            console.error('Login failed', result);
        }
    } catch (e) {
        console.error('onSignIn error', e);
    }
}
