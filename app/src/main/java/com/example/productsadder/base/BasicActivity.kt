package com.example.productsadder.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

abstract class BasicActivity : AppCompatActivity() {

    private val compositeDisposable = CompositeDisposable()

    var isTransactionSafe = false



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }


    override fun onPostResume() {
        super.onPostResume()
        isTransactionSafe = true
    }

    override fun onPause() {
        super.onPause()
        isTransactionSafe = false
    }

    override fun onDestroy() {
        compositeDisposable.clear()
        super.onDestroy()
    }

    fun Disposable.autoDispose() {
        compositeDisposable.add(this)
    }

}
