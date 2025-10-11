import React, { useState, useEffect } from 'react';
import './EditModal.css';

const EditModal = ({ product, onClose, onSave, updating }) => {
  const [formData, setFormData] = useState({
    id: null,
    name: '',
    category: ''
  });

  useEffect(() => {
    if (product) {
      setFormData({
        id: product.id,
        name: product.name,
        category: product.category
      });
    }
  }, [product]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSave = () => {
    onSave(formData);
  };

  const handleOverlayClick = (e) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="modal-content">
        <div className="modal-header">
          <h3>修改产品信息</h3>
          <button onClick={onClose} className="close-btn">&times;</button>
        </div>
        <div className="modal-body">
          <div className="form-group">
            <label>产品ID:</label>
            <input 
              type="text" 
              value={formData.id || ''} 
              disabled 
              className="disabled-input" 
            />
          </div>
          <div className="form-group">
            <label>产品名称:</label>
            <input 
              type="text" 
              name="name"
              value={formData.name}
              onChange={handleInputChange}
              placeholder="请输入产品名称"
              className="modal-input"
            />
          </div>
          <div className="form-group">
            <label>产品类别:</label>
            <input 
              type="text" 
              name="category"
              value={formData.category}
              onChange={handleInputChange}
              placeholder="请输入产品类别"
              className="modal-input"
            />
          </div>
        </div>
        <div className="modal-footer">
          <button onClick={onClose} className="cancel-btn">取消</button>
          <button onClick={handleSave} className="confirm-btn" disabled={updating}>
            {updating ? '保存中...' : '确认修改'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default EditModal;