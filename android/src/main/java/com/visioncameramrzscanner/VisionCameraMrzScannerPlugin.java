package com.visioncameramrzscanner;

import com.mrousavy.camera.frameprocessor.FrameProcessorPlugin;
import com.mrousavy.camera.frameprocessor.Frame;
import com.facebook.react.bridge.ReadableMap;

import android.graphics.Rect;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class VisionCameraMrzScannerPlugin extends FrameProcessorPlugin {
  public VisionCameraMrzScannerPlugin() {
    super("scanMRZ");
  }

  @Override
  public Object callback(Frame frame, @Nullable ReadableMap params) {
    InputImage image = InputImage.fromMediaImage(frame.getImage(), frame.getOrientation());
    TextRecognizer recognizer = TextRecognition.getClient();

    final CountDownLatch latch = new CountDownLatch(1);
    final Map<String, Object> result = new HashMap<>();

    recognizer.process(image)
      .addOnSuccessListener(visionText -> {
        result.put("text", visionText.getText());
        latch.countDown();
      })
      .addOnFailureListener(e -> {
        Log.e("VisionCameraMrzScanner", "Text recognition failed", e);
        latch.countDown();
      });

    try {
      latch.await();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    return result;
  }
}
