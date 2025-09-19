package com.einkscreensaver.app.gesture

import android.content.Context
import android.graphics.PointF
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.VelocityTracker
import kotlin.math.*

class GestureController(
    private val context: Context,
    private val gestureListener: GestureListener
) {
    
    private val gestureDetector: GestureDetector
    private val scaleGestureDetector: ScaleGestureDetector
    private var velocityTracker: VelocityTracker? = null
    
    private var isLongPressHandled = false
    private var lastTouchTime = 0L
    private var touchSequence = mutableListOf<PointF>()
    private var gestureHistory = mutableListOf<GestureType>()
    
    interface GestureListener {
        fun onSwipe(direction: SwipeDirection, velocity: Float)
        fun onTap(x: Float, y: Float)
        fun onDoubleTap(x: Float, y: Float)
        fun onLongPress(x: Float, y: Float)
        fun onPinch(scale: Float, focusX: Float, focusY: Float)
        fun onRotation(angle: Float, focusX: Float, focusY: Float)
        fun onMultiTouch(pointerCount: Int)
        fun onGestureSequence(sequence: List<GestureType>)
        fun onCustomGesture(gesture: CustomGesture)
    }
    
    enum class SwipeDirection {
        UP, DOWN, LEFT, RIGHT, UP_LEFT, UP_RIGHT, DOWN_LEFT, DOWN_RIGHT
    }
    
    enum class GestureType {
        TAP, DOUBLE_TAP, LONG_PRESS, SWIPE_UP, SWIPE_DOWN, SWIPE_LEFT, SWIPE_RIGHT,
        PINCH_IN, PINCH_OUT, ROTATION, MULTI_TOUCH
    }
    
    data class CustomGesture(
        val name: String,
        val points: List<PointF>,
        val duration: Long,
        val confidence: Float
    )
    
    init {
        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapUp(e: MotionEvent): Boolean {
                gestureListener.onTap(e.x, e.y)
                addToHistory(GestureType.TAP)
                return true
            }
            
            override fun onDoubleTap(e: MotionEvent): Boolean {
                gestureListener.onDoubleTap(e.x, e.y)
                addToHistory(GestureType.DOUBLE_TAP)
                return true
            }
            
            override fun onLongPress(e: MotionEvent) {
                if (!isLongPressHandled) {
                    gestureListener.onLongPress(e.x, e.y)
                    addToHistory(GestureType.LONG_PRESS)
                    isLongPressHandled = true
                }
            }
            
            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (e1 != null) {
                    val direction = calculateSwipeDirection(e1.x, e1.y, e2.x, e2.y, velocityX, velocityY)
                    val velocity = sqrt(velocityX * velocityX + velocityY * velocityY)
                    gestureListener.onSwipe(direction, velocity)
                    addSwipeToHistory(direction)
                }
                return true
            }
        })
        
        scaleGestureDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                gestureListener.onPinch(detector.scaleFactor, detector.focusX, detector.focusY)
                addToHistory(if (detector.scaleFactor > 1) GestureType.PINCH_OUT else GestureType.PINCH_IN)
                return true
            }
        })
    }
    
    fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isLongPressHandled = false
                lastTouchTime = System.currentTimeMillis()
                touchSequence.clear()
                touchSequence.add(PointF(event.x, event.y))
                
                velocityTracker = VelocityTracker.obtain()
                velocityTracker?.addMovement(event)
            }
            
            MotionEvent.ACTION_MOVE -> {
                touchSequence.add(PointF(event.x, event.y))
                velocityTracker?.addMovement(event)
                
                if (event.pointerCount > 1) {
                    gestureListener.onMultiTouch(event.pointerCount)
                    addToHistory(GestureType.MULTI_TOUCH)
                }
            }
            
            MotionEvent.ACTION_UP -> {
                velocityTracker?.addMovement(event)
                velocityTracker?.computeCurrentVelocity(1000)
                
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastTouchTime > 500) {
                    analyzeCustomGesture()
                }
                
                velocityTracker?.recycle()
                velocityTracker = null
            }
            
            MotionEvent.ACTION_CANCEL -> {
                velocityTracker?.recycle()
                velocityTracker = null
            }
        }
        
        gestureDetector.onTouchEvent(event)
        scaleGestureDetector.onTouchEvent(event)
        
        return true
    }
    
    private fun calculateSwipeDirection(
        startX: Float, startY: Float,
        endX: Float, endY: Float,
        velocityX: Float, velocityY: Float
    ): SwipeDirection {
        val deltaX = endX - startX
        val deltaY = endY - startY
        val angle = atan2(deltaY, deltaX) * 180 / PI
        
        return when {
            angle in -22.5..22.5 -> SwipeDirection.RIGHT
            angle in 22.5..67.5 -> SwipeDirection.DOWN_RIGHT
            angle in 67.5..112.5 -> SwipeDirection.DOWN
            angle in 112.5..157.5 -> SwipeDirection.DOWN_LEFT
            angle in 157.5..180 || angle in -180..-157.5 -> SwipeDirection.LEFT
            angle in -157.5..-112.5 -> SwipeDirection.UP_LEFT
            angle in -112.5..-67.5 -> SwipeDirection.UP
            angle in -67.5..-22.5 -> SwipeDirection.UP_RIGHT
            else -> SwipeDirection.RIGHT
        }
    }
    
    private fun addToHistory(gesture: GestureType) {
        gestureHistory.add(gesture)
        if (gestureHistory.size > 10) {
            gestureHistory.removeAt(0)
        }
        gestureListener.onGestureSequence(gestureHistory.toList())
    }
    
    private fun addSwipeToHistory(direction: SwipeDirection) {
        val gesture = when (direction) {
            SwipeDirection.UP -> GestureType.SWIPE_UP
            SwipeDirection.DOWN -> GestureType.SWIPE_DOWN
            SwipeDirection.LEFT -> GestureType.SWIPE_LEFT
            SwipeDirection.RIGHT -> GestureType.SWIPE_RIGHT
            else -> GestureType.SWIPE_UP
        }
        addToHistory(gesture)
    }
    
    private fun analyzeCustomGesture() {
        if (touchSequence.size < 3) return
        
        val customGesture = recognizeCustomGesture(touchSequence)
        if (customGesture != null) {
            gestureListener.onCustomGesture(customGesture)
        }
    }
    
    private fun recognizeCustomGesture(points: List<PointF>): CustomGesture? {
        // Circle gesture recognition
        if (isCircleGesture(points)) {
            return CustomGesture("Circle", points, System.currentTimeMillis() - lastTouchTime, 0.9f)
        }
        
        // Triangle gesture recognition
        if (isTriangleGesture(points)) {
            return CustomGesture("Triangle", points, System.currentTimeMillis() - lastTouchTime, 0.8f)
        }
        
        // Square gesture recognition
        if (isSquareGesture(points)) {
            return CustomGesture("Square", points, System.currentTimeMillis() - lastTouchTime, 0.85f)
        }
        
        // Heart gesture recognition
        if (isHeartGesture(points)) {
            return CustomGesture("Heart", points, System.currentTimeMillis() - lastTouchTime, 0.7f)
        }
        
        return null
    }
    
    private fun isCircleGesture(points: List<PointF>): Boolean {
        if (points.size < 8) return false
        
        val center = calculateCenter(points)
        val distances = points.map { sqrt((it.x - center.x).pow(2) + (it.y - center.y).pow(2)) }
        val avgDistance = distances.average()
        val variance = distances.map { (it - avgDistance).pow(2) }.average()
        
        return variance < avgDistance * 0.3
    }
    
    private fun isTriangleGesture(points: List<PointF>): Boolean {
        if (points.size < 6) return false
        
        val corners = findCorners(points)
        return corners.size >= 3 && calculateAngleVariance(corners) < 30
    }
    
    private fun isSquareGesture(points: List<PointF>): Boolean {
        if (points.size < 8) return false
        
        val corners = findCorners(points)
        if (corners.size < 4) return false
        
        val angles = calculateAngles(corners)
        val rightAngles = angles.count { abs(it - 90) < 20 }
        
        return rightAngles >= 3
    }
    
    private fun isHeartGesture(points: List<PointF>): Boolean {
        if (points.size < 10) return false
        
        val center = calculateCenter(points)
        val leftPoints = points.filter { it.x < center.x }
        val rightPoints = points.filter { it.x > center.x }
        
        return leftPoints.isNotEmpty() && rightPoints.isNotEmpty() &&
                isSymmetric(leftPoints, rightPoints, center)
    }
    
    private fun calculateCenter(points: List<PointF>): PointF {
        val avgX = points.map { it.x }.average().toFloat()
        val avgY = points.map { it.y }.average().toFloat()
        return PointF(avgX, avgY)
    }
    
    private fun findCorners(points: List<PointF>): List<PointF> {
        val corners = mutableListOf<PointF>()
        val threshold = 50f
        
        for (i in 1 until points.size - 1) {
            val prev = points[i - 1]
            val curr = points[i]
            val next = points[i + 1]
            
            val angle = calculateAngle(prev, curr, next)
            if (angle < 120) { // Sharp corner
                corners.add(curr)
            }
        }
        
        return corners
    }
    
    private fun calculateAngle(p1: PointF, p2: PointF, p3: PointF): Float {
        val v1 = PointF(p1.x - p2.x, p1.y - p2.y)
        val v2 = PointF(p3.x - p2.x, p3.y - p2.y)
        
        val dot = v1.x * v2.x + v1.y * v2.y
        val mag1 = sqrt(v1.x * v1.x + v1.y * v1.y)
        val mag2 = sqrt(v2.x * v2.x + v2.y * v2.y)
        
        return acos(dot / (mag1 * mag2)) * 180 / PI.toFloat()
    }
    
    private fun calculateAngleVariance(corners: List<PointF>): Float {
        if (corners.size < 3) return 0f
        
        val angles = mutableListOf<Float>()
        for (i in corners.indices) {
            val prev = corners[(i - 1 + corners.size) % corners.size]
            val curr = corners[i]
            val next = corners[(i + 1) % corners.size]
            angles.add(calculateAngle(prev, curr, next))
        }
        
        val avgAngle = angles.average().toFloat()
        val variance = angles.map { (it - avgAngle).pow(2) }.average().toFloat()
        return sqrt(variance)
    }
    
    private fun calculateAngles(corners: List<PointF>): List<Float> {
        val angles = mutableListOf<Float>()
        for (i in corners.indices) {
            val prev = corners[(i - 1 + corners.size) % corners.size]
            val curr = corners[i]
            val next = corners[(i + 1) % corners.size]
            angles.add(calculateAngle(prev, curr, next))
        }
        return angles
    }
    
    private fun isSymmetric(leftPoints: List<PointF>, rightPoints: List<PointF>, center: PointF): Boolean {
        if (leftPoints.size != rightPoints.size) return false
        
        val leftReflected = leftPoints.map { PointF(2 * center.x - it.x, it.y) }
        val rightPointsSorted = rightPoints.sortedBy { it.y }
        val leftReflectedSorted = leftReflected.sortedBy { it.y }
        
        var matches = 0
        for (i in rightPointsSorted.indices) {
            val right = rightPointsSorted[i]
            val left = leftReflectedSorted[i]
            val distance = sqrt((right.x - left.x).pow(2) + (right.y - left.y).pow(2))
            if (distance < 30f) matches++
        }
        
        return matches.toFloat() / rightPoints.size > 0.7f
    }
    
    fun clearHistory() {
        gestureHistory.clear()
    }
    
    fun getGestureHistory(): List<GestureType> = gestureHistory.toList()
}