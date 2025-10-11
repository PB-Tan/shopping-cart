import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { productAPI } from '../services/api';
import './CreateProduct.css';

const CreateProduct = () => {
  const navigate = useNavigate();
  const [creating, setCreating] = useState(false);
  const [createdProduct, setCreatedProduct] = useState(null);

  // 表单数据
  const [formData, setFormData] = useState({
    name: '',
    category: '',
    description: ''
  });

  // 表单验证
  const isFormValid = formData.name.trim() && formData.category.trim();

  // 处理输入变化
  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  // 创建产品
  const createProduct = async (e) => {
    e.preventDefault();
    
    if (!isFormValid) {
      alert('请填写必填字段（产品名称和类别）');
      return;
    }

    setCreating(true);
    setCreatedProduct(null);

    try {
      // 准备发送的数据
      const requestData = {
        name: formData.name.trim(),
        category: formData.category.trim(),
        description: formData.description.trim() || null
      };
      
      console.log('发送创建请求:', requestData);
      
      // 发送POST请求
      const response = await productAPI.createProduct(requestData);
      
      console.log('创建响应:', response.data);

      if (response.data.code === 200) {
        setCreatedProduct(response.data.data);
        
        // 显示成功消息
        alert('产品创建成功！');
        
        // 滚动到结果区域
        setTimeout(() => {
          const resultSection = document.querySelector('.result-section');
          if (resultSection) {
            resultSection.scrollIntoView({ behavior: 'smooth' });
          }
        }, 100);
        
      } else {
        alert(response.data.message || '创建失败');
      }

    } catch (error) {
      console.error('创建失败:', error);
      
      if (error.response?.status === 400) {
        alert('请求数据格式错误，请检查输入内容');
      } else if (error.response?.status === 500) {
        alert('服务器错误，创建失败');
      } else if (error.response?.data?.message) {
        alert('创建失败：' + error.response.data.message);
      } else {
        alert('网络错误，创建失败');
      }
    } finally {
      setCreating(false);
    }
  };

  // 重置表单
  const resetForm = () => {
    setFormData({
      name: '',
      category: '',
      description: ''
    });
    setCreatedProduct(null);
  };

  // 创建另一个产品
  const createAnother = () => {
    resetForm();
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  // 跳转到搜索页面
  const goToSearch = () => {
    navigate('/products');
  };

  // 格式化日期
  const formatDate = (dateString) => {
    if (!dateString) return '未知';
    
    try {
      const date = new Date(dateString);
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      });
    } catch (e) {
      return dateString;
    }
  };

  return (
    <div className="create-product">
      <div className="create-container">
        <h1>创建新产品</h1>
        
        <div className="create-form">
          <form onSubmit={createProduct}>
            <div className="form-group">
              <label htmlFor="productName">产品名称 <span className="required">*</span></label>
              <input 
                id="productName"
                name="name"
                value={formData.name}
                onChange={handleInputChange}
                type="text" 
                placeholder="请输入产品名称"
                required
                disabled={creating}
              />
            </div>

            <div className="form-group">
              <label htmlFor="productCategory">产品类别 <span className="required">*</span></label>
              <input 
                id="productCategory"
                name="category"
                value={formData.category}
                onChange={handleInputChange}
                type="text" 
                placeholder="请输入产品类别"
                required
                disabled={creating}
              />
            </div>

            <div className="form-group">
              <label htmlFor="productDescription">产品描述</label>
              <textarea 
                id="productDescription"
                name="description"
                value={formData.description}
                onChange={handleInputChange}
                placeholder="请输入产品描述（可选）"
                rows="4"
                disabled={creating}
              />
            </div>

            <div className="form-actions">
              <button 
                type="button" 
                onClick={resetForm} 
                className="reset-btn"
                disabled={creating}
              >
                重置
              </button>
              <button 
                type="submit" 
                className="create-btn"
                disabled={creating || !isFormValid}
              >
                {creating ? '创建中...' : '创建产品'}
              </button>
            </div>
          </form>
        </div>

        {/* 创建结果显示 */}
        {createdProduct && (
          <div className="result-section">
            <h2>✅ 产品创建成功！</h2>
            <div className="product-display">
              <div className="product-card">
                <div className="product-header">
                  <h3>{createdProduct.name}</h3>
                  <span className="product-id">#{createdProduct.id}</span>
                </div>
                <div className="product-info">
                  <p><strong>类别:</strong> {createdProduct.category}</p>
                  <p><strong>描述:</strong> {createdProduct.description || '无描述'}</p>
                  <p><strong>创建时间:</strong> {formatDate(createdProduct.createdAt)}</p>
                </div>
              </div>
            </div>
            
            <div className="result-actions">
              <button onClick={createAnother} className="create-another-btn">
                创建另一个产品
              </button>
              <button onClick={goToSearch} className="go-search-btn">
                前往搜索页面
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default CreateProduct;