//
//  KmpStarterApp.swift
//  KmpStarter
//
//  Created by Subair on 04/07/2026.
//

import KmpStarterShared
import SwiftUI
import UIKit

private func infoValue(_ key: String, fallback: String) -> String {
    guard let value = Bundle.main.object(forInfoDictionaryKey: key) as? String,
          !value.isEmpty,
          !value.hasPrefix("$("),
          !value.hasPrefix("REPLACE_WITH_") else {
        return fallback
    }
    return value
}

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let firebaseWebApiKey = infoValue("FIREBASE_WEB_API_KEY", fallback: "")
        let googleOAuthClientId = infoValue("GOOGLE_IOS_CLIENT_ID", fallback: "")
        let googleOAuthRedirectScheme = infoValue("GOOGLE_OAUTH_REDIRECT_SCHEME", fallback: "")

        return MainViewControllerKt.MainViewController(
            environmentName: infoValue("KMP_STARTER_ENVIRONMENT", fallback: "nonProd"),
            apiBaseUrl: infoValue("KMP_STARTER_API_BASE_URL", fallback: "https://api.nonprod.example.com/"),
            firebaseWebApiKey: firebaseWebApiKey,
            googleOAuthClientId: googleOAuthClientId,
            googleOAuthRedirectScheme: googleOAuthRedirectScheme,
            authGateway: IosLoginAuthGateway(
                firebaseWebApiKey: firebaseWebApiKey,
                googleOAuthClientId: googleOAuthClientId,
                googleOAuthRedirectScheme: googleOAuthRedirectScheme
            )
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

@main
struct KmpStarterApp: App {
    var body: some Scene {
        WindowGroup {
            ComposeView()
                .ignoresSafeArea()
        }
    }
}
