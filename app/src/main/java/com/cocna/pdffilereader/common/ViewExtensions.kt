@file:JvmName("ViewExtensions")
@file:Suppress("DEPRECATION")

package com.cocna.pdffilereader.common

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Context.ACTIVITY_SERVICE
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView

/**
 * Sets the visibility of a [View] to [View.VISIBLE]
 */
fun View?.visible() {
    if (this != null) visibility = View.VISIBLE
}

/**
 * Sets the visibility of a [View] to [View.INVISIBLE]
 */
fun View?.invisible() {
    if (this != null) visibility = View.INVISIBLE
}

/**
 * Sets the visibility of a [View] to [View.GONE]
 */
fun View?.gone() {
    if (this != null) visibility = View.GONE
}

/**
 * Allows a [ViewGroup] to inflate itself without all of the unneeded ceremony of getting a
 * [LayoutInflater] and always passing the [ViewGroup] + false. True can optionally be passed if
 * needed.
 *
 * @param layoutId The layout ID as an [Int]
 * @return The inflated [View]
 */
fun ViewGroup.inflate(layoutId: Int, attachToRoot: Boolean = false): View {
    return LayoutInflater.from(context).inflate(layoutId, this, attachToRoot)
}

/**
 * Returns the current [String] entered into an [EditText]. Non-null, ie can return an empty String.
 */
fun EditText?.getTextString(): String {
    return this?.text.toString()
}

fun TextView?.getTextString(): String {
    return this?.text.toString()
}

/**
 * This disables the soft keyboard as an input for a given [EditText]. The method
 * [EditText.setShowSoftInputOnFocus] is officially only available on >API21, but is actually hidden
 * from >API16. Here, we attempt to set that field to false, and catch any exception that might be
 * thrown if the Android implementation doesn't include it for some reason.
 */
@SuppressLint("NewApi")
fun EditText.disableSoftKeyboard() {
    try {
        showSoftInputOnFocus = false
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

/**
 * Returns a physics-based [SpringAnimation] for a given [View].
 *
 * @param property The [DynamicAnimation.ViewProperty] you wish to animate, such as rotation,
 * X or Y position etc.
 * @param finalPosition The end position for the [View] after animation complete
 * @param stiffness The stiffness of the animation, see [SpringForce]
 * @param dampingRatio The damping ratio of the animation, see [SpringForce]
 */
//fun View.createSpringAnimation(
//        property: DynamicAnimation.ViewProperty,
//        finalPosition: Float,
//        stiffness: Float,
//        dampingRatio: Float
//) = SpringAnimation(this, property).apply {
//    spring = SpringForce(finalPosition).apply {
//        this.stiffness = stiffness
//        this.dampingRatio = dampingRatio
//    }
//}

fun View.OnClickListener.listenClickViews(vararg views: View) {
    views.forEach { it.setOnClickListener(this) }
}

fun View.OnClickListener.isEnabled(vararg views: View, isEnabled: Boolean) {
    views.forEach { it.isEnabled = isEnabled }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.hideKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(findViewById<View>(android.R.id.content).windowToken, 0)
}

fun Activity.showKeyboard() {
    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
}

fun View.OnClickListener.clickHideKeyboard(vararg views: View) {
    views.forEach {
        it.setOnClickListener {
            it.hideKeyboard()
        }
    }
}

fun View.OnPreventMultiClick(debounceTime: Long = 600L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {

        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (SystemClock.elapsedRealtime() - lastClickTime < debounceTime) return
            else action()

            lastClickTime = SystemClock.elapsedRealtime()
        }
    })
}

/**
 * Extensions for simpler launching of Activities
 */

inline fun <reified T : Any> Activity.launchActivity(
    requestCode: Int = -1,
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {

    val intent = newIntent<T>(this)
    intent.init()
    startActivityForResult(intent, requestCode, options)
}

inline fun <reified T : Any> Context.launchActivity(
    options: Bundle? = null,
    noinline init: Intent.() -> Unit = {}
) {

    val intent = newIntent<T>(this)
    intent.init()
    startActivity(intent, options)
}

inline fun <reified T : Any> newIntent(context: Context): Intent =
    Intent(context, T::class.java)

fun Activity.isMyServiceRunning(serviceClass: Class<*>): Boolean {
    var manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
    for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
        if (serviceClass.name == service.service.className) {
            return true
        }
    }
    return false
}

fun EditText.limitLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
}

fun <K, V> Map<K, V>.toMutableMap2(): HashMap<K, V> {
    return HashMap(this)
}





