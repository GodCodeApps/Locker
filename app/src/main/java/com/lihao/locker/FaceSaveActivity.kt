package com.lihao.locker

import android.app.AlertDialog
import android.content.DialogInterface
import android.graphics.*
import android.hardware.Camera
import android.os.Bundle
import android.os.Message
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.arcsoft.facedetection.AFD_FSDKEngine
import com.arcsoft.facedetection.AFD_FSDKError
import com.arcsoft.facedetection.AFD_FSDKFace
import com.arcsoft.facedetection.AFD_FSDKVersion
import com.arcsoft.facerecognition.AFR_FSDKEngine
import com.arcsoft.facerecognition.AFR_FSDKFace
import com.arcsoft.facerecognition.AFR_FSDKVersion
import com.arcsoft.facetracking.AFT_FSDKEngine
import com.arcsoft.facetracking.AFT_FSDKFace
import com.guo.android_extend.GLES2Render
import com.guo.android_extend.image.ImageConverter
import com.guo.android_extend.widget.CameraFrameData
import com.guo.android_extend.widget.CameraSurfaceView
import com.guo.android_extend.widget.ExtImageView
import kotlinx.android.synthetic.main.activity_face_save.*
import java.util.ArrayList
import kotlin.Exception as Exception1


class FaceSaveActivity : AppCompatActivity(), CameraSurfaceView.OnCameraListener, SurfaceHolder.Callback {
    override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
    }

    override fun surfaceDestroyed(p0: SurfaceHolder?) {
        mSurfaceHolder = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {
        mSurfaceHolder = holder

    }

    var mSurfaceHolder: SurfaceHolder? = null
    var mCameraID: Int = 0
    var mCameraRotate: Int = 0
    var mCameraMirror: Int = 0
    var mCamera: Camera? = null
    private var mWidth: Int = 0
    var mHeight: Int = 0
    private var mFormat: Int = 0
    var result: MutableList<AFT_FSDKFace> = ArrayList()
    override fun setupChanged(format: Int, width: Int, height: Int) {
    }

    override fun onBeforeRender(data: CameraFrameData?) {
    }

    override fun startPreviewImmediately(): Boolean {
        return true
    }

    override fun onPreview(data: ByteArray?, width: Int, height: Int, format: Int, timestamp: Long): Any {

        //copy rects
        val rects = arrayOfNulls<Rect>(result.size)
        for (i in result.indices) {
            rects[i] = Rect(result.get(i).getRect())
        }
        result.clear()
        return rects
    }

    override fun setupCamera(): Camera {
        var mCamera = Camera.open(mCameraID)
        try {
            val parameters = mCamera.getParameters()
            parameters.setPreviewSize(mWidth, mHeight)
            parameters.setPreviewFormat(mFormat)

            for (size in parameters.getSupportedPreviewSizes()) {
            }
            for (format in parameters.getSupportedPreviewFormats()) {
            }

            val fps = parameters.getSupportedPreviewFpsRange()
            for (count in fps) {
                for (data in count) {
                }
            }
            mCamera.setParameters(parameters)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (mCamera != null) {
            mWidth = mCamera.getParameters().getPreviewSize().width
            mHeight = mCamera.getParameters().getPreviewSize().height
        }
        return mCamera
    }

    override fun onAfterRender(data: CameraFrameData?) {
        glsurfaceView.getGLES2Render().draw_rect(data?.params as Array<Rect>, Color.GREEN, 2)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCameraID = Camera.CameraInfo.CAMERA_FACING_FRONT

        mCameraRotate = 270
        mCameraMirror = GLES2Render.MIRROR_X
        mWidth = 1280
        mHeight = 960
        mFormat = ImageFormat.NV21
        setContentView(R.layout.activity_face_save)
        surfaceView.setOnCameraListener(this)
        surfaceView.setupGLSurafceView(glsurfaceView, true, mCameraMirror, mCameraRotate)
        surfaceView.debug_print_fps(true, false)

    }


}