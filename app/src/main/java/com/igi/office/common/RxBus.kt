package com.igi.office.common

import android.app.Activity
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.TimeUnit

// Use object so we have a singleton instance
object RxBus {

    private val publisher = PublishSubject.create<Any>()

    fun publish(event: Any) {
        publisher.onNext(event)
    }

    fun removeEvent() {
        publisher.onNext("")
    }

    // Listen should return an Observable and not the publisher
    // Using ofType we filter only events that match that class type
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
    fun <T> listenDeBounce(eventType: Class<T>): Observable<T> = publisher.ofType(eventType).debounce(300, TimeUnit.MILLISECONDS).distinctUntilChanged()
}