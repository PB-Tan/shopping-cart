import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './Register.css';

const Register = () => {
    const [formData, setFormData] = useState({
        name: '',
        firstName: '',
        lastName: '',
        email: '',
        phoneNumber: '',
        address: '',
        country: '',
        postalCode: '',
        password: '',
        confirmPassword: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [checkingUsername, setCheckingUsername] = useState(false);
    const [usernameAvailable, setUsernameAvailable] = useState(null); // null=unknown, true/false
    const navigate = useNavigate();

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setError('');
        setSuccess('');
        if (name === 'name') {
            setUsernameAvailable(null);
        }
    };

    const validateForm = () => {
        if (!formData.name.trim()) {
            setError('Username is required');
            return false;
        }
        if (!formData.firstName.trim()) {
            setError('First name is required');
            return false;
        }
        if (!formData.lastName.trim()) {
            setError('Last name is required');
            return false;
        }
        if (!formData.email.trim()) {
            setError('Email is required');
            return false;
        }
        if (!/\S+@\S+\.\S+/.test(formData.email)) {
            setError('Please enter a valid email address');
            return false;
        }
        if (!formData.phoneNumber.trim()) {
            setError('Phone number is required');
            return false;
        }
        if (!formData.address.trim()) {
            setError('Address is required');
            return false;
        }
        if (!formData.country.trim()) {
            setError('Country is required');
            return false;
        }
        if (!formData.postalCode.trim()) {
            setError('Postal code is required');
            return false;
        }
        if (formData.password.length < 6) {
            setError('Password must be at least 6 characters long');
            return false;
        }
        if (formData.password !== formData.confirmPassword) {
            setError('Passwords do not match');
            return false;
        }
        return true;
    };

    // debounce username availability check
    useEffect(() => {
        const name = formData.name && formData.name.trim();
        if (!name) {
            setUsernameAvailable(null);
            setCheckingUsername(false);
            return;
        }

        setCheckingUsername(true);
        const id = setTimeout(async () => {
            try {
                const res = await fetch(`/api/customers/exists/${encodeURIComponent(name)}`, { credentials: 'include' });
                if (!res.ok) {
                    setUsernameAvailable(null);
                    setCheckingUsername(false);
                    return;
                }
                const body = await res.json();
                if (body && body.code === 200) {
                    setUsernameAvailable(true);
                } else {
                    setUsernameAvailable(false);
                }
            } catch (e) {
                setUsernameAvailable(null);
            } finally {
                setCheckingUsername(false);
            }
        }, 500);

        return () => clearTimeout(id);
    }, [formData.name]);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!validateForm()) return;

        setLoading(true);
        setError('');

        try {
            if (usernameAvailable === false) {
                setError('Username already exists. Please choose another one.');
                setLoading(false);
                return;
            }

            // Generate a random salt and compute SHA-256(salt + password) client-side
            const generateSalt = (len = 16) => {
                const arr = new Uint8Array(len);
                crypto.getRandomValues(arr);
                return Array.from(arr).map(b => b.toString(16).padStart(2, '0')).join('');
            };

            const salt = generateSalt(16);
            const hash = await (async (password, salt) => {
                const enc = new TextEncoder();
                const data = enc.encode(salt + password);
                const buf = await crypto.subtle.digest('SHA-256', data);
                const arr = Array.from(new Uint8Array(buf));
                return arr.map(b => b.toString(16).padStart(2, '0')).join('');
            })(formData.password, salt);

            const response = await fetch('/api/customers/', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({
                    name: formData.name,
                    firstName: formData.firstName,
                    lastName: formData.lastName,
                    email: formData.email,
                    phoneNumber: formData.phoneNumber,
                    address: formData.address,
                    country: formData.country,
                    postalCode: formData.postalCode,
                    password: formData.password
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();

            if (result.code === 200) {
                setSuccess('Account created successfully! Redirecting to login...');
                setTimeout(() => {
                    window.location.href = 'https://localhost:5173/login';
                }, 2000);
            } else {
                setError(result.message || 'Registration failed');
            }
        } catch (error) {
            console.error('Registration error:', error);
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="register-container">
            <div className="register-card">
                <div className="register-header">
                    <h1>Create Account</h1>
                    <p>Join us today and get started</p>
                </div>

                <form onSubmit={handleSubmit} className="register-form">
                    {error && (
                        <div className="error-message">
                            <i className="error-icon">⚠️</i>
                            {error}
                        </div>
                    )}

                    {success && (
                        <div className="success-message">
                            <i className="success-icon">✅</i>
                            {success}
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
                            placeholder="Choose a username"
                            required
                        />
                        <div className="input-help" style={{marginTop: '6px'}}>
                            {checkingUsername && <span style={{color: '#666'}}>Checking username...</span>}
                            {usernameAvailable === true && <span style={{color: 'green'}}>Username available</span>}
                            {usernameAvailable === false && <span style={{color: 'red'}}>Username already exists</span>}
                        </div>
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="firstName">First Name</label>
                            <input
                                type="text"
                                id="firstName"
                                name="firstName"
                                value={formData.firstName}
                                onChange={handleInputChange}
                                placeholder="Enter your first name"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="lastName">Last Name</label>
                            <input
                                type="text"
                                id="lastName"
                                name="lastName"
                                value={formData.lastName}
                                onChange={handleInputChange}
                                placeholder="Enter your last name"
                                required
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="email">Email Address</label>
                        <input
                            type="email"
                            id="email"
                            name="email"
                            value={formData.email}
                            onChange={handleInputChange}
                            placeholder="Enter your email address"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="phoneNumber">Phone Number</label>
                        <input
                            type="tel"
                            id="phoneNumber"
                            name="phoneNumber"
                            value={formData.phoneNumber}
                            onChange={handleInputChange}
                            placeholder="Enter your phone number"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="address">Address</label>
                        <input
                            type="text"
                            id="address"
                            name="address"
                            value={formData.address}
                            onChange={handleInputChange}
                            placeholder="Enter your mailing address"
                            required
                        />
                    </div>

                    <div className="form-row">
                        <div className="form-group">
                            <label htmlFor="country">Country</label>
                            <input
                                type="text"
                                id="country"
                                name="country"
                                value={formData.country}
                                onChange={handleInputChange}
                                placeholder="Enter your country"
                                required
                            />
                        </div>
                        <div className="form-group">
                            <label htmlFor="postalCode">Postal Code</label>
                            <input
                                type="text"
                                id="postalCode"
                                name="postalCode"
                                value={formData.postalCode}
                                onChange={handleInputChange}
                                placeholder="Enter your postal code"
                                required
                            />
                        </div>
                    </div>

                    <div className="form-group">
                        <label htmlFor="password">Password</label>
                        <input
                            type="password"
                            id="password"
                            name="password"
                            value={formData.password}
                            onChange={handleInputChange}
                            placeholder="Create a password (min. 6 characters)"
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirm Password</label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleInputChange}
                            placeholder="Confirm your password"
                            required
                        />
                    </div>

                    <button 
                        type="submit" 
                        className="register-button"
                        disabled={loading}
                    >
                        {loading ? (
                            <>
                                <span className="spinner"></span>
                                Creating Account...
                            </>
                        ) : (
                            'Create Account'
                        )}
                    </button>
                </form>

                <div className="register-footer">
                    <span>Already have an account? </span>
                    <Link to="https://localhost:5173/login" className="link-button back-to-login">
                        Back to Login
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default Register;
