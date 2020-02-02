package jp.ac.dhw.a18dc593.staffshiftmanagement

data class MainMenuItems (var title: String)

object Supplier {
    var menu_items_admin = listOf(
        MainMenuItems("出勤シフト一覧"),
        MainMenuItems("出勤シフト登録"),
        MainMenuItems("会社情報"),
        MainMenuItems("ユーザーリスト"),
        MainMenuItems("ユーザー登録"),
        MainMenuItems("ログアウト")
    )
    var menu_items_user = listOf(
        MainMenuItems("出勤シフト一覧"),
        MainMenuItems("出勤シフト登録"),
        MainMenuItems("会社情報"),
        MainMenuItems("ユーザーリスト"),
        MainMenuItems("アカウント設定"),
        MainMenuItems("ログアウト")
    )
}