import Foundation
import VisionCamera
import MLKitVision
import MLKitTextRecognition
import AVFoundation

@objc(VisionCameraMrzScannerPlugin)
public class VisionCameraMrzScannerPlugin: NSObject, FrameProcessorPlugin {
    
    private lazy var textRecognizer = TextRecognizer.textRecognizer()

    public func callback(_ frame: Frame, withArguments arguments: [Any]?) -> Any? {
        let imageBuffer = CMSampleBufferGetImageBuffer(frame.buffer)
        guard imageBuffer != nil else {
            print("Failed to get image buffer from frame.")
            return nil
        }

        let visionImage = VisionImage(buffer: frame.buffer)
        visionImage.orientation = .up

        do {
            let result = try textRecognizer.results(in: visionImage)
            return [
                "result": [
                    "text": result.text,
                    "blocks": Self.getBlockArray(result.blocks)
                ]
            ]
        } catch {
            print("Text recognition error: \(error.localizedDescription)")
            return nil
        }
    }

    public static func pluginName() -> String {
        return "vision-camera-mrz-scanner"
    }

    private static func getBlockArray(_ blocks: [TextBlock]) -> [[String: Any]] {
        return blocks.map { block in
            [
                "text": block.text,
                "recognizedLanguages": getRecognizedLanguages(block.recognizedLanguages),
                "cornerPoints": getCornerPoints(block.cornerPoints),
                "frame": getFrame(block.frame),
                "lines": getLineArray(block.lines),
            ]
        }
    }

    private static func getLineArray(_ lines: [TextLine]) -> [[String: Any]] {
        return lines.map { line in
            [
                "text": line.text,
                "recognizedLanguages": getRecognizedLanguages(line.recognizedLanguages),
                "cornerPoints": getCornerPoints(line.cornerPoints),
                "frame": getFrame(line.frame),
                "elements": getElementArray(line.elements),
            ]
        }
    }

    private static func getElementArray(_ elements: [TextElement]) -> [[String: Any]] {
        return elements.map { element in
            [
                "text": element.text,
                "cornerPoints": getCornerPoints(element.cornerPoints),
                "frame": getFrame(element.frame),
            ]
        }
    }

    private static func getRecognizedLanguages(_ languages: [TextRecognizedLanguage]) -> [String] {
        return languages.compactMap { $0.languageCode }
    }

    private static func getCornerPoints(_ cornerPoints: [NSValue]) -> [[String: CGFloat]] {
        return cornerPoints.compactMap {
            guard let point = $0 as? CGPoint else { return nil }
            return ["x": point.x, "y": point.y]
        }
    }

    private static func getFrame(_ frameRect: CGRect) -> [String: CGFloat] {
        return [
            "x": frameRect.origin.x,
            "y": frameRect.origin.y,
            "top": frameRect.minY,
            "left": frameRect.minX,
            "right": frameRect.maxX,
            "bottom": frameRect.maxY,
            "width": frameRect.width,
            "height": frameRect.height,
            "boundingCenterX": frameRect.midX,
            "boundingCenterY": frameRect.midY
        ]
    }
}
