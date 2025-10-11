import React, { useState } from 'react';
import { productAPI } from '../services/api';
import EditModal from '../components/EditModal';
import './ProductManagement.css';

const ProductManagement = () => {
  const [searchBy, setSearchBy] = useState('name');
  const [keyword, setKeyword] = useState('');
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [updating, setUpdating] = useState(false);
  const [hasSearched, setHasSearched] = useState(false);
  const [lastSearchKeyword, setLastSearchKeyword] = useState('');
  
  // 修改弹窗相关状态
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingProduct, setEditingProduct] = useState({
    id: null,
    name: '',
    category: ''
  });

  // 获取占位符文本
  const getPlaceholder = () => {
    switch (searchBy) {
      case 'name':
        return '请输入产品名称';
      case 'category':
        return '请输入产品类别';
      case 'id':
        return '请输入产品ID';
      default:
        return '请输入关键词';
    }
  };

  // 搜索产品
  const searchProducts = async () => {
    if (!keyword.trim()) {
      alert('请输入搜索关键词');
      return;
    }

    setLoading(true);
    setLastSearchKeyword(keyword);

    try {
      let response;
      
      switch (searchBy) {
        case 'name':
          console.log('send name request:', keyword);
          response = await productAPI.findByName(keyword);
          break;
        case 'category':
          console.log('发送category请求:', keyword);
          response = await productAPI.findByCategory(keyword);
          break;
        case 'id':
          const id = parseInt(keyword);
          if (isNaN(id)) {
            alert('ID必须是数字');
            setLoading(false);
            return;
          }
          console.log('发送id请求:', id);
          response = await productAPI.findById(id);
          setProducts(response.data.code === 200 && response.data.data ? [response.data.data] : []);
          setHasSearched(true);
          setLoading(false);
          return;
        default:
          throw new Error('未知的搜索类型');
      }

      // 处理响应
      if (response.data.code === 200) {
        console.log('搜索结果:', response.data.data);
        setProducts(response.data.data || []);
      } else {
        setProducts([]);
        alert(response.data.message || '搜索失败');
      }

    } catch (error) {
      console.error('搜索失败:', error);
      setProducts([]);
      
      if (error.message && error.message.includes('CORS')) {
        alert('跨域请求被阻止');
      } else if (error.response?.status === 404) {
        alert('未找到相关产品');
      } else if (error.response?.status === 500) {
        alert('服务器错误，请稍后重试');
      } else {
        alert('搜索失败，请检查网络连接');
      }
    } finally {
      setHasSearched(true);
      setLoading(false);
    }
  };

  // 打开修改弹窗
  const openEditModal = (product) => {
    setEditingProduct({
      id: product.id,
      name: product.name,
      category: product.category
    });
    setShowEditModal(true);
  };

  // 关闭修改弹窗
  const closeEditModal = () => {
    setShowEditModal(false);
    setEditingProduct({
      id: null,
      name: '',
      category: ''
    });
  };

  // 修改产品
  const updateProduct = async (updatedData) => {
    if (!updatedData.name.trim()) {
      alert('请输入产品名称');
      return;
    }
    
    if (!updatedData.category.trim()) {
      alert('请输入产品类别');
      return;
    }

    setUpdating(true);

    try {
      console.log('修改产品:', updatedData);
      
      const response = await productAPI.updateProduct({
        id: updatedData.id,
        name: updatedData.name.trim(),
        category: updatedData.category.trim()
      });
      
      console.log('修改响应:', response.data);

      if (response.data.code === 200) {
        alert('产品修改成功');
        
        // 更新本地产品列表中的数据
        setProducts(prev => prev.map(p => 
          p.id === updatedData.id 
            ? { ...p, name: updatedData.name.trim(), category: updatedData.category.trim() }
            : p
        ));
        
        closeEditModal();
        
      } else {
        alert(response.data.message || '修改失败');
      }

    } catch (error) {
      console.error('修改失败:', error);
      
      if (error.response?.status === 404) {
        alert('产品不存在，无法修改');
      } else if (error.response?.status === 500) {
        alert('服务器错误，修改失败');
      } else {
        alert('网络错误，修改失败');
      }
    } finally {
      setUpdating(false);
    }
  };

  // 删除产品
  const deleteProduct = async (productId) => {
    if (!window.confirm(`确定要删除产品ID为 ${productId} 的产品吗？`)) {
      return;
    }

    setDeleting(true);

    try {
      console.log('删除产品ID:', productId);
      
      const response = await productAPI.deleteProduct(productId);
      
      console.log('删除响应:', response.data);

      if (response.data.code === 200) {
        alert('产品删除成功');
        
        // 从当前显示的产品列表中移除已删除的产品
        setProducts(prev => prev.filter(product => product.id !== productId));
        
      } else {
        alert(response.data.message || '删除失败');
      }

    } catch (error) {
      console.error('删除失败:', error);
      
      if (error.response?.status === 404) {
        alert('产品不存在或已被删除');
        // 从列表中移除
        setProducts(prev => prev.filter(product => product.id !== productId));
      } else if (error.response?.status === 500) {
        alert('服务器错误，删除失败');
      } else {
        alert('网络错误，删除失败');
      }
    } finally {
      setDeleting(false);
    }
  };

  // 更新搜索条件时清空结果
  const updateSearchCriteria = (newSearchBy) => {
    setSearchBy(newSearchBy);
    setKeyword('');
    setHasSearched(false);
    setProducts([]);
  };

  // 处理回车键搜索
  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      searchProducts();
    }
  };

  return (
    <div className="product-search">
      <div className="search-container">
        <h1>Search Products Provided By All Organizations</h1>
        
        <div className="search-form">
          <div className="form-group">
            <label htmlFor="searchBy">Search By:</label>
            <select 
              id="searchBy" 
              value={searchBy} 
              onChange={(e) => updateSearchCriteria(e.target.value)}
            >
              <option value="name">Name</option>
              <option value="category">Category</option>
              <option value="id">ID</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="keyword">Enter keyword:</label>
            <input 
              id="keyword" 
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              type="text" 
              placeholder={getPlaceholder()}
              onKeyPress={handleKeyPress}
            />
          </div>

          <button 
            onClick={searchProducts} 
            className="search-btn" 
            disabled={!keyword.trim()}
          >
            Search
          </button>
        </div>

        {/* 显示当前搜索条件 */}
        {hasSearched && (
          <div className="search-criteria">
            <h3>Display the different search criteria</h3>
            <div className="criteria-display">
              <strong>Search By:</strong> {searchBy.charAt(0).toUpperCase() + searchBy.slice(1)}
              <br />
              <strong>Keyword:</strong> {lastSearchKeyword}
            </div>
          </div>
        )}

        {/* 搜索结果 */}
        {hasSearched && (
          <div className="search-results">
            <h3>搜索结果 ({products.length} 条)</h3>
            
            {loading ? (
              <div className="loading">搜索中...</div>
            ) : products.length === 0 ? (
              <div className="no-results">
                <p>未找到相关产品</p>
              </div>
            ) : (
              <div className="products-grid">
                {products.map((product) => (
                  <div key={product.id} className="product-card">
                    <div className="product-header">
                      <h4>{product.name || '无名称'}</h4>
                      <span className="product-id">#{product.id}</span>
                    </div>
                    <div className="product-info">
                      <p><strong>类别:</strong> {product.category || '无类别'}</p>
                      <p><strong>描述:</strong> {product.description || '无描述'}</p>
                    </div>
                    {/* 操作按钮：修改和删除 */}
                    <div className="product-actions">
                      <button 
                        onClick={() => openEditModal(product)} 
                        className="edit-btn"
                        disabled={updating}
                      >
                        {updating ? '修改中...' : '修改'}
                      </button>
                      <button 
                        onClick={() => deleteProduct(product.id)} 
                        className="delete-btn"
                        disabled={deleting}
                      >
                        {deleting ? '删除中...' : '删除'}
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        )}
      </div>

      {/* 修改产品弹窗 */}
      {showEditModal && (
        <EditModal
          product={editingProduct}
          onClose={closeEditModal}
          onSave={updateProduct}
          updating={updating}
        />
      )}
    </div>
  );
};

export default ProductManagement;