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
  
  // 添加到购物车
  addToCart(dish, quantity = 1) {
    const cart = this.getCart()
    const existingItem = cart.find(item => item.id === dish.id)
    
    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      cart.push({
        id: dish.id,
        name: dish.name,
        imageUrl: dish.imageUrl,
        price: dish.price,
        quantity: quantity
      })
    }
    
    this.saveCart(cart)
    return cart
  }
  
  // 更新购物车项数量
  updateQuantity(dishId, quantity) {
    const cart = this.getCart()
    const item = cart.find(item => item.id === dishId)
    
    if (item) {
      if (quantity <= 0) {
        this.removeFromCart(dishId)
      } else {
        item.quantity = quantity
        this.saveCart(cart)
      }
    }
    
    return cart
  }
  
  // 从购物车移除
  removeFromCart(dishId) {
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
