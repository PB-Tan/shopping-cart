import axios from 'axios';

// 配置基础URL
const API_BASE = 'http://172.26.235.205:8080/api/products';

// 创建axios实例
const api = axios.create({
  baseURL: API_BASE,
  headers: {
    'Content-Type': 'application/json',
  },
});

// API服务
export const productAPI = {
  // 创建产品
  createProduct: (productData) => {
    return api.post('/productcreate', productData);
  },

  // 根据ID查找
  findById: (id) => {
    return api.post('/productsfindbyid', { id });
  },

  // 根据类别查找
  findByCategory: (category) => {
    return api.post('/productsfindbycategory', { category });
  },

  // 根据名称查找
  findByName: (name) => {
    return api.post('/productsfindbyname', { name });
  },

  // 删除产品
  deleteProduct: (id) => {
    return api.post('/productdeletebyid', { id });
  },

  // 更新产品
  updateProduct: (productData) => {
    return api.post('/productchange', productData);
  },

  // 获取所有产品
  getAllProducts: () => {
    return api.get('/all');
  },
};

export default api;