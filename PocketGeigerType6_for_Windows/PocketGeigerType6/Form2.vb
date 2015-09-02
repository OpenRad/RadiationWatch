Public Class Form2

    Private Sub Form2_Load(sender As System.Object, e As System.EventArgs) Handles MyBase.Load

        Dim Port As String
        Dim aPortName As String()

        Try
            For Each Port In aPorts
                aPortName = Port.Split(";")
                ComboBox1.Items.Add(aPortName(0))
                If aPortName(1) = oSerialParam.PortName Then
                    Dim index As Integer = ComboBox1.FindString(aPortName(0))
                    If index > -1 Then
                        ComboBox1.SelectedIndex = index
                    End If
                End If
            Next
        Catch ex As Exception
        End Try

        Select Case oSerialParam.BaudRate
            Case 9600
                ComboBox2.SelectedIndex = 0
            Case 19200
                ComboBox2.SelectedIndex = 1
            Case 38400
                ComboBox2.SelectedIndex = 2
            Case 57600
                ComboBox2.SelectedIndex = 3
            Case 115200
                ComboBox2.SelectedIndex = 4
        End Select

        Select Case oSerialParam.Parity
            Case "NONE"
                ComboBox3.SelectedIndex = 0
            Case "ODD"
                ComboBox3.SelectedIndex = 1
            Case "EVEN"
                ComboBox3.SelectedIndex = 2
        End Select

        Select Case oSerialParam.StopBits
            Case 1
                ComboBox4.SelectedIndex = 0
            Case 1.5
                ComboBox4.SelectedIndex = 1
            Case 2
                ComboBox4.SelectedIndex = 2
        End Select

        Select Case oSerialParam.DataBits
            Case 7
                ComboBox5.SelectedIndex = 0
            Case 8
                ComboBox5.SelectedIndex = 1
        End Select

    End Sub

    Private Sub ComboBox1_SelectedIndexChanged(sender As System.Object, e As System.EventArgs) Handles ComboBox1.SelectedIndexChanged
        If ComboBox1.SelectedIndex > -1 Then
            Button1.Enabled = True
        Else
            Button1.Enabled = False
        End If
    End Sub

    Private Sub Button1_Click(sender As System.Object, e As System.EventArgs) Handles Button1.Click

        If ComboBox1.SelectedIndex < 0 Then Exit Sub

        If Button1.Enabled = False Then Exit Sub

        Button1.Enabled = False

        SaveSerialParam()

        Try
            SerialPort1.PortName = oSerialParam.PortName
            SerialPort1.BaudRate = oSerialParam.BaudRate
            Select Case oSerialParam.Parity
                Case "NONE"
                    SerialPort1.Parity = IO.Ports.Parity.None
                Case "ODD"
                    SerialPort1.Parity = IO.Ports.Parity.Odd
                Case "EVEN"
                    SerialPort1.Parity = IO.Ports.Parity.Even
            End Select
            Select Case oSerialParam.StopBits
                Case 1
                    SerialPort1.StopBits = IO.Ports.StopBits.One
                Case 1.5
                    SerialPort1.StopBits = IO.Ports.StopBits.OnePointFive
                Case 2
                    SerialPort1.StopBits = IO.Ports.StopBits.Two
            End Select
            SerialPort1.DataBits = oSerialParam.DataBits
            SerialPort1.Open()
            If SerialPort1.IsOpen Then
                bPortFind = True
            End If
            SerialPort1.Close()

        Catch ex As Exception
        End Try

        Me.Close()

    End Sub

    Private Sub SaveSerialParam()

        Dim aPortName As String() = aPorts(ComboBox1.SelectedIndex).Split(";")
        oSerialParam.PortName = aPortName(1)

        Select Case ComboBox2.SelectedIndex
            Case 0
                oSerialParam.BaudRate = 9600
            Case 1
                oSerialParam.BaudRate = 19200
            Case 2
                oSerialParam.BaudRate = 38400
            Case 3
                oSerialParam.BaudRate = 57600
            Case 4
                oSerialParam.BaudRate = 115200
        End Select

        Select Case ComboBox3.SelectedIndex
            Case 0
                oSerialParam.Parity = "NONE"
            Case 1
                oSerialParam.Parity = "ODD"
            Case 2
                oSerialParam.Parity = "EVEN"
        End Select

        Select Case ComboBox4.SelectedIndex
            Case 0
                oSerialParam.StopBits = 1
            Case 1
                oSerialParam.StopBits = 1.5
            Case 2
                oSerialParam.StopBits = 2
        End Select

        Select Case ComboBox5.SelectedIndex
            Case 0
                oSerialParam.DataBits = 7
            Case 1
                oSerialParam.DataBits = 8
        End Select

        My.Settings.PortName = oSerialParam.PortName
        My.Settings.BaudRate = oSerialParam.BaudRate
        My.Settings.Parity = oSerialParam.Parity
        My.Settings.StopBits = oSerialParam.StopBits
        My.Settings.DataBits = oSerialParam.DataBits
        My.Settings.Save()

    End Sub

    Private Sub Button2_Click(sender As System.Object, e As System.EventArgs) Handles Button2.Click

        Me.Close()

    End Sub
End Class