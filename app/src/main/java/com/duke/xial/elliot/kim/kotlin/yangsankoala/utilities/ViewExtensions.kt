package com.duke.xial.elliot.kim.kotlin.yangsankoala.utilities

import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.Transformation

fun View.expand() {
    val matchParentMeasureSpec: Int = View.MeasureSpec.makeMeasureSpec(
        (this.parent as View).width,
        View.MeasureSpec.EXACTLY
    )

    val wrapContentMeasureSpec: Int =
        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
    this.measure(matchParentMeasureSpec, wrapContentMeasureSpec)
    val targetHeight: Int = this.measuredHeight
    val originHeight = this.height

    // Older versions of Android (prior to API 21) cancel the animation of 0 height views.
    this.layoutParams.height = originHeight
    this.visibility = View.VISIBLE
    val animation: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            if (interpolatedTime == 1F)
                this@expand.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            else
                this@expand.layoutParams.height =
                    if ((targetHeight * interpolatedTime).toInt() < originHeight)
                        originHeight
                    else
                        (targetHeight * interpolatedTime).toInt()

            this@expand.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // 1dp/ms expansion rate
    //animation.duration = (targetHeight / this.context.resources.displayMetrics.density).toLong()
    animation.duration = 200L
    this.startAnimation(animation)
}

fun View.collapse(targetHeight: Int) {
    val initialHeight: Int = this.measuredHeight
    val animation: Animation = object : Animation() {
        override fun applyTransformation(
            interpolatedTime: Float,
            t: Transformation?
        ) {
            if (interpolatedTime == 1F) {
                this@collapse.layoutParams.height = targetHeight
            } else {
                this@collapse.layoutParams.height =
                    if ((initialHeight - (initialHeight * interpolatedTime).toInt()) > targetHeight)
                        initialHeight - (initialHeight * interpolatedTime).toInt()
                    else
                        targetHeight
            }

            this@collapse.requestLayout()
        }

        override fun willChangeBounds(): Boolean {
            return true
        }
    }

    // 1dp/ms collapse rate
    // animation.duration = (initialHeight / this.context.resources.displayMetrics.density).toLong()

    animation.duration = 200L
    this.startAnimation(animation)
}