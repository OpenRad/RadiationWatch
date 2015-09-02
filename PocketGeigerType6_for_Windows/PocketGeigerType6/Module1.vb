Imports System.IO.Ports
Imports System.Management

Module Module1

    Public Class SerialParam
        Private vPortName As String = ""
        Private vBaudRate As Decimal = 115200
        Private vParity As String = "NONE"
        Private vStopBits As Decimal = 1
        Private vDataBits As Integer = 8

        Public Property PortName As String
            Get
                Return vPortName
            End Get
            Set(value As String)
                vPortName = value
            End Set
        End Property

        Public Property BaudRate As Decimal
            Get
                Return vBaudRate
            End Get
            Set(value As Decimal)
                vBaudRate = value
            End Set
        End Property

        Public Property Parity As String
            Get
                Return vParity
            End Get
            Set(value As String)
                vParity = value
            End Set
        End Property

        Public Property StopBits As Decimal
            Get
                Return vStopBits
            End Get
            Set(value As Decimal)
                vStopBits = value
            End Set
        End Property

        Public Property DataBits As Integer
            Get
                Return vDataBits
            End Get
            Set(value As Integer)
                vDataBits = value
            End Set
        End Property
    End Class

    Public oSerialParam As SerialParam = New SerialParam
    Public oForm1 As Form1
    Public oForm2 As Form2
    Public aPorts As String()
    Public bPortFind As Boolean = False


    Public Sub Main()

        oSerialParam.PortName = My.Settings.PortName
        oSerialParam.BaudRate = My.Settings.BaudRate
        oSerialParam.Parity = My.Settings.Parity
        oSerialParam.StopBits = My.Settings.StopBits
        oSerialParam.DataBits = My.Settings.DataBits

        aPorts = GetSerialPortList()
        If Not aPorts Is Nothing Then
            If aPorts.Length > 0 Then
                bPortFind = True
            End If
        End If

        If bPortFind Then
            If oSerialParam.PortName Is Nothing Then
                bPortFind = False
                oForm2 = New Form2
                oForm2.ShowDialog()
            Else
                Dim bPort As Boolean = False
                Dim Port As String
                For Each Port In aPorts
                    Dim PortName As String() = Port.Split(";")
                    If PortName(1) = oSerialParam.PortName Then
                        bPort = True
                        Exit For
                    End If
                Next
                If bPort = False Then
                    bPortFind = False
                    oForm2 = New Form2
                    oForm2.ShowDialog()
                End If
            End If
        End If

        If bPortFind = False Then
            MsgBox("接続可能なCOMポート機器が有りません。" + vbCr + "アプリケーションを終了します。", MsgBoxStyle.OkOnly + MsgBoxStyle.Critical, "接続機器エラー")
            End
        Else
            oForm1 = New Form1
            oForm1.ShowDialog()
        End If

    End Sub

    Public Function GetSerialPortList() As String()

        Dim portList As String = ""
        Dim ports As String() = SerialPort.GetPortNames()
        If ports.Length < 1 Then
            Return Nothing
        End If

        Dim mcPnPEntity As ManagementClass = New ManagementClass("Win32_PnPEntity")
        Dim manageObjCol As ManagementObjectCollection = mcPnPEntity.GetInstances()
        Dim manageObj As ManagementObject

        For Each manageObj In manageObjCol
            Dim namePropertyVal = manageObj.GetPropertyValue("Name")
            If Not namePropertyVal Is Nothing Then
                Dim name As String = namePropertyVal.ToString
                Dim port As String

                For Each port In ports
                    Dim check = New System.Text.RegularExpressions.Regex("(" + port + ")")
                    If check.IsMatch(name) Then
                        If portList.Length > 0 Then
                            portList += ","
                        End If
                        portList += name + ";" + port
                    End If
                Next
            End If
        Next

        If portList.Length > 0 Then
            ports = portList.Split(",")
        Else
            ports = Nothing
        End If

        Return ports

    End Function

End Module
