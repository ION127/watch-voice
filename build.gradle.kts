// 루트 빌드 스크립트 - 플러그인은 여기서 버전만 선언하고 적용은 모듈에서 함
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
