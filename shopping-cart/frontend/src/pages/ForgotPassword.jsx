import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import './ForgotPassword.css';

const ForgotPassword = () => {
    const [step, setStep] = useState(1); // 1: 输入用户名, 2: 输入验证码, 3: 设置新密码
    const [formData, setFormData] = useState({
        name: '',
        verificationCode: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [serverCode, setServerCode] = useState('');
    const navigate = useNavigate();

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({
            ...prev,
            [name]: value
        }));
        setError('');
        setSuccess('');
    };

    // 步骤1：发送验证码
    const handleSendCode = async (e) => {
        e.preventDefault();
        setLoading(true);
        setError('');

        try {
            const response = await fetch('/api/customers/customersendemail', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.name
                })
            });

            const result = await response.text();

            if (result !== "User not found") {
                setServerCode(result);
                setSuccess('Verification code sent to your email!');
                setStep(2);
            } else {
                setError('User not found with this username');
            }
        } catch (error) {
            console.error('Send code error:', error);
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    // 步骤2：验证验证码
    const handleVerifyCode = (e) => {
        e.preventDefault();
        
        if (formData.verificationCode === serverCode) {
            setSuccess('Code verified successfully!');
            setStep(3);
        } else {
            setError('Invalid verification code. Please try again.');
        }
    };

    // 步骤3：重置密码
    const handleResetPassword = async (e) => {
        e.preventDefault();
        
        if (formData.newPassword.length < 6) {
            setError('Password must be at least 6 characters long');
            return;
        }
        
        if (formData.newPassword !== formData.confirmPassword) {
            setError('Passwords do not match');
            return;
        }

        setLoading(true);
        setError('');

        try {
            const response = await fetch('/api/customers/customerchangepassword', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    name: formData.name,
                    newPassword: formData.newPassword
                })
            });

            const result = await response.json();

            if (result.code === 200) {
                setSuccess('Password reset successfully! Redirecting to login...');
                setTimeout(() => {
                    navigate('/login');
                }, 2000);
            } else {
                setError(result.message || 'Failed to reset password');
            }
        } catch (error) {
            console.error('Reset password error:', error);
            setError('Network error. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const renderStepContent = () => {
        switch (step) {
            case 1:
                return (
                    <form onSubmit={handleSendCode} className="forgot-form">
                        <div className="form-group">
                            <label htmlFor="name">Username</label>
                            <input
                                type="text"
                                id="name"
                                name="name"
                                value={formData.name}
                                onChange={handleInputChange}
                                placeholder="Enter your username"
                                required
                            />
                        </div>

                        <button 
                            type="submit" 
                            className="forgot-button"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <span className="spinner"></span>
                                    Sending Code...
                                </>
                            ) : (
                                'Send Verification Code'
                            )}
                        </button>
                    </form>
                );

            case 2:
                return (
                    <form onSubmit={handleVerifyCode} className="forgot-form">
                        <div className="form-group">
                            <label htmlFor="verificationCode">Verification Code</label>
                            <input
                                type="text"
                                id="verificationCode"
                                name="verificationCode"
                                value={formData.verificationCode}
                                onChange={handleInputChange}
                                placeholder="Enter the 6-digit code"
                                maxLength="6"
                                required
                            />
                            <small className="form-help">
                                Check your email for the verification code
                            </small>
                        </div>

                        <button 
                            type="submit" 
                            className="forgot-button"
                        >
                            Verify Code
                        </button>

                        <button 
                            type="button"
                            className="secondary-button"
                            onClick={() => handleSendCode({ preventDefault: () => {} })}
                            disabled={loading}
                        >
                            Resend Code
                        </button>
                    </form>
                );

            case 3:
                return (
                    <form onSubmit={handleResetPassword} className="forgot-form">
                        <div className="form-group">
                            <label htmlFor="newPassword">New Password</label>
                            <input
                                type="password"
                                id="newPassword"
                                name="newPassword"
                                value={formData.newPassword}
                                onChange={handleInputChange}
                                placeholder="Enter new password (min. 6 characters)"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label htmlFor="confirmPassword">Confirm New Password</label>
                            <input
                                type="password"
                                id="confirmPassword"
                                name="confirmPassword"
                                value={formData.confirmPassword}
                                onChange={handleInputChange}
                                placeholder="Confirm your new password"
                                required
                            />
                        </div>

                        <button 
                            type="submit" 
                            className="forgot-button"
                            disabled={loading}
                        >
                            {loading ? (
                                <>
                                    <span className="spinner"></span>
                                    Resetting Password...
                                </>
                            ) : (
                                'Reset Password'
                            )}
                        </button>
                    </form>
                );

            default:
                return null;
        }
    };

    const getStepTitle = () => {
        switch (step) {
            case 1:
                return 'Forgot Password?';
            case 2:
                return 'Verify Your Email';
            case 3:
                return 'Set New Password';
            default:
                return 'Forgot Password?';
        }
    };

    const getStepDescription = () => {
        switch (step) {
            case 1:
                return "Enter your username and we'll send you a verification code";
            case 2:
                return 'Enter the 6-digit code sent to your email address';
            case 3:
                return 'Create a new secure password for your account';
            default:
                return '';
        }
    };

    return (
        <div className="forgot-container">
            <div className="forgot-card">
                <div className="forgot-header">
                    <h1>{getStepTitle()}</h1>
                    <p>{getStepDescription()}</p>
                    
                    {/* 进度指示器 */}
                    <div className="progress-indicator">
                        <div className={`step ${step >= 1 ? 'active' : ''}`}>1</div>
                        <div className={`step ${step >= 2 ? 'active' : ''}`}>2</div>
                        <div className={`step ${step >= 3 ? 'active' : ''}`}>3</div>
                    </div>
                </div>

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

                {renderStepContent()}

                <div className="forgot-footer">
                    <Link to="/login" className="link-button back-to-login">
                        ← Back to Login
                    </Link>
                </div>
            </div>
        </div>
    );
};

export default ForgotPassword;
