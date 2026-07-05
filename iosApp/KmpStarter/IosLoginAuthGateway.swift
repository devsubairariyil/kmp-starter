//
//  IosLoginAuthGateway.swift
//  KmpStarter
//

import AuthenticationServices
import KmpStarterShared
import CryptoKit
import UIKit

final class IosLoginAuthGateway: NSObject, LoginAuthGateway, ASWebAuthenticationPresentationContextProviding {
    private let firebaseWebApiKey: String
    private let googleOAuthClientId: String
    private let googleOAuthRedirectScheme: String
    private var authSession: ASWebAuthenticationSession?

    init(firebaseWebApiKey: String, googleOAuthClientId: String, googleOAuthRedirectScheme: String) {
        self.firebaseWebApiKey = firebaseWebApiKey
        self.googleOAuthClientId = googleOAuthClientId
        self.googleOAuthRedirectScheme = googleOAuthRedirectScheme
    }

    func signIn(provider: LoginProvider, completionHandler: @escaping (LoginAuthResult?, Error?) -> Void) {
        switch provider {
        case LoginProvider.google:
            signInWithGoogle(completionHandler: completionHandler)
        case LoginProvider.apple:
            completionHandler(LoginAuthResultFailed(message: "Apple sign-in is not configured yet.", cause: nil), nil)
        case LoginProvider.facebook:
            completionHandler(LoginAuthResultFailed(message: "Facebook sign-in is not configured yet.", cause: nil), nil)
        default:
            completionHandler(LoginAuthResultFailed(message: "Unsupported sign-in provider.", cause: nil), nil)
        }
    }

    func presentationAnchor(for session: ASWebAuthenticationSession) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first { $0.isKeyWindow } ?? ASPresentationAnchor()
    }

    private func signInWithGoogle(completionHandler: @escaping (LoginAuthResult?, Error?) -> Void) {
        guard !firebaseWebApiKey.isEmpty, !googleOAuthClientId.isEmpty, !googleOAuthRedirectScheme.isEmpty else {
            completionHandler(LoginAuthResultFailed(message: "Google sign-in is missing Firebase or Google OAuth configuration.", cause: nil), nil)
            return
        }

        let state = randomUrlSafeString()
        let codeVerifier = randomUrlSafeString(byteCount: 64)
        let redirectUri = "\(googleOAuthRedirectScheme):/oauth2redirect"
        var components = URLComponents(string: "https://accounts.google.com/o/oauth2/v2/auth")
        components?.queryItems = [
            URLQueryItem(name: "client_id", value: googleOAuthClientId),
            URLQueryItem(name: "redirect_uri", value: redirectUri),
            URLQueryItem(name: "response_type", value: "code"),
            URLQueryItem(name: "scope", value: "openid email profile"),
            URLQueryItem(name: "state", value: state),
            URLQueryItem(name: "code_challenge", value: sha256Base64Url(codeVerifier)),
            URLQueryItem(name: "code_challenge_method", value: "S256")
        ]
        guard let authUrl = components?.url else {
            completionHandler(LoginAuthResultFailed(message: "Google sign-in URL could not be created.", cause: nil), nil)
            return
        }

        let session = ASWebAuthenticationSession(url: authUrl, callbackURLScheme: googleOAuthRedirectScheme) { callbackUrl, error in
            if let error = error as? ASWebAuthenticationSessionError,
               error.code == .canceledLogin {
                completionHandler(LoginAuthResultCancelled.shared, nil)
                return
            }
            guard error == nil, let callbackUrl else {
                completionHandler(LoginAuthResultFailed(message: "Google sign-in was cancelled.", cause: nil), nil)
                return
            }
            let callbackComponents = URLComponents(url: callbackUrl, resolvingAgainstBaseURL: false)
            let callbackState = callbackComponents?.queryItems?.first { $0.name == "state" }?.value
            let code = callbackComponents?.queryItems?.first { $0.name == "code" }?.value
            let oauthError = callbackComponents?.queryItems?.first { $0.name == "error" }?.value

            guard oauthError == nil else {
                completionHandler(LoginAuthResultFailed(message: oauthError ?? "Google sign-in failed.", cause: nil), nil)
                return
            }
            guard callbackState == state, let code, !code.isEmpty else {
                completionHandler(LoginAuthResultFailed(message: "Google sign-in response was invalid.", cause: nil), nil)
                return
            }

            self.exchangeGoogleCode(code, codeVerifier: codeVerifier, redirectUri: redirectUri, completionHandler: completionHandler)
        }
        session.presentationContextProvider = self
        session.prefersEphemeralWebBrowserSession = false
        authSession = session
        session.start()
    }

    private func exchangeGoogleCode(
        _ code: String,
        codeVerifier: String,
        redirectUri: String,
        completionHandler: @escaping (LoginAuthResult?, Error?) -> Void
    ) {
        let tokenBody = formEncoded([
            "client_id": googleOAuthClientId,
            "code": code,
            "code_verifier": codeVerifier,
            "grant_type": "authorization_code",
            "redirect_uri": redirectUri
        ])
        post(url: URL(string: "https://oauth2.googleapis.com/token")!, contentType: "application/x-www-form-urlencoded", body: tokenBody) { tokenJson in
            guard let googleIdToken = tokenJson["id_token"] as? String else {
                completionHandler(LoginAuthResultFailed(message: "Google token response did not include an ID token.", cause: nil), nil)
                return
            }
            let firebaseBody: [String: Any] = [
                "postBody": "id_token=\(self.urlEncode(googleIdToken))&providerId=google.com",
                "requestUri": "http://localhost",
                "returnIdpCredential": true,
                "returnSecureToken": true
            ]
            guard let bodyData = try? JSONSerialization.data(withJSONObject: firebaseBody),
                  let body = String(data: bodyData, encoding: .utf8) else {
                completionHandler(LoginAuthResultFailed(message: "Firebase sign-in request could not be created.", cause: nil), nil)
                return
            }
            self.post(
                url: URL(string: "https://identitytoolkit.googleapis.com/v1/accounts:signInWithIdp?key=\(self.firebaseWebApiKey)")!,
                contentType: "application/json",
                body: body
            ) { firebaseJson in
                guard let userId = firebaseJson["localId"] as? String else {
                    completionHandler(LoginAuthResultFailed(message: "Firebase sign-in response did not include a user.", cause: nil), nil)
                    return
                }
                completionHandler(
                    LoginAuthResultSignedIn(
                        userId: userId,
                        displayName: firebaseJson["displayName"] as? String,
                        email: firebaseJson["email"] as? String,
                        idToken: firebaseJson["idToken"] as? String
                    ),
                    nil
                )
            }
        }
    }

    private func post(url: URL, contentType: String, body: String, completion: @escaping ([String: Any]) -> Void) {
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue(contentType, forHTTPHeaderField: "Content-Type")
        request.httpBody = body.data(using: .utf8)

        URLSession.shared.dataTask(with: request) { data, response, _ in
            guard let httpResponse = response as? HTTPURLResponse,
                  (200..<300).contains(httpResponse.statusCode),
                  let data,
                  let json = try? JSONSerialization.jsonObject(with: data) as? [String: Any] else {
                completion([:])
                return
            }
            completion(json)
        }.resume()
    }

    private func formEncoded(_ values: [String: String]) -> String {
        values
            .map { "\(urlEncode($0.key))=\(urlEncode($0.value))" }
            .joined(separator: "&")
    }

    private func randomUrlSafeString(byteCount: Int = 32) -> String {
        var bytes = [UInt8](repeating: 0, count: byteCount)
        _ = SecRandomCopyBytes(kSecRandomDefault, byteCount, &bytes)
        return Data(bytes)
            .base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }

    private func sha256Base64Url(_ value: String) -> String {
        let digest = SHA256.hash(data: Data(value.utf8))
        return Data(digest)
            .base64EncodedString()
            .replacingOccurrences(of: "+", with: "-")
            .replacingOccurrences(of: "/", with: "_")
            .replacingOccurrences(of: "=", with: "")
    }

    private func urlEncode(_ value: String) -> String {
        value.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? value
    }
}
