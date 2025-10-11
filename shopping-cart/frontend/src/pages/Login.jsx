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

    // 从cookie获取值
    const getCookie = (name) => {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) return parts.pop().split(';').shift();
        return null;
    };

    // 检查是否已经登录
    useEffect(() => {
        const username = getCookie('username');
        if (!username) return;

        // 验证后端是否仍然有该用户
        (async () => {
            try {
                const res = await fetch(`/api/customers/name/${username}`);
                if (!res.ok) throw new Error('User check failed');
                const body = await res.json();
                if (body.code === 200 && body.data) {
                    console.log('User cookie valid, redirecting to profile');
                    navigate('/profile');
                }
            } catch (e) {
                console.log('User cookie invalid or server not reachable:', e.message);
            }
        })();
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
            const response = await fetch('/api/customers/customerlogin', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(formData)
            });
            console.log('Login request sent:', formData);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            
            console.log('Login response:', result);

            if (result.code === 200 && result.data) {
                // 将username存储到cookie
                setCookie('username', result.data.username, 7);
                // setCookie('userId', result.data.id, 7);
                
                // 同时存储到localStorage作为备份
                localStorage.setItem('user', JSON.stringify(result.data));
                
                console.log('Login successful, username stored in cookie');
                
                // 跳转到用户个人信息页面
                navigate('/profile');
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
            <div className="login-card">
                <div className="login-header">
                    <h1>Welcome Back</h1>
                    <p>Sign in to your account</p>
                </div>

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

                <div className="login-footer">
                    <div className="footer-left">
                        <Link to="/forgot-password" className="link-button">
                            Forgot Password?
                        </Link>
                    </div>
                    <div className="footer-right">
                        <span>Don't have an account? </span>
                        <Link to="/register" className="link-button register-link">
                            Sign Up
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Login;
