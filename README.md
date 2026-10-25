# 녹음기 (Galaxy Watch 9 / Wear OS)

갤럭시워치9용 단일 화면 보이스 레코더입니다.

- 앱(또는 타일)을 열면 **바로 녹음 시작**
- 가운데 버튼 = **일시정지/재개**, 정지 버튼 = **저장**
- 정지하면 같은 화면에서 **재생 / 일시정지 / 시크**
- 녹음 중 표시는 **아주 작은 점**(어두운 회색)을 **하단 중앙(6시)** 에 표시
- 목록/설정 화면 없음, 화면이 꺼져도 녹음 유지(포그라운드 서비스)

## 중요(솔직한 안내)
- 이 앱의 작은 점은 **앱 화면 안에서만** 바뀌는 표시입니다.
- 안드로이드가 강제하는 **시스템 마이크 표시(상단 초록/흰 점)** 와 **상시 알림**은 그대로 뜹니다.
  이건 OS 보안 기능이라 일반 앱에서 끌 수 없고, 끄지도 않습니다.

## 빌드 환경
- Android Studio(최신 안정판) + JDK 17
- Gradle 8.9 / AGP 8.6.1 / Kotlin 2.0.20 (버전 카탈로그: `gradle/libs.versions.toml`)
- `minSdk 33`, `compileSdk / targetSdk 34`

> Android Studio에서 프로젝트를 열면 Gradle Sync가 필요한 구성 요소를 자동으로 내려받습니다.
> 터미널에서 빌드하려면 Gradle이 설치된 상태에서 한 번 `gradle wrapper` 를 실행해 wrapper를 생성하세요.
> (이 저장소에는 `gradle-wrapper.jar` 바이너리는 포함돼 있지 않습니다.)

## 워치에 설치하기 (ADB 무선)
갤럭시워치9는 USB 연결이 없어 **무선 디버깅**으로 설치합니다.

1. 워치: 설정 → 정보 → 소프트웨어 정보 → **빌드 번호 7번 탭** → 개발자 옵션 활성화
2. 워치: 설정 → 개발자 옵션 → **ADB 디버깅** 켜기, **무선 디버깅** 켜기
3. 워치와 PC를 같은 Wi-Fi에 연결
4. 무선 디버깅 화면의 **페어링 코드로 기기 페어링**에서 IP:포트와 6자리 코드 확인
5. PC 터미널:
   ```
   adb pair <워치IP>:<페어링포트>
   # 코드 입력
   adb connect <워치IP>:<디버깅포트>
   ```
6. Android Studio에서 기기 선택 후 **Run** (또는 `gradlew installDebug`)
7. 첫 실행 시 **마이크 / 알림 권한 허용**
8. 타일 추가: 워치페이스에서 좌/우 스와이프 → 타일 편집 → **녹음** 타일 추가

## 저장 위치
앱 내부 저장소에 `.m4a`로 저장됩니다(별도 저장소 권한 불필요):
```
/data/data/com.example.watchrecorder/files/recordings/
```
PC로 꺼내기:
```
adb exec-out run-as com.example.watchrecorder tar c -C files/recordings . > recordings.tar
```

## 커스터마이즈
설정 화면이 없으므로 아래 파일의 값만 바꾸면 됩니다.

- 점 색 / 크기 / 하단 여백:
  `app/src/main/java/com/example/watchrecorder/presentation/IndicatorConfig.kt`
  - `color` : 점 색 (예: `Color(0xFFFF0000)` 빨강, `Color(0xFFFFFFFF)` 흰색)
  - `sizeDp` : 점 지름(dp)
- 점 위치를 다른 곳으로 옮기려면:
  `.../presentation/components/RecordingIndicator.kt` 의 `Alignment.BottomCenter`
  를 `TopCenter` / `CenterEnd` 등으로 변경

## 구조
```
app/src/main/
  AndroidManifest.xml
  java/com/example/watchrecorder/
    presentation/
      MainActivity.kt          # 진입점
      RecorderApp.kt           # 단일 화면 UI(권한/녹음/재생 분기)
      RecorderViewModel.kt     # 녹음 상태 구독 + 재생(MediaPlayer)
      IndicatorConfig.kt       # 점 색/크기 설정
      components/
        RecordingIndicator.kt  # 하단 중앙 작은 점
        SeekBar.kt             # 탭/드래그 seek 바
      theme/Theme.kt
    recorder/
      RecorderService.kt       # 포그라운드 녹음 서비스(MediaRecorder)
      RecorderState.kt         # 서비스<->UI 공유 상태(StateFlow)
    tile/
      RecorderTileService.kt   # 타일: 탭하면 앱 열며 녹음 시작
    util/TimeFmt.kt
```
