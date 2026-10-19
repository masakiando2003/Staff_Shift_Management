# デジハリ スマートフォンアプリ演習（デジタルハリウッド大学 2019年 4Q）

## 概要
デジタルハリウッド大学 2019年度 4Q「デジハリ スマートフォンアプリ演習」の授業で、
演習のために作ったスタッフのシフトを管理するシステムです。

アプリ名は「シフト管理システム」で、Android 向けのネイティブアプリとして実装しています。

## 使用技術
- 言語: Kotlin 1.3.61
- プラットフォーム: Android（minSdk 26 / targetSdk 28 / compileSdk 28）
- 開発環境: Android Studio、Gradle（Android Gradle Plugin 3.5.3）
- バックエンド: Firebase
  - Firebase Authentication（メールアドレス・パスワード認証、匿名認証）
  - Firebase Realtime Database（ユーザー・シフト・会社情報の保存）
  - Cloud Firestore（ログインユーザー名の取得、オフライン永続化を有効化）
- 主なライブラリ
  - AndroidX（AppCompat / ConstraintLayout / RecyclerView / CardView）
  - Kotlin Android Extensions（レイアウトのビュー参照）
  - [NetWatch](https://github.com/draxdave/netWatch)（ネットワーク接続状態の監視）
  - expandable-layout / expanding-view（展開できるリスト表示）

## 工夫した点
- データをGoogleのFirebaseに保存するように設定しています
- ログインユーザーの権限（一般ユーザー / 管理者）によって、表示するメニューや操作できる内容を切り替えています
- 各画面でネットワークの接続状態を監視し、接続が切れたときは通知とメッセージで知らせるようにしています
- ユーザーのアバター画像はギャラリーから選ぶか、カメラで撮影して登録できます。画像は Base64 文字列に変換してデータベースに保存しています
- 日付・時間の入力には DatePicker / TimePicker のダイアログを使い、入力ミスを減らしています
- シフト登録・編集では、未入力のチェックや「終了時間が出勤時間より後か」のチェックを行っています
- 削除操作の前に確認ダイアログを表示しています
- シフト一覧・ユーザー一覧は、項目を展開すると「見る / 編集する / 削除する」などの操作を選べるようにしています

## 設計
### 処理の流れ
1. アプリ起動時に `MainActivity` を表示します。ログイン情報（SharedPreferences）がなければ `LogInActivity` に移動します
2. `LogInActivity` で Firebase Authentication によるメールアドレス・パスワード認証を行います。認証できたら Realtime Database の `users` からユーザー名と権限を取得し、SharedPreferences に保存します
3. `MainActivity` のメインメニューから各機能の画面に移動します。管理者には「ユーザー登録」メニューも表示します
4. 各画面では Firebase Realtime Database のデータを読み書きします
5. 「ログアウト」を選ぶと `LogInActivity` に戻り、SharedPreferences に保存したログイン情報を削除します

```
LogInActivity
  └─ MainActivity（メインメニュー）
       ├─ 出勤シフト一覧: ShiftDateActivity（日付選択）→ ShiftListActivity → ShiftDetailActivity / ShiftEditActivity
       ├─ 出勤シフト登録: ShiftRegisterActivity
       ├─ 会社情報:       CompanyDetailActivity
       ├─ ユーザーリスト: UserListActivity → UserDetailActivity / UserEditActivity
       ├─ ユーザー登録:   UserRegisterActivity（管理者のみ）
       ├─ アカウント設定: UserSettingActivity
       └─ ログアウト:     LogInActivity
```

Realtime Database のデータ構成は次のとおりです。

```
users/
  {ユーザー名}/
    name, email, password, role（"admin" など）, avatarBase64
shift_list/
  {日付: yyyyMMdd}/
    {ユーザー名}/
      attendTime, endTime, memo
company_info/
  CompanyName, CompanyEmail, CompanyTel, CompanyDesc
```

### フォルダ構成
```
Staff_Shift_Management/
├── build.gradle                 # プロジェクト全体のビルド設定
├── settings.gradle
├── gradle/                      # Gradle Wrapper
└── app/
    ├── build.gradle             # アプリのビルド設定・依存ライブラリ
    ├── google-services.json     # Firebase の接続設定
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   ├── java/jp/ac/dhw/a18dc593/staffshiftmanagement/
        │   │   ├── *Activity.kt         # 各画面
        │   │   ├── *DatePick.kt         # 日付選択ダイアログ
        │   │   ├── *TimePick.kt         # 時間選択ダイアログ
        │   │   ├── *Model.kt            # データモデル（ユーザー・シフト・メニュー）
        │   │   └── MainMenuItemsAdapter.kt  # メインメニューの RecyclerView アダプター
        │   └── res/
        │       ├── layout/              # 画面レイアウト
        │       ├── drawable/            # アイコン・枠線
        │       └── values/              # 文字列・色・スタイル
        ├── test/                        # ユニットテスト
        └── androidTest/                 # インストルメンテーションテスト
```

### 開発時のルール
- 1画面につき1つの Activity を作り、クラス名は `機能名 + Activity`（例: `ShiftRegisterActivity`）にしています
- レイアウトファイル名はスネークケース（例: `shift_list.xml`, `user_detail.xml`）にしています
- 画面に表示する文字列は `res/values/strings.xml` にまとめています
- 機能ごと・日付ごとにブランチを作って開発し、`master` にマージしています（例: `ShiftListActivity_20200130`）
- コミットメッセージは「作業した画面名: 日付」の形式で書いています（例: `Shift List Activity: 2020-02-02`）

## 主な仕様
### ログイン・ログアウト
- メールアドレスとパスワードでログインします
- ログアウトするとログイン情報を削除し、ログイン画面に戻ります

### 出勤シフト
- 一覧: 日付を選ぶと、その日に出勤するスタッフの一覧（アバター・名前）を表示します
- 詳細: スタッフの出勤日・出勤時間・終了時間・メモを表示します
- 登録: 出勤日・出勤時間・終了時間・メモを入力して登録します。一般ユーザーは自分のシフトだけ、管理者は全スタッフのシフトを登録できます
- 編集・削除: 管理者だけが行えます

### ユーザー
- 一覧: 登録されているユーザーの一覧を表示します
- 詳細: ユーザー名・メールアドレス・権限・アバターを表示します
- 登録: ユーザー名・メールアドレス・パスワード・権限・アバターを入力して登録します（管理者のみ）
- 編集・削除: 管理者だけが行えます
- アカウント設定: ログインしているユーザーが自分の情報を変更できます

### 会社情報
- 会社名・メールアドレス・電話番号・会社概要を表示します

### 権限
| 機能 | 一般ユーザー | 管理者 |
| --- | :---: | :---: |
| シフトの閲覧 | ○ | ○ |
| シフトの登録 | 自分のみ | ○ |
| シフトの編集・削除 | × | ○ |
| ユーザーの閲覧 | ○ | ○ |
| ユーザーの登録・編集・削除 | × | ○ |
| アカウント設定 | ○ | ○ |
| 会社情報の閲覧 | ○ | ○ |

## 注意事項
このアプリは授業の演習として作ったもので、実際の運用は想定していません。
コードは提出したときのまま公開しているため、次のような既知の問題があります。

- ユーザーのパスワードを Firebase Realtime Database に平文で保存しています。ユーザー詳細画面にもパスワードを表示しています
- ログイン時に、端末の SharedPreferences にもパスワードを平文で保存しています
- 演習で使った Firebase プロジェクトのデータベースは無効になっているため、このままビルドしてもデータの読み書きはできません

参考にする場合は、パスワードの管理は Firebase Authentication だけに任せ、データベースや端末には保存しないようにしてください。

## ライセンス
[MIT License](LICENSE)
