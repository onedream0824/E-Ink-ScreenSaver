package com.einkscreensaver.app.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.math.*

class EInkAnimator {
    
    private var currentAnimation: ValueAnimator? = null
    private var animationCallback: ((Float) -> Unit)? = null
    
    enum class AnimationType {
        FADE_IN, FADE_OUT, SLIDE_LEFT, SLIDE_RIGHT, SLIDE_UP, SLIDE_DOWN,
        SCALE_IN, SCALE_OUT, ROTATE, WAVE, PARTICLE, TYPEWRITER, GLITCH
    }
    
    fun startAnimation(
        type: AnimationType,
        duration: Long = 1000L,
        callback: (Float) -> Unit
    ) {
        currentAnimation?.cancel()
        animationCallback = callback
        
        currentAnimation = when (type) {
            AnimationType.FADE_IN -> createFadeInAnimation(duration)
            AnimationType.FADE_OUT -> createFadeOutAnimation(duration)
            AnimationType.SLIDE_LEFT -> createSlideLeftAnimation(duration)
            AnimationType.SLIDE_RIGHT -> createSlideRightAnimation(duration)
            AnimationType.SLIDE_UP -> createSlideUpAnimation(duration)
            AnimationType.SLIDE_DOWN -> createSlideDownAnimation(duration)
            AnimationType.SCALE_IN -> createScaleInAnimation(duration)
            AnimationType.SCALE_OUT -> createScaleOutAnimation(duration)
            AnimationType.ROTATE -> createRotateAnimation(duration)
            AnimationType.WAVE -> createWaveAnimation(duration)
            AnimationType.PARTICLE -> createParticleAnimation(duration)
            AnimationType.TYPEWRITER -> createTypewriterAnimation(duration)
            AnimationType.GLITCH -> createGlitchAnimation(duration)
        }
        
        currentAnimation?.start()
    }
    
    private fun createFadeInAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createFadeOutAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(1f, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createSlideLeftAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(1f, 0f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createSlideRightAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(-1f, 0f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createSlideUpAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(1f, 0f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createSlideDownAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(-1f, 0f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createScaleInAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = DecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createScaleOutAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(1f, 0f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createRotateAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 360f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createWaveAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 2 * PI.toFloat()).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val progress = sin(animator.animatedValue as Float)
                animationCallback?.invoke(progress)
            }
        }
    }
    
    private fun createParticleAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createTypewriterAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                animationCallback?.invoke(animator.animatedValue as Float)
            }
        }
    }
    
    private fun createGlitchAnimation(duration: Long): ValueAnimator {
        return ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            repeatCount = ValueAnimator.INFINITE
            addUpdateListener { animator ->
                val progress = (animator.animatedValue as Float) * 10f
                val glitch = sin(progress) * 0.1f + cos(progress * 1.3f) * 0.05f
                animationCallback?.invoke(glitch)
            }
        }
    }
    
    fun stopAnimation() {
        currentAnimation?.cancel()
        currentAnimation = null
        animationCallback = null
    }
    
    fun isAnimating(): Boolean = currentAnimation?.isRunning == true
}

class ParticleSystem {
    private val particles = mutableListOf<Particle>()
    private var isActive = false
    
    data class Particle(
        var x: Float,
        var y: Float,
        var velocityX: Float,
        var velocityY: Float,
        var life: Float,
        var maxLife: Float,
        var size: Float,
        var alpha: Float
    )
    
    fun createParticles(count: Int, width: Int, height: Int) {
        particles.clear()
        repeat(count) {
            particles.add(
                Particle(
                    x = (0..width).random().toFloat(),
                    y = (0..height).random().toFloat(),
                    velocityX = (-2f..2f).random(),
                    velocityY = (-2f..2f).random(),
                    life = 1f,
                    maxLife = (60..120).random().toFloat(),
                    size = (2f..8f).random(),
                    alpha = 1f
                )
            )
        }
    }
    
    fun update() {
        if (!isActive) return
        
        particles.removeAll { particle ->
            particle.life -= 1f
            particle.x += particle.velocityX
            particle.y += particle.velocityY
            particle.alpha = particle.life / particle.maxLife
            
            particle.life <= 0f
        }
    }
    
    fun draw(canvas: Canvas, paint: Paint) {
        particles.forEach { particle ->
            paint.alpha = (particle.alpha * 255).toInt()
            canvas.drawCircle(particle.x, particle.y, particle.size, paint)
        }
    }
    
    fun start() { isActive = true }
    fun stop() { isActive = false }
    fun isRunning(): Boolean = isActive
}

class WaveEffect {
    private var amplitude = 50f
    private var frequency = 0.02f
    private var phase = 0f
    private var speed = 0.1f
    
    fun update() {
        phase += speed
    }
    
    fun drawWave(canvas: Canvas, paint: Paint, width: Int, height: Int) {
        val path = Path()
        path.moveTo(0f, height / 2f)
        
        for (x in 0..width step 5) {
            val y = height / 2f + sin(x * frequency + phase) * amplitude
            path.lineTo(x.toFloat(), y)
        }
        
        canvas.drawPath(path, paint)
    }
    
    fun setAmplitude(amplitude: Float) { this.amplitude = amplitude }
    fun setFrequency(frequency: Float) { this.frequency = frequency }
    fun setSpeed(speed: Float) { this.speed = speed }
}