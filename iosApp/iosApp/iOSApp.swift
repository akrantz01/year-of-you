import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinKt.startKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
