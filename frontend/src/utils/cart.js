// 购物车管理
class CartManager {
  constructor() {
    this.storageKey = 'shopping_cart'
  }
  
  // 获取购物车数据
  getCart() {
    const cart = uni.getStorageSync(this.storageKey)
    return cart || []
  }
  
  // 保存购物车数据
  saveCart(cart) {
    uni.setStorageSync(this.storageKey, cart)
  }
  
  // 生成键：同菜品不同选项应视为不同项
  buildKey(dishId, selectedOptions) {
    if (!selectedOptions || Object.keys(selectedOptions).length === 0) return String(dishId)
    try { return dishId + '|' + JSON.stringify(selectedOptions) } catch (e) { return String(dishId) }
  }

  // 添加到购物车（可传入单价，包含选项加价后的单份价格）
  addToCart(dish, quantity = 1, selectedOptions = null, unitPrice = null) {
    const cart = this.getCart()
    const key = this.buildKey(dish.id, selectedOptions)
    const existingItem = cart.find(item => item.key === key)
    
    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      cart.push({
        key,
        id: dish.id,
        name: dish.name,
        imageUrl: dish.imageUrl,
        price: unitPrice != null ? Number(unitPrice) : Number(dish.price),
        quantity: quantity,
        selectedOptions: selectedOptions || null
      })
    }
    
    this.saveCart(cart)
    return cart
  }
  
  // 更新购物车项数量（按key）
  updateQuantityByKey(key, quantity) {
    const cart = this.getCart()
    const item = cart.find(item => item.key === key)
    
    if (item) {
      if (quantity <= 0) {
        this.removeFromCart(key)
      } else {
        item.quantity = quantity
        this.saveCart(cart)
      }
    }
    
    return cart
  }
  
  // 更新购物车项数量（按dishId，兼容旧版本）
  updateQuantity(dishId, quantity) {
    const cart = this.getCart()
    const item = cart.find(item => item.id === dishId)
    
    if (item) {
      if (quantity <= 0) {
        this.removeFromCartByDishId(dishId)
      } else {
        item.quantity = quantity
        this.saveCart(cart)
      }
    }
    
    return cart
  }
  
  // 从购物车移除（按key删除，支持扩展选项）
  removeFromCart(key) {
    let cart = this.getCart()
    cart = cart.filter(item => item.key !== key)
    this.saveCart(cart)
    return cart
  }
  
  // 从购物车移除（按dishId删除，兼容旧版本）
  removeFromCartByDishId(dishId) {
    let cart = this.getCart()
    cart = cart.filter(item => item.id !== dishId)
    this.saveCart(cart)
    return cart
  }
  
  // 清空购物车
  clearCart() {
    this.saveCart([])
  }
  
  // 获取购物车总数量
  getTotalQuantity() {
    const cart = this.getCart()
    return cart.reduce((total, item) => total + item.quantity, 0)
  }
  
  // 获取购物车总金额
  getTotalAmount() {
    const cart = this.getCart()
    return cart.reduce((total, item) => total + item.price * item.quantity, 0)
  }
}

export default new CartManager()
