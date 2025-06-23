import MLKitVision
import MLKitTextRecognition
import AVFoundation
import Foundation

@objc(VisionCameraMrzScannerPlugin)
public class VisionCameraMrzScannerPlugin: NSObject {
  private let textRecognizer = TextRecognizer.textRecognizer()
  
  @objc
  public func onFrame(_ imageBuffer: CMSampleBuffer, withOrientation orientation: NSNumber, withConfig config: NSDictionary, withPromise resolve: @escaping RCTPromiseResolveBlock, withReject reject: @escaping RCTPromiseRejectBlock) {
    
    guard let pixelBuffer = CMSampleBufferGetImageBuffer(imageBuffer) else {
      reject("E_IMAGE_BUFFER", "No image buffer", nil)
      return
    }

    let visionImage = VisionImage(buffer: imageBuffer)
    visionImage.orientation = .up

    do {
      let result = try textRecognizer.results(in: visionImage)
      let blocks = result.blocks.map { block -> [String: Any] in
        return [
          "text": block.text,
          "cornerPoints": block.cornerPoints.map {
            return ["x": $0.cgPointValue.x, "y": $0.cgPointValue.y]
          },
          "frame": [
            "x": block.frame.origin.x,
            "y": block.frame.origin.y,
            "width": block.frame.size.width,
            "height": block.frame.size.height
          ]
        ]
      }

      resolve([
        "result": [
          "text": result.text,
          "blocks": blocks
        ]
      ])
    } catch {
      reject("E_TEXT_RECOGNITION", error.localizedDescription, error)
    }
  }
}
