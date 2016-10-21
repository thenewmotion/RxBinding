package com.jakewharton.rxbinding.support.design.widget

import android.support.design.widget.BottomNavigationView
import android.view.MenuItem
import rx.Observable

/**
 * Create an observable which emits the selected item in `view`.
 *
 * *Warning:* The created observable keeps a strong reference to `view`. Unsubscribe
 * to free this reference.
 *
 * *Note:* If an item is already selected, it will be emitted immediately on subscribe.
 */
inline fun BottomNavigationView.itemSelections(): Observable<MenuItem> = RxBottomNavigationView.itemSelections(this)
