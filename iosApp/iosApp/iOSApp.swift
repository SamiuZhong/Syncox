import Shared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        Syncox.shared.initialize(networkHandler: Syncox.shared.autoRouter)
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
