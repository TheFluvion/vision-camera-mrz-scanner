import Foundation
import AVFoundation
import MLKitVision
import MLKitTextRecognition
import VisionCamera

@objc(VisionCameraMrzScannerPlugin)
public class VisionCameraMrzScannerPlugin: NSObject {
    
    private let textRecognizer = TextRecognizer.textRecognizer()

    @objc
    public static func register(_ proxy: VisionCameraProxy) {
        proxy.registerFrameProcessorPlugin("scanMRZ", plugin: VisionCameraMrzScannerPlugin())
    }

    @objc
    public func callback(_ frame: Frame!, withArguments arguments: [Any]!) -> Any! {
        guard let buffer = CMSampleBufferGetImageBuffer(frame.buffer) else {
            print("Failed to get image buffer from sample buffer.")
            return nil
        }

        let visionImage = VisionImage(buffer: frame.buffer)
        visionImage.orientation = .up

        do {
            let result = try textRecognizer.results(in: visionImage)
            return [
                "result": [
                    "text": result.text,
                    "blocks": result.blocks.map { block in
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
                ]
            ]
        } catch {
            print("Text recognition failed: \(error.localizedDescription)")
            return nil
        }
    }
}
