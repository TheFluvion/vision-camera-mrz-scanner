import Foundation
import VisionCamera
import vision_camera_mrz_scanner

@objc(VisionCameraMrzScannerPackage)
public class VisionCameraMrzScannerPackage: NSObject, RCTBridgeModule {
    
    public static func moduleName() -> String! {
        return "VisionCameraMrzScannerPackage"
    }

    public static func requiresMainQueueSetup() -> Bool {
        return false
    }

    @objc public func register(_ pluginRegistry: FrameProcessorPluginRegistry) {
        pluginRegistry.register(VisionCameraMrzScannerPlugin())
    }
}
