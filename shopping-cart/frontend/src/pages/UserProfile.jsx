import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './UserProfile.css';

const UserProfile = () => {
    const navigate = useNavigate();
    const [userInfo, setUserInfo] = useState({
        id: '',
        name: '',
        firstName: '',
        lastName: '',
        address: '',
        country: '',
        email: '',
        phone: ''
    });
    const [loading, setLoading] = useState(true);
    const [showModal, setShowModal] = useState(false);
    const [modalType, setModalType] = useState(''); // 'address', 'email', 'phone'
    const [modalValue, setModalValue] = useState('');
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    
    // 邮箱验证相关状态
    const [emailStep, setEmailStep] = useState(1); // 1: 输入新邮箱, 2: 输入验证码
    const [newEmail, setNewEmail] = useState('');
    const [verificationCode, setVerificationCode] = useState('');
    const [serverCode, setServerCode] = useState('');
    const [sendingCode, setSendingCode] = useState(false);
    const [modalFirstName, setModalFirstName] = useState('');
    const [modalLastName, setModalLastName] = useState('');
    const [modalCountry, setModalCountry] = useState('');

    // 使用 session 加载用户信息
    useEffect(() => {
        // when on profile page, add a class so global nav can be hidden
        document.body.classList.add('profile-page');

        (async () => {
            try {
                const res = await fetch('/api/customers/session', { credentials: 'include' });
                if (!res.ok) {
                    navigate('/login');
                    return;
                }
                const body = await res.json();
                if (body.code === 200 && body.data) {
                    const d = body.data;
                    setUserInfo({
                        id: d.username || '',
                        name: d.username || '',
                        firstName: d.firstName || '',
                        lastName: d.lastName || '',
                        address: d.address || '',
                        country: d.country || '',
                        email: d.email || '',
                        phone: d.phoneNumber || ''
                    });
                } else {
                    navigate('/login');
                }
            } catch (e) {
                console.error('Session check failed:', e);
                navigate('/login');
            } finally {
                setLoading(false);
            }
        })();

        return () => {
            document.body.classList.remove('profile-page');
        };
    }, [navigate]);

    const fetchUserInfo = async (username) => {
        try {
            const response = await fetch(`/api/customers/name/${username}`, { credentials: 'include' });
            if (!response.ok) {
                throw new Error('Failed to fetch user info');
            }
            const result = await response.json();
            if (result.code === 200 && result.data) {
                const d = result.data;
                // map backend fields to frontend shape
                setUserInfo({
                    id: d.username || '',
                    name: d.username || '',
                    firstName: d.firstName || '',
                    lastName: d.lastName || '',
                    address: d.address || '',
                    country: d.country || '',
                    email: d.email || '',
                    phone: d.phoneNumber || ''
                });
                console.log('User info loaded:', d);
            }
        } catch (error) {
            console.error('Error fetching user info:', error);
            setError('Failed to load user information');
        } finally {
            setLoading(false);
        }
    };

    // 打开修改弹窗
    const openModal = (type) => {
        setModalType(type);
        setShowModal(true);
        setError('');
        setSuccess('');
        
        if (type === 'address') {
            setModalValue(userInfo.address || '');
        } else if (type === 'phone') {
            setModalValue(userInfo.phone || '');
        } else if (type === 'email') {
            setEmailStep(1);
            setNewEmail('');
            setVerificationCode('');
            setServerCode('');
        } else if (type === 'name') {
            // edit first/last/country combined form
            setModalValue('');
            setModalFirstName(userInfo.firstName || '');
            setModalLastName(userInfo.lastName || '');
            setModalCountry(userInfo.country || '');
        }
    };

    // 关闭弹窗
    const closeModal = () => {
        setShowModal(false);
        setModalType('');
        setModalValue('');
        setError('');
        setSuccess('');
        setEmailStep(1);
        setNewEmail('');
        setVerificationCode('');
        setServerCode('');
        setModalFirstName('');
        setModalLastName('');
        setModalCountry('');
    };

    // 发送验证码到新邮箱
    const sendVerificationCode = async () => {
        if (!newEmail) {
            setError('Please enter new email address');
            return;
        }

        const emailRegex = /\S+@\S+\.\S+/;
        if (!emailRegex.test(newEmail)) {
            setError('Please enter a valid email address');
            return;
        }

        setSendingCode(true);
        setError('');

        try {
            // 注意：这里需要后端添加一个新接口来向指定邮箱发送验证码
            const response = await fetch('/api/customers/sendemailto', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({ 
                    email: newEmail 
                })
            });

            if (!response.ok) {
                throw new Error('Failed to send verification code');
            }

            const code = await response.text();
            setServerCode(code);
            setEmailStep(2);
            setSuccess('Verification code sent successfully!');
        } catch (error) {
            console.error('Error sending verification code:', error);
            setError('Failed to send verification code. Please try again.');
        } finally {
            setSendingCode(false);
        }
    };

    // 修改地址
    const updateAddress = async () => {
        if (!modalValue.trim()) {
            setError('Address cannot be empty');
            return;
        }

        try {
            const response = await fetch('/api/customers/updateaddress', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({
                    name: userInfo.name,
                    address: modalValue
                })
            });

            const result = await response.json();
            
            if (result.code === 200) {
                setUserInfo({ ...userInfo, address: modalValue });
                setSuccess('Address updated successfully!');
                setTimeout(() => {
                    closeModal();
                }, 1500);
            } else {
                setError(result.message || 'Failed to update address');
            }
        } catch (error) {
            console.error('Error updating address:', error);
            setError('Network error. Please try again.');
        }
    };

    // 修改个人资料（firstName, lastName, country）
    const updateProfile = async (payload) => {
        try {
            const response = await fetch('/api/customers/updateprofile', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                credentials: 'include',
                body: JSON.stringify(payload)
            });
            const result = await response.json();
            if (result.code === 200) {
                // refresh user info
                fetchUserInfo(getCookie('username'));
                setSuccess('Profile updated successfully!');
                setTimeout(() => closeModal(), 1200);
            } else {
                setError(result.message || 'Failed to update profile');
            }
        } catch (error) {
            console.error('Error updating profile:', error);
            setError('Network error. Please try again.');
        }
    };

    // 修改电话
    const updatePhone = async () => {
        if (!modalValue.trim()) {
            setError('Phone number cannot be empty');
            return;
        }

        try {
            const response = await fetch('/api/customers/updatephone', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({
                    name: userInfo.name,
                    phone: modalValue
                })
            });

            const result = await response.json();
            
            if (result.code === 200) {
                setUserInfo({ ...userInfo, phone: modalValue });
                setSuccess('Phone number updated successfully!');
                setTimeout(() => {
                    closeModal();
                }, 1500);
            } else {
                setError(result.message || 'Failed to update phone number');
            }
        } catch (error) {
            console.error('Error updating phone:', error);
            setError('Network error. Please try again.');
        }
    };

    // 修改邮箱
    const updateEmail = async () => {
        if (emailStep === 1) {
            sendVerificationCode();
            return;
        }

        // 验证验证码
        if (!verificationCode) {
            setError('Please enter verification code');
            return;
        }

        if (verificationCode !== serverCode) {
            setError('Invalid verification code');
            return;
        }

        try {
            const response = await fetch('/api/customers/updateemail', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                credentials: 'include',
                body: JSON.stringify({
                    name: userInfo.name,
                    email: newEmail
                })
            });

            const result = await response.json();
            
            if (result.code === 200) {
                setUserInfo({ ...userInfo, email: newEmail });
                setSuccess('Email updated successfully!');
                setTimeout(() => {
                    closeModal();
                }, 1500);
            } else {
                setError(result.message || 'Failed to update email');
            }
        } catch (error) {
            console.error('Error updating email:', error);
            setError('Network error. Please try again.');
        }
    };

    // 统一的保存处理
    const handleSave = () => {
        setError('');
        setSuccess('');

        if (modalType === 'address') {
            updateAddress();
        } else if (modalType === 'phone') {
            updatePhone();
        } else if (modalType === 'email') {
            updateEmail();
        } else if (modalType === 'name') {
            // collect first/last/country from modal inputs
            const firstName = document.getElementById('modal-firstname')?.value || '';
            const lastName = document.getElementById('modal-lastname')?.value || '';
            const country = document.getElementById('modal-country')?.value || '';
            updateProfile({ name: userInfo.name, firstName, lastName, country });
        }
    };

    // 登出 - 调用后端注销 session 并清理本地缓存
    const handleLogout = async () => {
        try {
            await fetch('/api/customers/logout', { method: 'POST', credentials: 'include' });
        } catch (e) {
            console.warn('Logout request failed:', e);
        }

        const currentUsername = userInfo.name;
        localStorage.removeItem('user');
        localStorage.removeItem(`user_${currentUsername}`);
        console.log(`Logged out user: ${currentUsername}`);
        navigate('/login');
    };

    if (loading) {
        return (
            <div className="profile-container">
                <div className="loading">Loading user information...</div>
            </div>
        );
    }

    return (
        <div className="profile-container">
            <div className="profile-card">
                <div style={{ display: 'flex', alignItems: 'center', marginBottom: '16px' }}>
                    <button onClick={() => { window.location.href = 'http://localhost:8080/catalogue'; }} style={{ marginRight: 'auto', padding: '6px 16px', borderRadius: '6px', border: 'none', background: '#eee', cursor: 'pointer' }}>← 返回</button>
                    <button onClick={handleLogout} className="logout-btn">Logout</button>
                </div>

                <div className="profile-content">
                    {/* Name - 不可修改 */}
                    <div className="info-item">
                        <div className="info-label">
                            <span className="icon">📝</span>
                            Username
                        </div>
                        <div className="info-value">{userInfo.name}</div>
                        <button className="edit-btn disabled" disabled>
                            Cannot Edit
                        </button>
                    </div>

                    {/* Address */}
                    <div className="info-item">
                        <div className="info-label">
                            <span className="icon">🏠</span>
                            Address
                        </div>
                        <div className="info-value">
                            {userInfo.address || 'Not set'}
                        </div>
                        <button 
                            className="edit-btn"
                            onClick={() => openModal('address')}
                        >
                            Edit
                        </button>
                    </div>

                    {/* First Name */}
                    <div className="info-item">
                        <div className="info-label">
                            <span className="icon">👤</span>
                            First Name
                        </div>
                        <div className="info-value">{userInfo.firstName || 'Not set'}</div>
                        <button className="edit-btn" onClick={() => openModal('name')}>Edit</button>
                    </div>

                    {/* Last Name */}
                    <div className="info-item">
                        <div className="info-label">
                            <span className="icon">👥</span>
                            Last Name
                        </div>
                        <div className="info-value">{userInfo.lastName || 'Not set'}</div>
                        <button className="edit-btn" onClick={() => openModal('name')}>Edit</button>
                    </div>

                    {/* Country */}
                    <div className="info-item">
                        <div className="info-label">
                            <span className="icon">🌍</span>
                            Country
                        </div>
                        <div className="info-value">{userInfo.country || 'Not set'}</div>
                        <button className="edit-btn" onClick={() => openModal('name')}>Edit</button>
                    </div>

                    {/* Email */}
                    <div className="info-item">
                        <div className="info-label">
                            <span className="icon">📧</span>
                            Email
                        </div>
                        <div className="info-value">
                            {userInfo.email || 'Not set'}
                        </div>
                        <button 
                            className="edit-btn"
                            onClick={() => openModal('email')}
                        >
                            Edit
                        </button>
                    </div>

                    {/* Phone */}
                    <div className="info-item">
                        <div className="info-label">
                            <span className="icon">📱</span>
                            Phone
                        </div>
                        <div className="info-value">
                            {userInfo.phone || 'Not set'}
                        </div>
                        <button 
                            className="edit-btn"
                            onClick={() => openModal('phone')}
                        >
                            Edit
                        </button>
                    </div>
                </div>
            </div>

            {/* 修改弹窗 */}
            {showModal && (
                <div className="modal-overlay" onClick={closeModal}>
                    <div className="modal-content" onClick={(e) => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2>
                                {modalType === 'address' && '🏠 Edit Address'}
                                {modalType === 'email' && '📧 Edit Email'}
                                {modalType === 'phone' && '📱 Edit Phone'}
                            </h2>
                            <button className="close-btn" onClick={closeModal}>×</button>
                        </div>

                        <div className="modal-body">
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

                            {/* 地址修改 */}
                            {modalType === 'address' && (
                                <div className="form-group">
                                    <label>New Address</label>
                                    <input
                                        type="text"
                                        value={modalValue}
                                        onChange={(e) => setModalValue(e.target.value)}
                                        placeholder="Enter your new address"
                                    />
                                </div>
                            )}

                            {/* 个人资料编辑 */}
                            {modalType === 'name' && (
                                <>
                                    <div className="form-group">
                                        <label>First Name</label>
                                        <input
                                            id="modal-firstname"
                                            type="text"
                                            value={modalFirstName}
                                            onChange={(e) => setModalFirstName(e.target.value)}
                                            placeholder="First name"
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Last Name</label>
                                        <input
                                            id="modal-lastname"
                                            type="text"
                                            value={modalLastName}
                                            onChange={(e) => setModalLastName(e.target.value)}
                                            placeholder="Last name"
                                        />
                                    </div>
                                    <div className="form-group">
                                        <label>Country</label>
                                        <input
                                            id="modal-country"
                                            type="text"
                                            value={modalCountry}
                                            onChange={(e) => setModalCountry(e.target.value)}
                                            placeholder="Country"
                                        />
                                    </div>
                                </>
                            )}

                            {/* 电话修改 */}
                            {modalType === 'phone' && (
                                <div className="form-group">
                                    <label>New Phone Number</label>
                                    <input
                                        type="tel"
                                        value={modalValue}
                                        onChange={(e) => setModalValue(e.target.value)}
                                        placeholder="Enter your new phone number"
                                    />
                                </div>
                            )}

                            {/* 邮箱修改 - 两步验证 */}
                            {modalType === 'email' && (
                                <>
                                    {emailStep === 1 ? (
                                        <div className="form-group">
                                            <label>New Email Address</label>
                                            <input
                                                type="email"
                                                value={newEmail}
                                                onChange={(e) => setNewEmail(e.target.value)}
                                                placeholder="Enter your new email address"
                                            />
                                            <span className="form-help">
                                                A verification code will be sent to this email
                                            </span>
                                        </div>
                                    ) : (
                                        <div className="form-group">
                                            <label>Verification Code</label>
                                            <input
                                                type="text"
                                                value={verificationCode}
                                                onChange={(e) => setVerificationCode(e.target.value)}
                                                placeholder="Enter 6-digit verification code"
                                                maxLength="6"
                                            />
                                            <span className="form-help">
                                                Code sent to: {newEmail}
                                            </span>
                                        </div>
                                    )}
                                </>
                            )}
                        </div>

                        <div className="modal-footer">
                            <button className="cancel-btn" onClick={closeModal}>
                                Cancel
                            </button>
                            <button 
                                className="save-btn" 
                                onClick={handleSave}
                                disabled={sendingCode}
                            >
                                {sendingCode ? (
                                    <>
                                        <span className="spinner"></span>
                                        Sending...
                                    </>
                                ) : (
                                    <>
                                        {modalType === 'email' && emailStep === 1 ? 'Send Code' : 'Save'}
                                    </>
                                )}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default UserProfile;
