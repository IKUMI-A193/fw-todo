# やること管理アプリ DB設計書

## 1. 目的と対象

この文書は、`handout_app_spec.md` と `requirements.md` に基づき、やることを保存するDB（データベース。データを保存する仕組み）を定める。対象はMySQLの `todos` テーブル（表）1つだけで、やること1件を1行として保存する。画面の詳しい設計は別の設計書で扱う。

## 2. テーブルとカラムの定義

カラムは表の縦の項目、型は保存する値の種類、`NULL` は値が入っていない状態を表す。主キーは1行を一意に見分ける番号、既定値は値を指定しないときに入る値を表す。

| カラム | 保存する内容 | 型 | `NULL` | 既定値・自動入力 | 対応する要件 |
|---|---|---|---|---|---|
| `id` | やることを見分ける番号 | `BIGINT` | 不可 | 主キー。新規登録時に自動で増える | FR-06、FR-07、FR-09／要件 4.3／元仕様 2、5-2 |
| `title` | やることの名前 | `VARCHAR(255)` | 不可 | なし | FR-01、FR-02、FR-05、FR-06、FR-07／要件 4.1、4.2／元仕様 2、5-1、5-4 |
| `detail` | メモ | `VARCHAR(255)` | 可 | なし | FR-05、FR-06／要件 4.1、4.2／元仕様 2、5-1 |
| `category` | ジャンル | `VARCHAR(255)` | 不可 | なし | FR-01、FR-02、FR-05、FR-06、FR-07／要件 4.1／元仕様 2、5-4 |
| `priority` | 優先度 | `INT` | 不可 | `2`（中） | FR-01、FR-05、FR-06／要件 4.1、4.3／元仕様 2、4 |
| `due_date` | 期限 | `DATE` | 可 | なし | FR-01、FR-03、FR-05、FR-06、FR-07／要件 4.1／元仕様 2、5-4 |
| `completed` | 完了したかどうか | `BOOLEAN` | 不可 | `FALSE`（未完了、保存値 `0`） | FR-01、FR-05、FR-06／要件 4.1、4.3／元仕様 2、4 |
| `created_at` | 登録日時 | `DATETIME` | 不可 | 登録時にMySQLが現在日時を入れる | FR-05／要件 4.3／元仕様 2 |
| `updated_at` | 最後に直した日時 | `DATETIME` | 不可 | 登録時にMySQLが現在日時を入れ、更新時に更新する | FR-06／要件 4.3／元仕様 2 |

`VARCHAR(255)` は最大255文字の文字列、`DATE` は日付、`DATETIME` は日付と時刻を保存する型である。`BOOLEAN` は真偽（はい／いいえ）を表し、MySQLでは `1`／`0` として扱う。`id`・`created_at`・`updated_at` は利用者に入力させない（要件 4.3、元仕様 2）。

## 3. 保存する値と入力時の決まり

| 対象カラム | 保存できる値・入力時の決まり | 対応する要件 |
|---|---|---|
| `title` | 必須。255文字以内。半角・全角の空白だけの入力は空とみなし、保存前に止める。 | 要件 4.1、4.2／元仕様 5-1 |
| `detail` | 任意。入力する場合は255文字以内。入力しないときは `NULL` にできる。 | 要件 4.1、4.2／元仕様 2、5-1 |
| `category` | 必須。`デザイン`・`マーケティング`・`プログラミング`・`資格`・`就職活動` のいずれか。 | 要件 4.1／元仕様 2、4 |
| `priority` | 必須。`1`＝高、`2`＝中、`3`＝低。DBの既定値は `2` だが、登録画面では未選択を許さず、利用者に選んでもらう。 | FR-01、FR-05／要件 4.1、4.3／元仕様 2、4 |
| `due_date` | 任意。入力しないときは `NULL` にできる。期限独自の入力規則はない。 | FR-03／要件 4.1／元仕様 2、5-1 |
| `completed` | `0`＝未完了、`1`＝完了。登録時は `0`。編集時に変更できる。 | FR-01、FR-05、FR-06／元仕様 2、4 |

必須入力の確認とエラーメッセージ（入力が誤っているときの文）は保存前にアプリで扱う。`NOT NULL`（値なしを禁止する指定）だけでは、空文字や空白だけの `title` を防げない。

## 4. 検索・並び替えと保存しない情報

名前の部分一致検索（入力した文字が一部でも含まれる検索）は `title`、ジャンルの完全一致検索（同じ値だけを残す検索）は `category` を使う。期限の近い順・遠い順の並び替えには `due_date` を使い、初回表示は近い順とする（FR-02、FR-03／元仕様 5-4）。期限が未入力の行の表示位置は元の資料で指定されていない。

検索キーワード、選択した並び順、操作結果の表示文は `todos` に保存するカラムを設けない。検索条件と並び順はアドレスの後ろに付けて渡し、操作結果の文は直前の操作に応じて表示する（FR-03、FR-08／元仕様 5-3、5-4）。登録・編集・削除のログ（動作の記録）は操作の種類と `id` だけを記録する要件であり、追加のDBテーブルは設けない（要件 6／元仕様 5-5）。

## 5. CREATE TABLE文（テーブル作成用DDL）

DDLは、DBに表の形を作る命令文を指す。次のSQL（DBに命令を伝える文）はMySQL 8.0用で、`todos` テーブルを作成する。

```sql
CREATE TABLE todos (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    detail VARCHAR(255) NULL,
    category VARCHAR(255) NOT NULL,
    priority INT NOT NULL DEFAULT 2,
    due_date DATE NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT chk_todos_category CHECK (
        category IN ('デザイン', 'マーケティング', 'プログラミング', '資格', '就職活動')
    ),
    CONSTRAINT chk_todos_priority CHECK (priority IN (1, 2, 3))
);
```

`AUTO_INCREMENT` は番号を自動で増やす指定、`DEFAULT` は既定値の指定、`CHECK` は指定した値以外の保存を防ぐ条件である。`ON UPDATE CURRENT_TIMESTAMP` により、行を更新した際に `updated_at` が更新される。ジャンルと優先度の `CHECK` は、元仕様で決まっている選択肢をDB側でも守るために設ける。
