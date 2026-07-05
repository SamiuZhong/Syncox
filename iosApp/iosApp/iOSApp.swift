import Shared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        Syncox.shared.initialize(
            networkHandler: Syncox.shared.autoRouter,
            config: SyncoxConfig(
                baseBackoffDelayMs: 2000,
                maxBackoffDelayMs: 3_600_000,
                maxRetries: 10,
                batchSize: 50,
                pollIntervalMs: 5000
            ))
    }
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
