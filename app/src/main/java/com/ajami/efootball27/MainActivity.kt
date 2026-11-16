package com.ajami.efootball27

import android.app.Activity
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.View
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN
            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        setContentView(FootballView(this))
    }
}

private class FootballView(activity: Activity) : GLSurfaceView(activity) {
    init {
        setEGLContextClientVersion(2)
        setRenderer(PitchRenderer())
        renderMode = RENDERMODE_CONTINUOUSLY
    }
}

private class PitchRenderer : GLSurfaceView.Renderer {
    private var program = 0
    private var position = 0
    private var color = 0
    private val vertexShader = "attribute vec2 aPosition; void main(){ gl_Position=vec4(aPosition,0.0,1.0); }"
    private val fragmentShader = "precision mediump float; uniform vec4 uColor; void main(){ gl_FragColor=uColor; }"

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.02f, 0.16f, 0.06f, 1f)
        val vs = compile(GLES20.GL_VERTEX_SHADER, vertexShader)
        val fs = compile(GLES20.GL_FRAGMENT_SHADER, fragmentShader)
        program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vs); GLES20.glAttachShader(program, fs)
        GLES20.glLinkProgram(program)
        position = GLES20.glGetAttribLocation(program, "aPosition")
        color = GLES20.glGetUniformLocation(program, "uColor")
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) { GLES20.glViewport(0, 0, width, height) }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)
        GLES20.glUseProgram(program)
        rect(-0.9f,-0.78f,0.9f,0.78f,floatArrayOf(0.08f,0.40f,0.14f,1f))
        line(-0.9f,0f,0.9f,0f); outline(-0.9f,-0.78f,0.9f,0.78f)
        circle(0f,0f,0.16f)
        outline(-0.9f,-0.34f,-0.62f,0.34f); outline(0.62f,-0.34f,0.9f,0.34f)
        val home = arrayOf(-0.72f to 0f,-0.48f to 0.48f,-0.48f to 0.16f,-0.48f to -0.16f,-0.48f to -0.48f,-0.12f to 0.42f,-0.12f to 0.14f,-0.12f to -0.14f,-0.12f to -0.42f,0.34f to 0.20f,0.34f to -0.20f)
        val away = arrayOf(0.72f to 0f,0.48f to 0.48f,0.48f to 0.16f,0.48f to -0.16f,0.48f to -0.48f,0.12f to 0.42f,0.12f to 0.14f,0.12f to -0.14f,0.12f to -0.42f,-0.34f to 0.20f,-0.34f to -0.20f)
        home.forEach { circle(it.first,it.second,0.035f,floatArrayOf(0.12f,0.48f,1f,1f)) }
        away.forEach { circle(it.first,it.second,0.035f,floatArrayOf(0.96f,0.94f,0.86f,1f)) }
        circle(0.02f,0.02f,0.022f,floatArrayOf(1f,0.76f,0.10f,1f))
    }

    private fun compile(type:Int, source:String):Int {
        val shader=GLES20.glCreateShader(type); GLES20.glShaderSource(shader,source); GLES20.glCompileShader(shader); return shader
    }
    private fun draw(points:FloatArray, mode:Int, rgba:FloatArray) {
        val buffer:FloatBuffer=ByteBuffer.allocateDirect(points.size*4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(points).also{it.position(0)}
        GLES20.glEnableVertexAttribArray(position)
        GLES20.glVertexAttribPointer(position,2,GLES20.GL_FLOAT,false,0,buffer)
        GLES20.glUniform4fv(color,1,rgba,0); GLES20.glDrawArrays(mode,0,points.size/2)
        GLES20.glDisableVertexAttribArray(position)
    }
    private fun rect(l:Float,b:Float,r:Float,t:Float,c:FloatArray)=draw(floatArrayOf(l,b,r,b,l,t,l,t,r,b,r,t),GLES20.GL_TRIANGLES,c)
    private fun line(x1:Float,y1:Float,x2:Float,y2:Float)=draw(floatArrayOf(x1,y1,x2,y2),GLES20.GL_LINES,floatArrayOf(1f,1f,1f,0.9f))
    private fun outline(l:Float,b:Float,r:Float,t:Float){line(l,b,r,b);line(r,b,r,t);line(r,t,l,t);line(l,t,l,b)}
    private fun circle(x:Float,y:Float,r:Float,c:FloatArray=floatArrayOf(1f,1f,1f,0.9f)){
        val p=ArrayList<Float>()
        for(i in 0..32){val a=(Math.PI*2*i/32).toFloat();p.add(x);p.add(y);p.add(x+cos(a)*r);p.add(y+sin(a)*r)}
        draw(p.toFloatArray(),GLES20.GL_LINES,c)
    }
}
