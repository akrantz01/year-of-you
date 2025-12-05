import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinKt.startKoin()
        
        // hacky but forces the launch screen to show so ¯\_(ツ)_/¯
        usleep(useconds_t(300 * 1000))
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
