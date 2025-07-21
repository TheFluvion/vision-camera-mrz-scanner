package com.visioncameramrzscanner

import android.annotation.SuppressLint
import android.graphics.Point
import android.graphics.Rect
import android.media.Image
import android.util.Log
import androidx.camera.core.ImageProxy
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.mrousavy.camera.frameprocessors.Frame
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin

import com.mrousavy.camera.frameprocessors.FrameProcessorPluginRegistry
import com.mrousavy.camera.frameprocessors.VisionCameraProxy

class VisionCameraMrzScannerProcessor{
    private fun getBlockArray(blocks: MutableList<Text.TextBlock>): WritableNativeArray {
        val blockArray = WritableNativeArray()

        try {
            Log.d("VisionCameraMrzScanner", "blocks: ${blocks.size}")
            for (block in blocks) {
                val blockMap = WritableNativeMap()

                blockMap.putString("text", block.text)
                blockMap.putArray("recognizedLanguages", getRecognizedLanguages(block.recognizedLanguage))
                blockMap.putArray("cornerPoints", block.cornerPoints?.let { getCornerPoints(it) })
                blockMap.putMap("frame", getFrame(block.boundingBox))
                blockMap.putArray("lines", getLineArray(block.lines))

                blockArray.pushMap(blockMap)
            }
        }
        catch(e: Exception) {
            Log.e("VisionCameraMrzScanner", "Error: ${e.message}")
            e.printStackTrace()
        }
        return blockArray
    }

    private fun getLineArray(lines: MutableList<Text.Line>): WritableNativeArray {
        val lineArray = WritableNativeArray()
        try {
            Log.d("VisionCameraMrzScanner", "lines: ${lines.size}")
            for (line in lines) {
                val lineMap = WritableNativeMap()

                lineMap.putString("text", line.text)
                lineMap.putArray("recognizedLanguages", getRecognizedLanguages(line.recognizedLanguage))
                lineMap.putArray("cornerPoints", line.cornerPoints?.let { getCornerPoints(it) })
                lineMap.putMap("frame", getFrame(line.boundingBox))
                lineMap.putArray("elements", getElementArray(line.elements))

                lineArray.pushMap(lineMap)
            }
        } catch (e: Exception) {
            Log.e("VisionCameraMrzScanner", "Error: ${e.message}")
        }
        return lineArray
    }

    private fun getElementArray(elements: MutableList<Text.Element>): WritableNativeArray {
        val elementArray = WritableNativeArray()
        try {
            Log.d("VisionCameraMrzScanner", "elements: ${elements.size}")
            for (element in elements) {
                val elementMap = WritableNativeMap()

                elementMap.putString("text", element.text)
                elementMap.putArray("cornerPoints", element.cornerPoints?.let { getCornerPoints(it) })
                elementMap.putMap("frame", getFrame(element.boundingBox))

                elementArray.pushMap(elementMap)
            }
        } catch (e: Exception) {
            Log.e("VisionCameraMrzScanner", "Error: ${e.message}")
        }
        return elementArray
    }

    private fun getRecognizedLanguages(recognizedLanguage: String): WritableNativeArray {
        val recognizedLanguages = WritableNativeArray()
        try {
            Log.d("VisionCameraMrzScanner", "recognizedLanguage: ${recognizedLanguage}")
            recognizedLanguages.pushString(recognizedLanguage)
        } catch (e: Exception) {
            Log.e("VisionCameraMrzScanner", "Error: ${e.message}")
        }
        return recognizedLanguages
    }

    private fun getCornerPoints(points: Array<Point>): WritableNativeArray {
        val cornerPoints = WritableNativeArray()
        try {
            Log.d("VisionCameraMrzScanner", "points: ${points.size}")
            for (point in points) {
                val pointMap = WritableNativeMap()
                pointMap.putInt("x", point.x)
                pointMap.putInt("y", point.y)
                cornerPoints.pushMap(pointMap)
            }
        }
        catch(e: Exception) {
            Log.e("VisionCameraMrzScanner", "Error: ${e.message}")
        }
        return cornerPoints
    }

    private fun getFrame(boundingBox: Rect?): WritableNativeMap {
        val frame = WritableNativeMap()
        try {
            Log.d("VisionCameraMrzScanner", "boundingBox: ${boundingBox}")
            if (boundingBox != null) {
                frame.putDouble("x", boundingBox.exactCenterX().toDouble())
                frame.putDouble("y", boundingBox.exactCenterY().toDouble())
                frame.putInt("top", boundingBox.top)
                frame.putInt("left", boundingBox.left)
                frame.putInt("right", boundingBox.right)
                frame.putInt("bottom", boundingBox.bottom)
                frame.putInt("width", boundingBox.width())
                frame.putInt("height", boundingBox.height())
                frame.putInt("boundingCenterX", boundingBox.centerX())
                frame.putInt("boundingCenterY", boundingBox.centerY())
            }
        }
        catch(e: Exception) {
            Log.e("VisionCameraMrzScanner", "Error: ${e.message}")
        }
        return frame
    }

    @SuppressLint("NewApi")
    fun process(frame: ImageProxy, params: Map<String, Any>?): Any? {
        val result = WritableNativeMap()
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        @SuppressLint("UnsafeOptInUsageError")
        val mediaImage: Image? = frame.getImage()

        if (mediaImage != null) {
            return try {
                val image = InputImage.fromMediaImage(mediaImage, frame.imageInfo.rotationDegrees)
                val task: Task<Text> = recognizer.process(image)
                val text: Text = Tasks.await<Text>(task)
                result.putString("text", text.text)
                result.putArray("blocks", getBlockArray(text.textBlocks))
                val data = WritableNativeMap()
                data.putMap("result", result)
                return data
            } catch (e: Exception) {
                Log.e("VisionCameraMrzScanner", "Error: ${e.message}")
                e.printStackTrace()
                return null
            }
        }
        return null
    }
}

class VisionCameraMrzScannerPluginV4 {
    companion object {
        @JvmStatic
        fun install(proxy: VisionCameraProxy) {
            val processorInstance = VisionCameraMrzScannerProcessor()

            FrameProcessorPluginRegistry.addFrameProcessorPlugin(
                "__scanMRZ",
                { actualProxy: VisionCameraProxy, options: Map<String, Any>? ->
                    Log.d("MRZScannerModule", "Initializing FrameProcessorPlugin for __scanMRZ...")
                    object : FrameProcessorPlugin() {
                        override fun callback(frame: Frame, arguments: Map<String?, Any?>?): Any? {
                            Log.d("MRZScannerModule", "FrameProcessorPlugin callback for __scanMRZ invoked!")
                            val imageProxy = try {
                                frame.getImageProxy()
                            } catch (e: Exception) {
                                Log.e("MRZScannerModule", "Error getting ImageProxy from Frame: ${e.message}", e)
                                return null
                            }

                            return processorInstance.process(imageProxy, arguments as Map<String, Any>?)
                        }
                    }
                }
            )
            Log.d("MRZScannerModule", "Frame Processor '__scanMRZ' registered successfully!")
        }
    }
}