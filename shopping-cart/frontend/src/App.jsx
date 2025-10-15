import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link, Navigate } from 'react-router-dom';
import ProductManagement from './pages/ProductManagement';
import CreateProduct from './pages/CreateProduct';
import Login from './pages/Login';
import Register from './pages/Register';
import ForgotPassword from './pages/ForgotPassword';
import UserProfile from './pages/UserProfile';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        {/* 导航栏 */}
        {/* <nav className="navbar">
          <div className="nav-container">
            <Link to="/" className="nav-logo">
              Product Management System
            </Link>
            <ul className="nav-menu">
              <li>
                <Link to="/products" className="nav-link">
                  产品搜索
                </Link>
              </li>
              <li>
                <Link to="/create" className="nav-link">
                  创建产品
                </Link>
              </li>
            </ul>
          </div>
        </nav> */}

        {/* 主要内容区域 */}
        <main className="main-content">
            <Routes>
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/forgot-password" element={<ForgotPassword />} />
            <Route path="/profile" element={<UserProfile />} />
            <Route path="/" element={<Navigate to="/login" replace />} />
            <Route path="/products" element={<ProductManagement />} />
            <Route path="/create" element={<CreateProduct />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;