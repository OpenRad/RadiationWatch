ポケットガイガーWindows用サンプルプログラム
Sample program for Windows PC with Pocket Geiger Type6

■本圧縮ファイル「PocketGeigerType6_for_Windows.zip」を、デスクトップに解凍してください。
Decompress the file of 'PocketGeigerType6_for_Windows.zip' onto your desktop folder.

■WindowsマシンにType6を接続すると「PocketGeiger Type-6 CDC」として認識されますが、 
その他デバイスとして、ドライバーがインストールされていない状態で認識されます。
Please update a driver for PocketGeiger Type-6 by following instructions.

以下、ドライバーのインストール手順を示します。
①デバイスマネージャから「PocketGeiger Type-6 CDC」を選択し、「ドライバーの更新」を実行します。
(1) Run Device Manager and select 'Update driver software'.

②「コンピューターを検索してドライバーソフトウェアを検索」を選択します。
(2) Select 'Browse my computer for driver software'

③ドライバー（infファイル）の場所を選択します。
（PocketGeigerType6_for_Windowsの中にinfフォルダーが有りますので、それを指定します）
(3) Select the 'inf' folder in PocketGeigerType6_for_Windows.

④次へボタンでインストールを開始します。
(4) Click 'next' and install the driver software.

⑤ドライバー更新が正常に終了すると、「USB Serial Port (COMxx)」として認識されます
(5) After finishing the driver, your PC will detect PocketGeiger as 'USB Serial Port (COMxx)'.

⑥実行ファイルをクリックしてソフトを起動してください。（\\PocketGeigerType6_for_Windows\PocketGeigerType6\bin\Release\PocketGeigerType6.exe）
(6) Run the application. (\\PocketGeigerType6_for_Windows\PocketGeigerType6\bin\Release\PocketGeigerType6.exe)

※Windows8以降にインストールする場合はコマンドプロンプトから「shutdown /r /o /t 0」と入力し、「ドライバー署名の強制を無効化」モードで起動してください。
* When you use Windows8 or later,run 'Disable Driver Signature Enforcement' mode by 'shutdown /r /o /t 0' on command prompt.

※.NET Framework4.0 以降がインストールされている環境では、exeファイルのみでも実行可能です。
* When you already have .NET Framework4.0, you need not install the driver.
