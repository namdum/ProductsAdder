package com.example.productsadder.di

import android.app.Application
import com.example.productsadder.ui.category.CategoryFragment
import com.example.productsadder.ui.order.OrderFragment
import com.example.productsadder.ui.product.ProductsFragment
import com.example.productsadder.ui.category.AddCategoryActivity
import com.example.productsadder.ui.product.AddProductActivity
import com.example.productsadder.ui.category.EditCategoryActivity
import com.example.productsadder.ui.product.EditProductActivity
import com.example.productsadder.ui.activity.LoginActivity
import com.example.productsadder.ui.activity.SplashActivity
import com.example.productsadder.ui.order.OrderDetailsActivity
import com.example.productsadder.ui.activity.RegisterActivity

abstract class BaseUiApp : Application() {
    abstract fun getAppComponent(): BaseAppComponent
    abstract fun setAppComponent(baseAppComponent: BaseAppComponent)
}

interface BaseAppComponent {
    fun inject(app: Application)
    fun inject(orderDetailsActivity: OrderDetailsActivity)
    fun inject(orderFragment: OrderFragment)
    fun inject(categoryFragment: CategoryFragment)
    fun inject(addCategoryActivity: AddCategoryActivity)
    fun inject(editCategoryActivity: EditCategoryActivity)
    fun inject(editProductActivity: EditProductActivity)
    fun inject(loginActivity: LoginActivity)
    fun inject(splashActivity: SplashActivity)
    fun inject(registerActivity: RegisterActivity)
    fun inject(productsFragment: ProductsFragment)
    fun inject(addProductActivity: AddProductActivity)
}


fun BaseUiApp.getComponent(): BaseAppComponent {
    return this.getAppComponent()
}
