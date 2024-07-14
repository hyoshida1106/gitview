# GitView

GitViewはkotlin+JavaFXで記述されたGit GUIクライアントツールです。

![img.png](img.png)

### 特徴

* すべて[Kotlin](https://kotlinlang.org/)で記述されています
* GUIに[JavaFX(OpenJfx)](https://openjfx.io/)、Gitライブラリとして[JGit](https://github.com/eclipse-jgit/jgit)を使用しています
* JVM上で動作するため、実行環境を選ばず動作します(そのはずです)。

### 動作条件

JDK21を使用して開発しています。実行にはJava21の使用可能な環境を用意してください。
![img_1.png](img_1.png)
開発はWindows上で、[Microsoft Build of OpenJDK](https://learn.microsoft.com/ja-jp/java/openjdk/)を使用して行っています。  
LinuxとMacでも同等の環境を用意すれば動作すると思います(Linuxのみ確認済)。

### 実行方法

1. 公開されているZipファイルを適当な場所に展開します。
2. binディレクトリ下にある *gitview.bat*(Windows)または*gitview*(他)を実行してください。
3. 空白のウィンドウが開くので、リポジトリの「新規作成」「クローン」などを行います。クローンの取得元はURLで指定してください。

### 注意点など

* 仕事に使う人はいないと思いますが、「重要なリポジトリは操作しないこと」と警告させて頂きます。
* プログラム的に面白いと思って頂ける方、興味をお持ちの方はご連絡ください。

以上