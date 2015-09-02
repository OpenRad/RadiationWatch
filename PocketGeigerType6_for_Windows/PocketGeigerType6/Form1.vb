Imports System.Math

Public Class Form1


    Private mNoiseCount As Long = 0
    '100ms Buffer
    Private mBuffaCount As Integer() = {
        0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    }

    Private mNoiseReject As Boolean = False
    Private mNoiseMilisec As Long = 1000
    Private mTotalMilisec As Long = 0
    Private mTotalCount As Long = 0
    Private mTotalSecond As Double = 0
    Private mTotalMinute As Double = 0
    Private mMeasurementTime As Integer = 600

    Private mDoseRate As Double = 0
    Private mDoseRate_theta As Double = 0
    Private mCpm As Double = 0
    Private mCpm_theta As Double = 0

    Private mDataMark As Boolean = False
    Private mSeparated As Boolean = False
    Private mSignal As Integer = 0
    Private mNoise As Integer = 0

    Private mSignalPulse As Integer = 0
    Private mNoisePulse As Integer = 0

    Private mCoefficient As Double = 53.032

    Private Sub Form1_FormClosed(sender As Object, e As System.Windows.Forms.FormClosedEventArgs) Handles Me.FormClosed
        If Timer1.Enabled Then
            Timer1.Enabled = False
        End If
        If SerialPort1.IsOpen Then
            SerialPort1.Close()
        End If
    End Sub

    Private Sub Form1_Load(sender As System.Object, e As System.EventArgs) Handles MyBase.Load
        Label1.Text = "0.00"
        Label2.Text = "0.00±0.00"
        Label5.Text = ""
        Label6.Text = ""

        mMeasurementTime = My.Settings.MeasureTime

        Select Case mMeasurementTime
            Case 120
                ComboBox1.SelectedIndex = 0
            Case 300
                ComboBox1.SelectedIndex = 1
            Case 600
                ComboBox1.SelectedIndex = 2
            Case 1800
                ComboBox1.SelectedIndex = 3
            Case 3600
                ComboBox1.SelectedIndex = 4
        End Select
    End Sub

    Private Sub SerialPort1_DataReceived(sender As System.Object, e As System.IO.Ports.SerialDataReceivedEventArgs) Handles SerialPort1.DataReceived

        Dim rbuf As Byte() = New Byte(SerialPort1.BytesToRead - 1) {}
        SerialPort1.Read(rbuf, 0, rbuf.GetLength(0))

        For i As Integer = 0 To rbuf.Length - 1
            If System.Text.Encoding.GetEncoding("SHIFT-JIS").GetString(rbuf, i, 1) = ">" Then
                mDataMark = True
                mSignal = 0
                mNoise = 0
                mSeparated = False
            Else
                If mDataMark Then
                    If System.Text.Encoding.GetEncoding("SHIFT-JIS").GetString(rbuf, i, 1) >= "0" & _
                       System.Text.Encoding.GetEncoding("SHIFT-JIS").GetString(rbuf, i, 1) <= "9" Then
                        If mSeparated Then
                            mNoise = mNoise * 10 + (rbuf(i) - 48)
                        Else
                            mSignal = mSignal * 10 + (rbuf(i) - 48)
                        End If
                    End If
                    If System.Text.Encoding.GetEncoding("SHIFT-JIS").GetString(rbuf, i, 1) = "," Then
                        mSeparated = True
                    End If
                    If rbuf(i) = 13 Then
                        If mNoise > 0 Then
                            If mNoiseReject = False Then
                                mTotalMilisec -= 1000
                                If mTotalMilisec < 0 Then
                                    mTotalMilisec = 0
                                End If
                                mNoiseReject = True
                                mNoiseMilisec = 1000
                            End If
                            mNoisePulse += mNoise
                            mNoise = 0
                        End If
                        If mNoiseReject Then
                            mSignal = 0
                        End If
                        If mSignal > 0 Then
                            mBuffaCount(mBuffaCount.Length - 1) += mSignal
                            mSignalPulse += mSignal
                            mSignal = 0
                        End If
                    End If
                End If
                If rbuf(i) = 13 Then
                    mDataMark = False
                End If
            End If
        Next

    End Sub

    Private Sub Timer1_Tick(sender As System.Object, e As System.EventArgs) Handles Timer1.Tick

        If mNoiseReject = True Then
            mNoiseMilisec -= 100
            For i As Integer = 0 To mBuffaCount.Length - 1
                mBuffaCount(i) = 0
            Next
            If mNoiseMilisec < 100 Then
                mNoiseMilisec = 0
                mNoiseReject = False
            End If
            mSignalPulse = 0
        Else
            mTotalMilisec += 100
            mTotalCount += mBuffaCount(0)
            For i As Integer = 1 To mBuffaCount.Length - 1
                mBuffaCount(i - 1) = mBuffaCount(i)
            Next
            mBuffaCount(mBuffaCount.Length - 1) = 0
        End If

        If mTotalMilisec > 0 Then
            mTotalSecond = Math.Ceiling(CType(mTotalMilisec, Double) / 1000.0 * 100.0) / 100.0
            mTotalMinute = Math.Ceiling(mTotalSecond / 60.0 * 10000.0) / 10000.0

            mCpm = Math.Round(CType(mTotalCount, Double) / mTotalMinute * 10000.0) / 10000.0
            mCpm_theta = Math.Round(Math.Sqrt(CType(mTotalCount, Double)) / mTotalMinute * 10000.0) / 10000.0

            mDoseRate = Math.Round(mCpm / mCoefficient * 100.0) / 100.0
            mDoseRate_theta = Math.Round(mCpm_theta / mCoefficient * 100.0) / 100.0


            Dim percent As Integer = mTotalSecond / CType(mMeasurementTime, Double) * 100.0
            Dim sec As Double = mTotalSecond
            Dim mm As Integer = Math.Truncate(sec / 60)
            Dim ss As Integer = sec - mm * 60

            Label5.Text = Format(mm, "00") + ":" + Format(ss, "00") + "  (" + percent.ToString + "%)"

            If mTotalSecond < mMeasurementTime Then
                ProgressBar1.Value = CType(mTotalSecond, Integer)
            Else
                If mTotalSecond = mMeasurementTime Then
                    ProgressBar1.Value = CType(mTotalSecond, Integer)
                Else
                    StopMeasureMent()
                    For i As Integer = 0 To mBuffaCount.Length - 1
                        mTotalCount += mBuffaCount(i)
                    Next
                    SetDoseRate()
                End If
            End If
        End If

        SetDoseRate()

    End Sub

    Private Sub SetDoseRate()

        Label1.Text = Format(mCpm, "#,##0.00")
        Label2.Text = Format(mDoseRate, "#,##0.00") + "±" + Format(mDoseRate_theta, "0.00")

    End Sub

    Private Sub Label2_Click(sender As System.Object, e As System.EventArgs) Handles Label2.Click

    End Sub

    Private Sub Button1_Click(sender As System.Object, e As System.EventArgs) Handles Button1.Click

        Button1.Enabled = False
        Button1.BackColor = Color.DarkGray
        Button2.Enabled = True
        Button2.BackColor = Color.White
        ComboBox1.Enabled = False

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

        Catch ex As Exception
        End Try

        If Not SerialPort1.IsOpen Then
            Button1.Enabled = True
            Button1.BackColor = Color.White
            Button2.Enabled = False
            Button2.BackColor = Color.DarkGray
            Exit Sub
        End If

        StartMeasureMent()

        Timer1.Enabled = True
        Timer2.Enabled = True

    End Sub

    Private Sub Button2_Click(sender As System.Object, e As System.EventArgs) Handles Button2.Click

        StopMeasureMent()

        Timer1.Enabled = False
        Timer2.Enabled = False

    End Sub

    Private Sub StartMeasureMent()

        For i As Integer = 0 To mBuffaCount.Length - 1
            mBuffaCount(i) = 0
        Next

        ProgressBar1.Minimum = 0
        ProgressBar1.Maximum = mMeasurementTime
        ProgressBar1.Value = 0

        mCpm = 0
        mCpm_theta = 0
        mDoseRate = 0
        mDoseRate_theta = 0

        mNoiseCount = 0
        mTotalMilisec = 0
        mTotalSecond = 0
        mTotalCount = 0
        mNoiseReject = False
        mNoiseMilisec = 0

        mDataMark = False
        mSeparated = False

        mSignal = 0
        mNoise = 0

        mSignalPulse = 0
        mNoisePulse = 0

        SerialPort1.WriteLine("S")

    End Sub

    Private Sub StopMeasureMent()

        If SerialPort1.IsOpen Then
            SerialPort1.WriteLine("E")
            Threading.Thread.Sleep(200)
            SerialPort1.Close()
        End If

        Timer1.Enabled = False

        Button1.Enabled = True
        Button1.BackColor = Color.White
        Button2.Enabled = False
        Button2.BackColor = Color.DarkGray
        ComboBox1.Enabled = True

    End Sub

    Private Sub Timer2_Tick(sender As System.Object, e As System.EventArgs) Handles Timer2.Tick

        If mNoisePulse > 0 Then
            If mNoisePulse > 20 Then
                mNoisePulse = 20
            End If
            Label6.ForeColor = Color.Red
            Label6.Text = "●"
            mNoisePulse -= 1
        Else
            If mSignalPulse > 0 Then
                If mSignalPulse > 2 Then
                    mSignalPulse = 2
                End If
                Label6.ForeColor = Color.FromArgb(192, 192, 0)
                Label6.Text = "●"
                mSignalPulse -= 1
            Else
                Label6.Text = ""
            End If
        End If

    End Sub

    Private Sub ComboBox1_SelectedIndexChanged(sender As System.Object, e As System.EventArgs) Handles ComboBox1.SelectedIndexChanged

        Dim measureTime As Integer = 300

        Select Case ComboBox1.SelectedIndex
            Case 0
                measureTime = 120
            Case 1
                measureTime = 300
            Case 2
                measureTime = 600
            Case 3
                measureTime = 1800
            Case 4
                measureTime = 3600
        End Select

        If measureTime <> mMeasurementTime Then
            mMeasurementTime = measureTime
            My.Settings.MeasureTime = mMeasurementTime
            My.Settings.Save()
        End If

    End Sub
End Class
